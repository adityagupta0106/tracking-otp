package com.serviceplus.UserManagement.CustomAnnotation;

import static com.serviceplus.UserManagement.util.Utility.extractServiceName;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;

@Aspect
@Component
public class CircuitBreakerResponseAspect {
	
	@Value("${instances.circuitBreaker.critical.error}")
	private String CRITICAL_SERVICES;

	@Around("@annotation(checkCircuitBreakerResponse)")
    public Object checkCircuitBreakerResponse(ProceedingJoinPoint joinPoint, CheckCircuitBreakerResponse checkCircuitBreakerResponse) throws Throwable {
        // Method call
        Object result = joinPoint.proceed();
        
        Object[] args = joinPoint.getArgs();
        String url = null;
        if (args.length >= 5 && args[4] instanceof String) {
            url = (String) args[4];
        }

        String serviceName =  extractServiceName(url);        
        
        if (result instanceof ResponseEntity<?> responseEntity) {
            System.out.println("=================== TEST ===================");
            System.out.println(responseEntity);
            
            if (CRITICAL_SERVICES.contains(serviceName) && responseEntity.getStatusCode() == HttpStatus.FAILED_DEPENDENCY) {
                throw new SPRuntimeError(
                    checkCircuitBreakerResponse.errorMessage() + ": " + responseEntity.getBody(),
                    HttpStatus.FAILED_DEPENDENCY
                );
            }
        }

        return result;
    }
}

