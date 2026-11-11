package com.serviceplus.UserManagement.Service;

import com.serviceplus.UserManagement.ApiExecutor.ApiExecutor;
import com.serviceplus.UserManagement.Dto.TenantProfilePolicy;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TenantPolicyService {

    private static final Logger Logger = LogManager.getLogger("TenantPolicyService");

    @Value("${tenant.configuration.service}")
    private String configurationServiceUrl;

    private final ApiExecutor<?> apiExecutor;

    @Autowired
    public TenantPolicyService(ApiExecutor<?> apiExecutor) {
        this.apiExecutor = apiExecutor;
    }

    public TenantProfilePolicy fetchProfilePolicy(String tenantId, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();

        try {

            String url = configurationServiceUrl + "specific";
            Map<String, Object> params = new HashMap<>();
            params.put("keys", "REGISTRATION");
            Map<String, String> headers = extractHeaders(request);

            Logger.info("Fetching tenant registration policy requestId={} tenantId={},headers{}", requestId, tenantId,headers);

            ResponseEntity<?> response = apiExecutor.callExternalEndpoint(Map.class, HttpMethod.POST, headers, params, url + "?keys={keys}", null, MediaType.APPLICATION_JSON);

            if (response == null || response.getBody() == null) {
                throw new SPRuntimeError("Unable to fetch tenant configuration", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Map<?, ?> responseBody = (Map<?, ?>) response.getBody();

            Map<?, ?> data = (Map<?, ?>) responseBody.get("data");

            Map<?, ?> registration = (Map<?, ?>) data.get("REGISTRATION");

            TenantProfilePolicy policy = buildRegistrationPolicy(registration);

            Logger.info("Tenant registration policy loaded requestId={} tenantId={}", requestId, tenantId);

            return policy;

        } catch (Exception ex) {

            Logger.error("Failed to fetch tenant registration policy requestId={} tenantId={}", requestId, tenantId, ex);
            return defaultPolicy();
        }
    }

    private TenantProfilePolicy buildRegistrationPolicy(Map<?, ?> registration) {

        TenantProfilePolicy policy = new TenantProfilePolicy();

        policy.setEmailEnabled(getBooleanValue(registration, "email_display"));
        policy.setMobileEnabled(getBooleanValue(registration, "mobile_display"));
        policy.setEmailMandatory(getBooleanValue(registration, "email_mandatory"));
        policy.setMobileMandatory(getBooleanValue(registration, "mobile_mandatory"));
        policy.setUserNameMandatory(true);

        return policy;
    }

    private boolean getBooleanValue(Map<?, ?> map, String key) {

        if (map == null || map.get(key) == null) {
            return false;
        }

        return Boolean.parseBoolean(String.valueOf(map.get(key)));
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {

        Map<String, String> headers = new HashMap<>();

        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }

        return headers;
    }

    private TenantProfilePolicy defaultPolicy() {

        TenantProfilePolicy policy = new TenantProfilePolicy();

        policy.setEmailEnabled(true);
        policy.setMobileEnabled(true);

        policy.setEmailMandatory(true);
        policy.setMobileMandatory(false);

        policy.setUserNameMandatory(true);

        return policy;
    }
}
