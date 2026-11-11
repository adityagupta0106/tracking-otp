package com.serviceplus.UserManagement.Service;

import com.serviceplus.UserManagement.Dto.ProvisionedUserResult;
import com.serviceplus.UserManagement.Dto.UserProvisioningRequest;
import com.serviceplus.UserManagement.Entity.*;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Repository.*;
import com.serviceplus.UserManagement.SSO.Properties.SsoProperties;
import com.serviceplus.UserManagement.SSO.Provider.IdentityProvider;
import com.serviceplus.UserManagement.SSO.Provider.SSOProvider;
import com.serviceplus.UserManagement.SSO.Registry.IdentityProviderRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static java.util.Objects.isNull;

@Service
public class UserOnboardingService {

    private static final Logger Logger = LogManager.getLogger("UserProvisioningService");

    @PersistenceContext
    private EntityManager em;

    private final LoginDetailsRepository loginDetailsRepository;

    private final UserProfileRepository userProfileRepository;

    private final UserLocationRepository userLocationRepository;

    private final RoleAssignmentRepository roleAssignmentRepository;

    private final PasswordManagementService passwordManagementService;

    private final UserLocationDesignationRepository userLocationDesignationRepository;

    private final LGDService lgdService;

    private final IdentityProviderRegistry identityProviderRegistry;

    private final SsoProperties ssoProperties;

    public UserOnboardingService(LoginDetailsRepository loginDetailsRepository, UserProfileRepository userProfileRepository, UserLocationRepository userLocationRepository, RoleAssignmentRepository roleAssignmentRepository, PasswordManagementService passwordManagementService, UserLocationDesignationRepository userLocationDesignationRepository, LGDService lgdService, IdentityProviderRegistry identityProviderRegistry, SsoProperties ssoProperties) {
        this.loginDetailsRepository = loginDetailsRepository;
        this.userProfileRepository = userProfileRepository;
        this.userLocationRepository = userLocationRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.passwordManagementService = passwordManagementService;
        this.userLocationDesignationRepository = userLocationDesignationRepository;
        this.lgdService = lgdService;
        this.identityProviderRegistry = identityProviderRegistry;
        this.ssoProperties = ssoProperties;
    }

    @Transactional
    public ProvisionedUserResult onboardUser(UserProvisioningRequest request, UserSessionObject user, HttpServletRequest servletRequest) {

        String requestId = UUID.randomUUID().toString();

        try {

            Logger.info("Starting user provisioning requestId={} role={} entity={} location={}", requestId, request.getRoleId(), request.getEntityId(), request.getLocation().getEntityUnitCode());

            Long userId = generateInternalUserId();
            String tenantId = isNull(request.getTenantId()) ? user.getTenantId() : request.getTenantId();

            Logger.info("Starting user provisioning requestId={} role={} entity={} location={}", requestId, request.getRoleId(), request.getEntityId(), request.getLocation().getEntityUnitCode());

            String signNo = isNull(request.getSignNo()) ? generateSignNumber(request.getSigNoPrefix()) : request.getSignNo();

            String rawPassword = passwordManagementService.generateSecurePassword();

            String hashPassword = passwordManagementService.hashPassword(rawPassword);

            LoginDetails loginDetails = createLoginDetails(request, userId, signNo, hashPassword,user,tenantId);

            UserProfile userProfile = createUserProfile(request, userId,user,tenantId);

            UserLocation userLocation = createUserLocation(request, userId,user,tenantId);

            createRoleAssignment(request, userId, userLocation.getUserLocId(),user,tenantId);

            createUserDesignation(request,userLocation);

            Logger.info("User provisioning completed requestId={} userId={} signNo={}", requestId, userId, signNo);

            ProvisionedUserResult result = new ProvisionedUserResult();

            result.setUserId(userId);
            result.setUsername(signNo);
            result.setPassword(rawPassword);
            result.setLocationName(request.getLocation().getEntityUnitName());
            result.setParentLocationName(request.getLocation().getParentEntityUnitName());
            result.setCreatedAt(Instant.now());
            result.setHashPassword(hashPassword);
            result.setTenantId(tenantId);

            if(request.getPushToSSO()) {
                registerWithSSO(result, servletRequest);
            }


            return result;

        } catch (Exception ex) {

            Logger.error("User provisioning failed requestId={}", requestId, ex);
            throw new SPRuntimeError("Unable to create user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void createUserDesignation(UserProvisioningRequest request, UserLocation userLocation) {

        if (isNull(request.getDesignationId()) || request.getDesignationId() <= 0) {
            return;
        }

        String designationName = lgdService.getDesignationName(request.getDesignationId());

        UserLocationDesignation designation = new UserLocationDesignation();

        designation.setUserLocationId(userLocation.getUserLocId());
        designation.setDesignationId(request.getDesignationId());

        designation.setDesignationName(designationName);
        designation.setTenantId(userLocation.getTenantId());

        userLocationDesignationRepository.save(designation);
    }

    public Long generateInternalUserId() {

        Query query = em.createNativeQuery("select fnc_generate_seq from schm_sp.fnc_generate_seq(:type)");
        query.setParameter("type", 16);
        BigDecimal result = (BigDecimal) query.getSingleResult();
        BigInteger userId = result.toBigInteger();

        return userId.longValue();
    }

    private LoginDetails createLoginDetails(UserProvisioningRequest request, Long userId, String signNo, String hashPassword, UserSessionObject user, String tenantId) {

        LoginDetails loginDetails = new LoginDetails();

        loginDetails.setUserId(userId);
        loginDetails.setSignNo(signNo);
        loginDetails.setPasswd(hashPassword);
        loginDetails.setCreatedDate(new java.util.Date());
        loginDetails.setCreatedBy(request.getCreatedByUserId());
        loginDetails.setClcId(request.getClcId());
        loginDetails.setSpdiStatus(request.getInitialStatus());
        loginDetails.setTenantId(tenantId);
        if (request.getSsoProviderId() != null && !request.getSsoProviderId().trim().isEmpty()) {
            loginDetails.setSsoProviderUserId(request.getSsoProviderId().trim());
        }

        return loginDetailsRepository.save(loginDetails);
    }

    private UserProfile createUserProfile(UserProvisioningRequest request, Long userId, UserSessionObject user, String tenantId) {

        UserProfile profile = new UserProfile();

        profile.setUserId(userId);
        profile.setClcId(request.getClcId());
        profile.setClcName(request.getClcName());
        profile.setDesignationId(request.getDesignationId());
        profile.setApprovalRequired(request.getApprovalRequired());
        profile.setTenantId(tenantId);
        profile.setcDate(new Date());

        UserProvisioningRequest.UserProfile profileRequest = request.getUserProfile();

        if (profileRequest != null) {

            profile.setUserName(profileRequest.getUserName());
            profile.setMiddleName(profileRequest.getMiddleName());
            profile.setLastName(profileRequest.getLastName());
            profile.setEmailId(profileRequest.getEmailId());
            profile.setMobileNo(profileRequest.getMobileNo());
            profile.setDob(profileRequest.getDob());
            profile.setAddress(profileRequest.getAddress());
            profile.setPostalCode(profileRequest.getPostalCode());
            profile.setCugType(profileRequest.getCugType());
        }

        return userProfileRepository.save(profile);
    }

    private UserLocation createUserLocation(UserProvisioningRequest request, Long userId, UserSessionObject user, String tenantId) {

        Long locationMappingId = generateUserLocationId();

        UserLocation location = new UserLocation();

        location.setUserLocId(locationMappingId);
        location.setUserId(userId);
        location.setLocationId(request.getLocation().getEntityUnitCode());
        location.setLocationName(request.getLocation().getEntityUnitName());
        location.setCategoryId(request.getCategoryId());
        location.setEntityId(request.getEntityId());
        location.setEntityLevelId(request.getLevelId());
        location.setEntityLevelName(request.getLevelName());
        location.setTenantId(tenantId);

        return userLocationRepository.save(location);
    }

    private void createRoleAssignment(UserProvisioningRequest request, Long userId, Long userLocationId, UserSessionObject user, String tenantId) {

        Long roleAssignmentId = generateRoleAssignmentId();

        RoleAssignment assignment = new RoleAssignment();

        assignment.setUser_role_id(roleAssignmentId);
        assignment.setUserId(userId);
        assignment.setRoleId(request.getRoleId());
        assignment.setUserLocId(userLocationId);
        assignment.setTenantId(tenantId);

        roleAssignmentRepository.save(assignment);
    }

    private Long generateUserLocationId() {

        Query query = em.createNativeQuery("SELECT nextval('schm_sp.user_location_user_loc_id_seq')");
        return ((Long) query.getSingleResult());
    }

    private Long generateRoleAssignmentId() {

        Query query = em.createNativeQuery("SELECT nextval('schm_sp.role_assignment_user_role_id_seq')");
        return ((Long) query.getSingleResult());
    }

    private String generateSignNumber(String prefix) {

        long seq = ((Number) em.createNativeQuery("SELECT nextval('schm_sp.sign_no_seq')").getSingleResult()).longValue();
        String seqStr = String.valueOf(seq);

        while (seqStr.length() < 6) {
            seqStr = "0".concat(seqStr);
        }
        return prefix + seqStr;
    }

    private void registerWithSSO(ProvisionedUserResult result, HttpServletRequest servletRequest) {

        SSOProvider provider = ssoProperties.getProvider();

        IdentityProvider identityProvider = identityProviderRegistry.getProvider(provider);

        boolean registered = identityProvider.registerUser(servletRequest, result);

        if (!registered) {
            throw new SPRuntimeError("Failed to register user with " + provider, HttpStatus.BAD_GATEWAY);
        }

        Logger.info("User registered successfully with {}.", provider);
    }
}
