package com.serviceplus.UserManagement.Service;

import com.serviceplus.UserManagement.Dto.*;
import com.serviceplus.UserManagement.Entity.LoginDetails;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.Enum.UserStatus;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Kafka.NotificationKafkaService;
import com.serviceplus.UserManagement.Kafka.UpdateUserKafkaService;
import com.serviceplus.UserManagement.Repository.LoginDetailsRepository;
import com.serviceplus.UserManagement.util.ExcelGeneratorUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.serviceplus.UserManagement.util.ApplicationConstants.*;
import static com.serviceplus.UserManagement.util.Utility.*;

@Service
public class TenantAdminProvisioningService {

    private static final Logger Logger = LogManager.getLogger("TenantAdminProvisioningService");

    private final UpdateUserKafkaService updateUserKafkaService;

    private final UserOnboardingService userOnboardingService;

    private final LoginDetailsRepository loginDetailsRepository;

    @Value("${user.excel.path}")
    private String userExcelPath;

    @Value("${user.creation.email.subject}")
    private String USER_CREATION_EMAIL_SUBJECT;

    @Value("${user.creation.email.content}")
    private String USER_CREATION_EMAIL_CONTENT;

    public TenantAdminProvisioningService(UpdateUserKafkaService updateUserKafkaService, UserOnboardingService userOnboardingService, LoginDetailsRepository loginDetailsRepository) {
        this.updateUserKafkaService = updateUserKafkaService;
        this.userOnboardingService = userOnboardingService;
        this.loginDetailsRepository = loginDetailsRepository;
    }

    @Transactional
    public TenantRegistrationResponse registerTenant(TenantRegistrationRequest request, HttpServletRequest httpServletRequest) throws IOException {

        try {
            UserSessionObject loggedInUser = Objects.requireNonNull(getUserSessionDetails(httpServletRequest));
            String tenantId = request.getTenantId();

            Logger.info("Starting tenant admin provisioning tenantId={}", tenantId);

            UserProvisioningRequest provisioningRequest = buildTenantAdminProvisioningRequest(request, loggedInUser);

            ProvisionedUserResult provisionedUser = userOnboardingService.onboardUser(provisioningRequest, loggedInUser,httpServletRequest);

            Optional<LoginDetails> optionalUser = loginDetailsRepository.findBySignNo(provisionedUser.getUsername());

            optionalUser.ifPresent(admin ->
                    updateUserKafkaService.updateUserCredential(
                            admin.getUserId(),
                            admin.getSpdiStatus(),
                            admin.getPasswd(),
                            admin.getSignNo(), provisionedUser.getReferenceId())
            );

            TenantRegistrationResponse response = new TenantRegistrationResponse();

            response.setTenantId(tenantId);
            response.setTenantName(request.getTenantName());
            response.setAdminUsername(provisionedUser.getUsername());
            response.setTemporaryPassword(provisionedUser.getPassword());

            Logger.info("Tenant admin created successfully tenantId={} username={}", tenantId, provisionedUser.getUsername());

            return response;
        } catch (Exception e) {
            if(e instanceof SPRuntimeError)
                throw e;

            e.printStackTrace();
            throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String createExcel(List<ProvisionedUserResult> users, Long loggedInUser) throws IOException {

        if (users == null || users.isEmpty()) {
            return "";
        }

        String filePath = buildExcelFilePath(loggedInUser);

        List<String> headers = List.of("Serial No.", "User Name", "Password");
        List<Object[]> rows = new ArrayList<>();

        int serialNo = 1;

        for (ProvisionedUserResult user : users) {
            rows.add(new Object[]{serialNo++, user.getUsername(), user.getPassword()});
        }

        return ExcelGeneratorUtil.generateExcel("User List", filePath, "User Credentials", headers, rows);
    }

    private UserProvisioningRequest buildTenantAdminProvisioningRequest(TenantRegistrationRequest request, UserSessionObject loggedInUser) {

        UserProvisioningRequest provisioningRequest = new UserProvisioningRequest();

        CreateUserDto.SpecificLocation location = new CreateUserDto.SpecificLocation();

        location.setEntityUnitCode(0);
        location.setEntityUnitName(request.getTenantName());
        location.setParentEntityUnitName("");

        provisioningRequest.setCreatedByUserId(loggedInUser.getUserID());
        provisioningRequest.setRoleId(ROLE_TENANT_ADMIN);
        provisioningRequest.setLocation(location);
        provisioningRequest.setClcId(FALLBACK_CLC_CODE);
        provisioningRequest.setClcName("");
        provisioningRequest.setCategoryId(0);
        provisioningRequest.setEntityId(0);
        provisioningRequest.setLevelId(0);
        provisioningRequest.setLevelName("");
        provisioningRequest.setDesignationId(0);
        provisioningRequest.setApprovalRequired(false);
        provisioningRequest.setSigNoPrefix(TENANT_USER_SIGN_PREFIX);

        provisioningRequest.setInitialStatus(UserStatus.INITIAL_CREATION.getCode());
        provisioningRequest.setTenantId(request.getTenantId());

        return provisioningRequest;
    }
}