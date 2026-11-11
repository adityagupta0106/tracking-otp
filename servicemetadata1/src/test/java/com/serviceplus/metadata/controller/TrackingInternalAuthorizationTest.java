package com.serviceplus.metadata.controller;

import com.serviceplus.metadata.service.ServiceMetadataService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrackingInternalAuthorizationTest {

    private ServiceMetadataController controller(ServiceMetadataService service) {
        ServiceMetadataController controller = new ServiceMetadataController();
        ReflectionTestUtils.setField(controller, "serviceMetadataService", service);
        ReflectionTestUtils.setField(controller, "trackingInternalToken", "test-only-shared-token");
        return controller;
    }

    @Test
    void configuredServicesRejectMissingOrIncorrectInternalToken() {
        ServiceMetadataService service = mock(ServiceMetadataService.class);
        ServiceMetadataController controller = controller(service);
        assertEquals(HttpStatus.FORBIDDEN, controller.getConfigureServices("tenant", null).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, controller.getConfigureServices("tenant", "wrong").getStatusCode());
        verifyNoInteractions(service);
    }

    @Test
    void configurationRejectsMissingOrIncorrectInternalToken() {
        ServiceMetadataService service = mock(ServiceMetadataService.class);
        ServiceMetadataController controller = controller(service);
        assertEquals(HttpStatus.FORBIDDEN,
                controller.getPublicTrackingConfiguration(7L, "tenant", null).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN,
                controller.getPublicTrackingConfiguration(7L, "tenant", "wrong").getStatusCode());
        verifyNoInteractions(service);
    }

    @Test
    void trustedTokenCanFetchTenantScopedData() {
        ServiceMetadataService service = mock(ServiceMetadataService.class);
        when(service.getConfiguredServices("tenant")).thenReturn(List.of(Map.of("serviceId", 7L)));
        when(service.getPublicTrackingConfig(7L, "tenant")).thenReturn(Map.of("serviceId", 7L));
        ServiceMetadataController controller = controller(service);

        assertEquals(HttpStatus.OK,
                controller.getConfigureServices("tenant", "test-only-shared-token").getStatusCode());
        assertNotNull(controller.getPublicTrackingConfiguration(
                7L, "tenant", "test-only-shared-token").getBody());
        verify(service).getConfiguredServices("tenant");
        verify(service).getPublicTrackingConfig(7L, "tenant");
    }

    @Test
    void endpointsFailClosedWhenServerTokenIsUnconfigured() {
        ServiceMetadataService service = mock(ServiceMetadataService.class);
        ServiceMetadataController controller = controller(service);
        for (String configured : new String[] {null, "", "   "}) {
            ReflectionTestUtils.setField(controller, "trackingInternalToken", configured);
            assertEquals(HttpStatus.FORBIDDEN,
                    controller.getConfigureServices("tenant", "test-only-shared-token").getStatusCode());
            assertEquals(HttpStatus.FORBIDDEN,
                    controller.getPublicTrackingConfiguration(7L, "tenant", "").getStatusCode());
        }
        verifyNoInteractions(service);
    }

    @Test
    void httpRoutesEnforceHeaderAndPreserveResponseShape() throws Exception {
        ServiceMetadataService service = mock(ServiceMetadataService.class);
        when(service.getConfiguredServices("tenant")).thenReturn(List.of(Map.of("serviceId", 7L)));
        when(service.getPublicTrackingConfig(7L, "tenant")).thenReturn(Map.of("serviceId", 7L));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller(service)).build();

        mvc.perform(get("/internal/tracking/configured-services").param("tenantId", "tenant"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/internal/tracking/configuration")
                .param("serviceId", "7").param("tenantId", "tenant")
                .header("X-Tracking-Internal-Token", "wrong"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);

        mvc.perform(get("/internal/tracking/configured-services").param("tenantId", "tenant")
                .header("X-Tracking-Internal-Token", "test-only-shared-token"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].serviceId").value(7));
        mvc.perform(get("/internal/tracking/configuration")
                .param("serviceId", "7").param("tenantId", "tenant")
                .header("X-Tracking-Internal-Token", "test-only-shared-token"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.serviceId").value(7));
    }
}
