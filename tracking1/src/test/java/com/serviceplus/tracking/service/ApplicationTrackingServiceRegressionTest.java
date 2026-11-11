package com.serviceplus.tracking.service;

import com.serviceplus.tracking.dto.*;
import com.serviceplus.tracking.entity.*;
import com.serviceplus.tracking.feignClient.*;
import com.serviceplus.tracking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ApplicationTrackingServiceRegressionTest {
    private final ApplicationTrackingRepository tracking = mock(ApplicationTrackingRepository.class);
    private final WorkflowInboxRedisService inbox = mock(WorkflowInboxRedisService.class);
    private final ApplicationTrackingRepositoryCustomImpl custom = mock(ApplicationTrackingRepositoryCustomImpl.class);
    private final ApplicationTrackingHolderRepository holders = mock(ApplicationTrackingHolderRepository.class);
    private final ApplicationTrackingDocumentRepository documents = mock(ApplicationTrackingDocumentRepository.class);
    private final ApplicantInboxRepository applicants = mock(ApplicantInboxRepository.class);
    private final InstanceConfigRegistryFeignClient registry = mock(InstanceConfigRegistryFeignClient.class);
    private final ServiceMetadataFeignClient metadata = mock(ServiceMetadataFeignClient.class);
    private final FormDesignerFeignClient forms = mock(FormDesignerFeignClient.class);
    private final UserManagmentFeignClient users = mock(UserManagmentFeignClient.class);
    private final PublicTrackingAuthorizationService authorization = mock(PublicTrackingAuthorizationService.class);
    private ApplicationTrackingService service;

    @BeforeEach
    void setUp() {
        service = new ApplicationTrackingService(tracking, inbox, custom, registry, holders, documents,
                applicants, metadata, forms, users, authorization);
    }

    private MockHttpServletRequest authenticated(long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("USER-DETAILS", "{\"userID\":" + userId + ",\"locationId\":2,\"tenantId\":\"tenant\"}");
        return request;
    }

    private ApplicationTracking row(long applicant) {
        ApplicationTracking row = new ApplicationTracking();
        row.setApplId("app"); row.setServiceId(7); row.setProcessId("process");
        row.setAppliedUserId(applicant); row.setFormId("form"); row.setDataId("data");
        row.setActionRemarks("private remarks"); row.setBeneficiaryUserName("beneficiary");
        return row;
    }

    private ApplicationTrackingDocument document(long id, String permission) {
        ApplicationTrackingDocument document = new ApplicationTrackingDocument();
        document.setId(id); document.setDocumentName("Document " + id);
        document.setViewPermission(List.of(permission));
        return document;
    }

    @Test
    void authenticatedApplicantRetainsFormRemarksAndApplicantDocuments() {
        when(inbox.getTokensByUserAndLocation(5L, "2")).thenReturn(Set.of());
        when(tracking.findByApplIdAndTenantIdOrderByProcessIdAsc("app", "tenant")).thenReturn(List.of(row(5)));
        when(documents.findByTrackingProcessId("process"))
                .thenReturn(List.of(document(1, "applicant"), document(2, "official")));
        ApplicationTrackingRequest body = new ApplicationTrackingRequest(); body.setApplId("app");
        var result = service.commonTrack(body, authenticated(5));
        var detail = result.getTrackingDetails().getFirst();
        assertEquals("beneficiary", result.getBeneficiaryUserName());
        assertEquals("form", detail.getFormId()); assertEquals("data", detail.getDataId());
        assertEquals("private remarks", detail.getActionRemarks());
        assertEquals(List.of(1L), detail.getDocuments().stream().map(d -> d.getDocumentId()).toList());
        verifyNoInteractions(registry, metadata, forms, users, authorization);
    }

    @Test
    void authenticatedOfficialRetainsHolderScopedAccessAndOfficialDocuments() {
        when(inbox.getTokensByUserAndLocation(9L, "2")).thenReturn(Set.of("holder"));
        ApplicationTrackingHolder holder = new ApplicationTrackingHolder(); holder.setProcessId("process");
        when(holders.findByHolderIdInAndTenantId(Set.of("holder"), "tenant")).thenReturn(List.of(holder));
        when(tracking.findByApplIdAndTenantIdOrderByProcessIdAsc("app", "tenant")).thenReturn(List.of(row(5)));
        when(documents.findByTrackingProcessId("process"))
                .thenReturn(List.of(document(1, "applicant"), document(2, "official")));
        ApplicationTrackingRequest body = new ApplicationTrackingRequest(); body.setApplId("app");
        var detail = service.getTracking(body, authenticated(9)).getTrackingDetails().getFirst();
        assertEquals("form", detail.getFormId()); assertEquals("private remarks", detail.getActionRemarks());
        assertEquals(List.of(2L), detail.getDocuments().stream().map(d -> d.getDocumentId()).toList());
        verifyNoInteractions(registry, metadata, forms, users, authorization);
    }

    @Test
    void unrelatedAuthenticatedUserDoesNotGainPrivateFields() {
        when(inbox.getTokensByUserAndLocation(6L, "2")).thenReturn(Set.of());
        when(tracking.findByApplRefNoAndTenantIdOrderByProcessIdAsc("REF", "tenant")).thenReturn(List.of(row(5)));
        ApplicationTrackingRequest body = new ApplicationTrackingRequest(); body.setApplRefNo("REF");
        var detail = service.getTracking(body, authenticated(6)).getTrackingDetails().getFirst();
        assertNull(detail.getFormId()); assertNull(detail.getDataId());
        assertNull(detail.getActionRemarks()); assertNull(detail.getDocuments());
        verifyNoInteractions(documents, registry, metadata, forms, users, authorization);
    }

    @Test
    void anonymousCommonAndAuthenticatedEntryPointsUsePublicVerification() {
        ApplicationTrackingRequest body = new ApplicationTrackingRequest();
        MockHttpServletRequest request = new MockHttpServletRequest();
        ApplicationTrackingResponse response = new ApplicationTrackingResponse();
        ApplicationTrackingService routing = spy(service);
        doReturn(response).when(routing).trackPublicApplication(body, request);
        assertSame(response, routing.commonTrack(body, request));
        assertSame(response, routing.getTracking(body, request));
        verify(routing, times(2)).trackPublicApplication(body, request);
        verifyNoInteractions(tracking, inbox, holders, documents);
    }

    @Test
    void existingReferenceEmailOtpPreservesTenantScopeAndUsesConfiguredContact() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("SP-Client-Domain", "tenant.example");
        when(registry.fetchTenantId(Map.of("SP-Client-Domain", "tenant.example")))
                .thenReturn(ResponseEntity.ok("tenant"));
        when(tracking.findByApplRefNoAndApplyDateAndTenantIdOrderByProcessIdAsc(
                "REF", java.time.LocalDate.of(2026, 1, 1).atStartOfDay(), "tenant"))
                .thenReturn(List.of(row(5)));
        when(metadata.getPublicTrackingConfiguration(7L, "tenant"))
                .thenReturn(Map.of("trackingTypes", List.of("EMAIL_OTP"),
                        "emailAttribute", Map.of("attributeId", "email")));
        when(forms.getTrackingApplicationData(Map.of("tenantId", "tenant",
                "serviceId", 7, "applId", "app", "attributeIds", List.of("email"))))
                .thenReturn(ResponseEntity.ok(Map.of("attributeValues", Map.of("email", "saved@example.test"))));
        Map<String, Object> otpRequest = Map.of("tenantId", "tenant", "serviceId", 7,
                "applId", "app", "mode", "R", "email", "saved@example.test");
        when(users.sendTrackingEmailOtp(otpRequest))
                .thenReturn(ResponseEntity.ok(Map.of("challengeId", "challenge")));
        PublicApplicationTrackingRequest reference = new PublicApplicationTrackingRequest();
        reference.setApplRefNo("REF"); reference.setApplyDate("01/01/2026");
        assertEquals(Map.of("challengeId", "challenge", "message", "OTP request processed"),
                service.sendReferenceEmailOtp(reference, request));
        verify(users).sendTrackingEmailOtp(otpRequest);
        verifyNoInteractions(inbox, holders, documents, custom, applicants);
    }
}
