package com.serviceplus.UserManagement.Service;

import static com.serviceplus.UserManagement.util.ApplicationConstants.*;
import static com.serviceplus.UserManagement.util.Utility.*;
import static java.util.Objects.isNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.serviceplus.UserManagement.Dto.*;
import com.serviceplus.UserManagement.Entity.*;
import com.serviceplus.UserManagement.Enum.UserStatus;
import com.serviceplus.UserManagement.Factory.UserCreationStrategyFactory;
import com.serviceplus.UserManagement.Kafka.NotificationKafkaService;
import com.serviceplus.UserManagement.Kafka.UpdateUserKafkaService;
import com.serviceplus.UserManagement.Repository.*;
import com.serviceplus.UserManagement.Strategy.UserCreationStrategy;
import com.serviceplus.UserManagement.util.ExcelGeneratorUtil;
import com.serviceplus.UserManagement.util.FileEncryptionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import com.serviceplus.UserManagement.Dto.CreateUserDto.SpecificLocation;
import com.serviceplus.UserManagement.Entity.UserSessionObject.Roles;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class UserService {

    private final RedisService redisService;
    private final CustomQueryRepository customQueryRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final UserProfileService userProfileService;
    private final SessionService sessionService;
    private final AdminUserCreationAccessRepository adminUserCreationAccessRepository;
    private final LGDService lgdService;
    private final PasswordResetService passwordResetService;
    private final UpdateUserKafkaService UpdateUserKafkaService;
    private final UserLocationRepository userLocationRepository;
    private final UserLocationDesignationRepository userLocationDesignationRepository;
    private final RoleMasterRepository roleMasterRepository;
    private final UserCreationStrategyFactory userCreationStrategyFactory;
    private final NotificationKafkaService notificationKafkaService;
    private final UserOnboardingService userOnboardingService;
    private final LoginDetailsRepository loginDetailsRepository;
    private final TenantPolicyService tenantPolicyService;
    private final UserProfileRepository userProfileRepository;
    private final UserCreationHistoryRepository userCreationHistoryRepository;

    @Value("${notification.service}")
    private String NOTIFICATION_SERVICE;

    @Value("${user.creation.email.subject}")
    private String USER_CREATION_EMAIL_SUBJECT;

    @Value("${user.creation.email.content}")
    private String USER_CREATION_EMAIL_CONTENT;

    @Value("${user.creation.max.user.count}")
    private Integer USER_CREATION_MAX_USER_COUNT;

    public UserService(RedisService redisService, CustomQueryRepository customQueryRepository, UserDetailsRepository userDetailsRepository, UserProfileService userProfileService, SessionService sessionService, AdminUserCreationAccessRepository adminUserCreationAccessRepository, LGDService lgdService, PasswordResetService passwordResetService, UpdateUserKafkaService updateUserKafkaService, UserLocationRepository userLocationRepository, UserLocationDesignationRepository userLocationDesignationRepository, RoleMasterRepository roleMasterRepository, UserCreationStrategyFactory userCreationStrategyFactory, NotificationKafkaService notificationKafkaService, UserOnboardingService userOnboardingService, LoginDetailsRepository loginDetailsRepository, TenantPolicyService tenantPolicyService, UserProfileRepository userProfileRepository, UserCreationHistoryRepository userCreationHistoryRepository) {
        this.redisService = redisService;
        this.customQueryRepository = customQueryRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.userProfileService = userProfileService;
        this.sessionService = sessionService;
        this.adminUserCreationAccessRepository = adminUserCreationAccessRepository;
        this.lgdService = lgdService;
        this.passwordResetService = passwordResetService;
        this.UpdateUserKafkaService = updateUserKafkaService;
        this.userLocationRepository = userLocationRepository;
        this.userLocationDesignationRepository = userLocationDesignationRepository;
        this.roleMasterRepository = roleMasterRepository;
        this.userCreationStrategyFactory = userCreationStrategyFactory;
        this.notificationKafkaService = notificationKafkaService;
        this.userOnboardingService = userOnboardingService;
        this.loginDetailsRepository = loginDetailsRepository;
        this.tenantPolicyService = tenantPolicyService;
        this.userProfileRepository = userProfileRepository;
        this.userCreationHistoryRepository = userCreationHistoryRepository;
    }

    private static final Logger UserCreationLogger = LogManager.getLogger("userCreationLogger");

    @Transactional
    public ResponseEntity<?> createNewUser(HttpServletRequest request, @RequestBody CreateUserDto userDto) throws Exception {

        try {

            UserSessionObject user = getUserSessionDetails(request);

            UserCreationLogger.info("Logged user {} creating new user for role {}", user.getSignNo(), userDto.getCreatedRole());

            validateRequest(userDto, user);

            Integer clc = userDto.getClcId() == null ? user.getClcId() : userDto.getClcId();

            if(hasRole(user, ROLE_TENANT_ADMIN)){
                clc = isNull(userDto.getClcId()) ? FALLBACK_CLC_CODE : userDto.getClcId();
            }

            userDto.setClcId(clc);

            userCreationContext(user, userDto, request);


            Long loggedInUser = user.getUserID();
            String loggedInUserEmail = user.getEmailId();

            Integer levelId   = userDto.getLevelId();
            Integer entityId = userDto.getEntityId();

            Integer locationTypeId = userDto.getCategoryId();
            List<SpecificLocation> locs = userDto.getUnitId();

            Integer roleToBeCreated = userDto.getCreatedRole();

            int noOfUsers = userDto.getUserCount() == null ? 1 : userDto.getUserCount();

            noOfUsers = Math.min(noOfUsers, USER_CREATION_MAX_USER_COUNT);
            System.out.println("users --- " + noOfUsers);

            if (locs == null || locs.isEmpty()) {
                throw new SPRuntimeError("At least one location is required", HttpStatus.BAD_REQUEST);
            }

            String clcName = "NA";
            if(!isNull(clc)) {
                clcName = lgdService.getClcName(clc);
            }

            if (roleToBeCreated == ROLE_TENANT_ADMIN) {
                locs.getFirst().setEntityUnitName(clcName);
                UserCreationLogger.info("Fetched clc name {} from LGD", clcName);
            }

            String entityName = userDto.getEntityName();
            String entityLevelName = userDto.getEntityLevelName();

            List<ProvisionedUserResult> finalUserList = new ArrayList<>();
            Instant now = Instant.now();

            while (noOfUsers-- > 0) {

                for (SpecificLocation location : locs) {

                    UserProvisioningRequest provisioningRequest = new UserProvisioningRequest();

                    provisioningRequest.setClcId(clc);
                    provisioningRequest.setCategoryId(userDto.getCategoryId());
                    provisioningRequest.setEntityId(entityId);
                    provisioningRequest.setEntityName(entityName);
                    provisioningRequest.setLevelId(levelId);
                    provisioningRequest.setLevelName(entityLevelName);
                    provisioningRequest.setParentUnitId(userDto.getParentUnitId());
                    provisioningRequest.setCreatedByUserId(loggedInUser);
                    provisioningRequest.setRoleId(roleToBeCreated);
                    provisioningRequest.setLocation(location);
                    provisioningRequest.setDesignationId(userDto.getDesignationId());
                    provisioningRequest.setKioskType(userDto.getCugTypes());
                    provisioningRequest.setApprovalRequired(userDto.getApprovalRequired());
                    provisioningRequest.setInitialStatus(UserStatus.INITIAL_CREATION.getCode());
                    provisioningRequest.setSigNoPrefix(DEFAULT_USER_SIGN_PREFIX);

                    ProvisionedUserResult provisionedUser = userOnboardingService.onboardUser(provisioningRequest,user,request);

                    provisionedUser.setReferenceId(userDto.getReferenceId());
                    finalUserList.add(provisionedUser);
                }

            }

            if (!finalUserList.isEmpty()) {
                roleMasterRepository.findById(roleToBeCreated).ifPresent(roleMaster -> finalUserList.getFirst().setRoleName(roleMaster.getRoleName()));
            }

            UserCreationLogger.info("Users created successfully. Count={}", finalUserList.size());

            String filePath = createExcel(finalUserList, loggedInUser);

            UserCreationLogger.info("Excel created successfully at path {}", filePath);

            Path path = Path.of(filePath);
            Map<String, Object> result = new HashMap<>();

            if (!isEmpty(filePath)) {

                String password = user.getSignNo().substring(0, 4)
                        .concat(
                                DateTimeFormatter.ofPattern("ddMMyy")
                                        .withZone(ZoneId.systemDefault())
                                        .format(now)
                        );

                byte[] encryptedBytes = FileEncryptionUtil.encryptAndRead(
                        path,
                        password,
                        false);

                UserCreationHistory history = new UserCreationHistory();

                history.setCreatedBy(loggedInUser);
                history.setCreatedByName(user.getUserName());
                history.setRoleId(roleToBeCreated);
                history.setRoleName(finalUserList.getFirst().getRoleName());
                history.setEntityId(entityId);
                history.setEntityName(entityName);
                history.setEntityLevelId(levelId);
                history.setEntityLevelName(entityLevelName);
                history.setTotalUsers(finalUserList.size());
                history.setFileName("UserCreation.xlsx");
                history.setFileContent(encryptedBytes);
                history.setCreatedOn(now);
                history.setTenantId(user.getTenantId());

                userCreationHistoryRepository.save(history);

                notificationKafkaService.sendNotification(request,user.getTenantId(),loggedInUserEmail, USER_CREATION_EMAIL_SUBJECT, "", filePath, "xlsx", USER_CREATION_EMAIL_CONTENT, 0, 0, false, true, 0, 0);
                UserCreationLogger.info("Kafka notification event published for userId={}", loggedInUser);

                result.put("file", fileToBase64(filePath));
                Files.deleteIfExists(path);
            }

            result.put("message", "User(s) has been created successfully and credentials will be sent on registered email.");
            result.put("status", true);
            result.put("fileName", "UserCreation.xlsx");
            result.put("contentType", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            if (e instanceof SPRuntimeError) {
                throw e;
            }

            UserCreationLogger.error("Error while creating user", e);
            throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String createExcel(List<ProvisionedUserResult> users, Long loggedInUser) throws IOException {

        if (users == null || users.isEmpty()) {
            return "";
        }

        String filePath = buildExcelFilePath(loggedInUser);

        List<String> headers = List.of("Serial No.", "User Name", "Password", "Location", "Parent Location", "Created At");
        List<Object[]> rows = new ArrayList<>();

        int serialNo = 1;

        for (ProvisionedUserResult user : users) {
            rows.add(new Object[]{serialNo++, user.getUsername(), user.getPassword(), user.getLocationName(), user.getParentLocationName(), user.getCreatedAt()});
            publishAuthenticationSync(user);
        }

        return ExcelGeneratorUtil.generateExcel("User List", filePath, "User Credentials", headers, rows);
    }

    private void publishAuthenticationSync(ProvisionedUserResult provisionedUser) {

        try {

            Optional<LoginDetails> user = loginDetailsRepository.findBySignNo(provisionedUser.getUsername());

            if (user.isEmpty()) {
                UserCreationLogger.warn("Unable to find user for authentication sync username={}", provisionedUser.getUsername());
                return;
            }

            UserCreationLogger.info("Publishing authentication sync for user={}", user.get().getSignNo());
            UpdateUserKafkaService.updateUserCredential(user.get().getUserId(), user.get().getSpdiStatus(), user.get().getPasswd(), user.get().getSignNo(),provisionedUser.getReferenceId());

        } catch (Exception ex) {
            UserCreationLogger.error("Authentication sync failed for user={}", provisionedUser.getUsername(), ex);
        }
    }


    private void userCreationContext(UserSessionObject loggedInUser, CreateUserDto request, HttpServletRequest httpRequest) {

        Integer targetRole = request.getCreatedRole();

        UserCreationStrategy strategy = userCreationStrategyFactory.getStrategy(targetRole);
        strategy.process(loggedInUser, request, httpRequest);
    }

    private void validateRequest(CreateUserDto userDto, UserSessionObject user) {
        Integer userTobeCreated = userDto.getCreatedRole();

        if(hasRole(user,ROLE_DA) && ROLE_DEFINER == userTobeCreated){
            EntityLevelDetailsDto levelDetails = lgdService.getEntityLevelDetails(user.getEntityId(), user.getEntityLevelId());
            if(ENTITY_LEVEL_TOP != levelDetails.getSortOrder()){
                throw new SPRuntimeError("Only top level department admin can create service definer",HttpStatus.BAD_REQUEST);
            }
        }

        List<Roles> roles = user.getRoles();
        List<Integer> roleIds = roles.stream().map(Roles::getRoleId).collect(Collectors.toList());
        System.out.println("Logged in user Id: "+user.getUserID() +" role ids: " + roleIds);

        List<AdminUserCreationAccess> roleAccess =
                adminUserCreationAccessRepository.findByLoggedInRoleIn(roleIds);

        System.out.println("User Creation Access:");

        for (AdminUserCreationAccess access : roleAccess) {
            System.out.println(
                "LoggedInRole: " + access.getLoggedInRole()
                + ", CanCreateRole ID: " + access.getRoleId()
                + ", CanCreateRole Name: " + access.getRoleName()
            );
        }
        if(roleAccess == null || roleAccess.isEmpty())
            throw new SPRuntimeError("Access denied to create this user", HttpStatus.BAD_REQUEST);

        List<Integer> collect = roleAccess.stream().map(AdminUserCreationAccess::getRoleId).toList();

        if(!collect.contains(userTobeCreated))
            throw new SPRuntimeError("Access denied to create this user", HttpStatus.BAD_REQUEST);


    }

    @Transactional
    public ResponseEntity<?> initializeProfileDetails(HttpServletRequest request, InitializeProfileDto profileDto, HttpServletResponse response) {

        String requestId = UUID.randomUUID().toString();

        try {

            UserSessionObject loggedInUser = getUserSessionDetails(request);

            UserCreationLogger.info("Initialize profile request started requestId={}", requestId);

            validateInitializeProfileRequest(profileDto);

            String encryptedUserId = profileDto.getUid();

            String decryptedUserId = AESDecrypt(encryptedUserId);

            Long userId = Long.parseLong(Objects.requireNonNull(decryptedUserId));

            LoginDetails loginDetails = loginDetailsRepository.findById(userId).orElseThrow(() -> new SPRuntimeError("Invalid User", HttpStatus.FORBIDDEN));

            validateUserActivationState(loginDetails);

            TenantProfilePolicy tenantPolicy = tenantPolicyService.fetchProfilePolicy(loggedInUser.getTenantId(),request);

            validateMandatoryFields(profileDto, tenantPolicy);

            validateEmailIfRequired(profileDto, loginDetails, tenantPolicy);

            validateMobileIfRequired(profileDto, loginDetails, tenantPolicy);

            UserProfile profile = userProfileService.fetchProfile(loginDetails.getUserId());

            updateUserProfile(profile, profileDto);

            updateUserLocationDesignation(loginDetails.getUserId(), profileDto,loggedInUser);

            updateLoginDetails(loginDetails, profileDto, profile);

            removeUserSessionAndData(loggedInUser.getAtk(), profileDto.getUid(), response, request);

            initiatePasswordSetup(loginDetails, profileDto, request,loggedInUser);

            publishAuthenticationUpdate(loginDetails);

            Map<String, Object> result = new HashMap<>();
            result.put("status", true);
            result.put("message", "Verification link sent on email");

            UserCreationLogger.info("Initialize profile completed successfully requestId={} userId={}", requestId, userId);

            return ResponseEntity.ok(result);

        } catch (SPRuntimeError ex) {
            throw ex;

        } catch (Exception ex) {

            UserCreationLogger.error("Initialize profile failed requestId={}", requestId, ex);
            throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateInitializeProfileRequest(InitializeProfileDto profileDto) {

        if (profileDto == null) {
            throw new SPRuntimeError("Invalid request", HttpStatus.BAD_REQUEST);
        }

        if (isEmpty(profileDto.getUid())) {
            throw new SPRuntimeError("Invalid User", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUserActivationState(LoginDetails loginDetails) {

        Integer status = loginDetails.getSpdiStatus();

        if (status != null && (status == 2 || status == 3)) {
            throw new SPRuntimeError("Invalid User [001]", HttpStatus.FORBIDDEN);
        }
    }

    private void validateMandatoryFields(InitializeProfileDto profileDto, TenantProfilePolicy tenantPolicy) {

        if (tenantPolicy.isEmailMandatory() && isEmpty(profileDto.getEmail())) {
            throw new SPRuntimeError("Email is mandatory", HttpStatus.BAD_REQUEST);
        }

        if (tenantPolicy.isMobileMandatory() && isEmpty(profileDto.getMobileNo())) {
            throw new SPRuntimeError("Mobile number is mandatory", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateEmailIfRequired(InitializeProfileDto profileDto, LoginDetails loginDetails, TenantProfilePolicy tenantPolicy) {

        if (!tenantPolicy.isEmailEnabled()) {
            return;
        }

        String email = profileDto.getEmail();

        boolean valid = userProfileService.checkEmail(email, loginDetails.getUserId());

        if (!valid) {
            throw new SPRuntimeError("Invalid Email or Email already exists", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateMobileIfRequired(InitializeProfileDto profileDto, LoginDetails loginDetails, TenantProfilePolicy tenantPolicy) {

        if (!tenantPolicy.isMobileEnabled()) {
            return;
        }

        String mobile = profileDto.getMobileNo();

        boolean valid = userProfileService.checkMobile(mobile, loginDetails.getUserId());

        if (!valid) {
            throw new SPRuntimeError("Invalid Mobile or Mobile already exists", HttpStatus.BAD_REQUEST);
        }
    }

    private void updateUserProfile(UserProfile profile, InitializeProfileDto profileDto) {

        profile.setMobileNo(profileDto.getMobileNo());
        profile.setEmailId(profileDto.getEmail());
        profile.setDesignationId(profileDto.getDesignationId());
        profile.setUserPreference(profileDto.getLoginPreference());
        profile.setUserName(profileDto.getUserName());

        userProfileRepository.save(profile);
    }

    private void updateUserLocationDesignation(Long userId, InitializeProfileDto profileDto, UserSessionObject loggedInUser) {

        List<UserLocation> locations = userLocationRepository.findByUserId(userId);

        if (locations == null || locations.isEmpty()) {
            return;
        }

        UserLocation location = locations.getFirst();

        List<UserLocationDesignation> designations = userLocationDesignationRepository.findByUserLocationId(location.getUserLocId());
        UserLocationDesignation designation;

        if (designations != null && !designations.isEmpty()) {
            designation = designations.getFirst();

        } else {
            designation = new UserLocationDesignation();
        }

        //LATER SET THIS NAME FROM API CALL ONCE NEW LGD API IS READY

        designation.setDesignationId(profileDto.getDesignationId());
        designation.setDesignationName(profileDto.getDesignationName());
        designation.setUserLocationId(location.getUserLocId());
        designation.setTenantId(loggedInUser.getTenantId());

        userLocationDesignationRepository.save(designation);
    }

    private void updateLoginDetails(LoginDetails loginDetails, InitializeProfileDto profileDto, UserProfile profile) {

        loginDetails.setSpdiStatus(1);

        if (profileDto.getLoginPreference() == 0) {
            loginDetails.setSignNo(profile.getEmailId());
        }

        loginDetailsRepository.save(loginDetails);
    }

    private void initiatePasswordSetup(LoginDetails loginDetails, InitializeProfileDto profileDto, HttpServletRequest request, UserSessionObject loggedInUser) {

        String subject = "Action Required";
        passwordResetService.generateLink(loginDetails.getUserId(), profileDto.getEmail(), subject, request,loggedInUser);
    }

    private void publishAuthenticationUpdate(LoginDetails loginDetails) {

        try {
            UpdateUserKafkaService.updateUserCredential(loginDetails.getUserId(), loginDetails.getSpdiStatus(), loginDetails.getPasswd(), loginDetails.getSignNo(),null);

        } catch (Exception ex) {
            UserCreationLogger.error("Failed to publish authentication update for userId={}", loginDetails.getUserId(), ex);
        }
    }

    private void removeUserSessionAndData(String atk,String uid,HttpServletResponse response, HttpServletRequest request) {
        redisService.removeFromList("NonActivatedUsers",atk + ":" + uid);
        sessionService.removeSession(atk);
    }


//    private CreateUserDto createLoggedInUserDepartmentAndLocation(UserSessionObject user, CreateUserDto userDto, List<SpecificLocation> locations, Integer locationTypeId) {
//
//        Integer level = userDto.getDepartmentDetails().getDeptLevel();
//
//        LGDCommonDto lgddata = lgdService.getLocationName(user.getLocationId());
//        SpecificLocation loc = new SpecificLocation();
//        loc.setId(user.getLocationId());
//        loc.setName(lgddata.getOrgUnitName());
//        locations.add(loc);
//
//        userDto.setSpecificLocation(locations);
//        userDto.setLocationTypeId(locationTypeId);
//
//        DepartmentDetails dept = new DepartmentDetails();
//        dept.setId(user.getDepartmentId());
//        dept.setSubOffice(user.getDepartmentLevelId());
//        dept.setDepartmentName(lgdService.getOrgLevelName(dept.getSubOffice()).getOrgLevelSpecName());
//        userDto.setDepartmentDetails(dept);
//
//        return userDto;
//    }

    public List<UserSessionObject.Roles> getRoles(Integer userId){
        List<Object[]> userRoles = userDetailsRepository.fetchUserRoles(userId);
        List<UserSessionObject.Roles> roles = new ArrayList<>();
        userRoles.forEach(r -> {
            UserSessionObject.Roles role = new UserSessionObject.Roles();
            role.setRoleId((Integer) r[0]);
            role.setRoleName((String) r[1]);
            roles.add(role);
        });

        return roles;
    }

    public ResponseEntity<?> fetchUserList(FetchRoleDto dto, HttpServletRequest request) {
        try {

            UserSessionObject user = getUserSessionDetails(request);

            if(!validateUserId(dto.getUserId(),user.getUserID()))
                throw new SPRuntimeError("Unauthorized Access",HttpStatus.UNAUTHORIZED);

            FetchUserListResponseDto fetchUserList = customQueryRepository.fetchUserList(user.getUserID());

            return ResponseEntity.ok(fetchUserList);

        }
        catch(Exception e) {
            if(e instanceof SPRuntimeError)
                throw e;

            e.printStackTrace();
            throw new SPRuntimeError("Something went wrong",HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ServerSidePaginationRecord<UserCreationHistoryResponse> fetchHistory(
            UserSessionObject loggedInUser,
            UserCreationHistoryRequest request) {

        Pageable pageable = PageRequest.of(request.getPageNo(), request.getPageSize());

        Instant from = null;
        Instant to;

        if (request.getFromDate() != null) {
            from = request.getFromDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
        }

        if (request.getToDate() != null) {
            to = request.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        } else {
            to = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        }

        Page<UserCreationHistory> page;

        if (from != null) {
            page = userCreationHistoryRepository.findByCreatedByAndCreatedOnBetweenOrderByCreatedOnDesc(loggedInUser.getUserID(), from, to, pageable);

        } else {
            page = userCreationHistoryRepository.findByCreatedByOrderByCreatedOnDesc(loggedInUser.getUserID(), pageable);
        }

        List<UserCreationHistoryResponse> data = page.getContent()
                .stream()
                .map(history -> new UserCreationHistoryResponse(
                        history.getHistoryId(),
                        history.getCreatedByName(),
                        history.getCreatedOn(),
                        history.getRoleName(),
                        history.getEntityName(),
                        history.getEntityLevelName(),
                        history.getTotalUsers()))
                .toList();


        ServerSidePaginationRecord<UserCreationHistoryResponse> response = new ServerSidePaginationRecord<>();

        response.setData(data);
        response.setCurrentPage(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalPages(page.getTotalPages());
        response.setTotalRecords(page.getTotalElements());

        return response;
    }

    public UserCreationDownloadResponse downloadExcel(Long historyId, UserSessionObject loggedInUser) {

        UserCreationHistory history =
                userCreationHistoryRepository.findByHistoryIdAndCreatedBy(
                                                historyId,loggedInUser.getUserID()
                                ).orElseThrow(() -> new SPRuntimeError("Record not found", HttpStatus.NOT_FOUND)
                                );

        UserCreationDownloadResponse response = new UserCreationDownloadResponse();

        response.setFileName(history.getFileName());
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setFile(
                Base64.getEncoder().encodeToString(history.getFileContent()));

        return response;
    }
}
