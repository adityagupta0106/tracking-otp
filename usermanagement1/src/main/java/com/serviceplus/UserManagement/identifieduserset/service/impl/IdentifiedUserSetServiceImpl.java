package com.serviceplus.UserManagement.identifieduserset.service.impl;

import com.serviceplus.UserManagement.Dto.JobStatusDto;
import com.serviceplus.UserManagement.Entity.LoginDetails;
import com.serviceplus.UserManagement.Entity.RoleAssignment;
import com.serviceplus.UserManagement.Entity.UserLocation;
import com.serviceplus.UserManagement.Entity.UserProfile;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.Enum.UserStatus;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Repository.LoginDetailsRepository;
import com.serviceplus.UserManagement.Repository.RoleAssignmentRepository;
import com.serviceplus.UserManagement.Repository.UserDetailsRepository;
import com.serviceplus.UserManagement.Repository.UserLocationRepository;
import com.serviceplus.UserManagement.Repository.UserProfileRepository;
import com.serviceplus.UserManagement.util.ApplicationConstants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import com.serviceplus.UserManagement.identifieduserset.dto.*;
import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSet;
import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSetMember;
import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSetUploadJob;
import com.serviceplus.UserManagement.identifieduserset.enums.JobStatus;
import com.serviceplus.UserManagement.identifieduserset.enums.JobType;
import com.serviceplus.UserManagement.identifieduserset.repository.IdentifiedUserSetMemberRepository;
import com.serviceplus.UserManagement.identifieduserset.repository.IdentifiedUserSetRepository;
import com.serviceplus.UserManagement.identifieduserset.repository.IdentifiedUserSetUploadJobRepository;
import com.serviceplus.UserManagement.identifieduserset.service.IdentifiedUserSetService;
import com.serviceplus.UserManagement.identifieduserset.util.IdentifiedUserSetExcelUtil;
import com.serviceplus.UserManagement.identifieduserset.util.IdentifiedUserSetExcelUtil.ExcelMemberExportRow;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.serviceplus.UserManagement.util.ApplicationConstants.ROLE_TENANT_ADMIN;
import static com.serviceplus.UserManagement.util.Utility.*;

@Service
public class IdentifiedUserSetServiceImpl implements IdentifiedUserSetService {

    private static final Logger log = LogManager.getLogger(IdentifiedUserSetServiceImpl.class);

    private final IdentifiedUserSetRepository userSetRepository;
    private final IdentifiedUserSetMemberRepository memberRepository;
    private final IdentifiedUserSetUploadJobRepository uploadJobRepository;
    private final LoginDetailsRepository loginDetailsRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final UserLocationRepository userLocationRepository;
    private final UserDetailsRepository userDetailsRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public IdentifiedUserSetServiceImpl(IdentifiedUserSetRepository userSetRepository,
                                        IdentifiedUserSetMemberRepository memberRepository,
                                        IdentifiedUserSetUploadJobRepository uploadJobRepository,
                                        LoginDetailsRepository loginDetailsRepository,
                                        UserProfileRepository userProfileRepository,
                                        RoleAssignmentRepository roleAssignmentRepository,
                                        UserLocationRepository userLocationRepository,
                                        @Autowired(required = false) UserDetailsRepository userDetailsRepository) {
        this.userSetRepository = userSetRepository;
        this.memberRepository = memberRepository;
        this.uploadJobRepository = uploadJobRepository;
        this.loginDetailsRepository = loginDetailsRepository;
        this.userProfileRepository = userProfileRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.userLocationRepository = userLocationRepository;
        this.userDetailsRepository = userDetailsRepository;
    }

    private UserSessionObject getValidatedAdmin(HttpServletRequest request) {
        UserSessionObject user = getUserSessionDetails(request);
        if (user == null) {
            throw new SPRuntimeError("Invalid session or unauthenticated request", HttpStatus.UNAUTHORIZED);
        }
        boolean isTenantAdmin = user.getRoles() != null && user.getRoles().stream()
                .anyMatch(r -> r.getRoleId() == ROLE_TENANT_ADMIN);
        if (!isTenantAdmin && !Boolean.TRUE.equals(user.getTopLevelAdmin())) {
            throw new SPRuntimeError("Only authorized Tenant Administrator or Admin can perform this operation", HttpStatus.FORBIDDEN);
        }
        return user;
    }

    private String getTenantId(UserSessionObject user) {
        return user.getTenantId() != null ? user.getTenantId() : "";
    }

    @Override
    @Transactional
    public UserSetResponse createUserSet(CreateUserSetRequest request, HttpServletRequest httpRequest) {
        UserSessionObject admin = getValidatedAdmin(httpRequest);
        String tenantId = getTenantId(admin);

        if (request == null || isEmpty(request.getName())) {
            throw new SPRuntimeError("Identified User Set name is mandatory", HttpStatus.BAD_REQUEST);
        }

        String name = request.getName().trim();
        if (userSetRepository.existsByNameAndTenantId(name, tenantId)) {
            throw new SPRuntimeError("An Identified User Set with this name already exists for this tenant", HttpStatus.BAD_REQUEST);
        }

        IdentifiedUserSet set = new IdentifiedUserSet();
        set.setName(name);
        set.setDescription(request.getDescription());
        set.setActive(true);
        set.setCrBy(admin.getUserID());
        set.setCDate(new Date());
        set.setTenantId(tenantId);

        IdentifiedUserSet saved = userSetRepository.save(set);
        log.info("Identified User Set created successfully id={} name={} tenantId={}", saved.getId(), saved.getName(), tenantId);

        return mapToUserSetResponse(saved, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSetResponse> getUserSets(HttpServletRequest httpRequest) {
        UserSessionObject admin = getValidatedAdmin(httpRequest);
        String tenantId = getTenantId(admin);

        List<IdentifiedUserSet> sets = userSetRepository.findByCrByAndTenantId(admin.getUserID(),tenantId, Sort.by(Sort.Direction.ASC, "name"));
        List<UserSetResponse> responseList = new ArrayList<>();

        for (IdentifiedUserSet set : sets) {
            long count = memberRepository.countByUserSetIdAndTenantIdAndActive(set.getId(), tenantId, true);
            responseList.add(mapToUserSetResponse(set, count));
        }

        return responseList;
    }

    @Override
    @Transactional(readOnly = true)
    public UserSetDetailResponse getUserSetDetail(Long id, HttpServletRequest httpRequest) {
        UserSessionObject admin = getValidatedAdmin(httpRequest);
        String tenantId = getTenantId(admin);

        if (id == null) {
            throw new SPRuntimeError("Identified User Set ID is mandatory", HttpStatus.BAD_REQUEST);
        }

        IdentifiedUserSet set = userSetRepository.findByCrByAndIdAndTenantId(admin.getUserID(),id, tenantId)
                .orElseThrow(() -> new SPRuntimeError("Identified User Set not found with ID: " + id, HttpStatus.BAD_REQUEST));

        List<MemberResponse> memberResponses = new ArrayList<>();

        long count = memberRepository.countByUserSetIdAndTenantIdAndActive(set.getId(), tenantId, true);

        UserSetResponse setResponse = mapToUserSetResponse(set, count);
        return new UserSetDetailResponse(setResponse, memberResponses);
    }

    @Override
    @Transactional
    public UserSetResponse updateUserSet(Long id, UpdateUserSetRequest request, HttpServletRequest httpRequest) {
        UserSessionObject admin = getValidatedAdmin(httpRequest);
        String tenantId = getTenantId(admin);

        if (id == null) {
            throw new SPRuntimeError("Identified User Set ID is mandatory", HttpStatus.BAD_REQUEST);
        }

        IdentifiedUserSet set = userSetRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new SPRuntimeError("Identified User Set not found with ID: " + id, HttpStatus.BAD_REQUEST));

        if (request != null && !isEmpty(request.getName())) {
            String name = request.getName().trim();
            if (userSetRepository.existsByNameAndTenantIdAndIdNot(name, tenantId, id)) {
                throw new SPRuntimeError("Another Identified User Set with this name already exists", HttpStatus.BAD_REQUEST);
            }
            set.setName(name);
        }

        if (request != null && request.getDescription() != null) {
            set.setDescription(request.getDescription());
        }

        if (request != null && request.getActive() != null) {
            set.setActive(request.getActive());
        }

        set.setUBy(admin.getUserID());
        set.setUDate(new Date());

        IdentifiedUserSet saved = userSetRepository.save(set);
        long count = memberRepository.countByUserSetIdAndTenantIdAndActive(saved.getId(), tenantId, true);

        return mapToUserSetResponse(saved, count);
    }

    @Override
    @Transactional
    public UserSetResponse changeUserSetStatus(Long id, HttpServletRequest httpRequest) {
        UserSessionObject admin = getValidatedAdmin(httpRequest);
        String tenantId = getTenantId(admin);

        if (id == null) {
            throw new SPRuntimeError("Identified User Set ID is mandatory", HttpStatus.BAD_REQUEST);
        }

        IdentifiedUserSet set = userSetRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new SPRuntimeError("Identified User Set not found with ID: " + id, HttpStatus.BAD_REQUEST));

        set.setActive(!Boolean.TRUE.equals(set.getActive()));
        set.setUBy(admin.getUserID());
        set.setUDate(new Date());

        IdentifiedUserSet saved = userSetRepository.save(set);
        long count = memberRepository.countByUserSetIdAndTenantIdAndActive(saved.getId(), tenantId, true);

        return mapToUserSetResponse(saved, count);
    }

    @Override
    @Transactional
    public MemberResponse addMember(Long userSetId, AddMemberRequest request, HttpServletRequest httpRequest) {
        UserSessionObject admin = getValidatedAdmin(httpRequest);
        String tenantId = getTenantId(admin);

        if (userSetId == null) {
            throw new SPRuntimeError("Identified User Set ID is mandatory", HttpStatus.BAD_REQUEST);
        }

        IdentifiedUserSet set = userSetRepository.findByIdAndTenantId(userSetId, tenantId)
                .orElseThrow(() -> new SPRuntimeError("Identified User Set not found", HttpStatus.BAD_REQUEST));

        if (!Boolean.TRUE.equals(set.getActive())) {
            throw new SPRuntimeError("Cannot add members to an inactive Identified User Set", HttpStatus.BAD_REQUEST);
        }

        LoginDetails userLogin = resolveUser(request, tenantId);
        UserProfile profile = userProfileRepository.findByUserId(userLogin.getUserId());

        validateUserEligibility(userLogin, profile, tenantId);

        Optional<IdentifiedUserSetMember> existingOpt = memberRepository.findByUserSetIdAndUserIdAndTenantId(userSetId, userLogin.getUserId(), tenantId);
        IdentifiedUserSetMember member;

        if (existingOpt.isPresent()) {
            member = existingOpt.get();
            if (Boolean.TRUE.equals(member.getActive())) {
                throw new SPRuntimeError("User is already an active member of this Identified User Set", HttpStatus.BAD_REQUEST);
            }
            member.setActive(true);
            member.setUBy(admin.getUserID());
            member.setUDate(new Date());
            if (request.getMembershipRole() != null) {
                member.setMembershipRole(request.getMembershipRole());
            }
        } else {
            member = new IdentifiedUserSetMember();
            member.setUserSetId(userSetId);
            member.setUserId(userLogin.getUserId());
            member.setSignNo(userLogin.getSignNo());
            member.setSsoProviderUserId(userLogin.getSsoProviderUserId());
            member.setMembershipRole(request.getMembershipRole() != null ? request.getMembershipRole() : "MEMBER");
            member.setActive(true);
            member.setCrBy(admin.getUserID());
            member.setCDate(new Date());
            member.setTenantId(tenantId);
        }

        if (request.getProviderUserId() != null && !request.getProviderUserId().trim().isEmpty()) {
            member.setSsoProviderUserId(request.getProviderUserId().trim());
            if (isEmpty(userLogin.getSsoProviderUserId())) {
                userLogin.setSsoProviderUserId(request.getProviderUserId().trim());
                loginDetailsRepository.save(userLogin);
            }
        }

        IdentifiedUserSetMember savedMember = memberRepository.save(member);

        // Assign ROLE_IDENTIFIED_USER (role_id 7) in role_assignment if not already assigned
        assignIdentifiedUserRoleIfNotPresent(userLogin.getUserId(), tenantId);

        MemberResponse resp = mapToMemberResponse(savedMember, profile);

        // Check if user belongs to a DIFFERENT Identified User Set and add warning if so
        List<IdentifiedUserSetMember> otherMemberships = memberRepository.findByUserIdAndTenantIdAndActiveAndUserSetIdNot(
                userLogin.getUserId(), tenantId, true, userSetId);

        if (!otherMemberships.isEmpty()) {
            Long otherSetId = otherMemberships.get(0).getUserSetId();
            Optional<IdentifiedUserSet> otherSet = userSetRepository.findById(otherSetId);
            String otherSetName = otherSet.map(IdentifiedUserSet::getName).orElse(String.valueOf(otherSetId));
            resp.setWarning("Warning: User already belongs to another Identified User Set: '" + otherSetName + "'");
            log.warn("User {} added to set {} with warning: already belongs to set {}", userLogin.getUserId(), userSetId, otherSetName);
        }

        log.info("Member added successfully userSetId={} userId={} signNo={}", userSetId, userLogin.getUserId(), userLogin.getSignNo());
        return resp;
    }

    @Override
    @Transactional
    public void removeMember(Long userSetId, Long userId, HttpServletRequest httpRequest) {
        UserSessionObject admin = getValidatedAdmin(httpRequest);
        String tenantId = getTenantId(admin);

        if (userSetId == null || userId == null) {
            throw new SPRuntimeError("User Set ID and User ID to remove are mandatory", HttpStatus.BAD_REQUEST);
        }

        IdentifiedUserSet set = userSetRepository.findByIdAndTenantId(userSetId, tenantId)
                .orElseThrow(() -> new SPRuntimeError("Identified User Set not found", HttpStatus.BAD_REQUEST));

        IdentifiedUserSetMember member = memberRepository.findByUserSetIdAndUserIdAndTenantIdAndActive(userSetId, userId, tenantId, true)
                .orElseThrow(() -> new SPRuntimeError("User is not an active member of this Identified User Set", HttpStatus.BAD_REQUEST));

        member.setActive(false);
        member.setUBy(admin.getUserID());
        member.setUDate(new Date());
        memberRepository.save(member);

        // If user is no longer in ANY active Identified User Set for this tenant, remove role 7
        boolean hasOtherActiveMemberships = memberRepository.existsByUserIdAndTenantIdAndActive(userId, tenantId, true);
        if (!hasOtherActiveMemberships) {
            roleAssignmentRepository.deleteByUserIdAndRoleId(userId, ApplicationConstants.ROLE_IDENTIFIED_USER);
            log.info("User {} is no longer in any active Identified User Set. Removed ROLE_IDENTIFIED_USER ({}) from role_assignment.",
                    userId, ApplicationConstants.ROLE_IDENTIFIED_USER);
        }

        log.info("Identified User Set membership deactivated for userId={} userSetId={} tenantId={}",
                userId, userSetId, tenantId);
    }

    public void assignIdentifiedUserRoleIfNotPresent(Long userId, String tenantId) {
        try {
            boolean hasRole = roleAssignmentRepository.existsByUserIdAndRoleId(userId, ApplicationConstants.ROLE_IDENTIFIED_USER);
            if (!hasRole) {
                RoleAssignment assignment = new RoleAssignment();
                assignment.setUser_role_id(generateRoleAssignmentId());
                assignment.setUserId(userId);
                assignment.setRoleId(ApplicationConstants.ROLE_IDENTIFIED_USER);
                assignment.setTenantId(tenantId);

                List<UserLocation> locations = userLocationRepository.findByUserId(userId);
                if (locations != null && !locations.isEmpty()) {
                    assignment.setUserLocId(locations.get(0).getUserLocId());
                }

                roleAssignmentRepository.save(assignment);
                log.info("Assigned ROLE_IDENTIFIED_USER ({}) to user {} in role_assignment", ApplicationConstants.ROLE_IDENTIFIED_USER, userId);
            }
        } catch (Exception e) {
            log.warn("Could not assign ROLE_IDENTIFIED_USER to user {}: {}", userId, e.getMessage());
        }
    }

    private Long generateRoleAssignmentId() {
        try {
            if (em != null) {
                Query query = em.createNativeQuery("SELECT nextval('schm_sp.role_assignment_user_role_id_seq')");
                return ((Number) query.getSingleResult()).longValue();
            }
        } catch (Exception e) {
            log.warn("Could not query sequence schm_sp.role_assignment_user_role_id_seq: {}", e.getMessage());
        }
        return System.currentTimeMillis();
    }

    @Override
    @Transactional
    public ExcelUploadJobResponse uploadMembersExcel(Long userSetId, MultipartFile file, HttpServletRequest httpRequest) {
        UserSessionObject admin = getValidatedAdmin(httpRequest);
        String tenantId = getTenantId(admin);

        if (userSetId == null) {
            throw new SPRuntimeError("Identified User Set ID is mandatory", HttpStatus.BAD_REQUEST);
        }

        IdentifiedUserSet set = userSetRepository.findByIdAndTenantId(userSetId, tenantId)
                .orElseThrow(() -> new SPRuntimeError("Identified User Set not found", HttpStatus.BAD_REQUEST));

        if (!Boolean.TRUE.equals(set.getActive())) {
            throw new SPRuntimeError("Cannot add members to an inactive Identified User Set", HttpStatus.BAD_REQUEST);
        }

        IdentifiedUserSetExcelUtil.validateFile(file);

        // Fetch admin email
        String adminEmail = null;
        UserProfile adminProfile = userProfileRepository.findByUserId(admin.getUserID());
        if (adminProfile != null) {
            adminEmail = adminProfile.getEmailId();
        }

        IdentifiedUserSetUploadJob job = new IdentifiedUserSetUploadJob();
        job.setJobType(JobType.UPLOAD);
        job.setUserSetId(userSetId);
        job.setFileName(file.getOriginalFilename());
        try {
            job.setFileData(file.getBytes());
        } catch (Exception e) {
            throw new SPRuntimeError("Failed to read uploaded file: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        job.setStatus(JobStatus.PENDING);
        job.setAdminEmail(adminEmail);
        job.setCrBy(admin.getUserID());
        job.setCDate(new Date());
        job.setTenantId(tenantId);

        IdentifiedUserSetUploadJob savedJob = uploadJobRepository.save(job);
        log.info("Identified User Set upload job queued with id={} userSetId={} adminEmail={}",
                savedJob.getId(), userSetId, adminEmail);

        return new ExcelUploadJobResponse(
                savedJob.getId(),
                JobStatus.PENDING,
                "Excel file uploaded successfully and queued for asynchronous processing. You will receive an email report once completed.",
                file.getOriginalFilename()
        );
    }

    @Override
    @Transactional
    public ExcelUploadJobResponse exportMembersAsync(Long userSetId, HttpServletRequest httpRequest) {
        UserSessionObject admin = getValidatedAdmin(httpRequest);
        String tenantId = getTenantId(admin);

        if (userSetId == null) {
            throw new SPRuntimeError("Identified User Set ID is mandatory", HttpStatus.BAD_REQUEST);
        }

        IdentifiedUserSet set = userSetRepository.findByIdAndTenantId(userSetId, tenantId)
                .orElseThrow(() -> new SPRuntimeError("Identified User Set not found with ID: " + userSetId, HttpStatus.BAD_REQUEST));

        String adminEmail = null;
        UserProfile adminProfile = userProfileRepository.findByUserId(admin.getUserID());
        if (adminProfile != null) {
            adminEmail = adminProfile.getEmailId();
        }

        IdentifiedUserSetUploadJob job = new IdentifiedUserSetUploadJob();
        job.setJobType(JobType.EXPORT);
        job.setUserSetId(userSetId);
        job.setFileName(set.getName() + "_members.xlsx");
        job.setStatus(JobStatus.PENDING);
        job.setAdminEmail(adminEmail);
        job.setCrBy(admin.getUserID());
        job.setCDate(new Date());
        job.setTenantId(tenantId);

        IdentifiedUserSetUploadJob savedJob = uploadJobRepository.save(job);
        log.info("Identified User Set export job queued with id={} userSetId={} adminEmail={}",
                savedJob.getId(), userSetId, adminEmail);

        return new ExcelUploadJobResponse(
                savedJob.getId(),
                JobStatus.PENDING,
                "Export request queued for asynchronous processing. You will receive an email with the members file attached once complete.",
                set.getName() + "_members.xlsx"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportMembersToExcel(Long userSetId, HttpServletRequest httpRequest) {
        UserSessionObject admin = getValidatedAdmin(httpRequest);
        String tenantId = getTenantId(admin);

        if (userSetId == null) {
            throw new SPRuntimeError("Identified User Set ID is mandatory", HttpStatus.BAD_REQUEST);
        }

        IdentifiedUserSet set = userSetRepository.findByIdAndTenantId(userSetId, tenantId)
                .orElseThrow(() -> new SPRuntimeError("Identified User Set not found with ID: " + userSetId, HttpStatus.BAD_REQUEST));

        List<IdentifiedUserSetMember> members = memberRepository.findByUserSetIdAndTenantIdAndActive(userSetId, tenantId, true);
        List<ExcelMemberExportRow> exportRows = new ArrayList<>();

        for (IdentifiedUserSetMember member : members) {
            UserProfile profile = userProfileRepository.findByUserId(member.getUserId());
            ExcelMemberExportRow row = new ExcelMemberExportRow();
            row.setUserId(member.getUserId());
            row.setUserName(profile != null ? profile.getUserName() : "");
            row.setEmail(profile != null ? profile.getEmailId() : "");
            row.setMobile(profile != null ? profile.getMobileNo() : "");
            row.setProviderId(member.getSsoProviderUserId());
            row.setRole(resolveUserRoles(member.getUserId()));
            row.setActive(member.getActive());
            row.setAddedOn(member.getCDate());
            exportRows.add(row);
        }

        return IdentifiedUserSetExcelUtil.generateMembersExportExcel(exportRows, set.getName());
    }

    private String resolveUserRoles(Long userId) {
        if (userDetailsRepository == null || userId == null) {
            return "";
        }
        try {
            List<Object[]> roles = userDetailsRepository.fetchUserRoles(userId.intValue());
            if (roles == null || roles.isEmpty()) {
                return "";
            }
            List<String> roleNames = new ArrayList<>();
            for (Object[] r : roles) {
                if (r != null && r.length > 1 && r[1] != null && !r[1].toString().trim().isEmpty()) {
                    roleNames.add(r[1].toString().trim());
                } else if (r != null && r.length > 0 && r[0] != null) {
                    roleNames.add("Role " + r[0].toString());
                }
            }
            return String.join(", ", roleNames);
        } catch (Exception e) {
            log.warn("Could not fetch roles from role_assignment for user {}: {}", userId, e.getMessage());
            return "";
        }
    }

    @Override
    public byte[] downloadTemplate() {
        return IdentifiedUserSetExcelUtil.generateTemplate();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobStatusDto> getUploadJobStatus(Long jobId, HttpServletRequest httpRequest) {
        UserSessionObject admin = getValidatedAdmin(httpRequest);
        String tenantId = getTenantId(admin);

        List<IdentifiedUserSetUploadJob> jobs;

        if (jobId == null) {
            jobs = uploadJobRepository.findByCrByAndTenantId(admin.getUserID(), tenantId);
        }
        else {
            IdentifiedUserSetUploadJob job =
                    uploadJobRepository.findByIdAndTenantId(jobId, tenantId)
                            .orElseThrow(() -> new SPRuntimeError(
                                    "Job not found with ID: " + jobId,
                                    HttpStatus.BAD_REQUEST));

            jobs = List.of(job);
        }

        return jobs.stream()
                .map(job -> {
                    JobStatusDto status = new JobStatusDto();
                    status.setStatus(job.getStatus().name());
                    status.setId(job.getId());
                    status.setCdate(job.getCDate());
                    status.setFileName(job.getFileName());
                    status.setJobType(job.getJobType().name());
                    return status;
                })
                .toList();
    }

    private LoginDetails resolveUser(AddMemberRequest request, String tenantId) {
        if (request == null) {
            throw new SPRuntimeError("Add member request cannot be null", HttpStatus.BAD_REQUEST);
        }

        if (request.getUserId() != null) {
            return loginDetailsRepository.findByUserId(request.getUserId())
                    .orElseThrow(() -> new SPRuntimeError("User not found with ID: " + request.getUserId(), HttpStatus.BAD_REQUEST));
        }

        if (!isEmpty(request.getEmail())) {
            UserProfile profile = userProfileRepository.findByEmailId(request.getEmail().trim());
            if (profile != null) {
                return loginDetailsRepository.findByUserId(profile.getUserId())
                        .orElseThrow(() -> new SPRuntimeError("User login details not found for email: " + request.getEmail(), HttpStatus.BAD_REQUEST));
            }
        }

        if (!isEmpty(request.getSignNo())) {
            return loginDetailsRepository.findBySignNo(request.getSignNo().trim())
                    .orElseThrow(() -> new SPRuntimeError("User not found with Sign No: " + request.getSignNo(), HttpStatus.BAD_REQUEST));
        }

        if (!isEmpty(request.getMobileNo())) {
            UserProfile profile = userProfileRepository.findByMobileNo(request.getMobileNo().trim());
            if (profile != null) {
                return loginDetailsRepository.findByUserId(profile.getUserId())
                        .orElseThrow(() -> new SPRuntimeError("User login details not found for mobile: " + request.getMobileNo(), HttpStatus.BAD_REQUEST));
            }
        }

        if (!isEmpty(request.getProviderUserId())) {
            return loginDetailsRepository.findBySsoProviderUserId(request.getProviderUserId().trim())
                    .orElseThrow(() -> new SPRuntimeError("User not found with provider user ID: " + request.getProviderUserId(), HttpStatus.BAD_REQUEST));
        }

        throw new SPRuntimeError("User does not exist in ServicePlus", HttpStatus.BAD_REQUEST);
    }

    private void validateUserEligibility(LoginDetails login, UserProfile profile, String adminTenantId) {
        if (login.getSpdiStatus() == null || !Objects.equals(login.getSpdiStatus(), UserStatus.ACTIVE.getCode())) {
            throw new SPRuntimeError("User account is inactive or suspended", HttpStatus.BAD_REQUEST);
        }
        if (!isEmpty(adminTenantId) && !isEmpty(login.getTenantId()) && !adminTenantId.equalsIgnoreCase(login.getTenantId())) {
            throw new SPRuntimeError("Cannot add user belonging to unauthorized tenant: " + login.getTenantId(), HttpStatus.BAD_REQUEST);
        }
    }

    private UserSetResponse mapToUserSetResponse(IdentifiedUserSet set, Long memberCount) {
        UserSetResponse resp = new UserSetResponse();
        resp.setId(set.getId());
        resp.setName(set.getName());
        resp.setDescription(set.getDescription());
        resp.setActive(set.getActive());
        resp.setMemberCount(memberCount);
        return resp;
    }

    private MemberResponse mapToMemberResponse(IdentifiedUserSetMember member) {
        UserProfile profile = userProfileRepository.findByUserId(member.getUserId());
        return mapToMemberResponse(member, profile);
    }

    private MemberResponse mapToMemberResponse(IdentifiedUserSetMember member, UserProfile profile) {
        MemberResponse resp = new MemberResponse();
        resp.setId(member.getId());
        resp.setUserSetId(member.getUserSetId());
        resp.setUserId(member.getUserId());
        resp.setSignNo(member.getSignNo());
        resp.setSsoProviderUserId(member.getSsoProviderUserId());
        resp.setMembershipRole(member.getMembershipRole());
        resp.setActive(member.getActive());
        resp.setCreatedOn(member.getCDate());
        resp.setUpdatedOn(member.getUDate());

        if (profile != null) {
            resp.setUserName(profile.getUserName());
            resp.setEmailId(profile.getEmailId());
            resp.setMobileNo(profile.getMobileNo());
        }

        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getUserSetIdsForUser(Long userId, String tenantId, HttpServletRequest httpRequest) {

        UserSessionObject session = getUserSessionDetails(httpRequest);
        boolean internalCall = isServicePlusInternalCall(httpRequest);

        if (session == null && !internalCall) {
            throw new SPRuntimeError("Unauthorized access to identified user set IDs", HttpStatus.FORBIDDEN);
        }

        List<Long> setIds = memberRepository.findActiveUserSetIdsByUserId(userId);


        if (setIds == null || setIds.isEmpty()) {
            return Collections.emptyList();
        }
        return setIds.stream().map(Long::intValue).collect(Collectors.toList());
    }
}
