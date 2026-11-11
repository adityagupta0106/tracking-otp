package com.serviceplus.UserManagement.ExternalApiClient;

import com.serviceplus.UserManagement.ApiExecutor.ApiExecutor;
import com.serviceplus.UserManagement.Dto.IntegrationRequest;
import com.serviceplus.UserManagement.Dto.IntegrationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.serviceplus.UserManagement.util.Utility.entityToString;
import static com.serviceplus.UserManagement.util.Utility.stringToEntity;

@Service
public class EsbClient {

    @Value("${splus.esb.service}")
    private String esbService;

    private final ApiExecutor<Object> callExternalEndpointService;

    public EsbClient(ApiExecutor<Object> callExternalEndpointService) {
        this.callExternalEndpointService = callExternalEndpointService;
    }

    public IntegrationResponse invoke(IntegrationRequest request) {

        try {

            String body = request == null ? null : entityToString(request);

            String url = esbService.concat("/b/invoke");

            ResponseEntity<?> response =
                    callExternalEndpointService.callExternalEndpoint(
                            IntegrationResponse.class,
                            HttpMethod.POST,
                            buildHeaders(),
                            new HashMap<>(),
                            url,
                            body,
                            MediaType.APPLICATION_JSON
                    );

            if (response.getBody() instanceof IntegrationResponse) {
                return (IntegrationResponse) response.getBody();
            }

            return (IntegrationResponse) stringToEntity(String.valueOf(response.getBody()), IntegrationResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke ESB", e);
        }
    }

    private Map<String, String> buildHeaders() {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        return headers;
    }
}