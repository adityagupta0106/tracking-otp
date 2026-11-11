package com.serviceplus.UserManagement.Controller;

import com.serviceplus.UserManagement.Dto.RegisterSsoUserRequest;
import com.serviceplus.UserManagement.Dto.RegisterSsoUserResponse;
import com.serviceplus.UserManagement.Service.SSOUserRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/b")
public class SSOUserRegistrationController {

    private final SSOUserRegistrationService citizenRegistrationService;

    @Autowired
    public SSOUserRegistrationController(SSOUserRegistrationService citizenRegistrationService) {
        this.citizenRegistrationService = citizenRegistrationService;
    }

    @PostMapping("/onboard/sso/user")
    public ResponseEntity<?> registerCitizen(@RequestBody RegisterSsoUserRequest profile, HttpServletRequest request) {

        RegisterSsoUserResponse response = citizenRegistrationService.onboardUser(profile,request);
        return ResponseEntity.ok(response);
    }
}
