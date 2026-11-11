package com.serviceplus.UserManagement.Service;

import com.serviceplus.UserManagement.Dto.*;
import com.serviceplus.UserManagement.Entity.LoginDetails;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.Enum.SSOMappingType;
import com.serviceplus.UserManagement.Enum.UserStatus;
import com.serviceplus.UserManagement.Repository.LoginDetailsRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.serviceplus.UserManagement.util.ApplicationConstants.*;

@Service
public class SSOUserRegistrationService {

    private final UserOnboardingService userOnboardingService;

    private final LoginDetailsRepository loginDetailsRepository;

    public SSOUserRegistrationService(UserOnboardingService userOnboardingService, LoginDetailsRepository loginDetailsRepository) {
        this.userOnboardingService = userOnboardingService;
        this.loginDetailsRepository = loginDetailsRepository;
    }

    @Transactional
    public RegisterSsoUserResponse onboardUser(RegisterSsoUserRequest req, HttpServletRequest servletRequest) {

        RegisterSsoUserResponse response = new RegisterSsoUserResponse();
        ProvisionedUserResult provisionedUser = new ProvisionedUserResult();

        try {

            Optional<LoginDetails> user = loginDetailsRepository.findBySignNo(req.getIssueUserId());

            if (user.isPresent()) {

                LoginDetails existingUser = user.get();
                String ssoProviderId = req.getSsoProviderId();
                if (ssoProviderId != null && !ssoProviderId.trim().isEmpty()) {
                    existingUser.setSsoProviderUserId(ssoProviderId.trim());
                    loginDetailsRepository.save(existingUser);
                }

                response.setSuccess(Boolean.TRUE);
                response.setAlreadyRegistered(Boolean.TRUE);
                response.setUserId(existingUser.getUserId());
                response.setSignNo(existingUser.getSignNo());
                response.setPassword(existingUser.getPasswd());

                return response;
            }

            UserProvisioningRequest request = buildProvisioningRequest(req);
            UserSessionObject userSessionObject = new UserSessionObject();
            userSessionObject.setTenantId(req.getTenantId());

            provisionedUser = userOnboardingService.onboardUser(request, userSessionObject,servletRequest);
            response.setSuccess(Boolean.TRUE);
        }
        catch(Exception e){
            e.printStackTrace();
            response.setSuccess(Boolean.FALSE);
        }

        response.setUserId(provisionedUser.getUserId());
        response.setSignNo(provisionedUser.getUsername());
        response.setPassword(provisionedUser.getHashPassword());

        return response;
    }

    private UserProvisioningRequest buildProvisioningRequest(RegisterSsoUserRequest profile) {

        UserProvisioningRequest request = new UserProvisioningRequest();

        if(SSOMappingType.CSC.name().equals(profile.getIdentity())){
            request.setRoleId(ROLE_CUG);
        }
        else {
            request.setRoleId(ROLE_CITIZEN);
        }

        request.setInitialStatus(UserStatus.ACTIVE.getCode());
        request.setSignNo(profile.getIssueUserId());
        String ssoProviderId = profile.getSsoProviderId();
        if (ssoProviderId != null && !ssoProviderId.trim().isEmpty()) {
            request.setSsoProviderId(ssoProviderId.trim());
        }

        request.setClcId(0);
        request.setCategoryId(0);
        request.setEntityId(0);
        request.setLevelId(0);
        request.setLevelName("");
        request.setDesignationId(0);
        request.setApprovalRequired(Boolean.FALSE);
        request.setPushToSSO(Boolean.FALSE);

        UserProvisioningRequest.UserProfile prof = new UserProvisioningRequest.UserProfile();

        prof.setUserName(profile.getFullName());
        prof.setLastName(prof.getLastName());
        prof.setEmailId(profile.getVerification().getEmail());
        prof.setMobileNo(profile.getVerification().getMobile());

        if(SSOMappingType.CSC.name().equals(profile.getIdentity())) {
            prof.setCugType(CUG_TYPE_CSC_KIOSK);
        }

        request.setUserProfile(prof);

        CreateUserDto.SpecificLocation location = new CreateUserDto.SpecificLocation();
        location.setEntityUnitCode(0);
        location.setEntityUnitName("");
        location.setParentEntityUnitName("");

        request.setLocation(location);

        return request;
    }
}
