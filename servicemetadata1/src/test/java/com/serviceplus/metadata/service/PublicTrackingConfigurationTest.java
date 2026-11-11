package com.serviceplus.metadata.service;

import com.serviceplus.metadata.feignClient.FormDesignerFeignClient;
import com.serviceplus.metadata.dto.ServiceTrackingConfigDTO;
import com.serviceplus.metadata.entity.ServiceLog;
import com.serviceplus.metadata.entity.ServiceTemplate;
import com.serviceplus.metadata.entity.ServiceTrackingConfig;
import com.serviceplus.metadata.exception.SPRuntimeError;
import com.serviceplus.metadata.repository.IServiceLogRepository;
import com.serviceplus.metadata.repository.IServiceTemplateRepository;
import com.serviceplus.metadata.repository.IServiceTrackingConfigRepository;
import com.serviceplus.metadata.utility.ApplicationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PublicTrackingConfigurationTest {
    private final IServiceLogRepository logs = mock(IServiceLogRepository.class);
    private final IServiceTrackingConfigRepository configs = mock(IServiceTrackingConfigRepository.class);
    private final IServiceTemplateRepository templates = mock(IServiceTemplateRepository.class);
    private final FormDesignerFeignClient forms = mock(FormDesignerFeignClient.class);
    private ServiceMetadataService service;
    private ServiceTrackingConfigDTO saved;

    @BeforeEach
    void setUp() {
        service = mock(ServiceMetadataService.class, CALLS_REAL_METHODS);
        ReflectionTestUtils.setField(service, "serviceLogRepository", logs);
        ReflectionTestUtils.setField(service, "trackingConfigRepository", configs);
        ReflectionTestUtils.setField(service, "serviceTemplateRepository", templates);
        ReflectionTestUtils.setField(service, "formFeignClient", forms);
        saved = new ServiceTrackingConfigDTO(List.of("MOBILE_OTP", "EMAIL_OTP", "FORM_ATTRIBUTES"),
                "mobile", "email", List.of("third", "first", "second"), true);
    }

    private void configuredService() {
        ServiceLog log = new ServiceLog();
        log.setTenantId("tenant");
        when(logs.findByServiceId(7)).thenReturn(log);
        ServiceTrackingConfig config = new ServiceTrackingConfig();
        config.setTrackingConfigJson(saved);
        when(configs.findByServiceId(7L)).thenReturn(config);
    }

    private void submissionForm() {
        ServiceTemplate template = new ServiceTemplate();
        template.setFormId("form");
        when(templates.findByServiceIdAndTaskId(7, ApplicationConstants.SUBMISSION_NODE_ID)).thenReturn(template);
        doReturn(ResponseEntity.ok(Map.of("attributes", List.of(
                field("first", 1), field("second", 2), field("third", 4),
                field("mobile", 1), field("email", 3), field("private", 1)))))
                .when(forms).getAllAttributesByFormId("form");
    }

    private Map<String, Object> field(String id, int type) {
        return Map.of("attributeId", id, "attributeLabel", "Label " + id, "typeId", type, "required", true);
    }

    private void assertStatus(HttpStatus status, Runnable action) {
        assertEquals(status, assertThrows(SPRuntimeError.class, action::run).getErrorCode());
    }

    @Test
    void configuredServicesUseTenantQueryAndReturnNumericIds() {
        when(configs.findConfiguredServices("tenant"))
                .thenReturn(Arrays.<Object[]>asList(new Object[] {7, "First"}, new Object[] {8L, "Second"}));
        assertEquals(List.of(Map.of("serviceId", 7L, "serviceName", "First"),
                Map.of("serviceId", 8L, "serviceName", "Second")), service.getConfiguredServices("tenant"));
        verify(configs).findConfiguredServices("tenant");
        verifyNoInteractions(logs, templates, forms);
    }

    @Test
    void noConfiguredServicesReturnEmptyList() {
        when(configs.findConfiguredServices("tenant")).thenReturn(Collections.emptyList());
        assertTrue(service.getConfiguredServices("tenant").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, 2147483648L, Long.MAX_VALUE})
    void rejectsInvalidServiceIdBeforeRepositoryAccess(long id) {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.getPublicTrackingConfig(id, "tenant"));
        verifyNoInteractions(logs, configs, templates, forms);
    }

    @Test
    void rejectsNullServiceIdBeforeRepositoryAccess() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.getPublicTrackingConfig(null, "tenant"));
        verifyNoInteractions(logs, configs, templates, forms);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingTenantBeforeRepositoryAccess(String tenant) {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.getPublicTrackingConfig(7L, tenant));
        verifyNoInteractions(logs, configs, templates, forms);
    }

    @Test
    void concealsCrossTenantAndMissingServicesBeforeLoadingConfiguration() {
        assertStatus(HttpStatus.NOT_FOUND, () -> service.getPublicTrackingConfig(7L, "tenant"));
        ServiceLog otherTenant = new ServiceLog();
        otherTenant.setTenantId("other");
        when(logs.findByServiceId(7)).thenReturn(otherTenant);
        assertStatus(HttpStatus.NOT_FOUND, () -> service.getPublicTrackingConfig(7L, "tenant"));
        verifyNoInteractions(configs, templates, forms);
    }

    @Test
    void missingConfigurationOrJsonReturnNotFound() {
        configuredService();
        when(configs.findByServiceId(7L)).thenReturn(null);
        assertStatus(HttpStatus.NOT_FOUND, () -> service.getPublicTrackingConfig(7L, "tenant"));
        when(configs.findByServiceId(7L)).thenReturn(new ServiceTrackingConfig());
        assertStatus(HttpStatus.NOT_FOUND, () -> service.getPublicTrackingConfig(7L, "tenant"));
        verifyNoInteractions(templates, forms);
    }

    @Test
    void missingTrackingTypesReturnConflict() {
        configuredService();
        for (List<String> types : Arrays.<List<String>>asList(null, List.of())) {
            saved.setTrackingTypes(types);
            assertStatus(HttpStatus.CONFLICT, () -> service.getPublicTrackingConfig(7L, "tenant"));
        }
        verifyNoInteractions(templates, forms);
    }

    @Test
    void returnsOnlyConfiguredFieldsInConfiguredOrder() {
        configuredService();
        submissionForm();
        Map<String, Object> result = service.getPublicTrackingConfig(7L, "tenant");
        assertEquals(7L, result.get("serviceId"));
        assertEquals(saved.getTrackingTypes(), result.get("trackingTypes"));
        assertEquals(true, result.get("displayActionTakenDate"));
        assertEquals(Map.of("attributeId", "mobile", "attributeLabel", "Label mobile", "typeId", 1),
                result.get("mobileAttribute"));
        assertEquals(Map.of("attributeId", "email", "attributeLabel", "Label email", "typeId", 3),
                result.get("emailAttribute"));
        List<?> fields = (List<?>) result.get("formAttributes");
        assertEquals(List.of("third", "first", "second"), fields.stream()
                .map(field -> ((Map<?, ?>) field).get("attributeId")).toList());
        assertEquals(6, result.size());
    }

    @Test
    void disabledMethodsDoNotExposeStaleSavedAttributes() {
        configuredService();
        submissionForm();
        saved.setTrackingTypes(List.of("FORM_ATTRIBUTES"));
        saved.setDisplayActionTakenDate(null);
        Map<String, Object> result = service.getPublicTrackingConfig(7L, "tenant");
        assertNull(result.get("mobileAttribute"));
        assertNull(result.get("emailAttribute"));
        assertEquals(false, result.get("displayActionTakenDate"));
        saved.setTrackingTypes(List.of("MOBILE_OTP"));
        assertEquals(List.of(), service.getPublicTrackingConfig(7L, "tenant").get("formAttributes"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "removed"})
    void invalidOrRemovedConfiguredFieldsReturnConflict(String id) {
        configuredService();
        submissionForm();
        saved.setMobileAttributeId(id);
        assertStatus(HttpStatus.CONFLICT, () -> service.getPublicTrackingConfig(7L, "tenant"));
    }

    @Test
    void missingSelectedFormAttributesReturnConflict() {
        configuredService();
        submissionForm();
        saved.setSelectedAttributeIds(null);
        assertStatus(HttpStatus.CONFLICT, () -> service.getPublicTrackingConfig(7L, "tenant"));
    }

    @Test
    void missingSubmissionTemplateOrFormIdReturnNotFound() {
        configuredService();
        assertStatus(HttpStatus.NOT_FOUND, () -> service.getPublicTrackingConfig(7L, "tenant"));
        ServiceTemplate template = new ServiceTemplate();
        when(templates.findByServiceIdAndTaskId(7, ApplicationConstants.SUBMISSION_NODE_ID)).thenReturn(template);
        assertStatus(HttpStatus.NOT_FOUND, () -> service.getPublicTrackingConfig(7L, "tenant"));
        template.setFormId(" ");
        assertStatus(HttpStatus.NOT_FOUND, () -> service.getPublicTrackingConfig(7L, "tenant"));
        verifyNoInteractions(forms);
    }

    @Test
    void malformedOrUnsuccessfulFormDesignerResponsesReturnBadGateway() {
        configuredService();
        submissionForm();
        for (ResponseEntity<?> response : Arrays.<ResponseEntity<?>>asList(null,
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build(),
                ResponseEntity.ok("invalid"), ResponseEntity.ok(Map.of()),
                ResponseEntity.ok(Map.of("attributes", "invalid")))) {
            doReturn(response).when(forms).getAllAttributesByFormId("form");
            assertStatus(HttpStatus.BAD_GATEWAY, () -> service.getPublicTrackingConfig(7L, "tenant"));
        }
    }
}
