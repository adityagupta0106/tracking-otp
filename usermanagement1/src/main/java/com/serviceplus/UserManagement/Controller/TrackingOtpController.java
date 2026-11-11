package com.serviceplus.UserManagement.Controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serviceplus.UserManagement.Service.TrackingOtpService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/internal/tracking/otp")
public class TrackingOtpController {

    private final TrackingOtpService otpService;

    @Value("${TRACKING_INTERNAL_TOKEN:}")
    private String trackingInternalToken;

    public TrackingOtpController(TrackingOtpService otpService) {
        this.otpService = otpService;
    }

    private boolean authorized(String token) {
        return trackingInternalToken != null && !trackingInternalToken.isBlank()
                && token != null && MessageDigest.isEqual(trackingInternalToken.getBytes(StandardCharsets.UTF_8),
                        token.getBytes(StandardCharsets.UTF_8));
    }

    // Keep the original Mode R endpoint; delegate to the same sender as Mode O.
    @PostMapping("/send/email")
    public ResponseEntity<?> sendEmailOtp(@RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Tracking-Internal-Token", required = false) String token,
            HttpServletRequest request) {

        if (!authorized(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (body == null || !(body.get("tenantId") instanceof String)
                || !(body.get("serviceId") instanceof Number)
                || !(body.get("mode") instanceof String)
                || !(body.get("email") instanceof String)) 
        {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid OTP request"));
        }

        Map<String, Object> send = new HashMap<>(body);
        send.put("method", "EMAIL_OTP");
        send.put("contact", body.get("email"));
        return sendOtp(send, token, request);
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(
    		@RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Tracking-Internal-Token", required = false) String token,  HttpServletRequest request) {

        if (!authorized(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (body == null
                || !(body.get("tenantId") instanceof String tenantId)
                || !(body.get("serviceId") instanceof Number serviceId)
                || !(body.get("method") instanceof String method)
                || !(body.get("mode") instanceof String mode)
                || !(body.get("contact") instanceof String contact)) {

            return ResponseEntity.badRequest().build();
        }

        if (serviceId.longValue() <= 0
                || serviceId.longValue() > Integer.MAX_VALUE
                || serviceId.doubleValue() != serviceId.intValue()) {

            return ResponseEntity.badRequest().build();
        }

        String applId = body.get("applId") instanceof String id ? id : null;
        String challengeId = otpService.sendOtp(
                request, tenantId, serviceId.intValue(), applId, mode, method, contact);

        return ResponseEntity.ok(Map.of("challengeId", challengeId, "message", "OTP request processed"));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Tracking-Internal-Token", required = false) String token) {

        if (!authorized(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (body == null || !(body.get("challengeId") instanceof String challengeId)
                || !(body.get("otp") instanceof String otp)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid OTP verification request"));
        }

        Map<String, Object> verificationData = otpService.verifyOtp(challengeId, otp);

        return ResponseEntity.ok(Map.of("verified", true, "verificationData", verificationData));
    }
}
