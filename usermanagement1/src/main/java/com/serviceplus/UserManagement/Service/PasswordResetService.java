package com.serviceplus.UserManagement.Service;

import com.serviceplus.UserManagement.Entity.UserSessionObject;
import org.springframework.http.ResponseEntity;

import com.serviceplus.UserManagement.Dto.PasswordResetRequestDto;

import jakarta.servlet.http.HttpServletRequest;

public interface PasswordResetService {

	boolean generateLink(Long userId, String email, String subject, HttpServletRequest request, UserSessionObject loggedInUser);
	
	ResponseEntity<?> checkPwdResetToken(String token, HttpServletRequest request);

	ResponseEntity<?> updatePwd(PasswordResetRequestDto requestdto, HttpServletRequest request);
}
