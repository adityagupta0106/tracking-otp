package com.serviceplus.UserManagement.profile.controller;

import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.profile.dto.ModifyEmailDTO;
import com.serviceplus.UserManagement.profile.dto.UserModifyProfileDTO;
import com.serviceplus.UserManagement.profile.service.ModifyProfileService;
import com.serviceplus.UserManagement.util.Utility;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/a/profile")
public class ModifyProfileController {
	@Autowired
	private ModifyProfileService profileService;

	private static final Logger LOG = LogManager.getLogger("ModifyProfile");

	@PostMapping("/save")
	public ResponseEntity<?> saveProfile(HttpServletRequest request, @RequestBody UserModifyProfileDTO profileDTO){
		try {
			UserSessionObject user = Utility.getUserSessionDetails(request);
			if (user == null) {
				throw new SPRuntimeError("Unauthorized user", HttpStatus.FORBIDDEN);
			}
			Long userID = user.getUserID();
			return profileService.saveProfile(userID, profileDTO);
		} catch (Exception e) {
			if (e instanceof SPRuntimeError) {
				throw e;
			} else {
				LOG.error("ModifyProfile-Error : {}", e);
				throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	@PostMapping("/email/otp/request")
	public ResponseEntity<?> requestEmailOtp(HttpServletRequest request, @RequestParam String emailId) {
		try {
			UserSessionObject user = Utility.getUserSessionDetails(request);
			if (user == null) {
				throw new SPRuntimeError("Unauthorized user", HttpStatus.FORBIDDEN);
			}
			Long userID = user.getUserID();
			return profileService.requestEmailOtp(request,user,userID, emailId);
		} catch (Exception e) {
			if (e instanceof SPRuntimeError) {
				throw e;
			} else {
				LOG.error("ModifyProfile-Error : {}", e);
				throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	@PostMapping("/email/otp/verify")
	public ResponseEntity<?> verifyEmailOtp(HttpServletRequest request,
			@Valid @RequestBody ModifyEmailDTO modifyEmailDTO) {
		try {
			UserSessionObject user = Utility.getUserSessionDetails(request);
			if (user == null) {
				throw new SPRuntimeError("Unauthorized user", HttpStatus.FORBIDDEN);
			}
			Long userID = user.getUserID();
			return profileService.verifyEmailOtp(userID, modifyEmailDTO);
		} catch (Exception e) {
			if (e instanceof SPRuntimeError) {
				throw e;
			} else {
				LOG.error("ModifyProfile-Error : {}", e);
				throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	@PostMapping("/mobile/otp/request")
	public ResponseEntity<?> requestMobileOtp(HttpServletRequest request, @RequestParam String mobileNo) {
		try {
			UserSessionObject user = Utility.getUserSessionDetails(request);
			if (user == null) {
				throw new SPRuntimeError("Unauthorized user", HttpStatus.FORBIDDEN);
			}
			Long userID = user.getUserID();
			return profileService.requestMobileOtp(request, user, userID, mobileNo);
		} catch (Exception e) {
			if (e instanceof SPRuntimeError) {
				throw e;
			} else {
				LOG.error("ModifyProfile-Error : {}", e);
				throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	@PostMapping("/mobile/otp/verify")
	public ResponseEntity<?> verifyMobileOtp(HttpServletRequest request, @Valid @RequestBody ModifyEmailDTO otpDTO) {
		try {
			UserSessionObject user = Utility.getUserSessionDetails(request);
			if (user == null) {
				throw new SPRuntimeError("Unauthorized user", HttpStatus.FORBIDDEN);
			}
			Long userID = user.getUserID();
			return profileService.verifyMobileOtp(userID, otpDTO);
		} catch (Exception e) {
			if (e instanceof SPRuntimeError) {
				throw e;
			} else {
				LOG.error("ModifyProfile-Error : {}", e);
				throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
}
