package com.serviceplus.UserManagement.SSO.JanParichay;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class JanParichaySignatureUtil {

    private static final String HMAC_ALGORITHM = "HmacSHA512";

    private JanParichaySignatureUtil() {
    }

    public static String generateSignature(String serviceId, long ttl, String requestUri, String authKey, String secretKey, String... bodyValues) {

        try {

            StringBuilder signatureBuilder = new StringBuilder();

            signatureBuilder.append(serviceId).append(ttl).append(requestUri);

            for (String value : bodyValues) {
                if (value != null) {
                    signatureBuilder.append(value);
                }
            }

            String signature = signatureBuilder.toString();
            System.out.println(signature);

            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));

            byte[] hmac = mac.doFinal(signature.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);

        } catch (Exception ex) {
            throw new RuntimeException("Unable to generate JanParichay Signature", ex);
        }
    }
}