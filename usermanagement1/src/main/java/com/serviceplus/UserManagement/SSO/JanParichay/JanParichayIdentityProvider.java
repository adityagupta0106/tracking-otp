package com.serviceplus.UserManagement.SSO.JanParichay;

import com.google.gson.reflect.TypeToken;
import com.serviceplus.UserManagement.Dto.IntegrationRequest;
import com.serviceplus.UserManagement.Dto.IntegrationResponse;
import com.serviceplus.UserManagement.Dto.ProvisionedUserResult;
import com.serviceplus.UserManagement.ExternalApiClient.EsbClient;
import com.serviceplus.UserManagement.SSO.Properties.JanParichayProperties;
import com.serviceplus.UserManagement.SSO.Provider.IdentityProvider;
import com.serviceplus.UserManagement.SSO.Provider.SSOProvider;
import com.serviceplus.UserManagement.Service.TenantConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static com.serviceplus.UserManagement.util.Utility.*;

@Component
public class JanParichayIdentityProvider implements IdentityProvider {

    private static final Logger log = LoggerFactory.getLogger("userCreationLogger");

    private final TenantConfiguration tenantConfiguration;
    private final JanParichayProperties properties;
    private final EsbClient esbClient;

    public JanParichayIdentityProvider(TenantConfiguration tenantConfiguration, JanParichayProperties properties, EsbClient esbClient) {
        this.tenantConfiguration = tenantConfiguration;
        this.properties = properties;
        this.esbClient = esbClient;
    }

    @Override
    public SSOProvider getProvider() {
        return SSOProvider.JANPARICHAY;
    }

    @Override
    public Boolean registerUser(HttpServletRequest request, ProvisionedUserResult registrationRequest) {

        log.info("Starting JanParichay user registration. UserId={}", registrationRequest.getUserId());

        JanParichayTenantConfiguration tenantConfiguration = getTenantConfiguration(request,registrationRequest.getTenantId());

        JanParichayTenantConfiguration.JanParichayConfig config = tenantConfiguration.getData().getJanParichay();

        log.info("Tenant Configuration={}", entityToString(tenantConfiguration));

        long ttl = System.currentTimeMillis();

        String signature = JanParichaySignatureUtil.generateSignature(
                config.getServiceId(),
                ttl,
                properties.getUserOnboardingUrl(),
                config.getServiceId(),
                properties.getHmacSecret(),
                registrationRequest.getUserId().toString(),
                registrationRequest.getPassword(),
                config.getServiceId()
        );

        JanParichayUserRegistrationRequest janParichayUserRegistrationRequest = new JanParichayUserRegistrationRequest();
        janParichayUserRegistrationRequest.setUserId(registrationRequest.getUsername());
        janParichayUserRegistrationRequest.setPassword(SHA256(registrationRequest.getPassword()).toLowerCase());
        janParichayUserRegistrationRequest.setServiceId(config.getServiceId());

        Map<String, String> headers = new HashMap<>();
        headers.put("RestAuthID", properties.getRestAuthId());
        headers.put("TTL", String.valueOf(ttl));
        headers.put("Signature", signature);

        String payload = entityToString(janParichayUserRegistrationRequest);

        log.info("Calling JanParichay User Registration API.");
        log.info("URL={}", properties.getUserOnboardingUrl());
        log.info("signature={}", signature);
        log.info("Payload={}", payload);

        IntegrationRequest integrationRequest = new IntegrationRequest();

        integrationRequest.setProvider(SSOProvider.JANPARICHAY.name());
        integrationRequest.setOperation("REGISTRATION");
        integrationRequest.setUrl(properties.getUserOnboardingUrl());
        integrationRequest.setMethod(HttpMethod.POST.name());
        integrationRequest.setProtocol("REST");
        integrationRequest.setContentType(MediaType.APPLICATION_JSON_VALUE);
        integrationRequest.setHeaders(headers);
        integrationRequest.setBody(payload);

        log.info("Calling ESB for JanParichay user registration. " + "operation=USER_REGISTRATION, userId={}", registrationRequest.getUserId());

        IntegrationResponse integrationResponse;

        try {

            integrationResponse = esbClient.invoke(integrationRequest);

        } catch (Exception ex) {

            log.error("ESB invocation failed for JanParichay user registration. " + "UserId={}", registrationRequest.getUserId(), ex);
            return Boolean.FALSE;
        }

        if (integrationResponse == null) {

            log.error("Null response received from ESB for JanParichay registration. " + "UserId={}", registrationRequest.getUserId());
            return Boolean.FALSE;
        }

        log.info("JanParichay registration response received through ESB. " + "status={}, userId={}", integrationResponse.getStatusCode(), registrationRequest.getUserId());

        if (!integrationResponse.isSuccess()) {

            log.error("JanParichay registration failed. " + "HTTP status={}, userId={}", integrationResponse.getStatusCode(), registrationRequest.getUserId());
            return Boolean.FALSE;
        }

        String responseBody = String.valueOf(integrationResponse.getBody());

        JanParichayUserRegistrationResponse jpResponse =
                (JanParichayUserRegistrationResponse)
                        stringToEntity(
                                responseBody,
                                JanParichayUserRegistrationResponse.class
                        );

        if (jpResponse == null) {

            log.error("Null JanParichay response after parsing. UserId={}", registrationRequest.getUserId());
            return Boolean.FALSE;
        }

        if (!"success".equalsIgnoreCase(jpResponse.getStatus())) {

            log.error("JanParichay registration failed. Status={}, Message={}, UserId={}", jpResponse.getStatus(), jpResponse.getMsg(), registrationRequest.getUserId());
            return Boolean.FALSE;
        }

        log.info("User successfully registered at JanParichay. UserId={},Message={}", registrationRequest.getUserId(),jpResponse.getMsg());

        return Boolean.TRUE;
    }

    private JanParichayTenantConfiguration getTenantConfiguration(HttpServletRequest request, String tenantId) {

        Map<String,Object> configuration = tenantConfiguration.fetchConfig(request,tenantId, "JANPARICHAY");
        Type type = new TypeToken<JanParichayTenantConfiguration>() {}.getType();
        return (JanParichayTenantConfiguration) stringToEntityUsingType(entityToString(configuration), type);

    }

}