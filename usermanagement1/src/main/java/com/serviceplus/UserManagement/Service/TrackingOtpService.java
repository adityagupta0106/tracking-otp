package com.serviceplus.UserManagement.Service;

import static com.serviceplus.UserManagement.util.SnowflakeIdGenerator.createUniqueId;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.serviceplus.UserManagement.Dto.NotificationRequestDTO;
import com.serviceplus.UserManagement.Dto.NotificationResponseDTO;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.feign.EsbFeignClient;
import com.serviceplus.UserManagement.profile.service.BucketService;
import com.serviceplus.UserManagement.profile.service.ModifyProfileRedisService;
import com.serviceplus.UserManagement.util.Utility;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;

@Service

public class TrackingOtpService {

    private final ModifyProfileRedisService redisService;
    private final BucketService bucketService;
    private final TenantConfiguration tenantConfiguration;
    private final EsbFeignClient esbFeign;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TrackingOtpService.class);

    @Value("${tracking.otp.hmac-secret:}")
    private String otpSecret;

    @Value("${tracking.otp.validity-minutes:5}")
    private int validityMinutes;
    

    public TrackingOtpService( ModifyProfileRedisService redisService, BucketService bucketService, TenantConfiguration tenantConfiguration, EsbFeignClient esbFeign) {

        this.redisService = redisService;

        this.bucketService = bucketService;

        this.tenantConfiguration = tenantConfiguration;

        this.esbFeign = esbFeign;

    }

    @Value("${tracking.otp.email.subject:Application Tracking OTP}")

    private String trackingEmailSubject;

    @Value("${tracking.otp.email.body:Your application tracking OTP is {0}. It expires in {1} minutes.}")

    private String trackingEmailBody;

    // Called only by an authenticated internal API.

    public String sendEmailOtp(

            HttpServletRequest request,

            String tenantId,

            Integer serviceId,

            String applId,

            String mode,

            String email) {

        return sendOtp(request,tenantId,serviceId,applId,mode,"EMAIL_OTP",email);

    }

    public String sendOtp(HttpServletRequest request,String tenantId,Integer serviceId,String applId,String mode,String method,String contact) {

        boolean mobile="MOBILE_OTP".equals(method);

        if (tenantId == null || tenantId.isBlank()
                || serviceId == null || serviceId <= 0
                || (!"R".equals(mode) && !"O".equals(mode))
                || ("R".equals(mode)

                        && (applId == null || applId.isBlank()))

                || (!mobile && !"EMAIL_OTP".equals(method))
                || contact == null
                || (mobile?!contact.matches("[0-9]{8,15}"):!Utility.isValidEmail(contact))) {

            throw new SPRuntimeError("Invalid tracking OTP request",HttpStatus.BAD_REQUEST);

        }

        String recipient =contact.trim().toLowerCase(Locale.ROOT);

        String identityKey = hmac(tenantId + "|" + method + "|" + recipient);

        ConsumptionProbe limit = bucketService.getTrackingRequestBucket(identityKey).tryConsumeAndReturnRemaining(1);

        if (!limit.isConsumed()) {

            throw new SPRuntimeError("OTP request limit exceeded",HttpStatus.TOO_MANY_REQUESTS);

        }

        String otp = String.format("%06d", 100000 + RANDOM.nextInt(900000));

        String nonce = UUID.randomUUID().toString();

        String correlationId = createUniqueId();

        Map<String, Object> challenge = new HashMap<>();

        challenge.put("tenantId", tenantId);

        challenge.put("serviceId", serviceId);

        challenge.put("applId", applId == null ? "" : applId);

        challenge.put("mode", mode);

        challenge.put("channel", mobile?"SMS":"EMAIL");

        challenge.put("method",method);

        challenge.put("purpose","PUBLIC_TRACKING");

        challenge.put("recipient", recipient);

        challenge.put("nonce", nonce);

        challenge.put("otpHash", hmac(nonce + ":" + otp));

        String challengeId =redisService.saveTrackingOtp(challenge);

        // A fresh challenge ID gets its own verification bucket; no profile/user bucket reset is needed.

        String channel = mobile ? "SMS" : "EMAIL";

        String configurationKey = mobile ? "SMS" : "MAIL";

        String stage = "TENANT_CONFIGURATION";

        try {

            Map<String, Object> config = tenantConfiguration.fetchConfig(request, tenantId, configurationKey);

            stage = "CONFIGURATION_VALIDATION";
            if (config == null || !(config.get("data") instanceof Map<?, ?> data)
                    || !(data.get(configurationKey) instanceof Map<?, ?> configured)) {
                throw new IllegalStateException("Notification configuration unavailable");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> notificationConfig = (Map<String, Object>) configured;

            // Existing email OTP integration allows a null provider and forwards MAIL config to ESB.
            String provider = (String) notificationConfig.get("provider");

            // Older SMS tenant configs use serviceProvider rather than provider.
            if (mobile && (provider == null || provider.isBlank())) {
                provider = (String) notificationConfig.get("serviceProvider");
            }
            if (mobile && (provider == null || provider.isBlank())) {
                throw new IllegalStateException("SMS provider is not configured");
            }

            stage = mobile ? "SMS_TEMPLATE" : "EMAIL_TEMPLATE";

            Object templateValue = mobile ? notificationConfig.get("trackingOtpTemplate") : trackingEmailBody;

            if (!(templateValue instanceof String template) || !template.contains("{0}")) {

                throw new IllegalStateException("Approved tracking OTP template unavailable");

            }

            String message = MessageFormat.format(template, otp, validityMinutes);

            if (mobile) {

                stage = "SMS_DLT_TEMPLATE";

                if (!(notificationConfig.get("dltTemplateId") instanceof String templateId) || templateId.isBlank()) {

                    throw new IllegalStateException("Approved DLT template ID unavailable");

                }

            }

            NotificationRequestDTO notification = new NotificationRequestDTO();

            notification.setTenantId(tenantId);

            notification.setChannel(channel);

            // Modify Profile leaves adapterCode unset for email; preserve the existing SMS adapter.
            if (mobile) notification.setAdapterCode("SMS");

            notification.setProvider(provider);

            notification.setRecipient(mobile ? Map.of("mobile", recipient) : Map.of("to", List.of(recipient)));

            notification.setPayload(mobile ? Map.of("message", message)

                    : Map.of("subject", trackingEmailSubject, "body", message));

            notification.setConfiguration(notificationConfig);

            // Use the same notification correlation mechanism as Modify Profile, separate from verification tokens.

            notification.setCorrelationId(correlationId);

            stage = "ESB_SEND";

            NotificationResponseDTO response = esbFeign.send(notification);

            if (response == null) {
                stage = "ESB_RESPONSE";
                throw new IllegalStateException("Notification response unavailable");
            }

            if (!response.isSuccess()) {
                stage = "ESB_REJECTED";
                throw new IllegalStateException("OTP notification rejected");
            }

            LOG.info("Tracking OTP dispatch accepted. correlationId={}, serviceId={}, channel={}",

                    correlationId, serviceId, channel);

            return challengeId;

        } catch (Exception ex) {

            int httpStatus = ex instanceof feign.FeignException failure
                    ? failure.status()
                    : 0;

            LOG.error("Tracking OTP dispatch failed. correlationId={}, serviceId={}, channel={}, stage={}, httpStatus={}, exceptionType={}",correlationId,serviceId,channel,stage,httpStatus,
                    ex.getClass().getSimpleName()
            );

            redisService.deleteTrackingOtp(challengeId);

            throw new SPRuntimeError(
                    "Tracking OTP dispatch failed at " + stage
                            + " (correlationId=" + correlationId + ")",
                    HttpStatus.BAD_GATEWAY
            );
        }

    }

    // Internal use only. Never expose verified scope

    // directly through a public endpoint.

    public Map<String, Object> verifyOtp(

            String challengeId,

            String submittedOtp) {

        if (challengeId == null || challengeId.isBlank()

                || submittedOtp == null

                || !submittedOtp.matches("\\d{6}")) {

            throw invalidOtp();

        }

        Map<String, Object> challenge = redisService.getTrackingOtp(challengeId);

        if (challenge == null) {
            throw invalidOtp();
        }

        ConsumptionProbe limit = bucketService

                .getTrackingVerifyBucket(challengeId).tryConsumeAndReturnRemaining(1);

        if (!limit.isConsumed()) {

            redisService.deleteTrackingOtp(challengeId);

            throw new SPRuntimeError(

                    "OTP verification attempts exceeded",

                    HttpStatus.TOO_MANY_REQUESTS);

        }

        if (!(challenge.get("nonce") instanceof String nonce)
                || !(challenge.get("otpHash") instanceof String savedHash)) {

            throw invalidOtp();

        }

        String expectedHash =

                hmac(nonce + ":" + submittedOtp);

        if (!MessageDigest.isEqual(expectedHash.getBytes(StandardCharsets.UTF_8),
                savedHash.getBytes(StandardCharsets.UTF_8))) {

            // Like Modify Profile, the last incorrect attempt invalidates this OTP.
            if (limit.getRemainingTokens() == 0) redisService.deleteTrackingOtp(challengeId);

            throw invalidOtp();

        }

        // A valid challenge may authorize only one request, including concurrent verifications.
        if (!redisService.consumeTrackingOtp(challengeId, nonce, savedHash)) {
            throw invalidOtp();
        }

        Map<String, Object> verified = new HashMap<>();

        verified.put("tenantId", challenge.get("tenantId"));

        verified.put("serviceId", challenge.get("serviceId"));

        verified.put("applId", challenge.get("applId"));

        verified.put("mode", challenge.get("mode"));

        verified.put("channel", challenge.get("channel"));

        verified.put("method",challenge.get("method"));

        verified.put("purpose",challenge.get("purpose"));

        verified.put("recipient", challenge.get("recipient"));

        return verified;

    }

    private SPRuntimeError invalidOtp() {

        return new SPRuntimeError(

                "Invalid or expired OTP",

                HttpStatus.BAD_REQUEST);

    }

    private String hmac(String value) {

        if(otpSecret==null || otpSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<32)

            throw new SPRuntimeError("Tracking OTP signing unavailable",HttpStatus.SERVICE_UNAVAILABLE);

        try {

            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(new SecretKeySpec(

                    otpSecret.getBytes(

                            java.nio.charset.StandardCharsets.UTF_8),

                    "HmacSHA256"));

            return Base64.getEncoder().encodeToString(

                    mac.doFinal(value.getBytes(

                            java.nio.charset.StandardCharsets.UTF_8)));

        } catch (Exception ex) {

            throw new IllegalStateException(

                    "OTP signing unavailable", ex);

        }

    }

}
