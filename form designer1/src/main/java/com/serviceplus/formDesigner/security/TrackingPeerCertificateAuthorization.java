package com.serviceplus.formDesigner.security;

import jakarta.servlet.http.HttpServletRequest;
import java.security.cert.CertificateParsingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Authorizes only mutually authenticated service peers for tracking internal APIs.
 * The TLS connector must validate peer certificate chains against a trusted CA.
 * Never trust client certificate values forwarded through user-supplied HTTP headers.
 */
@Component
public final class TrackingPeerCertificateAuthorization {
    static final String CERTIFICATE_ATTRIBUTE = "jakarta.servlet.request.X509Certificate";
    private final Set<String> allowedPeerDns;

    public TrackingPeerCertificateAuthorization(
            @Value("${tracking.internal.mtls.allowed-peer-dns:}") String allowedPeerDns) {
        this.allowedPeerDns = Arrays.stream(allowedPeerDns == null ? new String[0] : allowedPeerDns.split(","))
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .filter(value -> !value.isBlank() && !value.contains("*"))
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean authorized(HttpServletRequest request) {
        if (request == null || !request.isSecure() || allowedPeerDns.isEmpty()) {
            return false;
        }
        Object attribute = request.getAttribute(CERTIFICATE_ATTRIBUTE);
        if (!(attribute instanceof X509Certificate[] certificates)
                || certificates.length == 0 || certificates[0] == null) {
            return false;
        }

        X509Certificate leaf = certificates[0];
        try {
            leaf.checkValidity();
            Collection<List<?>> sans = leaf.getSubjectAlternativeNames();
            if (sans == null) {
                return false;
            }
            for (List<?> san : sans) {
                // type 2 = dNSName; exact SAN match, never CN substring or wildcard.
                if (san.size() >= 2 && Integer.valueOf(2).equals(san.get(0))
                        && san.get(1) instanceof String dns
                        && allowedPeerDns.contains(dns.toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
        } catch (CertificateParsingException | CertificateExpiredException | CertificateNotYetValidException ex) {
            return false;
        }
        return false;
    }
}
