package com.serviceplus.UserManagement.ApiExecutor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.serviceplus.UserManagement.CustomAnnotation.CheckCircuitBreakerResponse;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import static com.serviceplus.UserManagement.util.Utility.extractServiceName;

@Service
@Primary
public class SynchronousApiExecutor<T> implements ApiExecutor<T> {

	@Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
	@CheckCircuitBreakerResponse(errorMessage = "Failed to fetch due to circuit breaker ")
    public ResponseEntity<?> callExternalEndpoint(Class<?> returnObj, HttpMethod httpMethod,
                                                  Map<String, String> headers, Map<String, Object> params,
                                                  String url, String bodyContent, MediaType contentType) {
    	 String serviceName = extractServiceName(url);
    	 boolean exists = circuitBreakerRegistry.getAllCircuitBreakers().stream()
    			 										.anyMatch(cb -> cb.getName().equals(serviceName));	

    	 String circuitBreakerName = exists ? serviceName : "default";         

         CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);

         if (headers == null) {
             headers = new HashMap<>();
         }

         HttpHeaders headerParams = new HttpHeaders();
         headers.forEach(headerParams::add);
         headerParams.setContentType(contentType);

         HttpEntity<Object> requestEntity = new HttpEntity<>(bodyContent, headerParams);

         try {
             return circuitBreaker.executeSupplier(() -> {
                 try {
                     return restTemplate.exchange(url, httpMethod, requestEntity, returnObj, params);
                 }
                 catch (Exception e) {
                     throw e;
                 }
             });
         } catch (CallNotPermittedException  t) {
             return callExternalEndpointFallback(returnObj, httpMethod, headers, params, url, bodyContent, contentType, t);
         }
    }

    public ResponseEntity<? extends Object> callExternalEndpointFallback(Class<?> returnObj, HttpMethod httpMethod,
                                                                        Map<String, String> headers, Map<String, Object> params,
                                                                        String url, String bodyContent, MediaType contentType,
                                                                        Throwable t) {
        String serviceName = extractServiceName(url);
        return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(serviceName);
    }

}
