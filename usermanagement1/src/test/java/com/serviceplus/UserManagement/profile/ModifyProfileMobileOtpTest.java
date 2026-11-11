package com.serviceplus.UserManagement.profile;

import com.serviceplus.UserManagement.Dto.NotificationRequestDTO;
import com.serviceplus.UserManagement.Dto.NotificationResponseDTO;
import com.serviceplus.UserManagement.Entity.UserProfile;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Repository.UserProfileRepository;
import com.serviceplus.UserManagement.Service.TenantConfiguration;
import com.serviceplus.UserManagement.feign.EsbFeignClient;
import com.serviceplus.UserManagement.profile.dto.ModifyEmailDTO;
import com.serviceplus.UserManagement.profile.dto.ModifyMobileOtpDTO;
import com.serviceplus.UserManagement.profile.service.BucketService;
import com.serviceplus.UserManagement.profile.service.ModifyProfileRedisService;
import com.serviceplus.UserManagement.profile.service.ModifyProfileService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModifyProfileMobileOtpTest {

    @Mock
    private UserProfileRepository profileRepository;

    @Mock
    private ModifyProfileRedisService redisService;

    @Mock
    private BucketService bucketService;

    @Mock
    private TenantConfiguration tenantConfiguration;

    @Mock
    private EsbFeignClient esbFeign;

    @Mock
    private Environment environment;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Bucket requestBucket;

    @Mock
    private Bucket verifyBucket;

    @InjectMocks
    private ModifyProfileService modifyProfileService;

    private UserSessionObject userSession;
    private UserProfile userProfile;
    private final Long userId = 1001L;
    private final String oldMobile = "9876543210";
    private final String newMobile = "9123456780";

    @BeforeEach
    void setUp() {
        userSession = new UserSessionObject();
        userSession.setUserID(userId);
        userSession.setTenantId("tenant_sp");

        userProfile = new UserProfile();
        userProfile.setUserId(userId);
        userProfile.setMobileNo(oldMobile);
        userProfile.setTenantId("tenant_sp");

        com.serviceplus.UserManagement.util.SnowflakeIdGenerator generator =
                new com.serviceplus.UserManagement.util.SnowflakeIdGenerator();
        ReflectionTestUtils.setField(generator, "workerIdConfigured", 1L);
        ReflectionTestUtils.setField(generator, "datacenterIdConfigured", 1L);
        ReflectionTestUtils.invokeMethod(generator, "init");

        ReflectionTestUtils.setField(modifyProfileService, "validityMin", 15);
        ReflectionTestUtils.setField(modifyProfileService, "mockEnabled", false);
        ReflectionTestUtils.setField(modifyProfileService, "modifyProfileMobileOtpContent",
                "Your One Time Password (OTP) is {0}. Validity is {1} minutes.");
    }

    @Test
    void testRequestMobileOtp_InvalidMobile_ThrowsException() {
        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);

        // Not 10 digits
        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () ->
                modifyProfileService.requestMobileOtp(request, userSession, userId, "12345"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("valid 10-digit mobile number"));

        // Non-numeric
        assertThrows(SPRuntimeError.class, () ->
                modifyProfileService.requestMobileOtp(request, userSession, userId, "abcdefghij"));
    }

    @Test
    void testRequestMobileOtp_SameAsCurrentMobile_Rejected() {
        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);

        ResponseEntity<?> response = modifyProfileService.requestMobileOtp(request, userSession, userId, oldMobile);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("must be different"));
    }

    @Test
    void testRequestMobileOtp_AlreadyRegisteredWithAnotherUser_Rejected() {
        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);
        when(profileRepository.findByMobileNoAndUserIdNot(newMobile, userId)).thenReturn(new UserProfile(9999L));

        ResponseEntity<?> response = modifyProfileService.requestMobileOtp(request, userSession, userId, newMobile);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("already registered"));
    }

    @Test
    void testRequestMobileOtp_RateLimitExceeded_ThrowsException() {
        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);
        when(profileRepository.findByMobileNoAndUserIdNot(newMobile, userId)).thenReturn(null);
        when(bucketService.getMobileOtpVerifyBucket(userId)).thenReturn(verifyBucket);
        when(verifyBucket.getAvailableTokens()).thenReturn(3L);
        when(bucketService.getMobileOtpRequestBucket(userId)).thenReturn(requestBucket);

        ConsumptionProbe rejectedProbe = ConsumptionProbe.rejected(0, 120_000_000_000L, 120_000_000_000L);
        when(requestBucket.tryConsumeAndReturnRemaining(1)).thenReturn(rejectedProbe);

        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () ->
                modifyProfileService.requestMobileOtp(request, userSession, userId, newMobile));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("maximum OTP request limit"));
    }

    @Test
    void testRequestMobileOtp_MockMode_GeneratesOtpWithoutEsbCall() {
        ReflectionTestUtils.setField(modifyProfileService, "mockEnabled", true);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"local"});

        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);
        when(profileRepository.findByMobileNoAndUserIdNot(newMobile, userId)).thenReturn(null);
        when(bucketService.getMobileOtpVerifyBucket(userId)).thenReturn(verifyBucket);
        when(verifyBucket.getAvailableTokens()).thenReturn(3L);
        when(bucketService.getMobileOtpRequestBucket(userId)).thenReturn(requestBucket);

        ConsumptionProbe consumedProbe = ConsumptionProbe.consumed(4, 0);
        when(requestBucket.tryConsumeAndReturnRemaining(1)).thenReturn(consumedProbe);

        ResponseEntity<?> response = modifyProfileService.requestMobileOtp(request, userSession, userId, newMobile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(redisService, times(1)).saveMobileOtp(eq(userId), eq(newMobile), anyString());
        // Verify attempts bucket is NOT reset on request to prevent bypassing user-level limits
        verify(bucketService, never()).resetMobileVerifyBucket(userId);
        verifyNoInteractions(tenantConfiguration);
        verifyNoInteractions(esbFeign);
    }

    @Test
    void testRequestMobileOtp_ConfigureProfile_DoesNotActivateMockMode() {
        // mock-enabled=true but profile is 'configure' -> must NOT run in mock mode
        ReflectionTestUtils.setField(modifyProfileService, "mockEnabled", true);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"configure"});

        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);
        when(profileRepository.findByMobileNoAndUserIdNot(newMobile, userId)).thenReturn(null);
        when(bucketService.getMobileOtpVerifyBucket(userId)).thenReturn(verifyBucket);
        when(verifyBucket.getAvailableTokens()).thenReturn(3L);
        when(bucketService.getMobileOtpRequestBucket(userId)).thenReturn(requestBucket);
        when(requestBucket.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.consumed(4, 0));

        when(tenantConfiguration.fetchConfig(any(), any(), eq("SMS"))).thenReturn(null);

        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () ->
                modifyProfileService.requestMobileOtp(request, userSession, userId, newMobile));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getErrorCode());
        verify(redisService, times(1)).deleteMobileOtpIfMatching(eq(userId), anyString());
    }

    @Test
    void testRequestMobileOtp_RealMode_EsbFailure_InvalidatesPendingOtp() {
        ReflectionTestUtils.setField(modifyProfileService, "mockEnabled", false);

        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);
        when(profileRepository.findByMobileNoAndUserIdNot(newMobile, userId)).thenReturn(null);
        when(bucketService.getMobileOtpVerifyBucket(userId)).thenReturn(verifyBucket);
        when(verifyBucket.getAvailableTokens()).thenReturn(3L);
        when(bucketService.getMobileOtpRequestBucket(userId)).thenReturn(requestBucket);
        when(requestBucket.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.consumed(4, 0));

        Map<String, Object> smsConfig = Map.of(
                "provider", "NIC_SMS",
                "dltTemplateId", "DLT12345",
                "modifyProfileOtpTemplate", "OTP is {0}. Valid for {1} mins."
        );
        Map<String, Object> configResponse = Map.of("data", Map.of("SMS", smsConfig));
        when(tenantConfiguration.fetchConfig(request, "tenant_sp", "SMS")).thenReturn(configResponse);

        NotificationResponseDTO failedResponse = new NotificationResponseDTO();
        failedResponse.setSuccess(false);
        when(esbFeign.send(any(NotificationRequestDTO.class))).thenReturn(failedResponse);

        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () ->
                modifyProfileService.requestMobileOtp(request, userSession, userId, newMobile));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getErrorCode());
        // Verify OTP is cleaned up atomically if matching
        verify(redisService, times(1)).deleteMobileOtpIfMatching(eq(userId), anyString());
    }

    @Test
    void testVerifyMobileOtp_InvalidOtpFormat_ThrowsException() {
        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);

        ModifyEmailDTO dto = new ModifyEmailDTO();
        dto.setOtp("123"); // only 3 digits

        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () ->
                modifyProfileService.verifyMobileOtp(userId, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Invalid OTP format"));
    }

    @Test
    void testVerifyMobileOtp_ExpiredOtp_ThrowsException() {
        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);
        when(redisService.getMobileOtp(userId)).thenReturn(null); // expired

        ModifyEmailDTO dto = new ModifyEmailDTO();
        dto.setOtp("123456");

        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () ->
                modifyProfileService.verifyMobileOtp(userId, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("OTP expired"));
    }

    @Test
    void testVerifyMobileOtp_VerificationRateLimitExceeded_ThrowsException() {
        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);
        when(redisService.getMobileOtp(userId)).thenReturn(new ModifyMobileOtpDTO("654321", newMobile));
        when(bucketService.getMobileOtpVerifyBucket(userId)).thenReturn(verifyBucket);

        ConsumptionProbe rejectedProbe = ConsumptionProbe.rejected(0, 600_000_000_000L, 600_000_000_000L);
        when(verifyBucket.tryConsumeAndReturnRemaining(1)).thenReturn(rejectedProbe);

        ModifyEmailDTO dto = new ModifyEmailDTO();
        dto.setOtp("654321");

        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () ->
                modifyProfileService.verifyMobileOtp(userId, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Maximum OTP verification attempts exceeded"));
        verify(redisService, times(1)).deleteMobileOtp(userId);
    }

    @Test
    void testVerifyMobileOtp_WrongOtp_DecrementsAttempts() {
        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);
        when(redisService.getMobileOtp(userId)).thenReturn(new ModifyMobileOtpDTO("654321", newMobile));
        when(bucketService.getMobileOtpVerifyBucket(userId)).thenReturn(verifyBucket);

        ConsumptionProbe probe = ConsumptionProbe.consumed(2, 0); // 2 remaining
        when(verifyBucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        ModifyEmailDTO dto = new ModifyEmailDTO();
        dto.setOtp("111111"); // wrong OTP

        ResponseEntity<?> response = modifyProfileService.verifyMobileOtp(userId, dto);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Wrong OTP. 2 attempt(s) remaining."));
        verify(profileRepository, never()).save(any());
    }

    @Test
    void testVerifyMobileOtp_WrongOtp_LastAttempt_DeletesOtp() {
        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);
        when(redisService.getMobileOtp(userId)).thenReturn(new ModifyMobileOtpDTO("654321", newMobile));
        when(bucketService.getMobileOtpVerifyBucket(userId)).thenReturn(verifyBucket);

        ConsumptionProbe probe = ConsumptionProbe.consumed(0, 0); // 0 remaining, 3rd wrong attempt
        when(verifyBucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        ModifyEmailDTO dto = new ModifyEmailDTO();
        dto.setOtp("111111");

        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () ->
                modifyProfileService.verifyMobileOtp(userId, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Maximum OTP verification attempts exceeded"));
        verify(redisService, times(1)).deleteMobileOtp(userId);
        // Verify bucket is NOT reset on failed attempt exhaustion so the block persists
        verify(bucketService, never()).resetMobileVerifyBucket(userId);
    }

    @Test
    void testVerifyMobileOtp_CorrectOtp_SuccessUpdatesProfile() {
        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);
        when(redisService.getMobileOtp(userId)).thenReturn(new ModifyMobileOtpDTO("654321", newMobile));
        when(bucketService.getMobileOtpVerifyBucket(userId)).thenReturn(verifyBucket);
        when(verifyBucket.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.consumed(2, 0));
        when(redisService.deleteMobileOtpAtomically(userId, "654321")).thenReturn(true);
        when(profileRepository.findByMobileNoAndUserIdNot(newMobile, userId)).thenReturn(null);

        ModifyEmailDTO dto = new ModifyEmailDTO();
        dto.setOtp("654321");

        ResponseEntity<?> response = modifyProfileService.verifyMobileOtp(userId, dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Mobile number updated successfully."));

        assertEquals(newMobile, userProfile.getMobileNo());
        verify(profileRepository, times(1)).save(userProfile);
        verify(bucketService, times(1)).resetMobileVerifyBucket(userId);
    }

    @Test
    void testVerifyMobileOtp_OtpAlreadyUsed_ThrowsException() {
        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);
        when(redisService.getMobileOtp(userId)).thenReturn(new ModifyMobileOtpDTO("654321", newMobile));
        when(bucketService.getMobileOtpVerifyBucket(userId)).thenReturn(verifyBucket);
        when(verifyBucket.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.consumed(2, 0));
        when(redisService.deleteMobileOtpAtomically(userId, "654321")).thenReturn(false); // already consumed concurrently

        ModifyEmailDTO dto = new ModifyEmailDTO();
        dto.setOtp("654321");

        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () ->
                modifyProfileService.verifyMobileOtp(userId, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("already used or expired"));
        verify(profileRepository, never()).save(any());
    }

    // --- NEW TESTS FOR REPEATED REQUESTS & CONCURRENCY ---

    @Test
    void testRequestMobileOtp_BlockedUserCannotRequestNewOtp() {
        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);
        when(profileRepository.findByMobileNoAndUserIdNot(newMobile, userId)).thenReturn(null);
        when(bucketService.getMobileOtpVerifyBucket(userId)).thenReturn(verifyBucket);
        when(verifyBucket.getAvailableTokens()).thenReturn(0L); // user exhausted verify attempts

        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () ->
                modifyProfileService.requestMobileOtp(request, userSession, userId, newMobile));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Maximum OTP verification attempts exceeded"));
        verify(redisService, never()).saveMobileOtp(anyLong(), anyString(), anyString());
    }

    @Test
    void testRequestMobileOtp_RepeatedRequest_DoesNotResetVerifyBucket() {
        ReflectionTestUtils.setField(modifyProfileService, "mockEnabled", true);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});

        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);
        when(profileRepository.findByMobileNoAndUserIdNot(newMobile, userId)).thenReturn(null);
        when(bucketService.getMobileOtpVerifyBucket(userId)).thenReturn(verifyBucket);
        when(verifyBucket.getAvailableTokens()).thenReturn(2L); // 2 remaining attempts
        when(bucketService.getMobileOtpRequestBucket(userId)).thenReturn(requestBucket);
        when(requestBucket.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.consumed(4, 0));

        ResponseEntity<?> response = modifyProfileService.requestMobileOtp(request, userSession, userId, newMobile);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Ensure verify bucket was NOT reset on new OTP request
        verify(bucketService, never()).resetMobileVerifyBucket(userId);
    }

    @Test
    void testVerifyMobileOtp_ConcurrentVerification_OnlyOneSucceeds() throws Exception {
        when(profileRepository.findByUserId(userId)).thenReturn(userProfile);
        when(redisService.getMobileOtp(userId)).thenReturn(new ModifyMobileOtpDTO("654321", newMobile));
        when(bucketService.getMobileOtpVerifyBucket(userId)).thenReturn(verifyBucket);
        when(verifyBucket.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.consumed(1, 0));
        when(profileRepository.findByMobileNoAndUserIdNot(newMobile, userId)).thenReturn(null);

        // Atomic compare-and-delete: only the first thread gets true, all subsequent threads get false
        AtomicBoolean consumed = new AtomicBoolean(false);
        when(redisService.deleteMobileOtpAtomically(userId, "654321")).thenAnswer(inv -> consumed.compareAndSet(false, true));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    ModifyEmailDTO dto = new ModifyEmailDTO();
                    dto.setOtp("654321");
                    ResponseEntity<?> response = modifyProfileService.verifyMobileOtp(userId, dto);
                    if (response.getStatusCode() == HttpStatus.OK) {
                        successCount.incrementAndGet();
                    }
                } catch (SPRuntimeError ex) {
                    if (ex.getMessage().contains("already used or expired")) {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                }
            }));
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        for (Future<?> f : futures) {
            f.get(5, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertEquals(1, successCount.get(), "Exactly one thread should succeed");
        assertEquals(threadCount - 1, failureCount.get(), "All other threads should be rejected");
        verify(profileRepository, times(1)).save(userProfile);
    }
}
