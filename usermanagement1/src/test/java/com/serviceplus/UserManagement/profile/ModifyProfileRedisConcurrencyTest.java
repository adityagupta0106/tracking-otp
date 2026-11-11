package com.serviceplus.UserManagement.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviceplus.UserManagement.profile.dto.ModifyMobileOtpDTO;
import com.serviceplus.UserManagement.profile.service.ModifyProfileRedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModifyProfileRedisConcurrencyTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ModifyProfileRedisService redisService;

    private final Long userId = 1001L;
    private final String correctOtp = "123456";
    private final String wrongOtp = "999999";
    private final String mobileKey = "modifyProfileMobileOtp:1001";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(redisService, "otpValidityMin", 15);
    }

    @Test
    void testDeleteMobileOtpAtomically_MatchingOtp_ReturnsTrue() {
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList(mobileKey)), eq(correctOtp)))
                .thenReturn(1L);

        boolean result = redisService.deleteMobileOtpAtomically(userId, correctOtp);
        assertTrue(result, "Atomic delete must return true when OTP matches in Redis");
    }

    @Test
    void testDeleteMobileOtpAtomically_MismatchedOtp_ReturnsFalse() {
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList(mobileKey)), eq(wrongOtp)))
                .thenReturn(0L);

        boolean result = redisService.deleteMobileOtpAtomically(userId, wrongOtp);
        assertFalse(result, "Atomic delete must return false when OTP does not match in Redis");
    }

    @Test
    void testDeleteMobileOtpAtomically_InvalidInput_ReturnsFalseWithoutCallingRedis() {
        assertFalse(redisService.deleteMobileOtpAtomically(null, "123456"));
        assertFalse(redisService.deleteMobileOtpAtomically(userId, null));
        assertFalse(redisService.deleteMobileOtpAtomically(userId, "  "));
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void testDeleteMobileOtpIfMatching_MismatchedOtp_DoesNotDelete() {
        // Simulates old failed ESB call trying to delete with old OTP while new OTP is present in Redis
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList(mobileKey)), eq("OLD_OTP")))
                .thenReturn(0L);

        boolean deleted = redisService.deleteMobileOtpIfMatching(userId, "OLD_OTP");
        assertFalse(deleted, "Must not delete when challenge OTP does not match expected");
    }

    @Test
    void testDeleteMobileOtpIfMatching_MatchingOtp_Deletes() {
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList(mobileKey)), eq("MATCHING_OTP")))
                .thenReturn(1L);

        boolean deleted = redisService.deleteMobileOtpIfMatching(userId, "MATCHING_OTP");
        assertTrue(deleted, "Must delete when challenge OTP matches expected");
    }

    @Test
    void testConcurrentAtomicDeletion_SimulatedRace_OnlyOneThreadSucceeds() throws Exception {
        // Mock Redis Lua execution: only first caller gets 1L (deleted), subsequent callers get 0L
        AtomicBoolean deletedOnce = new AtomicBoolean(false);
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList(mobileKey)), eq(correctOtp)))
                .thenAnswer(inv -> deletedOnce.compareAndSet(false, true) ? 1L : 0L);

        int concurrency = 16;
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch readyLatch = new CountDownLatch(concurrency);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < concurrency; i++) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    boolean res = redisService.deleteMobileOtpAtomically(userId, correctOtp);
                    if (res) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            }));
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        for (Future<?> f : futures) {
            f.get(5, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertEquals(1, successCount.get(), "Exactly one thread must execute atomic deletion successfully");
        assertEquals(concurrency - 1, failCount.get(), "All other concurrent callers must receive false");
    }

    @Test
    void testConcurrentDeleteIfMatching_SimulatedRaceWithNewOtpRequest() throws Exception {
        // Thread A is a delayed failed ESB handler trying to delete "OTP_A"
        // Thread B is a concurrent verification or valid consumer
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList(mobileKey)), eq("OTP_OLD")))
                .thenReturn(0L); // Redis key now holds OTP_NEW, so Lua returns 0
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList(mobileKey)), eq("OTP_NEW")))
                .thenReturn(1L); // Matches new challenge

        boolean oldDeleted = redisService.deleteMobileOtpIfMatching(userId, "OTP_OLD");
        boolean newDeleted = redisService.deleteMobileOtpIfMatching(userId, "OTP_NEW");

        assertFalse(oldDeleted, "Stale request with old OTP must not delete current OTP");
        assertTrue(newDeleted, "Request with current OTP must succeed in deletion");
    }
}
