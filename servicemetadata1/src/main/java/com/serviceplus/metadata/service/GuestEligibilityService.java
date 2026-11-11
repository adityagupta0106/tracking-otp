package com.serviceplus.metadata.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.serviceplus.metadata.dto.GuestEligibilityDTO;
import com.serviceplus.metadata.dto.LabelValue;
import com.serviceplus.metadata.dto.ServiceJSONDTO;
import com.serviceplus.metadata.entity.ServiceLog;
import com.serviceplus.metadata.entity.ServiceMetadata;
import com.serviceplus.metadata.enums.ServiceStatus;
import com.serviceplus.metadata.repository.IServiceDefinitionRepository;
import com.serviceplus.metadata.repository.IServiceLogRepository;
import com.serviceplus.metadata.repository.IServiceMetadataRepository;

@Service
public class GuestEligibilityService {

    private final IServiceDefinitionRepository definitionRepository;
    private final IServiceLogRepository serviceLogRepository;
    private final IServiceMetadataRepository metadataRepository;
    private final String internalSecret;
    private final Set<String> onlineModes;

    public GuestEligibilityService(IServiceDefinitionRepository definitionRepository,IServiceLogRepository serviceLogRepository,IServiceMetadataRepository metadataRepository,
    		@Value("${guest.internal-secret:}") String internalSecret, @Value("${guest.online-mode-values:112}") String onlineModeValues) {

        this.definitionRepository = definitionRepository;
        this.serviceLogRepository = serviceLogRepository;
        this.metadataRepository = metadataRepository;
        this.internalSecret = internalSecret;
        this.onlineModes = Arrays.stream(onlineModeValues.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
    }

    public ResponseEntity<?> checkEligibility(Integer serviceId, String suppliedSecret) {
        if (isBlank(internalSecret) || internalSecret.length() < 32) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if (!secretMatches(suppliedSecret)) {
            return forbidden();
        }
        if (serviceId == null || serviceId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        String registerToAvail = definitionRepository.findRegisterToAvailByServiceId(serviceId);
        if (!"Y".equalsIgnoreCase(registerToAvail)  && !"N".equalsIgnoreCase(registerToAvail)) {
            return forbidden();
        }

        ServiceLog serviceLog = serviceLogRepository.findByServiceId(serviceId);
        if (serviceLog == null
                || !Objects.equals(serviceLog.getServiceStatus(), ServiceStatus.LAUNCHED.getCode())
                || !Character.valueOf('Y').equals(serviceLog.getLaunchFlag())
                || isBlank(serviceLog.getTenantId())) {
            return forbidden();
        }

        ServiceMetadata metadata = metadataRepository.findByServiceIdAndTenantId(
                serviceId, serviceLog.getTenantId());
        if (metadata == null || metadata.getMetadataJson() == null
                || !Objects.equals(metadata.getTenantId(), serviceLog.getTenantId())
                || !Objects.equals(metadata.getBaseServiceId(), serviceLog.getBaseServiceId())) {
            return forbidden();
        }

        ServiceJSONDTO serviceJson = metadata.getMetadataJson();
        boolean onlineSubmissionAllowed = serviceJson.getSubmissionModes() != null
                && serviceJson.getSubmissionModes().stream()
                        .filter(Objects::nonNull)
                        .map(LabelValue::getValue)
                        .filter(Objects::nonNull)
                        .map(String::valueOf)
                        .map(String::trim)
                        .anyMatch(onlineModes::contains);

        if (!onlineSubmissionAllowed
                || isBlank(serviceJson.getApplSubmissionTaskId())
                || isBlank(serviceJson.getApplFormId())) {
            return forbidden();
        }

        return ResponseEntity.ok(new GuestEligibilityDTO(
                serviceLog.getServiceId(),
                serviceLog.getBaseServiceId(),
                serviceLog.getTenantId(),
                serviceJson.getStateId(),
                serviceJson.getApplSubmissionTaskId(),
                serviceJson.getApplFormId(),
                "Y".equalsIgnoreCase(registerToAvail)));
    }

    private boolean secretMatches(String suppliedSecret) {
        return suppliedSecret != null && MessageDigest.isEqual(
                internalSecret.getBytes(StandardCharsets.UTF_8),
                suppliedSecret.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
