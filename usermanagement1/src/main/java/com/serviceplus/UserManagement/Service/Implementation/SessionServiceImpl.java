package com.serviceplus.UserManagement.Service.Implementation;

import static com.serviceplus.UserManagement.util.ApplicationConstants.AUTH_SESSION_HEADER;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.ApiExecutor.ApiExecutor;
import com.serviceplus.UserManagement.Service.SessionService;

@Service
public class SessionServiceImpl implements SessionService{

	@Autowired
	private ApiExecutor<?> apiExecutor;
	
	@Value("${authorization.service}")
	private String AUTHORIZATION_SERVICE;
	
	@Override
	public boolean removeSession(String atk) {
		try {
			
			String url = AUTHORIZATION_SERVICE.concat("a/logout");
			
			Map<String, String> headers = new HashMap<>();
			headers.put(AUTH_SESSION_HEADER, atk);
			
			ResponseEntity<? extends Object> apiResponse = apiExecutor.callExternalEndpoint(
																Boolean.class, HttpMethod.POST, 
																headers, 
																new HashMap<>(), url,
																null, MediaType.APPLICATION_JSON);
			
			return (boolean) apiResponse.getBody();
			
		}
		catch(Exception e) {
			if(e instanceof SPRuntimeError) {
				throw e;
			}
			
			e.printStackTrace();
			throw new SPRuntimeError("Something went wrong" ,HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
