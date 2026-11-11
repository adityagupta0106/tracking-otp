package com.serviceplus.tracking.service;

import com.serviceplus.tracking.ExceptionHandler.SPRuntimeError;
import com.serviceplus.tracking.dto.ApplicationTrackingRequest;
import com.serviceplus.tracking.dto.ApplicationTrackingResponse;
import com.serviceplus.tracking.entity.ApplicationTracking;
import com.serviceplus.tracking.feignClient.*;
import com.serviceplus.tracking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static com.serviceplus.tracking.utility.ApplicationConstants.CLIENT_DOMAIN_HEADER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class PublicTrackingVerificationServiceTest {
    private final ApplicationTrackingRepository tracking = mock(ApplicationTrackingRepository.class);
    private final InstanceConfigRegistryFeignClient registry = mock(InstanceConfigRegistryFeignClient.class);
    private final ServiceMetadataFeignClient metadata = mock(ServiceMetadataFeignClient.class);
    private final FormDesignerFeignClient forms = mock(FormDesignerFeignClient.class);
    private final UserManagmentFeignClient users = mock(UserManagmentFeignClient.class);
    private final PublicTrackingAuthorizationService authorization = mock(PublicTrackingAuthorizationService.class);
    private ApplicationTrackingService service;
    private MockHttpServletRequest request;
    private Map<String, Object> config;

    @BeforeEach
    void setUp() {
        service = new ApplicationTrackingService(tracking, mock(WorkflowInboxRedisService.class),
                mock(ApplicationTrackingRepositoryCustomImpl.class), registry,
                mock(ApplicationTrackingHolderRepository.class), mock(ApplicationTrackingDocumentRepository.class),
                mock(ApplicantInboxRepository.class), metadata, forms, users, authorization);
        request = new MockHttpServletRequest();
        request.addHeader(CLIENT_DOMAIN_HEADER, "tenant.example");
        when(registry.fetchTenantId(anyMap())).thenReturn(ResponseEntity.ok("tenant"));
        config = new HashMap<>(Map.of("serviceId", 7, "trackingTypes",
                List.of("MOBILE_OTP", "EMAIL_OTP", "FORM_ATTRIBUTES"), "displayActionTakenDate", false));
        when(metadata.getPublicTrackingConfiguration(7L, "tenant")).thenReturn(config);
        when(authorization.fingerprint(any())).thenReturn("config-v1");
        when(authorization.saveRequest(anyMap())).thenReturn("request");
    }

    private void status(HttpStatus expected, Runnable action) {
        assertEquals(expected, assertThrows(SPRuntimeError.class, action::run).getErrorCode());
    }

    private ApplicationTracking history(String id, int serviceId) {
        ApplicationTracking row = new ApplicationTracking();
        row.setApplId(id); row.setServiceId(serviceId); row.setApplRefNo("REF");
        row.setApplyDate(LocalDateTime.of(2026, 1, 1, 0, 0)); row.setServiceName("Service");
        row.setProcessId("private-process"); row.setTaskName("Review"); row.setActionName("Received");
        row.setActionOn(LocalDateTime.of(2026, 1, 2, 0, 0)); row.setActionRemarks("private remarks");
        row.setFormId("private-form"); row.setDataId("private-data"); row.setBeneficiaryUserName("private name");
        return row;
    }

    private Map<String, Object> otpBody() {
        return Map.of("serviceId", 7, "method", "EMAIL_OTP", "contact", "input@example.test");
    }

    @Test
    void metadataCallsUseResolvedTenantWithoutCustomHeader() {
        when(metadata.getConfigureServices("tenant"))
                .thenReturn(List.of(Map.of("serviceId", 7)));
        assertEquals(List.of(Map.of("serviceId", 7)), service.getConfiguredServices(request));
        assertSame(config, service.getPublicTrackingConfig(7L, request));
        verify(metadata).getConfigureServices("tenant");
        verify(metadata).getPublicTrackingConfiguration(7L, "tenant");
        verify(registry, times(2)).fetchTenantId(Map.of(CLIENT_DOMAIN_HEADER, "tenant.example"));
    }

    @Test
    void missingDomainAndInvalidRegistryResponsesFailClosed() {
        status(HttpStatus.BAD_REQUEST, () -> service.getConfiguredServices(new MockHttpServletRequest()));
        for (ResponseEntity<String> response : Arrays.asList(null, ResponseEntity.ok(" "),
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).<String>build())) {
            when(registry.fetchTenantId(anyMap())).thenReturn(response);
            status(HttpStatus.BAD_GATEWAY, () -> service.getConfiguredServices(request));
        }
        verifyNoInteractions(metadata);
    }

    @Test
    void disabledFeaturePreventsVerification() {
        doThrow(new SPRuntimeError("Disabled", HttpStatus.SERVICE_UNAVAILABLE)).when(authorization)
                .rateLimit(anyString(), anyInt(), anyString());
        status(HttpStatus.SERVICE_UNAVAILABLE, () -> service.sendServiceTrackingOtp(otpBody(), request));
        verifyNoInteractions(metadata, forms, users);
    }

    @Test
    void unexpectedOrNullBodyCannotInjectTenantOrApplication() {
        Map<String, Object> body = new HashMap<>(otpBody()); body.put("tenantId", "other");
        status(HttpStatus.BAD_REQUEST, () -> service.sendServiceTrackingOtp(body, request));
        status(HttpStatus.BAD_REQUEST, () -> service.sendServiceTrackingOtp(null, request));
        verifyNoInteractions(registry, metadata, forms, users);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, 0, 7.5, 2147483648d})
    void invalidServiceNumbersAreRejectedBeforeDependencyCalls(double id) {
        status(HttpStatus.BAD_REQUEST, () -> service.sendServiceTrackingOtp(
                Map.of("serviceId", id, "method", "EMAIL_OTP", "contact", "contact"), request));
        verifyNoInteractions(registry, metadata, forms, users);
    }

    @Test
    void mismatchedServiceOrDisabledMethodDoesNotReachMatcher() {
        config.put("serviceId", 8);
        status(HttpStatus.FORBIDDEN, () -> service.sendServiceTrackingOtp(otpBody(), request));
        config.put("serviceId", 7); config.put("trackingTypes", List.of("MOBILE_OTP"));
        status(HttpStatus.FORBIDDEN, () -> service.sendServiceTrackingOtp(otpBody(), request));
        verifyNoInteractions(forms, users);
    }

    @ParameterizedTest
    @ValueSource(strings = {"MOBILE_OTP", "EMAIL_OTP"})
    void matchedContactUsesTrustedRecipientAndStoresScopedRequest(String method) {
        when(forms.matchTrackingApplications(anyMap())).thenReturn(ResponseEntity.ok(
                Map.of("applicationIds", List.of("app"), "contact", "canonical-recipient")));
        when(users.sendTrackingOtp(anyMap()))
                .thenReturn(ResponseEntity.ok(Map.of("challengeId", "challenge")));
        Map<String, Object> result = service.sendServiceTrackingOtp(
                Map.of("serviceId", 7, "method", method, "contact", "input"), request);
        assertEquals(Map.of("requestId", "request", "message", "If eligible, a verification code has been sent"), result);
        verify(users).sendTrackingOtp(Map.of("tenantId", "tenant", "serviceId", 7,
                "mode", "O", "method", method, "contact", "canonical-recipient"));
        ArgumentCaptor<Map<String, Object>> state = ArgumentCaptor.forClass(Map.class);
        verify(authorization).saveRequest(state.capture());
        assertEquals("challenge", state.getValue().get("challengeId"));
        assertEquals("config-v1", state.getValue().get("configuration"));
        assertEquals(List.of("app"), state.getValue().get("applicationIds"));
        assertFalse(result.containsKey("challengeId"));
    }

    @Test
    void unmatchedContactNeverTriggersDeliveryAndHasSamePublicResponse() {
        when(forms.matchTrackingApplications(anyMap()))
                .thenReturn(ResponseEntity.ok(Map.of("applicationIds", List.of())));
        assertEquals(Map.of("requestId", "request", "message", "If eligible, a verification code has been sent"),
                service.sendServiceTrackingOtp(otpBody(), request));
        verifyNoInteractions(users);
        ArgumentCaptor<Map<String, Object>> state = ArgumentCaptor.forClass(Map.class);
        verify(authorization).saveRequest(state.capture());
        assertFalse(state.getValue().containsKey("challengeId"));
        assertFalse(state.getValue().containsKey("contact"));
    }

    @Test
    void deliveryFailureUsesDecoyStateAndDoesNotLogSensitiveError(CapturedOutput output) {
        when(forms.matchTrackingApplications(anyMap())).thenReturn(ResponseEntity.ok(
                Map.of("applicationIds", List.of("app"), "contact", "sensitive-recipient")));
        when(users.sendTrackingOtp(anyMap()))
                .thenThrow(new IllegalStateException("sensitive-error-token"));
        assertEquals("request", service.sendServiceTrackingOtp(otpBody(), request).get("requestId"));
        ArgumentCaptor<Map<String, Object>> state = ArgumentCaptor.forClass(Map.class);
        verify(authorization).saveRequest(state.capture());
        assertEquals(List.of(), state.getValue().get("applicationIds"));
        assertFalse(state.getValue().containsKey("contact"));
        assertFalse(state.getValue().containsKey("challengeId"));
        assertFalse(output.getAll().contains("sensitive-error-token"));
        assertFalse(output.getAll().contains("sensitive-recipient"));
    }

    private Map<String, Object> verificationState() {
        Map<String, Object> state = new HashMap<>(Map.of("tenantId", "tenant", "serviceId", 7,
                "method", "EMAIL_OTP", "configuration", "config-v1", "challengeId", "challenge",
                "contact", "canonical-recipient", "applicationIds", List.of("app", "removed")));
        when(authorization.request("request")).thenReturn(state);
        when(authorization.consumeRequest("request", state)).thenReturn(true);
        Map<String, Object> verified = new HashMap<>(Map.of("tenantId", "tenant", "serviceId", 7,
                "mode", "O", "method", "EMAIL_OTP", "purpose", "PUBLIC_TRACKING", "recipient", "canonical-recipient"));
        when(users.verifyTrackingOtp(Map.of("challengeId", "challenge", "otp", "123456")))
                .thenReturn(ResponseEntity.ok(Map.of("verified", true, "verificationData", verified)));
        when(forms.matchTrackingApplications(anyMap())).thenReturn(ResponseEntity.ok(
                Map.of("applicationIds", List.of("app", "new"), "contact", "canonical-recipient")));
        when(tracking.findByApplIdAndTenantIdOrderByProcessIdAsc("app", "tenant")).thenReturn(List.of(history("app", 7)));
        when(authorization.issue("tenant", 7, "app", "EMAIL_OTP", "config-v1")).thenReturn("proof");
        return state;
    }

    private Map<String, Object> verifyBody() { return Map.of("serviceId", 7, "requestId", "request", "otp", "123456"); }

    @Test
    void verifiedOtpRechecksMatchesAndIssuesProofOnlyForOriginalIntersection() {
        verificationState();
        List<?> applications = (List<?>) service.verifyServiceTrackingOtp(verifyBody(), request).get("applications");
        assertEquals(1, applications.size());
        assertEquals("proof", ((Map<?, ?>) applications.getFirst()).get("verificationProof"));
        verify(authorization).consumeRequest(eq("request"), anyMap());
        verify(authorization).issue("tenant", 7, "app", "EMAIL_OTP", "config-v1");
        verify(tracking, never()).findByApplIdAndTenantIdOrderByProcessIdAsc("new", "tenant");
    }

    @ParameterizedTest
    @ValueSource(strings = {"tenantId", "serviceId", "mode", "method", "purpose", "recipient"})
    void otpVerificationRejectsAnyMismatchedScope(String field) {
        verificationState();
        Map<String, Object> verified = new HashMap<>(Map.of("tenantId", "tenant", "serviceId", 7,
                "mode", "O", "method", "EMAIL_OTP", "purpose", "PUBLIC_TRACKING", "recipient", "canonical-recipient"));
        verified.put(field, "wrong");
        when(users.verifyTrackingOtp(anyMap()))
                .thenReturn(ResponseEntity.ok(Map.of("verified", true, "verificationData", verified)));
        status(HttpStatus.FORBIDDEN, () -> service.verifyServiceTrackingOtp(verifyBody(), request));
        verify(authorization, never()).consumeRequest(anyString(), anyMap());
        verifyNoInteractions(tracking);
    }

    @Test
    void malformedOtpRequestStateFailsBeforeUserManagementCall() {
        Map<String, Object> state = verificationState();
        clearInvocations(users);
        state.remove("contact");
        status(HttpStatus.FORBIDDEN, () -> service.verifyServiceTrackingOtp(verifyBody(), request));
        verifyNoInteractions(users);

        state.put("contact", "canonical-recipient");
        state.remove("challengeId");
        status(HttpStatus.FORBIDDEN, () -> service.verifyServiceTrackingOtp(verifyBody(), request));
        verifyNoInteractions(users);
    }

    @Test
    void staleConfigurationOrCrossTenantRequestFailsBeforeOtpCheck() {
        Map<String, Object> state = verificationState(); clearInvocations(users);
        state.put("tenantId", "other");
        status(HttpStatus.FORBIDDEN, () -> service.verifyServiceTrackingOtp(verifyBody(), request));
        state.put("tenantId", "tenant"); state.put("configuration", "old-config");
        status(HttpStatus.FORBIDDEN, () -> service.verifyServiceTrackingOtp(verifyBody(), request));
        verifyNoInteractions(users);
    }

    @Test
    void consumedRequestCannotIssueProofAgain() {
        Map<String, Object> state = verificationState();
        when(authorization.consumeRequest("request", state)).thenReturn(false);
        status(HttpStatus.FORBIDDEN, () -> service.verifyServiceTrackingOtp(verifyBody(), request));
        verify(authorization, never()).issue(anyString(), anyInt(), anyString(), anyString(), anyString());
        verifyNoInteractions(tracking);
    }

    @Test
    void attributeVerificationRequiresUniqueMatchAndMatchingService() {
        Map<String, Object> body = Map.of("serviceId", 7, "values", Map.of("a", "A", "b", "B", "c", "C"));
        when(forms.matchTrackingApplications(anyMap()))
                .thenReturn(ResponseEntity.ok(Map.of("sufficient", true, "applicationIds", List.of("app", "other"))));
        assertEquals(true, service.verifyServiceTrackingAttributes(body, request).get("additionalVerificationRequired"));
        when(forms.matchTrackingApplications(anyMap()))
                .thenReturn(ResponseEntity.ok(Map.of("sufficient", true, "applicationIds", List.of("app"))));
        when(tracking.findByApplIdAndTenantIdOrderByProcessIdAsc("app", "tenant")).thenReturn(List.of(history("app", 8)));
        assertEquals(List.of(), service.verifyServiceTrackingAttributes(body, request).get("applications"));
        when(tracking.findByApplIdAndTenantIdOrderByProcessIdAsc("app", "tenant")).thenReturn(List.of(history("app", 7)));
        when(authorization.issue("tenant", 7, "app", "FORM_ATTRIBUTES", "config-v1")).thenReturn("proof");
        assertEquals(1, ((List<?>) service.verifyServiceTrackingAttributes(body, request).get("applications")).size());
        verifyNoInteractions(users);
    }

    @Test
    void malformedMatcherResponseFailsClosed() {
        when(forms.matchTrackingApplications(anyMap()))
                .thenReturn(ResponseEntity.ok(Map.of("applicationIds", List.of(7))));
        status(HttpStatus.BAD_GATEWAY, () -> service.sendServiceTrackingOtp(otpBody(), request));
        verifyNoInteractions(users);
        verify(authorization, never()).saveRequest(anyMap());
    }

    @Test
    void publicTrackingConsumesScopedProofAndRedactsAuthenticatedDetails() {
        ApplicationTrackingRequest body = new ApplicationTrackingRequest();
        body.setServiceId(7); body.setApplId("app"); body.setVerificationProof("proof");
        when(tracking.findByApplIdAndTenantIdOrderByProcessIdAsc("app", "tenant")).thenReturn(List.of(history("app", 7)));
        ApplicationTrackingResponse result = service.trackPublicApplication(body, request);
        var ordered = inOrder(authorization, tracking);
        ordered.verify(authorization).consume("proof", "tenant", 7, "app", "config-v1");
        ordered.verify(tracking).findByApplIdAndTenantIdOrderByProcessIdAsc("app", "tenant");
        var detail = result.getTrackingDetails().getFirst();
        assertEquals("Received", detail.getActionName()); assertNull(detail.getActionOn());
        assertNull(detail.getProcessId()); assertNull(detail.getFormId()); assertNull(detail.getDataId());
        assertNull(detail.getActionRemarks()); assertNull(detail.getDocuments()); assertNull(detail.getPermissions());
        assertNull(result.getBeneficiaryUserName());
        config.put("displayActionTakenDate", true);
        assertNotNull(service.trackPublicApplication(body, request).getTrackingDetails().getFirst().getActionOn());
    }

    @Test
    void rejectedProofStopsTrackingLookup() {
        ApplicationTrackingRequest body = new ApplicationTrackingRequest();
        body.setServiceId(7); body.setApplId("app"); body.setVerificationProof("wrong");
        doThrow(PublicTrackingAuthorizationService.denied()).when(authorization)
                .consume("wrong", "tenant", 7, "app", "config-v1");
        status(HttpStatus.FORBIDDEN, () -> service.trackPublicApplication(body, request));
        verifyNoInteractions(tracking);
    }
}
