package com.serviceplus.tracking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviceplus.tracking.ExceptionHandler.SPRuntimeError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PublicTrackingAuthorizationServiceTest {
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> values = mock(ValueOperations.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private PublicTrackingAuthorizationService service;
    private final String id = "a".repeat(43);

    @BeforeEach
    void setUp() {
        service = new PublicTrackingAuthorizationService(redis, mapper);
        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "proofTtl", 180L);
        ReflectionTestUtils.setField(service, "requestTtl", 300L);
        ReflectionTestUtils.setField(service, "requestLimit", 10);
        when(redis.opsForValue()).thenReturn(values);
    }

    private Map<String, Object> scope() {
        return new HashMap<>(Map.of("tenantId", "tenant", "serviceId", 7, "applicationId", "app",
                "configuration", "config", "method", "EMAIL_OTP", "purpose", "PUBLIC_TRACKING"));
    }

    @Test
    void disabledFeatureFailsBeforeRedis() {
        ReflectionTestUtils.setField(service, "enabled", false);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, assertThrows(SPRuntimeError.class,
                () -> service.issue("tenant", 7, "app", "EMAIL_OTP", "config")).getErrorCode());
        verifyNoInteractions(redis);
    }

    @Test
    void savedProofHasExpiryAndOpaqueHashedKey() {
        String proof = service.issue("tenant", 7, "app", "EMAIL_OTP", "config");
        assertTrue(proof.matches("[A-Za-z0-9_-]{43}"));
        verify(values).set(eq("tracking:public:proof:" + service.fingerprint(proof)), anyString(), eq(Duration.ofSeconds(180)));
        service.saveRequest(Map.of("tenantId", "tenant"));
        verify(values).set(startsWith("tracking:public:request:"), anyString(), eq(Duration.ofSeconds(300)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"tenantId", "serviceId", "applicationId", "configuration", "purpose"})
    void mismatchedProofScopeFailsBeforeConsumption(String field) throws Exception {
        Map<String, Object> scope = scope(); scope.put(field, "wrong");
        when(values.get(anyString())).thenReturn(mapper.writeValueAsString(scope));
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(SPRuntimeError.class,
                () -> service.consume(id, "tenant", 7, "app", "config")).getErrorCode());
        verify(redis, never()).execute(any(RedisScript.class), anyList(), any(Object[].class));
    }

    @Test
    void proofReplayOrConcurrentConsumeLoserCannotAuthorize() throws Exception {
        when(values.get(anyString())).thenReturn(mapper.writeValueAsString(scope()));
        when(redis.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenReturn(1L, 0L);
        assertDoesNotThrow(() -> service.consume(id, "tenant", 7, "app", "config"));
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(SPRuntimeError.class,
                () -> service.consume(id, "tenant", 7, "app", "config")).getErrorCode());
    }

    @Test
    void expiredOrMalformedProofCannotAuthorize() {
        when(values.get(anyString())).thenReturn(null);
        assertThrows(SPRuntimeError.class, () -> service.consume(id, "tenant", 7, "app", "config"));
        when(values.get(anyString())).thenReturn("not-json");
        assertThrows(SPRuntimeError.class, () -> service.consume(id, "tenant", 7, "app", "config"));
        assertThrows(SPRuntimeError.class, () -> service.consume("short", "tenant", 7, "app", "config"));
    }

    @Test
    void rateLimitRejectsExceededLimitAndUnavailableCounter() {
        when(redis.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenReturn(10L, 11L, null);
        assertDoesNotThrow(() -> service.rateLimit("tenant", 7, "peer"));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, assertThrows(SPRuntimeError.class,
                () -> service.rateLimit("tenant", 7, "peer")).getErrorCode());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, assertThrows(SPRuntimeError.class,
                () -> service.rateLimit("tenant", 7, "peer")).getErrorCode());
    }
}
