package com.serviceplus.UserManagement.profile.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;

@Service
public class BucketService {

    private static final String EMAIL_REQUEST_PREFIX =
            "modifyProfileEmailRequestBucket:";
    private static final String EMAIL_VERIFY_PREFIX =
            "modifyProfileEmailVerifyBucket:";

    private static final String MOBILE_REQUEST_PREFIX =
            "modifyProfileMobileRequestBucket:";
    private static final String MOBILE_VERIFY_PREFIX =
            "modifyProfileMobileVerifyBucket:";

    private static final String TRACKING_REQUEST_PREFIX =
            "trackingOtpRequestBucket:";
    private static final String TRACKING_VERIFY_PREFIX =
            "trackingOtpVerifyBucket:";

    private final ProxyManager<byte[]> proxyManager;

    @Value("${modify.profile.email.sms.request.limit}")
    private Integer requestLimit;

    @Value("${modify.profile.email.sms.session.validity.min}")
    private Integer sessionValidityMin;

    @Value("${modify.profile.email.sms.otp.attemps}")
    private Integer totalOtpAttemps;

    @Value("${modify.profile.email.sms.otp.attemps.block.time}")
    private Integer otpAttempsBlockTime;

    @Autowired
    public BucketService(ProxyManager<byte[]> proxyManager) {
        this.proxyManager = proxyManager;
    }

    private byte[] key(String name) {
        return name.getBytes(StandardCharsets.UTF_8);
    }

    private Bucket createBucket(
            String name, Integer capacity, Integer refillMinutes) {

        if (capacity == null || capacity < 1
                || refillMinutes == null || refillMinutes < 1) {
            throw new IllegalStateException(
                    "Invalid OTP rate-limit configuration");
        }

        Supplier<BucketConfiguration> config = () ->
                BucketConfiguration.builder()
                    .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillIntervally(
                            capacity,
                            Duration.ofMinutes(refillMinutes))
                        .build())
                    .build();

        return proxyManager.builder().build(key(name), config);
    }

    // MODIFY PROFILE - EMAIL OTP

    public Bucket getOtpRequestBucket(Long userId) {
        return createBucket(
                EMAIL_REQUEST_PREFIX + userId,
                requestLimit, sessionValidityMin);
    }

    public Bucket getOtpVerifyBucket(Long userId) {
        return createBucket(
                EMAIL_VERIFY_PREFIX + userId,
                totalOtpAttemps, otpAttempsBlockTime);
    }

    public void resetVerifyBucket(Long userId) {
        proxyManager.removeProxy(key(EMAIL_VERIFY_PREFIX + userId));
    }

    // MODIFY PROFILE - MOBILE OTP

    public Bucket getMobileOtpRequestBucket(Long userId) {
        return createBucket(
                MOBILE_REQUEST_PREFIX + userId,
                requestLimit, sessionValidityMin);
    }

    public Bucket getMobileOtpVerifyBucket(Long userId) {
        return createBucket(
                MOBILE_VERIFY_PREFIX + userId,
                totalOtpAttemps, otpAttempsBlockTime);
    }

    public void resetMobileVerifyBucket(Long userId) {
        proxyManager.removeProxy(key(MOBILE_VERIFY_PREFIX + userId));
    }

    // PUBLIC TRACKING - MOBILE / EMAIL OTP

    public Bucket getTrackingRequestBucket(String identityKey) {
        return createBucket(
                TRACKING_REQUEST_PREFIX + identityKey,
                requestLimit, sessionValidityMin);
    }

    public Bucket getTrackingVerifyBucket(String challengeId) {
        return createBucket(
                TRACKING_VERIFY_PREFIX + challengeId,
                totalOtpAttemps, otpAttempsBlockTime);
    }
}
