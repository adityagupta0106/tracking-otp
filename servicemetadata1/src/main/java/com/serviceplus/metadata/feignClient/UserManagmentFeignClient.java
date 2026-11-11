package com.serviceplus.metadata.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.serviceplus.metadata.dto.CreateUserDto;

@FeignClient(name = "UserManagement", path = "/usermgmt", url = "${usermanagement.service.url}")
public interface UserManagmentFeignClient {

	@PostMapping(value = "/create/newuser", consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<?> createUser(@RequestHeader("USER-DETAILS") String userDetail, @RequestBody CreateUserDto userDto);
}
