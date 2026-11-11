package com.serviceplus.UserManagement.Controller;

import com.serviceplus.UserManagement.Dto.TenantRegistrationRequest;
import com.serviceplus.UserManagement.Dto.TenantRegistrationResponse;
import com.serviceplus.UserManagement.Service.TenantAdminProvisioningService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/b/tenant")
public class TenantRegistrationController {

    @Autowired
    private TenantAdminProvisioningService provisioningService;

    @PostMapping("/register")
    public ResponseEntity<?> registerTenant(@RequestBody TenantRegistrationRequest request, HttpServletRequest httpServletRequest) throws IOException {

        TenantRegistrationResponse response = provisioningService.registerTenant(request,httpServletRequest);
        return ResponseEntity.ok(response);
    }
}
