package com.serviceplus.UserManagement.Service.Implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.serviceplus.UserManagement.Dto.CaptchaDto;
import com.serviceplus.UserManagement.ApiExecutor.ApiExecutor;
import com.serviceplus.UserManagement.Service.CaptchaService;
import static com.serviceplus.UserManagement.util.ApplicationConstants.PRESID_HEADER;
import static com.serviceplus.UserManagement.util.Utility.stringToEntity;
import static com.serviceplus.UserManagement.util.Utility.getServicePlusInternalRequestAuth;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

@Service
@Primary
public class CaptchaServiceImpl implements CaptchaService{

	@Autowired
	private ApiExecutor<?> apiExecutor;
	
	@Value("${authentication.service}")
	private String AUTHENTICATION_SERVICE;

	@Override
	public CaptchaDto fetchCaptcha(HttpServletRequest request) {
		
		CaptchaDto dto = new CaptchaDto();
		
		try {

			String url = AUTHENTICATION_SERVICE.concat("b/fetchCaptcha?presid={presid}");

			String pre_sid = request.getHeader(PRESID_HEADER);
			Map<String, Object> params = Map.of("presid", pre_sid);
			
			ResponseEntity<? extends Object> apiResponse = apiExecutor.callExternalEndpoint(
																String.class, HttpMethod.GET,
																getServicePlusInternalRequestAuth() , 
																params, url, "", MediaType.APPLICATION_JSON);
			
			if(apiResponse.getStatusCode().is2xxSuccessful()) {
				dto = (CaptchaDto) stringToEntity((String)apiResponse.getBody(),CaptchaDto.class);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dto;
	}
}
