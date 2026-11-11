package com.serviceplus.UserManagement.Controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.serviceplus.UserManagement.Dto.DSCVerificationDetails;
import com.serviceplus.UserManagement.Dto.DSCVerificationRequest;
import com.serviceplus.UserManagement.Dto.RegisterSsoUserRequest;
import com.serviceplus.UserManagement.Dto.UpdateOfficialProfileResponse;
import com.serviceplus.UserManagement.Dto.UserRoleRequest;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Service.UserProfileService;
import com.serviceplus.UserManagement.Service.UserRoleService;
import com.serviceplus.UserManagement.Service.Implementation.DSCManagementService;

@RestController
@RequestMapping("/b")
public class PrivateApiController {

    private final UserRoleService userRoleService;

    private final UserProfileService userProfileService;
    
    private final DSCManagementService dscManagementService;

    public PrivateApiController(UserRoleService userRoleService, UserProfileService userProfileService, DSCManagementService dscManagementService) {
        this.userRoleService = userRoleService;
        this.userProfileService = userProfileService;
		this.dscManagementService = dscManagementService;
    }

    @PostMapping("/user-role/add")
    public ResponseEntity<?> addRole(@RequestBody UserRoleRequest request) {

        userRoleService.addRole(request);

        return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Role assigned successfully"));
    }

    @PostMapping("/user-role/remove")
    public ResponseEntity<?> removeRole(@RequestBody UserRoleRequest request) {

        userRoleService.removeRole(request);

        return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Role removed successfully"));
    }

    @PostMapping("/official/updateProfile")
    public UpdateOfficialProfileResponse updateOfficialProfile(@RequestBody RegisterSsoUserRequest request) {
        return userProfileService.updateProfile(request);
    }
    
	@PostMapping(value = "dsc/verification-details")
	public ResponseEntity<DSCVerificationDetails> getDSCVerificationDetails(
			@RequestBody DSCVerificationRequest request) {

		try {
			return ResponseEntity.ok(dscManagementService.getDSCVerificationDetails(request));
		} catch (Exception ex) {
			if (ex instanceof SPRuntimeError) {
				throw (SPRuntimeError) ex;
			}
			ex.printStackTrace();
			throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

    @GetMapping("/checkUserExistInTenant/{tenantId}")
    public Boolean checkUserExistInTenant(@PathVariable String tenantId) {
        return userProfileService.checkUserExistInTenant(tenantId);
    }
}
