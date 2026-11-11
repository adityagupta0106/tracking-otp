package com.serviceplus.UserManagement.profile.service;

import java.util.Map;
import java.util.Collections;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import com.serviceplus.UserManagement.profile.dto.ModifyMobileOtpDTO;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.profile.dto.ModifyProfileOtpDTO;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class ModifyProfileRedisService {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyProfileRedisService.class);

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    // String serialization is mandatory for Redis Lua cjson decoding.
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${modify.profile.email.sms.otp.validity.min}")
    private Integer otpValidityMin;

    @Value("${tracking.otp.validity-minutes:5}")
    private int trackingValidityMinutes;

    private static final String KEY_PREFIX = "modifyProfileEmailOtp:";
    private static final String MOBILE_KEY_PREFIX = "modifyProfileMobileOtp:";
    private static final RedisScript<Long> COMPARE_AND_DELETE_SCRIPT = new DefaultRedisScript<>(
            "local val = redis.call('get', KEYS[1])\n" +
            "if not val then return 0 end\n" +
            "local ok, data = pcall(cjson.decode, val)\n" +
            "if ok and type(data) == 'table' and data.otp == ARGV[1] then\n" +
            " return redis.call('del', KEYS[1])\n" +
            "end\n" +
            "return 0\n", Long.class);

    public void saveOtp(Long userId, String email, String otp) {
        try {
            ModifyProfileOtpDTO dto = new ModifyProfileOtpDTO(otp, email);
            String json = objectMapper.writeValueAsString(dto);
            redisTemplate.opsForValue().set(KEY_PREFIX + userId, json, otpValidityMin, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ModifyProfileOtpDTO getOtp(Long userId) {
        try {
            String json = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
            return json != null ? objectMapper.readValue(json, ModifyProfileOtpDTO.class) : null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteOtp(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
    

    private static final String TRACKING_OTP_PREFIX = "tracking:otp:";

    /** Atomic consume for public tracking: verified nonce, hash and purpose must match. */
    private static final RedisScript<Long> CONSUME_TRACKING_OTP_SCRIPT = new DefaultRedisScript<>(
            "local raw = redis.call('GET', KEYS[1])\n"
            + "if not raw then return 0 end\n"
            + "local ok, data = pcall(cjson.decode, raw)\n"
            + "if not ok or type(data) ~= 'table' then return 0 end\n"
            + "if data.purpose == 'PUBLIC_TRACKING' and data.nonce == ARGV[1] and data.otpHash == ARGV[2] then\n"
            + "  return redis.call('DEL', KEYS[1])\n"
            + "end\n"
            + "return 0\n", Long.class);


    public String saveTrackingOtp(Map<String, Object> otpData) {
        if (trackingValidityMinutes < 1 || trackingValidityMinutes > 15) {
            throw new IllegalStateException("Invalid tracking OTP validity");
        }
        try {
            String challengeId = UUID.randomUUID().toString();
            String json = objectMapper.writeValueAsString(otpData);
            stringRedisTemplate.opsForValue().set(
                    TRACKING_OTP_PREFIX + challengeId, json, trackingValidityMinutes, TimeUnit.MINUTES);
            return challengeId;
        } catch (Exception e) {
            throw new SPRuntimeError("Unable to save tracking OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Map<String, Object> getTrackingOtp(String challengeId) {
        try {
            if (challengeId == null || challengeId.isBlank()) {
                return null;
            }
            String json = stringRedisTemplate.opsForValue().get(TRACKING_OTP_PREFIX + challengeId);
            return json != null
                    ? objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {})
                    : null;
        } catch (Exception e) {
            throw new SPRuntimeError("Unable to retrieve tracking OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean consumeTrackingOtp(String challengeId, String nonce, String expectedHash) {
        if (challengeId == null || challengeId.isBlank() || challengeId.length() > 64
                || nonce == null || nonce.isBlank()
                || expectedHash == null || expectedHash.isBlank()) return false;
        try {
            Long consumed = stringRedisTemplate.execute(CONSUME_TRACKING_OTP_SCRIPT,
                    Collections.singletonList(TRACKING_OTP_PREFIX + challengeId), nonce, expectedHash);
            return Long.valueOf(1L).equals(consumed);
        } catch (Exception ex) {
            throw new SPRuntimeError("Unable to consume tracking OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteTrackingOtp(String challengeId) {
        if (challengeId == null || challengeId.isBlank()) {
            return;
        }
        stringRedisTemplate.delete(TRACKING_OTP_PREFIX + challengeId);
    }
    public void saveMobileOtp(Long userId, String mobile, String otp) {
        try {
            String json = objectMapper.writeValueAsString(new ModifyMobileOtpDTO(otp, mobile));
            stringRedisTemplate.opsForValue().set(MOBILE_KEY_PREFIX + userId, json, otpValidityMin, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new SPRuntimeError("Unable to store Mobile OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ModifyMobileOtpDTO getMobileOtp(Long userId) {
        try {
            String json = stringRedisTemplate.opsForValue().get(MOBILE_KEY_PREFIX + userId);
            return json != null ? objectMapper.readValue(json, ModifyMobileOtpDTO.class) : null;
        } catch (Exception e) {
            throw new SPRuntimeError("Unable to retrieve Mobile OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteMobileOtp(Long userId) {
        stringRedisTemplate.delete(MOBILE_KEY_PREFIX + userId);
    }

    public boolean deleteMobileOtpAtomically(Long userId, String expectedOtp) {
        if (userId == null || expectedOtp == null || expectedOtp.isBlank()) return false;
        try {
            Long result = stringRedisTemplate.execute(COMPARE_AND_DELETE_SCRIPT,
                    Collections.singletonList(MOBILE_KEY_PREFIX + userId), expectedOtp.trim());
            return Long.valueOf(1L).equals(result);
        } catch (Exception e) {
            throw new SPRuntimeError("Unable to verify Mobile OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean deleteMobileOtpIfMatching(Long userId, String expectedOtp) {
        try {
            return deleteMobileOtpAtomically(userId, expectedOtp);
        } catch (Exception ex) {
            LOG.warn("Unsent mobile OTP cleanup failed, userId={}", userId);
            return false;
        }
    }

}