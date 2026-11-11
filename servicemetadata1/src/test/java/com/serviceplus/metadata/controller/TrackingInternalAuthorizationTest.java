package com.serviceplus.metadata.controller;

import com.serviceplus.metadata.security.TrackingPeerCertificateAuthorization;
import com.serviceplus.metadata.service.ServiceMetadataService;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrackingInternalAuthorizationTest {

    private ServiceMetadataController controller(ServiceMetadataService service, String peers) {
        ServiceMetadataController controller = new ServiceMetadataController();
        ReflectionTestUtils.setField(controller, "serviceMetadataService", service);
        ReflectionTestUtils.setField(controller, "trackingPeerAuthorization",
                new TrackingPeerCertificateAuthorization(peers));
        return controller;
    }

    private MockHttpServletRequest request(String dns) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSecure(true);
        X509Certificate cert = mock(X509Certificate.class);
        when(cert.getSubjectAlternativeNames()).thenReturn(List.of(List.of(2, dns)));
        request.setAttribute("jakarta.servlet.request.X509Certificate", new X509Certificate[]{cert});
        return request;
    }

    @Test
    void rejectsMissingCertificateAndWrongPeer() throws Exception {
        ServiceMetadataService service = mock(ServiceMetadataService.class);
        ServiceMetadataController controller = controller(service, "trackingservice.internal");
        assertEquals(HttpStatus.FORBIDDEN,
                controller.getConfigureServices("tenant", new MockHttpServletRequest()).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN,
                controller.getPublicTrackingConfiguration(7L, "tenant", request("unknown.internal")).getStatusCode());
        verifyNoInteractions(service);
    }

    @Test
    void authorizedPeerMayReadOnlyTenantScopedTrackingConfiguration() throws Exception {
        ServiceMetadataService service = mock(ServiceMetadataService.class);
        when(service.getConfiguredServices("tenant")).thenReturn(List.of(Map.of("serviceId", 7L)));
        when(service.getPublicTrackingConfig(7L, "tenant")).thenReturn(Map.of("serviceId", 7L));
        ServiceMetadataController controller = controller(service, "trackingservice.internal");
        MockHttpServletRequest request = request("trackingservice.internal");

        assertEquals(HttpStatus.OK, controller.getConfigureServices("tenant", request).getStatusCode());
        assertNotNull(controller.getPublicTrackingConfiguration(7L, "tenant", request).getBody());
        verify(service).getConfiguredServices("tenant");
        verify(service).getPublicTrackingConfig(7L, "tenant");
    }

    @Test
    void unconfiguredPeerAllowlistFailsClosed() throws Exception {
        ServiceMetadataService service = mock(ServiceMetadataService.class);
        ServiceMetadataController controller = controller(service, "");
        assertEquals(HttpStatus.FORBIDDEN,
                controller.getConfigureServices("tenant", request("trackingservice.internal")).getStatusCode());
        verifyNoInteractions(service);
    }

    @Test
    void httpRoutesRequireClientCertificateWithoutCustomToken() throws Exception {
        ServiceMetadataService service = mock(ServiceMetadataService.class);
        when(service.getConfiguredServices("tenant")).thenReturn(List.of(Map.of("serviceId", 7L)));
        when(service.getPublicTrackingConfig(7L, "tenant")).thenReturn(Map.of("serviceId", 7L));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller(service, "trackingservice.internal")).build();

        mvc.perform(get("/internal/tracking/configured-services").param("tenantId", "tenant"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/internal/tracking/configuration").secure(true)
                .param("serviceId", "7").param("tenantId", "tenant")
                .header("X-Tracking-Internal-Token", "untrusted"))
                .andExpect(status().isForbidden());

        X509Certificate cert = mock(X509Certificate.class);
        when(cert.getSubjectAlternativeNames()).thenReturn(List.of(List.of(2, "trackingservice.internal")));
        mvc.perform(get("/internal/tracking/configured-services").secure(true).param("tenantId", "tenant")
                .requestAttr("jakarta.servlet.request.X509Certificate", new X509Certificate[]{cert}))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].serviceId").value(7));
        mvc.perform(get("/internal/tracking/configuration").secure(true)
                .param("serviceId", "7").param("tenantId", "tenant")
                .requestAttr("jakarta.servlet.request.X509Certificate", new X509Certificate[]{cert}))
                .andExpect(status().isOk()).andExpect(jsonPath("$.serviceId").value(7));
    }
}
