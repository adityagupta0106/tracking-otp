package com.serviceplus.UserManagement.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CryptoUtil {

    private CryptoUtil() {
    }

    public static String md5(String value) {
        return doHash(value, "MD5");
    }

    public static String sha256(String value) {
        return doHash(value, "SHA-256");
    }

    public static String sha512(String value) {
        return doHash(value, "SHA-512");
    }

    public static String doHash(String value, String algorithm) {

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] encryptedBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encryptedBytes);

        } catch (Exception ex) {
            throw new RuntimeException("Unable to encrypt using algorithm : " + algorithm, ex);
        }
    }

    private static String bytesToHex(byte[] bytes) {

        StringBuilder hexString = new StringBuilder();

        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);

            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
