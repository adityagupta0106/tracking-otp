//package com.serviceplus.UserManagement.identifieduserset;
//
//import com.google.gson.Gson;
//import com.serviceplus.UserManagement.Entity.LoginDetails;
//import com.serviceplus.UserManagement.Entity.RoleAssignment;
//import com.serviceplus.UserManagement.Entity.UserProfile;
//import com.serviceplus.UserManagement.Entity.UserSessionObject;
//import com.serviceplus.UserManagement.Enum.UserStatus;
//import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
//import com.serviceplus.UserManagement.Kafka.NotificationKafkaService;
//import com.serviceplus.UserManagement.Repository.LoginDetailsRepository;
//import com.serviceplus.UserManagement.Repository.RoleAssignmentRepository;
//import com.serviceplus.UserManagement.Repository.UserLocationRepository;
//import com.serviceplus.UserManagement.Repository.UserProfileRepository;
//import com.serviceplus.UserManagement.identifieduserset.dto.*;
//import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSet;
//import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSetMember;
//import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSetUploadJob;
//import com.serviceplus.UserManagement.identifieduserset.enums.JobStatus;
//import com.serviceplus.UserManagement.identifieduserset.enums.JobType;
//import com.serviceplus.UserManagement.identifieduserset.repository.IdentifiedUserSetMemberRepository;
//import com.serviceplus.UserManagement.identifieduserset.repository.IdentifiedUserSetRepository;
//import com.serviceplus.UserManagement.identifieduserset.repository.IdentifiedUserSetUploadJobRepository;
//import com.serviceplus.UserManagement.identifieduserset.scheduler.IdentifiedUserSetUploadScheduler;
//import com.serviceplus.UserManagement.identifieduserset.service.impl.IdentifiedUserSetServiceImpl;
//import com.serviceplus.UserManagement.util.ApplicationConstants;
//import jakarta.servlet.http.HttpServletRequest;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.mock.web.MockMultipartFile;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class IdentifiedUserSetServiceTest {
//
//    @Mock
//    private IdentifiedUserSetRepository userSetRepository;
//
//    @Mock
//    private IdentifiedUserSetMemberRepository memberRepository;
//
//    @Mock
//    private IdentifiedUserSetUploadJobRepository uploadJobRepository;
//
//    @Mock
//    private LoginDetailsRepository loginDetailsRepository;
//
//    @Mock
//    private UserProfileRepository userProfileRepository;
//
//    @Mock
//    private RoleAssignmentRepository roleAssignmentRepository;
//
//    @Mock
//    private UserLocationRepository userLocationRepository;
//
//    @Mock
//    private NotificationKafkaService notificationKafkaService;
//
//    @Mock
//    private HttpServletRequest httpRequest;
//
//    @InjectMocks
//    private IdentifiedUserSetServiceImpl userSetService;
//
//    private UserSessionObject adminSession;
//    private final Gson gson = new Gson();
//
//    @BeforeEach
//    void setUp() {
//        adminSession = new UserSessionObject();
//        adminSession.setUserID(100L);
//        adminSession.setTenantId("tenant_uk");
//        UserSessionObject.Roles role = new UserSessionObject.Roles(ApplicationConstants.ROLE_TENANT_ADMIN, "Tenant Admin");
//        adminSession.setRoles(List.of(role));
//    }
//
//    private void mockAdminSession() {
//        when(httpRequest.getHeader("USER-DETAILS")).thenReturn(gson.toJson(adminSession));
//    }
//
//    @Test
//    void testCreateIdentifiedUserSet_Success() {
//        mockAdminSession();
//        CreateUserSetRequest request = new CreateUserSetRequest();
//        request.setName("Revenue Collectors");
//        request.setDescription("Revenue department collectors set");
//
//        when(userSetRepository.existsByNameAndTenantId("Revenue Collectors", "tenant_uk")).thenReturn(false);
//
//        IdentifiedUserSet saved = new IdentifiedUserSet();
//        saved.setId(1L);
//        saved.setName("Revenue Collectors");
//        saved.setDescription("Revenue department collectors set");
//        saved.setActive(true);
//        saved.setTenantId("tenant_uk");
//        saved.setCrBy(100L);
//        saved.setCDate(new Date());
//
//        when(userSetRepository.save(any(IdentifiedUserSet.class))).thenReturn(saved);
//
//        UserSetResponse response = userSetService.createUserSet(request, httpRequest);
//
//        assertNotNull(response);
//        assertEquals(1L, response.getId());
//        assertEquals("Revenue Collectors", response.getName());
//        assertTrue(response.getActive());
//        verify(userSetRepository, times(1)).save(any(IdentifiedUserSet.class));
//    }
//
//    @Test
//    void testCreateIdentifiedUserSet_DuplicateName_ThrowsException() {
//        mockAdminSession();
//        CreateUserSetRequest request = new CreateUserSetRequest();
//        request.setName("Revenue Collectors");
//
//        when(userSetRepository.existsByNameAndTenantId("Revenue Collectors", "tenant_uk")).thenReturn(true);
//
//        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () -> userSetService.createUserSet(request, httpRequest));
//        assertTrue(ex.getMessage().contains("already exists"));
//    }
//
//    @Test
//    void testCreateIdentifiedUserSet_NonAdminRole_ThrowsForbidden() {
//        UserSessionObject citizenSession = new UserSessionObject();
//        citizenSession.setUserID(200L);
//        citizenSession.setTenantId("tenant_uk");
//        citizenSession.setRoles(List.of(new UserSessionObject.Roles(ApplicationConstants.ROLE_CITIZEN, "Citizen")));
//
//        when(httpRequest.getHeader("USER-DETAILS")).thenReturn(gson.toJson(citizenSession));
//
//        CreateUserSetRequest request = new CreateUserSetRequest();
//        request.setName("Test Set");
//
//        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () -> userSetService.createUserSet(request, httpRequest));
//        assertTrue(ex.getMessage().contains("Only authorized Tenant Administrator"));
//    }
//
//    @Test
//    void testAddMember_Success_Clean() {
//        mockAdminSession();
//        Long userSetId = 1L;
//
//        IdentifiedUserSet set = new IdentifiedUserSet();
//        set.setId(userSetId);
//        set.setActive(true);
//        set.setTenantId("tenant_uk");
//        when(userSetRepository.findByIdAndTenantId(userSetId, "tenant_uk")).thenReturn(Optional.of(set));
//
//        AddMemberRequest addReq = new AddMemberRequest();
//        addReq.setEmail("officer@serviceplus.gov.in");
//
//        UserProfile profile = new UserProfile();
//        profile.setUserId(205L);
//        profile.setUserName("Rajesh Kumar");
//        profile.setEmailId("officer@serviceplus.gov.in");
//        profile.setMobileNo("9876543210");
//        when(userProfileRepository.findByEmailId("officer@serviceplus.gov.in")).thenReturn(profile);
//
//        LoginDetails login = new LoginDetails();
//        login.setUserId(205L);
//        login.setSignNo("SP_OFF_205");
//        login.setSpdiStatus(UserStatus.ACTIVE.getCode());
//        login.setTenantId("tenant_uk");
//        when(loginDetailsRepository.findByUserId(205L)).thenReturn(Optional.of(login));
//
//        when(memberRepository.findByUserSetIdAndUserIdAndTenantId(userSetId, 205L, "tenant_uk")).thenReturn(Optional.empty());
//
//        IdentifiedUserSetMember savedMember = new IdentifiedUserSetMember();
//        savedMember.setId(10L);
//        savedMember.setUserSetId(userSetId);
//        savedMember.setUserId(205L);
//        savedMember.setSignNo("SP_OFF_205");
//        savedMember.setActive(true);
//        savedMember.setTenantId("tenant_uk");
//        savedMember.setCDate(new Date());
//        when(memberRepository.save(any(IdentifiedUserSetMember.class))).thenReturn(savedMember);
//        when(userProfileRepository.findByUserId(205L)).thenReturn(profile);
//
//        // No memberships in other user sets
//        when(memberRepository.findByUserIdAndTenantIdAndActiveAndUserSetIdNot(205L, "tenant_uk", true, userSetId))
//                .thenReturn(Collections.emptyList());
//
//        MemberResponse response = userSetService.addMember(userSetId, addReq, httpRequest);
//
//        assertNotNull(response);
//        assertEquals(205L, response.getUserId());
//        assertEquals("SP_OFF_205", response.getSignNo());
//        assertNull(response.getWarning());
//        verify(roleAssignmentRepository, times(1)).save(any(RoleAssignment.class));
//    }
//
//    @Test
//    void testAddMember_WithWarning_WhenUserBelongsToAnotherSet() {
//        mockAdminSession();
//        Long userSetId = 1L;
//
//        IdentifiedUserSet set = new IdentifiedUserSet();
//        set.setId(userSetId);
//        set.setActive(true);
//        set.setTenantId("tenant_uk");
//        when(userSetRepository.findByIdAndTenantId(userSetId, "tenant_uk")).thenReturn(Optional.of(set));
//
//        AddMemberRequest addReq = new AddMemberRequest();
//        addReq.setEmail("officer@serviceplus.gov.in");
//
//        UserProfile profile = new UserProfile();
//        profile.setUserId(205L);
//        profile.setUserName("Rajesh Kumar");
//        profile.setEmailId("officer@serviceplus.gov.in");
//        profile.setMobileNo("9876543210");
//        when(userProfileRepository.findByEmailId("officer@serviceplus.gov.in")).thenReturn(profile);
//
//        LoginDetails login = new LoginDetails();
//        login.setUserId(205L);
//        login.setSignNo("SP_OFF_205");
//        login.setSpdiStatus(UserStatus.ACTIVE.getCode());
//        login.setTenantId("tenant_uk");
//        when(loginDetailsRepository.findByUserId(205L)).thenReturn(Optional.of(login));
//
//        when(memberRepository.findByUserSetIdAndUserIdAndTenantId(userSetId, 205L, "tenant_uk")).thenReturn(Optional.empty());
//
//        IdentifiedUserSetMember savedMember = new IdentifiedUserSetMember();
//        savedMember.setId(10L);
//        savedMember.setUserSetId(userSetId);
//        savedMember.setUserId(205L);
//        savedMember.setSignNo("SP_OFF_205");
//        savedMember.setActive(true);
//        savedMember.setTenantId("tenant_uk");
//        savedMember.setCDate(new Date());
//        when(memberRepository.save(any(IdentifiedUserSetMember.class))).thenReturn(savedMember);
//        when(userProfileRepository.findByUserId(205L)).thenReturn(profile);
//
//        // User belongs to another user set (ID 2L: "Transport Operators")
//        IdentifiedUserSetMember otherMember = new IdentifiedUserSetMember();
//        otherMember.setUserSetId(2L);
//        otherMember.setUserId(205L);
//        otherMember.setActive(true);
//        when(memberRepository.findByUserIdAndTenantIdAndActiveAndUserSetIdNot(205L, "tenant_uk", true, userSetId))
//                .thenReturn(List.of(otherMember));
//
//        IdentifiedUserSet otherSet = new IdentifiedUserSet();
//        otherSet.setId(2L);
//        otherSet.setName("Transport Operators");
//        when(userSetRepository.findById(2L)).thenReturn(Optional.of(otherSet));
//
//        MemberResponse response = userSetService.addMember(userSetId, addReq, httpRequest);
//
//        assertNotNull(response);
//        assertEquals(205L, response.getUserId());
//        assertNotNull(response.getWarning());
//        assertTrue(response.getWarning().contains("Transport Operators"));
//    }
//
//    @Test
//    void testAddMember_UserNotFound_ThrowsException() {
//        mockAdminSession();
//        Long userSetId = 1L;
//
//        IdentifiedUserSet set = new IdentifiedUserSet();
//        set.setId(userSetId);
//        set.setActive(true);
//        set.setTenantId("tenant_uk");
//        when(userSetRepository.findByIdAndTenantId(userSetId, "tenant_uk")).thenReturn(Optional.of(set));
//
//        AddMemberRequest addReq = new AddMemberRequest();
//        addReq.setEmail("nonexistent@serviceplus.gov.in");
//        when(userProfileRepository.findByEmailId("nonexistent@serviceplus.gov.in")).thenReturn(null);
//
//        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () -> userSetService.addMember(userSetId, addReq, httpRequest));
//        assertTrue(ex.getMessage().contains("does not exist") || ex.getMessage().contains("not found"));
//    }
//
//    @Test
//    void testAddMember_UnauthorizedTenant_ThrowsException() {
//        mockAdminSession();
//        Long userSetId = 1L;
//
//        IdentifiedUserSet set = new IdentifiedUserSet();
//        set.setId(userSetId);
//        set.setActive(true);
//        set.setTenantId("tenant_uk");
//        when(userSetRepository.findByIdAndTenantId(userSetId, "tenant_uk")).thenReturn(Optional.of(set));
//
//        AddMemberRequest addReq = new AddMemberRequest();
//        addReq.setUserId(300L);
//
//        LoginDetails foreignUserLogin = new LoginDetails();
//        foreignUserLogin.setUserId(300L);
//        foreignUserLogin.setSpdiStatus(UserStatus.ACTIVE.getCode());
//        foreignUserLogin.setTenantId("tenant_other_state");
//        when(loginDetailsRepository.findByUserId(300L)).thenReturn(Optional.of(foreignUserLogin));
//
//        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () -> userSetService.addMember(userSetId, addReq, httpRequest));
//        assertTrue(ex.getMessage().contains("unauthorized tenant"));
//    }
//
//    @Test
//    void testAddMember_DuplicateMembershipInSameSet_ThrowsException() {
//        mockAdminSession();
//        Long userSetId = 1L;
//
//        IdentifiedUserSet set = new IdentifiedUserSet();
//        set.setId(userSetId);
//        set.setActive(true);
//        set.setTenantId("tenant_uk");
//        when(userSetRepository.findByIdAndTenantId(userSetId, "tenant_uk")).thenReturn(Optional.of(set));
//
//        AddMemberRequest addReq = new AddMemberRequest();
//        addReq.setUserId(205L);
//
//        LoginDetails login = new LoginDetails();
//        login.setUserId(205L);
//        login.setSpdiStatus(UserStatus.ACTIVE.getCode());
//        login.setTenantId("tenant_uk");
//        when(loginDetailsRepository.findByUserId(205L)).thenReturn(Optional.of(login));
//
//        IdentifiedUserSetMember activeMember = new IdentifiedUserSetMember();
//        activeMember.setActive(true);
//        when(memberRepository.findByUserSetIdAndUserIdAndTenantId(userSetId, 205L, "tenant_uk")).thenReturn(Optional.of(activeMember));
//
//        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () -> userSetService.addMember(userSetId, addReq, httpRequest));
//        assertTrue(ex.getMessage().contains("already an active member"));
//    }
//
//    @Test
//    void testRemoveMember_Success_Clean() {
//        mockAdminSession();
//        Long userSetId = 1L;
//        Long userId = 205L;
//
//        IdentifiedUserSet set = new IdentifiedUserSet();
//        set.setId(userSetId);
//        set.setTenantId("tenant_uk");
//        when(userSetRepository.findByIdAndTenantId(userSetId, "tenant_uk")).thenReturn(Optional.of(set));
//
//        IdentifiedUserSetMember member = new IdentifiedUserSetMember();
//        member.setId(10L);
//        member.setUserSetId(userSetId);
//        member.setUserId(userId);
//        member.setActive(true);
//        member.setTenantId("tenant_uk");
//        when(memberRepository.findByUserSetIdAndUserIdAndTenantIdAndActive(userSetId, userId, "tenant_uk", true))
//                .thenReturn(Optional.of(member));
//
//        userSetService.removeMember(userSetId, userId, httpRequest);
//
//        assertFalse(member.getActive());
//        assertNotNull(member.getUBy());
//        assertNotNull(member.getUDate());
//        verify(memberRepository, times(1)).save(member);
//        verify(roleAssignmentRepository, times(1)).deleteByUserIdAndRoleId(userId, ApplicationConstants.ROLE_IDENTIFIED_USER);
//    }
//
//    @Test
//    void testRemoveMember_WhenOtherActiveSetsExist_RoleNotRemoved() {
//        mockAdminSession();
//        Long userSetId = 1L;
//        Long userId = 205L;
//
//        IdentifiedUserSet set = new IdentifiedUserSet();
//        set.setId(userSetId);
//        set.setTenantId("tenant_uk");
//        when(userSetRepository.findByIdAndTenantId(userSetId, "tenant_uk")).thenReturn(Optional.of(set));
//
//        IdentifiedUserSetMember member = new IdentifiedUserSetMember();
//        member.setId(10L);
//        member.setUserSetId(userSetId);
//        member.setUserId(userId);
//        member.setActive(true);
//        member.setTenantId("tenant_uk");
//        when(memberRepository.findByUserSetIdAndUserIdAndTenantIdAndActive(userSetId, userId, "tenant_uk", true))
//                .thenReturn(Optional.of(member));
//
//        // User is still in another active Identified User Set
//        when(memberRepository.existsByUserIdAndTenantIdAndActive(userId, "tenant_uk", true)).thenReturn(true);
//
//        userSetService.removeMember(userSetId, userId, httpRequest);
//
//        assertFalse(member.getActive());
//        verify(roleAssignmentRepository, never()).deleteByUserIdAndRoleId(anyLong(), anyInt());
//    }
//
//    @Test
//    void testUploadMembersExcel_QueuesAsyncJob() throws IOException {
//        mockAdminSession();
//        Long userSetId = 1L;
//
//        IdentifiedUserSet set = new IdentifiedUserSet();
//        set.setId(userSetId);
//        set.setActive(true);
//        set.setTenantId("tenant_uk");
//        when(userSetRepository.findByIdAndTenantId(userSetId, "tenant_uk")).thenReturn(Optional.of(set));
//
//        UserProfile adminProfile = new UserProfile();
//        adminProfile.setUserId(100L);
//        adminProfile.setEmailId("admin@serviceplus.gov.in");
//        when(userProfileRepository.findByUserId(100L)).thenReturn(adminProfile);
//
//        byte[] sampleExcel = createSampleExcel();
//        MockMultipartFile file = new MockMultipartFile("file", "users.xlsx",
//                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", sampleExcel);
//
//        IdentifiedUserSetUploadJob savedJob = new IdentifiedUserSetUploadJob();
//        savedJob.setId(501L);
//        savedJob.setUserSetId(userSetId);
//        savedJob.setStatus(JobStatus.PENDING);
//        savedJob.setJobType(JobType.UPLOAD);
//        savedJob.setFileName("users.xlsx");
//        when(uploadJobRepository.save(any(IdentifiedUserSetUploadJob.class))).thenReturn(savedJob);
//
//        ExcelUploadJobResponse response = userSetService.uploadMembersExcel(userSetId, file, httpRequest);
//
//        assertNotNull(response);
//        assertEquals(501L, response.getJobId());
//        assertEquals(JobStatus.PENDING, response.getStatus());
//        assertTrue(response.getMessage().contains("asynchronous"));
//        verify(uploadJobRepository, times(1)).save(any(IdentifiedUserSetUploadJob.class));
//    }
//
//    @Test
//    void testExportMembersAsync_QueuesJob() {
//        mockAdminSession();
//        Long userSetId = 1L;
//
//        IdentifiedUserSet set = new IdentifiedUserSet();
//        set.setId(userSetId);
//        set.setName("Revenue Collectors");
//        set.setActive(true);
//        set.setTenantId("tenant_uk");
//        when(userSetRepository.findByIdAndTenantId(userSetId, "tenant_uk")).thenReturn(Optional.of(set));
//
//        UserProfile adminProfile = new UserProfile();
//        adminProfile.setUserId(100L);
//        adminProfile.setEmailId("admin@serviceplus.gov.in");
//        when(userProfileRepository.findByUserId(100L)).thenReturn(adminProfile);
//
//        IdentifiedUserSetUploadJob savedJob = new IdentifiedUserSetUploadJob();
//        savedJob.setId(601L);
//        savedJob.setUserSetId(userSetId);
//        savedJob.setStatus(JobStatus.PENDING);
//        savedJob.setJobType(JobType.EXPORT);
//        when(uploadJobRepository.save(any(IdentifiedUserSetUploadJob.class))).thenReturn(savedJob);
//
//        ExcelUploadJobResponse response = userSetService.exportMembersAsync(userSetId, httpRequest);
//
//        assertNotNull(response);
//        assertEquals(601L, response.getJobId());
//        assertEquals(JobStatus.PENDING, response.getStatus());
//        assertTrue(response.getMessage().contains("asynchronous"));
//        verify(uploadJobRepository, times(1)).save(any(IdentifiedUserSetUploadJob.class));
//    }
//
//    @Test
//    void testExportMembersToExcel_Success() {
//        mockAdminSession();
//        Long userSetId = 1L;
//
//        IdentifiedUserSet set = new IdentifiedUserSet();
//        set.setId(userSetId);
//        set.setName("Revenue Collectors");
//        set.setTenantId("tenant_uk");
//        when(userSetRepository.findByIdAndTenantId(userSetId, "tenant_uk")).thenReturn(Optional.of(set));
//
//        IdentifiedUserSetMember m1 = new IdentifiedUserSetMember();
//        m1.setId(10L);
//        m1.setUserId(201L);
//        m1.setSignNo("OFFICER_201");
//        m1.setMembershipRole("MEMBER");
//        m1.setActive(true);
//        m1.setCDate(new Date());
//
//        when(memberRepository.findByUserSetIdAndTenantIdAndActive(userSetId, "tenant_uk", true))
//                .thenReturn(List.of(m1));
//
//        UserProfile p1 = new UserProfile();
//        p1.setUserId(201L);
//        p1.setUserName("Amit Patel");
//        p1.setEmailId("amit@serviceplus.gov.in");
//        p1.setMobileNo("9822334455");
//        when(userProfileRepository.findByUserId(201L)).thenReturn(p1);
//
//        byte[] exportBytes = userSetService.exportMembersToExcel(userSetId, httpRequest);
//
//        assertNotNull(exportBytes);
//        assertTrue(exportBytes.length > 0);
//    }
//
//    @Test
//    void testDownloadTemplate_Success() {
//        byte[] bytes = userSetService.downloadTemplate();
//        assertNotNull(bytes);
//        assertTrue(bytes.length > 0);
//    }
//
//    @Test
//    void testScheduler_ProcessPendingJob_SuccessWithWarningAndKafka() throws Exception {
//        IdentifiedUserSetUploadScheduler scheduler = new IdentifiedUserSetUploadScheduler(
//                uploadJobRepository, userSetRepository, memberRepository,
//                loginDetailsRepository, userProfileRepository, notificationKafkaService
//        );
//
//        byte[] excelBytes = createSampleExcel();
//
//        IdentifiedUserSetUploadJob job = new IdentifiedUserSetUploadJob();
//        job.setId(99L);
//        job.setUserSetId(1L);
//        job.setTenantId("tenant_uk");
//        job.setCrBy(100L);
//        job.setStatus(JobStatus.PENDING);
//        job.setJobType(JobType.UPLOAD);
//        job.setAdminEmail("admin@serviceplus.gov.in");
//        job.setFileData(excelBytes);
//
//        IdentifiedUserSet set = new IdentifiedUserSet();
//        set.setId(1L);
//        set.setActive(true);
//        when(userSetRepository.findById(1L)).thenReturn(Optional.of(set));
//
//        // Mock row user: user1@example.com -> exists in DB
//        UserProfile user1Profile = new UserProfile();
//        user1Profile.setUserId(301L);
//        user1Profile.setUserName("User One");
//        user1Profile.setEmailId("user1@example.com");
//        user1Profile.setMobileNo("9876543210");
//        when(userProfileRepository.findByEmailId("user1@example.com")).thenReturn(user1Profile);
//
//        LoginDetails user1Login = new LoginDetails();
//        user1Login.setUserId(301L);
//        user1Login.setSignNo("SIGN_301");
//        user1Login.setSpdiStatus(UserStatus.ACTIVE.getCode());
//        user1Login.setTenantId("tenant_uk");
//        when(loginDetailsRepository.findByUserId(301L)).thenReturn(Optional.of(user1Login));
//
//        when(memberRepository.findByUserSetIdAndUserIdAndTenantId(1L, 301L, "tenant_uk")).thenReturn(Optional.empty());
//
//        // User belongs to another user set (ID 5: "Land Records Set") -> triggers WARNING
//        IdentifiedUserSetMember otherMember = new IdentifiedUserSetMember();
//        otherMember.setUserSetId(5L);
//        otherMember.setUserId(301L);
//        when(memberRepository.findByUserIdAndTenantIdAndActiveAndUserSetIdNot(301L, "tenant_uk", true, 1L))
//                .thenReturn(List.of(otherMember));
//
//        IdentifiedUserSet set5 = new IdentifiedUserSet();
//        set5.setId(5L);
//        set5.setName("Land Records Set");
//        when(userSetRepository.findById(5L)).thenReturn(Optional.of(set5));
//
//        scheduler.processSingleJob(job);
//
//        assertEquals(JobStatus.COMPLETED, job.getStatus());
//        assertEquals(1, job.getTotalRecords());
//        assertEquals(0, job.getAddedCount());
//        assertEquals(1, job.getWarningCount());
//        assertEquals(0, job.getFailedCount());
//        assertNotNull(job.getResultFilePath());
//        assertNull(job.getFileData()); // Cleared to save space
//
//        // Verify Kafka notification was sent with result attachment
//        verify(notificationKafkaService, times(1)).sendNotification(
//                isNull(), eq("tenant_uk"), eq("admin@serviceplus.gov.in"),
//                contains("Report"), anyString(), eq(job.getResultFilePath()), eq("xlsx"), anyString(),
//                eq(0), eq(0), eq(false), eq(true), eq(0), eq(0)
//        );
//        scheduler.destroy();
//    }
//
//    @Test
//    void testScheduler_ProcessExportJob_SuccessWithKafka() {
//        IdentifiedUserSetUploadScheduler scheduler = new IdentifiedUserSetUploadScheduler(
//                uploadJobRepository, userSetRepository, memberRepository,
//                loginDetailsRepository, userProfileRepository, notificationKafkaService
//        );
//
//        IdentifiedUserSetUploadJob job = new IdentifiedUserSetUploadJob();
//        job.setId(102L);
//        job.setUserSetId(1L);
//        job.setTenantId("tenant_uk");
//        job.setCrBy(100L);
//        job.setStatus(JobStatus.PENDING);
//        job.setJobType(JobType.EXPORT);
//        job.setAdminEmail("admin@serviceplus.gov.in");
//
//        IdentifiedUserSet set = new IdentifiedUserSet();
//        set.setId(1L);
//        set.setName("District Collectors");
//        set.setActive(true);
//        when(userSetRepository.findById(1L)).thenReturn(Optional.of(set));
//
//        IdentifiedUserSetMember m1 = new IdentifiedUserSetMember();
//        m1.setId(10L);
//        m1.setUserId(301L);
//        m1.setSignNo("SIGN_301");
//        m1.setActive(true);
//        m1.setCDate(new Date());
//        when(memberRepository.findByUserSetIdAndTenantIdAndActive(1L, "tenant_uk", true)).thenReturn(List.of(m1));
//
//        UserProfile p1 = new UserProfile();
//        p1.setUserId(301L);
//        p1.setUserName("User One");
//        p1.setEmailId("user1@example.com");
//        when(userProfileRepository.findByUserId(301L)).thenReturn(p1);
//
//        scheduler.processSingleJob(job);
//
//        assertEquals(JobStatus.COMPLETED, job.getStatus());
//        assertEquals(1, job.getTotalRecords());
//        assertNotNull(job.getResultFilePath());
//        assertNull(job.getFileData());
//
//        verify(notificationKafkaService, times(1)).sendNotification(
//                isNull(), eq("tenant_uk"), eq("admin@serviceplus.gov.in"),
//                contains("Export"), anyString(), eq(job.getResultFilePath()), eq("xlsx"), anyString(),
//                eq(0), eq(0), eq(false), eq(true), eq(0), eq(0)
//        );
//        scheduler.destroy();
//    }
//
//    @Test
//    void testScheduler_ProcessPendingUploadJobs_ThreadPoolAndSafety() throws Exception {
//        IdentifiedUserSetUploadScheduler scheduler = new IdentifiedUserSetUploadScheduler(
//                uploadJobRepository, userSetRepository, memberRepository,
//                loginDetailsRepository, userProfileRepository, notificationKafkaService
//        );
//
//        IdentifiedUserSetUploadJob job = new IdentifiedUserSetUploadJob();
//        job.setId(201L);
//        job.setUserSetId(1L);
//        job.setTenantId("tenant_uk");
//        job.setCrBy(100L);
//        job.setStatus(JobStatus.PENDING);
//        job.setJobType(JobType.UPLOAD);
//        job.setFileData(createSampleExcel());
//
//        when(uploadJobRepository.findByStatusOrderByCDateAsc(JobStatus.PENDING)).thenReturn(List.of(job));
//        when(uploadJobRepository.claimJob(eq(201L), eq(JobStatus.PROCESSING), eq(JobStatus.PENDING), any(Date.class))).thenReturn(1);
//
//        IdentifiedUserSet set = new IdentifiedUserSet();
//        set.setId(1L);
//        set.setActive(true);
//        when(userSetRepository.findById(1L)).thenReturn(Optional.of(set));
//
//        scheduler.processPendingUploadJobs();
//
//        // Give thread pool task a short moment to run
//        Thread.sleep(400);
//
//        verify(uploadJobRepository, times(1)).claimJob(eq(201L), eq(JobStatus.PROCESSING), eq(JobStatus.PENDING), any(Date.class));
//        scheduler.destroy();
//    }
//
//    private byte[] createSampleExcel() throws IOException {
//        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
//            Sheet sheet = workbook.createSheet("Users");
//            Row header = sheet.createRow(0);
//            header.createCell(0).setCellValue("Email");
//            header.createCell(1).setCellValue("User Identifier");
//
//            Row r1 = sheet.createRow(1);
//            r1.createCell(0).setCellValue("user1@example.com");
//            r1.createCell(1).setCellValue("SIGN_301");
//
//            workbook.write(out);
//            return out.toByteArray();
//        }
//    }
//
//    @Test
//    void testGetUserSetIds_MultipleSets() {
//        lenient().when(httpRequest.getHeader("USER-DETAILS")).thenReturn(null);
//        lenient().when(httpRequest.getHeader("_scall")).thenReturn("true");
//        lenient().when(httpRequest.getHeader("Atk")).thenReturn(com.serviceplus.UserManagement.Enum.ApplicationConstants.STATIC_AUTH_TOKEN.getString());
//
//        when(memberRepository.findActiveUserSetIdsByUserIdAndTenantId(101L, "tenant_uk")).thenReturn(List.of(113L, 114L, 115L));
//
//        List<Integer> ids = userSetService.getUserSetIdsForUser(101L, "tenant_uk", httpRequest);
//        assertEquals(3, ids.size());
//        assertEquals(List.of(113, 114, 115), ids);
//    }
//
//    @Test
//    void testGetUserSetIds_NoSets() {
//        lenient().when(httpRequest.getHeader("USER-DETAILS")).thenReturn(null);
//        lenient().when(httpRequest.getHeader("_scall")).thenReturn("true");
//        lenient().when(httpRequest.getHeader("Atk")).thenReturn(com.serviceplus.UserManagement.Enum.ApplicationConstants.STATIC_AUTH_TOKEN.getString());
//
//        when(memberRepository.findActiveUserSetIdsByUserIdAndTenantId(101L, "tenant_uk")).thenReturn(Collections.emptyList());
//
//        List<Integer> ids = userSetService.getUserSetIdsForUser(101L, "tenant_uk", httpRequest);
//        assertNotNull(ids);
//        assertTrue(ids.isEmpty());
//    }
//
//    @Test
//    void testGetUserSetIds_InvalidUserId() {
//        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () -> userSetService.getUserSetIdsForUser(null, "tenant_uk", httpRequest));
//        assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
//
//        SPRuntimeError ex2 = assertThrows(SPRuntimeError.class, () -> userSetService.getUserSetIdsForUser(-5L, "tenant_uk", httpRequest));
//        assertEquals(HttpStatus.BAD_REQUEST, ex2.getErrorCode());
//    }
//
//    @Test
//    void testGetUserSetIds_Unauthorized() {
//        lenient().when(httpRequest.getHeader("USER-DETAILS")).thenReturn(null);
//        lenient().when(httpRequest.getHeader("_scall")).thenReturn(null);
//        lenient().when(httpRequest.getHeader("Atk")).thenReturn(null);
//
//        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () -> userSetService.getUserSetIdsForUser(101L, "tenant_uk", httpRequest));
//        assertEquals(HttpStatus.FORBIDDEN, ex.getErrorCode());
//    }
//
//    @Test
//    void testGetUserSetIds_TenantIsolation() {
//        UserSessionObject session = new UserSessionObject();
//        session.setUserID(101L);
//        session.setTenantId("tenant_uk");
//        session.setTopLevelAdmin(false);
//        lenient().when(httpRequest.getHeader("USER-DETAILS")).thenReturn(gson.toJson(session));
//
//        // When requesting mismatched tenant as non-admin, should throw FORBIDDEN
//        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () -> userSetService.getUserSetIdsForUser(101L, "other_tenant", httpRequest));
//        assertEquals(HttpStatus.FORBIDDEN, ex.getErrorCode());
//
//        // When requesting same tenant or null tenant, tenant_uk is used
//        when(memberRepository.findActiveUserSetIdsByUserIdAndTenantId(101L, "tenant_uk")).thenReturn(List.of(113L));
//        List<Integer> ids = userSetService.getUserSetIdsForUser(101L, null, httpRequest);
//        assertEquals(List.of(113), ids);
//    }
//}
