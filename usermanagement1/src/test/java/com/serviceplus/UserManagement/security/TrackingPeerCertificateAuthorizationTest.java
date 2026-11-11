package com.serviceplus.UserManagement.security;

import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrackingPeerCertificateAuthorizationTest {
    private static final String ATTRIBUTE = "jakarta.servlet.request.X509Certificate";

    private MockHttpServletRequest secureRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSecure(true);
        return request;
    }

    private X509Certificate peer(String dns) throws Exception {
        X509Certificate certificate = mock(X509Certificate.class);
        when(certificate.getSubjectAlternativeNames()).thenReturn(List.of(List.of(2, dns)));
        return certificate;
    }

    @Test
    void allowedPeerWithValidCertificateIsAccepted() throws Exception {
        var guard = new TrackingPeerCertificateAuthorization("trackingservice.internal,formdesigner.internal");
        var request = secureRequest();
        request.setAttribute(ATTRIBUTE, new X509Certificate[]{peer("trackingservice.internal")});
        assertTrue(guard.authorized(request));
    }

    @Test
    void noCertificateOrSpoofedHeaderIsDenied() {
        var guard = new TrackingPeerCertificateAuthorization("trackingservice.internal");
        var request = secureRequest();
        request.addHeader("X-Tracking-Internal-Token", "anything");
        request.addHeader("X-Forwarded-Client-Cert", "spoofed");
        assertFalse(guard.authorized(request));
        assertFalse(guard.authorized(new MockHttpServletRequest()));
    }

    @Test
    void wrongPeerWildcardAndNoAllowlistAreDenied() throws Exception {
        var request = secureRequest();
        request.setAttribute(ATTRIBUTE, new X509Certificate[]{peer("other.internal")});
        assertFalse(new TrackingPeerCertificateAuthorization("trackingservice.internal").authorized(request));
        assertFalse(new TrackingPeerCertificateAuthorization("*").authorized(request));
        assertFalse(new TrackingPeerCertificateAuthorization("").authorized(request));
    }

    @Test
    void expiredCertificateIsDenied() throws Exception {
        var request = secureRequest();
        X509Certificate cert = peer("trackingservice.internal");
        doThrow(new CertificateExpiredException()).when(cert).checkValidity();
        request.setAttribute(ATTRIBUTE, new X509Certificate[]{cert});
        assertFalse(new TrackingPeerCertificateAuthorization("trackingservice.internal").authorized(request));
    }
}
