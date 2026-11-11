package com.serviceplus.UserManagement.Service;

import static com.serviceplus.UserManagement.util.ApplicationConstants.HOST_HEADER;

import java.util.Collections;
import java.util.Map;

import com.serviceplus.UserManagement.ApiExecutor.ApiExecutor;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.util.Utility;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.serviceplus.UserManagement.util.Utility.isEmpty;
import static java.util.Objects.isNull;

@Service
public class TenantConfiguration {

    @Value("${tenant.configuration.service}")
    private String TENANT_SERVICE_URL;

    @Autowired
    private ApiExecutor<?> apiExecutor;
    
    @Autowired
    private ObjectMapper objectMapper;

    public Map<String,Object> fetchConfig(HttpServletRequest request, String tenantId, String keys){
    	
        try{
            String url = TENANT_SERVICE_URL.concat("/a/find?tenantId={tenantId}");
            Map<String, Object> params = Map.of("tenantId", tenantId);
            Map<String, String> headers = Collections.emptyMap();

            if(isEmpty(tenantId) && !isEmpty(request.getHeader(HOST_HEADER))){
                url = TENANT_SERVICE_URL.concat("/o/specific?keys={key}");
                params = Map.of("key", keys);
                headers = Map.of(HOST_HEADER, request.getHeader(HOST_HEADER));
            }

            ResponseEntity<?> response = apiExecutor.callExternalEndpoint(
                                                    String.class, HttpMethod.POST, headers,
                                                    params, url,
                                                    "", MediaType.APPLICATION_JSON);
            
            return objectMapper.readValue(
                    (String) response.getBody(),
                    new TypeReference<Map<String, Object>>() {});

        } catch (Exception e) {
        	e.printStackTrace();
            if(e instanceof SPRuntimeError)
                throw (SPRuntimeError) e;

            throw new SPRuntimeError("Something went wrong [TC - 01]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String fetchConfiguration(HttpServletRequest request, String key) {

        try {

            String url = TENANT_SERVICE_URL.concat("/o/specific?keys={key}");

            Map<String, Object> params = Map.of("key", key);

            Map<String, String> headers = Map.of(HOST_HEADER, request.getHeader(HOST_HEADER));

            ResponseEntity<?> apiResponse = apiExecutor
                    .callExternalEndpoint(
                            String.class,
                            HttpMethod.POST,
                            headers,
                            params,
                            url,
                            "",
                            MediaType.APPLICATION_JSON);

            return (String) apiResponse.getBody();

        } catch (Exception ex) {
            throw new SPRuntimeError("Something went wrong [TN-CFG-01]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
