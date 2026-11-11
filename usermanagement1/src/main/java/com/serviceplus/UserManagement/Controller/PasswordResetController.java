package com.serviceplus.UserManagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serviceplus.UserManagement.Dto.PasswordResetRequestDto;
import com.serviceplus.UserManagement.Service.PasswordResetService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/o")
public class PasswordResetController {

	@Autowired
	private PasswordResetService passwordResetService;
	
	@GetMapping("checkPwdResetToken/{token}")
	public ResponseEntity<?> checkPwdResetToken(@PathVariable String token,HttpServletRequest request){
		return passwordResetService.checkPwdResetToken(token,request);
	}
	
	@PostMapping("updatePwd")
	public ResponseEntity<?> updatePwd(@RequestBody PasswordResetRequestDto requestdto,HttpServletRequest request){
		return passwordResetService.updatePwd(requestdto,request);
	}
}
