package com.serviceplus.UserManagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serviceplus.UserManagement.Dto.CreateUserDto;
import com.serviceplus.UserManagement.Dto.InitializeProfileDto;
import com.serviceplus.UserManagement.Service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/create")
public class CreateUserController {

	@Autowired
	private UserService userService;
	
	@PostMapping(value = "newuser",consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createUser(@RequestBody CreateUserDto userDto,HttpServletRequest request,HttpServletResponse srvResponse) throws Exception {
		return userService.createNewUser(request,userDto);
		
	}
	
	@PostMapping("initializeProfileDetails")
	public ResponseEntity<?> initializeProfileDetails(HttpServletRequest request,HttpServletResponse response,@RequestBody InitializeProfileDto profileDto) {
		return userService.initializeProfileDetails(request,profileDto,response);
	}
}
