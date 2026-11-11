package com.serviceplus.UserManagement.Service;

import com.serviceplus.UserManagement.Dto.RegisterSsoUserRequest;
import com.serviceplus.UserManagement.Dto.UpdateOfficialProfileResponse;
import com.serviceplus.UserManagement.Entity.LoginDetails;
import com.serviceplus.UserManagement.Enum.UserStatus;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Repository.LoginDetailsRepository;
import com.serviceplus.UserManagement.Repository.RoleAssignmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.serviceplus.UserManagement.Entity.UserProfile;
import com.serviceplus.UserManagement.Repository.UserProfileRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.serviceplus.UserManagement.util.ApplicationConstants.EMAIL_PATTERN;
import static com.serviceplus.UserManagement.util.ApplicationConstants.PHONE_PATTERN;
import static com.serviceplus.UserManagement.util.GeneralValidation.isValidRegex;
import static com.serviceplus.UserManagement.util.Utility.*;

@Service
public class UserProfileService {

	private final UserProfileRepository userProfileRepository;

    private final LoginDetailsRepository loginDetailsRepository;

    public UserProfileService(UserProfileRepository userProfileRepository, LoginDetailsRepository loginDetailsRepository) {
        this.userProfileRepository = userProfileRepository;
        this.loginDetailsRepository = loginDetailsRepository;
    }

    public boolean checkEmail(String email, Long uid) {
		if((isEmpty(email) || !isValidRegex(email, EMAIL_PATTERN))) {
			 return false;
		}
		
		UserProfile profile = userProfileRepository.findByEmailIdAndUserIdNot(email,uid);
        return profile == null;
    }

	public boolean checkMobile(String mobileNo, Long uid) {
		if((isEmpty(mobileNo) || !isValidRegex(mobileNo, PHONE_PATTERN))) {
			 return false;
		}
		
		UserProfile profile = userProfileRepository.findByMobileNoAndUserIdNot(mobileNo,uid);
        return profile == null;
    }
	
	public UserProfile fetchProfile(Long userId) {
		return userProfileRepository.findByUserId(userId);
	}

    @Transactional
    public UpdateOfficialProfileResponse updateProfile(RegisterSsoUserRequest request) {

        UpdateOfficialProfileResponse response = new UpdateOfficialProfileResponse();

        Optional<LoginDetails> login = loginDetailsRepository.findBySignNo(request.getIssueUserId());

        if (login.isEmpty()) {
            throw new SPRuntimeError("User not found", HttpStatus.NOT_FOUND);
        }

        LoginDetails loginDetails = login.get();
        UserProfile profile = userProfileRepository.findByUserId(loginDetails.getUserId());

        profile.setUserName(request.getFullName());
        profile.setEmailId(request.getVerification().getEmail());
        profile.setMobileNo(request.getVerification().getMobile());

        userProfileRepository.save(profile);

        loginDetails.setSpdiStatus(UserStatus.ACTIVE.getCode());
        String ssoProviderId = request.getSsoProviderId();
        if (ssoProviderId != null && !ssoProviderId.trim().isEmpty()) {
            loginDetails.setSsoProviderUserId(ssoProviderId.trim());
        }

        loginDetailsRepository.save(loginDetails);

        response.setSuccess(Boolean.TRUE);
        response.setMessage("Profile updated successfully.");

        return response;
    }

    public Boolean checkUserExistInTenant(String tenantId) {
        long count = loginDetailsRepository.countByTenantId(tenantId);
        return count > 1;
    }
}
