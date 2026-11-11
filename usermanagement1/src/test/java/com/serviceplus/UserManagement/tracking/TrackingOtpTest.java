package com.serviceplus.UserManagement.tracking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviceplus.UserManagement.Controller.TrackingOtpController;
import com.serviceplus.UserManagement.Dto.NotificationRequestDTO;
import com.serviceplus.UserManagement.Dto.NotificationResponseDTO;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Service.TenantConfiguration;
import com.serviceplus.UserManagement.Service.TrackingOtpService;
import com.serviceplus.UserManagement.feign.EsbFeignClient;
import com.serviceplus.UserManagement.profile.service.BucketService;
import com.serviceplus.UserManagement.profile.service.ModifyProfileRedisService;
import com.serviceplus.UserManagement.util.SnowflakeIdGenerator;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class TrackingOtpTest {
    private final ModifyProfileRedisService redis = mock(ModifyProfileRedisService.class);
    private final BucketService buckets = mock(BucketService.class);
    private final TenantConfiguration tenants = mock(TenantConfiguration.class);
    private final EsbFeignClient esb = mock(EsbFeignClient.class);
    private final Bucket requests = mock(Bucket.class), attempts = mock(Bucket.class);
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final Map<String, Object> sms = new HashMap<>(Map.of("serviceProvider", "test-provider",
            "trackingOtpTemplate", "Tracking OTP {0}, expires in {1} minutes", "dltTemplateId", "test-template"));
    private TrackingOtpService service;
    private Map<String, Object> saved;
    private NotificationRequestDTO notification;

    @BeforeEach
    void setUp() {
        service = new TrackingOtpService(redis, buckets, tenants, esb);
        ReflectionTestUtils.setField(service, "otpSecret", "s".repeat(32));
        ReflectionTestUtils.setField(service, "validityMinutes", 5);
        ReflectionTestUtils.setField(service, "trackingEmailSubject", "Tracking test");
        ReflectionTestUtils.setField(service, "trackingEmailBody", "Tracking OTP {0}, expires in {1} minutes");
        SnowflakeIdGenerator ids = new SnowflakeIdGenerator();
        ReflectionTestUtils.setField(ids, "workerIdConfigured", 1L);
        ReflectionTestUtils.setField(ids, "datacenterIdConfigured", 1L);
        ReflectionTestUtils.invokeMethod(ids, "init");
        when(buckets.getTrackingRequestBucket(anyString())).thenReturn(requests);
        when(buckets.getTrackingVerifyBucket("challenge")).thenReturn(attempts);
        when(requests.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.consumed(2, 0));
        when(attempts.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.consumed(2, 0));
        when(redis.saveTrackingOtp(anyMap())).thenAnswer(inv -> { saved = new HashMap<>(inv.getArgument(0)); return "challenge"; });
        when(redis.getTrackingOtp("challenge")).thenAnswer(inv -> saved == null ? null : new HashMap<>(saved));
        when(redis.consumeTrackingOtp(eq("challenge"), anyString(), anyString())).thenReturn(true);
        when(tenants.fetchConfig(request, "tenant", "MAIL")).thenReturn(Map.of("data", Map.of("MAIL", Map.of())));
        when(tenants.fetchConfig(request, "tenant", "SMS")).thenReturn(Map.of("data", Map.of("SMS", sms)));
        when(esb.send(any())).thenAnswer(inv -> {
            notification = inv.getArgument(0);
            NotificationResponseDTO response = new NotificationResponseDTO(); response.setSuccess(true); return response;
        });
    }

    private String send(String method, String mode) {
        return service.sendOtp(request, "tenant", 7, "R".equals(mode) ? "app" : null,
                mode, method, "MOBILE_OTP".equals(method) ? "919999900000" : "SYNTHETIC@example.invalid");
    }

    private String otp() {
        Object body = notification.getPayload().get("SMS".equals(notification.getChannel()) ? "message" : "body");
        var matcher = Pattern.compile("\\b[0-9]{6}\\b").matcher(body.toString());
        assertTrue(matcher.find()); return matcher.group();
    }

    private void status(HttpStatus expected, Runnable action) {
        assertEquals(expected, assertThrows(SPRuntimeError.class, action::run).getErrorCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"MOBILE_OTP", "EMAIL_OTP"})
    void sendsThroughExistingEsbAndStoresOnlyScopedHash(String method, CapturedOutput output) {
        assertEquals("challenge", send(method, "O"));
        String otp = otp();
        assertEquals("tenant", saved.get("tenantId")); assertEquals(7, saved.get("serviceId"));
        assertEquals("", saved.get("applId")); assertEquals("O", saved.get("mode"));
        assertEquals(method, saved.get("method")); assertEquals("PUBLIC_TRACKING", saved.get("purpose"));
        assertNotEquals(otp, saved.get("otpHash")); assertFalse(saved.containsKey("otp"));
        assertFalse(Pattern.compile("\\b" + otp + "\\b").matcher(output.getAll()).find());
        assertEquals("tenant", notification.getTenantId()); assertNotEquals("challenge", notification.getCorrelationId());
        assertEquals("MOBILE_OTP".equals(method) ? "SMS" : "EMAIL", notification.getChannel());
        if ("MOBILE_OTP".equals(method)) {
            assertEquals("SMS", notification.getAdapterCode()); assertEquals("test-provider", notification.getProvider());
            assertEquals(Map.of("mobile", "919999900000"), notification.getRecipient());
        } else {
            assertNull(notification.getAdapterCode()); assertEquals(Map.of("to", List.of("synthetic@example.invalid")), notification.getRecipient());
        }
        verify(esb).send(notification);
        verify(buckets, never()).resetVerifyBucket(anyLong());
        verify(buckets, never()).resetMobileVerifyBucket(anyLong());
    }

    @Test
    void referenceEmailKeepsApplicationScope() {
        assertEquals("challenge", service.sendEmailOtp(request, "tenant", 7, "app", "R", "synthetic@example.invalid"));
        assertEquals("app", saved.get("applId")); assertEquals("R", saved.get("mode"));
        assertEquals("app", service.verifyOtp("challenge", otp()).get("applId"));
    }

    @Test
    void verificationAtomicallyConsumesNonceAndHashAndOmitsSecrets() {
        send("EMAIL_OTP", "O");
        Map<String, Object> result = service.verifyOtp("challenge", otp());
        verify(redis).consumeTrackingOtp("challenge", (String) saved.get("nonce"), (String) saved.get("otpHash"));
        verify(redis, never()).deleteTrackingOtp("challenge");
        assertEquals("tenant", result.get("tenantId")); assertEquals(7, result.get("serviceId"));
        assertEquals("PUBLIC_TRACKING", result.get("purpose")); assertEquals("EMAIL_OTP", result.get("method"));
        assertEquals("synthetic@example.invalid", result.get("recipient"));
        assertFalse(result.containsKey("nonce")); assertFalse(result.containsKey("otpHash"));
    }

    @Test
    void replayCannotReturnVerifiedScope() {
        send("EMAIL_OTP", "O"); String otp = otp();
        when(redis.consumeTrackingOtp(eq("challenge"), anyString(), anyString())).thenReturn(true, false);
        assertNotNull(service.verifyOtp("challenge", otp));
        status(HttpStatus.BAD_REQUEST, () -> service.verifyOtp("challenge", otp));
    }

    @Test
    void concurrentValidVerificationsHaveOneWinner() throws Exception {
        send("EMAIL_OTP", "O"); String otp = otp();
        CyclicBarrier barrier = new CyclicBarrier(2);
        when(redis.getTrackingOtp("challenge")).thenAnswer(inv -> { barrier.await(5, TimeUnit.SECONDS); return new HashMap<>(saved); });
        AtomicBoolean consumed = new AtomicBoolean();
        when(redis.consumeTrackingOtp(eq("challenge"), anyString(), anyString()))
                .thenAnswer(inv -> consumed.compareAndSet(false, true));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> verify = () -> {
                try { service.verifyOtp("challenge", otp); return true; }
                catch (SPRuntimeError ex) { assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode()); return false; }
            };
            Future<Boolean> first = executor.submit(verify), second = executor.submit(verify);
            assertNotEquals(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
        } finally { executor.shutdownNow(); }
        verify(redis, never()).deleteTrackingOtp("challenge");
    }

    @Test
    void expiredOrMalformedOtpNeverConsumesChallenge() {
        status(HttpStatus.BAD_REQUEST, () -> service.verifyOtp("challenge", "123456"));
        status(HttpStatus.BAD_REQUEST, () -> service.verifyOtp("challenge", "bad"));
        verify(redis, never()).consumeTrackingOtp(anyString(), anyString(), anyString());
    }

    @Test
    void incorrectOtpRetainsChallengeUntilFinalAttempt() {
        send("EMAIL_OTP", "O");
        status(HttpStatus.BAD_REQUEST, () -> service.verifyOtp("challenge", "000000"));
        verify(redis, never()).deleteTrackingOtp("challenge");
        when(attempts.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.consumed(0, 0));
        status(HttpStatus.BAD_REQUEST, () -> service.verifyOtp("challenge", "000000"));
        verify(redis).deleteTrackingOtp("challenge");
        verify(redis, never()).consumeTrackingOtp(anyString(), anyString(), anyString());
    }

    @Test
    void exhaustedAttemptBucketDeletesChallenge() {
        send("EMAIL_OTP", "O"); String otp = otp();
        when(attempts.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.rejected(0, 1, 1));
        status(HttpStatus.TOO_MANY_REQUESTS, () -> service.verifyOtp("challenge", otp));
        verify(redis).deleteTrackingOtp("challenge");
    }

    @Test
    void requestLimitAndMissingSigningSecretFailBeforeSending() {
        when(requests.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.rejected(0, 1, 1));
        status(HttpStatus.TOO_MANY_REQUESTS, () -> send("EMAIL_OTP", "O"));
        ReflectionTestUtils.setField(service, "otpSecret", "short");
        status(HttpStatus.SERVICE_UNAVAILABLE, () -> send("EMAIL_OTP", "O"));
        verifyNoInteractions(esb); verify(redis, never()).saveTrackingOtp(anyMap());
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "rejected", "exception"})
    void failedDispatchDeletesChallengeAndDoesNotLeakException(String failure, CapturedOutput output) {
        if ("exception".equals(failure)) when(esb.send(any())).thenThrow(new IllegalStateException("sensitive-provider-error"));
        else when(esb.send(any())).thenReturn("null".equals(failure) ? null : new NotificationResponseDTO());
        status(HttpStatus.BAD_GATEWAY, () -> send("EMAIL_OTP", "O"));
        verify(redis).deleteTrackingOtp("challenge");
        assertFalse(output.getAll().contains("sensitive-provider-error"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"provider", "trackingOtpTemplate", "dltTemplateId"})
    void missingSmsConfigurationFailsClosed(String missing) {
        sms.remove("provider".equals(missing) ? "serviceProvider" : missing);
        status(HttpStatus.BAD_GATEWAY, () -> send("MOBILE_OTP", "O"));
        verifyNoInteractions(esb); verify(redis).deleteTrackingOtp("challenge");
    }

    @Test
    void internalEndpointsRejectMissingWrongOrUnconfiguredToken() {
        TrackingOtpService delegate = mock(TrackingOtpService.class);
        TrackingOtpController controller = new TrackingOtpController(delegate);
        ReflectionTestUtils.setField(controller, "trackingInternalToken", "test-token");
        for (String token : new String[] {null, "wrong"}) {
            assertEquals(403, controller.sendOtp(Map.of(), token, request).getStatusCode().value());
            assertEquals(403, controller.sendEmailOtp(Map.of(), token, request).getStatusCode().value());
            assertEquals(403, controller.verifyOtp(Map.of(), token).getStatusCode().value());
        }
        ReflectionTestUtils.setField(controller, "trackingInternalToken", " ");
        assertEquals(403, controller.verifyOtp(Map.of(), " ").getStatusCode().value());
        verifyNoInteractions(delegate);
    }

    @Test
    void internalControllerPreservesOtpContractsAndServiceScope() {
        TrackingOtpService delegate = mock(TrackingOtpService.class);
        TrackingOtpController controller = new TrackingOtpController(delegate);
        ReflectionTestUtils.setField(controller, "trackingInternalToken", "test-token");
        when(delegate.sendOtp(request, "tenant", 7, null, "O", "MOBILE_OTP", "919999900000")).thenReturn("challenge");
        assertEquals(Map.of("challengeId", "challenge", "message", "OTP request processed"), controller.sendOtp(
                Map.of("tenantId", "tenant", "serviceId", 7, "mode", "O", "method", "MOBILE_OTP", "contact", "919999900000"),
                "test-token", request).getBody());
        Map<String, Object> scope = Map.of("tenantId", "tenant", "serviceId", 7, "purpose", "PUBLIC_TRACKING");
        when(delegate.verifyOtp("challenge", "123456")).thenReturn(scope);
        assertEquals(Map.of("verified", true, "verificationData", scope),
                controller.verifyOtp(Map.of("challengeId", "challenge", "otp", "123456"), "test-token").getBody());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, 7.5, 2147483648d})
    void controllerRejectsInvalidServiceNumbers(double id) {
        TrackingOtpService delegate = mock(TrackingOtpService.class);
        TrackingOtpController controller = new TrackingOtpController(delegate);
        ReflectionTestUtils.setField(controller, "trackingInternalToken", "test-token");
        assertEquals(400, controller.sendOtp(Map.of("tenantId", "tenant", "serviceId", id, "mode", "O",
                "method", "EMAIL_OTP", "contact", "a@example.invalid"), "test-token", request).getStatusCode().value());
        verifyNoInteractions(delegate);
    }

    @Test
    void sharedRedisPreservesTrackingTtlAndProfileEmailMobileNamespaces() {
        ModifyProfileRedisService storage = new ModifyProfileRedisService();
        RedisTemplate<String, String> emailRedis = mock(RedisTemplate.class);
        StringRedisTemplate stringRedis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> emailValues = mock(ValueOperations.class), stringValues = mock(ValueOperations.class);
        when(emailRedis.opsForValue()).thenReturn(emailValues); when(stringRedis.opsForValue()).thenReturn(stringValues);
        ReflectionTestUtils.setField(storage, "redisTemplate", emailRedis);
        ReflectionTestUtils.setField(storage, "stringRedisTemplate", stringRedis);
        ReflectionTestUtils.setField(storage, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(storage, "otpValidityMin", 15);
        ReflectionTestUtils.setField(storage, "trackingValidityMinutes", 5);
        storage.saveOtp(9L, "a@example.invalid", "111111");
        storage.saveMobileOtp(9L, "9999900000", "222222");
        String id = storage.saveTrackingOtp(Map.of("purpose", "PUBLIC_TRACKING"));
        verify(emailValues).set(eq("modifyProfileEmailOtp:9"), anyString(), eq(15L), eq(TimeUnit.MINUTES));
        verify(stringValues).set(eq("modifyProfileMobileOtp:9"), anyString(), eq(15L), eq(TimeUnit.MINUTES));
        verify(stringValues).set(eq("tracking:otp:" + id), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        when(stringRedis.execute(any(RedisScript.class), eq(List.of("tracking:otp:" + id)), eq("nonce"), eq("hash")))
                .thenReturn(1L, 0L);
        assertTrue(storage.consumeTrackingOtp(id, "nonce", "hash"));
        assertFalse(storage.consumeTrackingOtp(id, "nonce", "hash"));
        verify(emailRedis, never()).delete(anyString());
    }
}
