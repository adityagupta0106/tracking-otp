package com.serviceplus.UserManagement.identifieduserset.scheduler;

import com.serviceplus.UserManagement.Entity.LoginDetails;
import com.serviceplus.UserManagement.Entity.RoleAssignment;
import com.serviceplus.UserManagement.Entity.UserLocation;
import com.serviceplus.UserManagement.Entity.UserProfile;
import com.serviceplus.UserManagement.Enum.UserStatus;
import com.serviceplus.UserManagement.Kafka.NotificationKafkaService;
import com.serviceplus.UserManagement.Repository.LoginDetailsRepository;
import com.serviceplus.UserManagement.Repository.RoleAssignmentRepository;
import com.serviceplus.UserManagement.Repository.UserDetailsRepository;
import com.serviceplus.UserManagement.Repository.UserLocationRepository;
import com.serviceplus.UserManagement.Repository.UserProfileRepository;
import com.serviceplus.UserManagement.util.ApplicationConstants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSet;
import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSetMember;
import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSetUploadJob;
import com.serviceplus.UserManagement.identifieduserset.enums.ExcelRowStatus;
import com.serviceplus.UserManagement.identifieduserset.enums.JobStatus;
import com.serviceplus.UserManagement.identifieduserset.enums.JobType;
import com.serviceplus.UserManagement.identifieduserset.repository.IdentifiedUserSetMemberRepository;
import com.serviceplus.UserManagement.identifieduserset.repository.IdentifiedUserSetRepository;
import com.serviceplus.UserManagement.identifieduserset.repository.IdentifiedUserSetUploadJobRepository;
import com.serviceplus.UserManagement.identifieduserset.util.IdentifiedUserSetExcelUtil;
import com.serviceplus.UserManagement.identifieduserset.util.IdentifiedUserSetExcelUtil.ExcelMemberExportRow;
import com.serviceplus.UserManagement.identifieduserset.util.IdentifiedUserSetExcelUtil.ExcelRowResult;
import com.serviceplus.UserManagement.identifieduserset.util.IdentifiedUserSetExcelUtil.ExcelUserRecord;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.serviceplus.UserManagement.util.Utility.isEmpty;
import static com.serviceplus.UserManagement.util.Utility.isValidEmail;

@Component
public class IdentifiedUserSetUploadScheduler {

    private static final Logger log = LogManager.getLogger(IdentifiedUserSetUploadScheduler.class);

    private final IdentifiedUserSetUploadJobRepository uploadJobRepository;
    private final IdentifiedUserSetRepository userSetRepository;
    private final IdentifiedUserSetMemberRepository memberRepository;
    private final LoginDetailsRepository loginDetailsRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final UserLocationRepository userLocationRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final NotificationKafkaService notificationKafkaService;

    @PersistenceContext
    private EntityManager em;

    // In-memory safety tracker: prevents multiple worker threads in this node from picking the same job
    private final Set<Long> inProgressJobIds = ConcurrentHashMap.newKeySet();

    // Dedicated worker thread pool with custom naming and CallerRuns backpressure
    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            4,
            8,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500),
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "ident-userset-worker-" + count.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Value("${identified.userset.upload.email.subject:Identified User Set Member Upload Report - Job #%d}")
    private String uploadEmailSubject = "Identified User Set Member Upload Report - Job #%d";

    @Value("${identified.userset.upload.email.content:<html><body><p>Hello,</p><p>The bulk upload processing for Identified User Set (Job #%d) has completed.</p><p>Total: %d, Added: %d, Warnings: %d, Failed: %d</p></body></html>}")
    private String uploadEmailContent = "<html><body><p>Hello,</p><p>The bulk upload processing for Identified User Set (Job #%d) has completed.</p><p>Total: %d, Added: %d, Warnings: %d, Failed: %d</p></body></html>";

    @Value("${identified.userset.export.email.subject:Identified User Set Members Export - %s}")
    private String exportEmailSubject = "Identified User Set Members Export - %s";

    @Value("${identified.userset.export.email.content:<html><body><p>Hello,</p><p>The export of members for Identified User Set %s (Job #%d) is complete.</p><p>Total members: %d</p></body></html>}")
    private String exportEmailContent = "<html><body><p>Hello,</p><p>The export of members for Identified User Set %s (Job #%d) is complete.</p><p>Total members: %d</p></body></html>";

    @Autowired
    public IdentifiedUserSetUploadScheduler(IdentifiedUserSetUploadJobRepository uploadJobRepository,
                                           IdentifiedUserSetRepository userSetRepository,
                                           IdentifiedUserSetMemberRepository memberRepository,
                                           LoginDetailsRepository loginDetailsRepository,
                                           UserProfileRepository userProfileRepository,
                                           @Autowired(required = false) RoleAssignmentRepository roleAssignmentRepository,
                                           @Autowired(required = false) UserLocationRepository userLocationRepository,
                                           @Autowired(required = false) UserDetailsRepository userDetailsRepository,
                                           @Autowired(required = false) NotificationKafkaService notificationKafkaService) {
        this.uploadJobRepository = uploadJobRepository;
        this.userSetRepository = userSetRepository;
        this.memberRepository = memberRepository;
        this.loginDetailsRepository = loginDetailsRepository;
        this.userProfileRepository = userProfileRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.userLocationRepository = userLocationRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.notificationKafkaService = notificationKafkaService;
    }

    public IdentifiedUserSetUploadScheduler(IdentifiedUserSetUploadJobRepository uploadJobRepository,
                                           IdentifiedUserSetRepository userSetRepository,
                                           IdentifiedUserSetMemberRepository memberRepository,
                                           LoginDetailsRepository loginDetailsRepository,
                                           UserProfileRepository userProfileRepository,
                                           NotificationKafkaService notificationKafkaService) {
        this(uploadJobRepository, userSetRepository, memberRepository, loginDetailsRepository, userProfileRepository, null, null, null, notificationKafkaService);
    }

    /**
     * Periodic scheduler running every 10 minutes (cron = 0 * / 10 * * * *)
     */
    @Scheduled(cron = "${identified.userset.scheduler.cron:0 */2 * * * *}")
    public void processPendingUploadJobs() {
        try {
            List<IdentifiedUserSetUploadJob> pendingJobs = uploadJobRepository.findByStatusOrderByCDateAsc(JobStatus.PENDING);
            if (pendingJobs == null || pendingJobs.isEmpty()) {
                return;
            }

            log.info("Scheduler discovered {} pending Identified User Set job(s). Dispatching to thread pool.", pendingJobs.size());

            for (IdentifiedUserSetUploadJob job : pendingJobs) {
                // Thread safety layer 1: In-memory concurrency tracker
                if (!inProgressJobIds.add(job.getId())) {
                    log.info("Job #{} is already submitted to or running in thread pool. Skipping duplicate dispatch.", job.getId());
                    continue;
                }

                threadPool.submit(() -> {
                    try {
                        // Thread safety layer 2: Database atomic claim (prevents multiple nodes/threads picking same job)
                        int claimed = uploadJobRepository.claimJob(job.getId(), JobStatus.PROCESSING, JobStatus.PENDING, new Date());
                        if (claimed == 0) {
                            log.info("Job #{} was already claimed by another worker/node. Skipping execution.", job.getId());
                            return;
                        }

                        job.setStatus(JobStatus.PROCESSING);
                        processSingleJob(job);
                    } catch (Exception e) {
                        log.error("Unhandled error executing worker task for job #{}", job.getId(), e);
                    } finally {
                        inProgressJobIds.remove(job.getId());
                    }
                });
            }
        } catch (Exception e) {
            log.warn("Identified User Set scheduler polling skipped: {}", e.getMessage());
        }
    }

    public void processSingleJob(IdentifiedUserSetUploadJob job) {
        if (JobType.EXPORT.equals(job.getJobType())) {
            processExportJob(job);
        } else {
            processUploadJob(job);
        }
    }

    private void processExportJob(IdentifiedUserSetUploadJob job) {
        log.info("Processing Identified User Set export job id={} userSetId={} tenantId={}",
                job.getId(), job.getUserSetId(), job.getTenantId());

        job.setStatus(JobStatus.PROCESSING);
        job.setUDate(new Date());
        uploadJobRepository.saveAndFlush(job);

        try {
            Optional<IdentifiedUserSet> setOpt = userSetRepository.findById(job.getUserSetId());
            if (setOpt.isEmpty()) {
                job.setStatus(JobStatus.FAILED);
                job.setErrorMessage("Identified User Set not found with ID: " + job.getUserSetId());
                job.setUDate(new Date());
                uploadJobRepository.save(job);
                return;
            }

            IdentifiedUserSet set = setOpt.get();
            List<IdentifiedUserSetMember> members = memberRepository.findByUserSetIdAndTenantIdAndActive(job.getUserSetId(), job.getTenantId(), true);
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

            byte[] exportBytes = IdentifiedUserSetExcelUtil.generateMembersExportExcel(exportRows, set.getName());
            File tempExportFile = File.createTempFile("export_set_" + job.getUserSetId() + "_", ".xlsx");
            Files.write(tempExportFile.toPath(), exportBytes);

            job.setStatus(JobStatus.COMPLETED);
            job.setTotalRecords(members.size());
            job.setAddedCount(members.size());
            job.setResultFilePath(tempExportFile.getAbsolutePath());
            job.setFileData(null); // Clear from DB to optimize storage
            job.setUDate(new Date());
            job.setUBy(job.getCrBy());
            uploadJobRepository.save(job);

            log.info("Export Job #{} completed: total members exported={}", job.getId(), members.size());

            // Send notification email
            sendExportReportEmail(job, set.getName(), tempExportFile, members.size());

        } catch (Exception e) {
            log.error("Fatal failure processing export job #{}", job.getId(), e);
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage("Fatal error: " + e.getMessage());
            job.setFileData(null);
            job.setUDate(new Date());
            uploadJobRepository.save(job);
        }
    }

    private void processUploadJob(IdentifiedUserSetUploadJob job) {
        log.info("Processing Identified User Set upload job id={} userSetId={} tenantId={}",
                job.getId(), job.getUserSetId(), job.getTenantId());

        job.setStatus(JobStatus.PROCESSING);
        job.setUDate(new Date());
        uploadJobRepository.saveAndFlush(job);

        try {
            Optional<IdentifiedUserSet> setOpt = userSetRepository.findById(job.getUserSetId());
            if (setOpt.isEmpty() || !Boolean.TRUE.equals(setOpt.get().getActive())) {
                job.setStatus(JobStatus.FAILED);
                job.setErrorMessage("Identified User Set not found or is inactive");
                job.setFileData(null);
                job.setUDate(new Date());
                uploadJobRepository.save(job);
                return;
            }

            if (job.getFileData() == null || job.getFileData().length == 0) {
                job.setStatus(JobStatus.FAILED);
                job.setErrorMessage("File data is missing or empty");
                job.setFileData(null);
                job.setUDate(new Date());
                uploadJobRepository.save(job);
                return;
            }

            List<ExcelUserRecord> records = IdentifiedUserSetExcelUtil.parseExcel(new ByteArrayInputStream(job.getFileData()));
            List<ExcelRowResult> results = new ArrayList<>();
            Set<String> seenEmails = new HashSet<>();

            int addedCount = 0;
            int warningCount = 0;
            int failedCount = 0;

            for (ExcelUserRecord rec : records) {
                int rowNum = rec.getRowNumber();
                String email = rec.getEmail();

                if (isEmpty(email)) {
                    results.add(new ExcelRowResult(rowNum, email, rec.getSignNo(), ExcelRowStatus.FAILED, "Email ID is mandatory"));
                    failedCount++;
                    continue;
                }

                if (!isValidEmail(email)) {
                    results.add(new ExcelRowResult(rowNum, email, rec.getSignNo(), ExcelRowStatus.FAILED, "Invalid email format"));
                    failedCount++;
                    continue;
                }

                String normalizedEmail = email.trim().toLowerCase();
                if (seenEmails.contains(normalizedEmail)) {
                    results.add(new ExcelRowResult(rowNum, email, rec.getSignNo(), ExcelRowStatus.FAILED, "Duplicate record within uploaded file"));
                    failedCount++;
                    continue;
                }
                seenEmails.add(normalizedEmail);

                try {
                    // Resolve user directly from DB
                    LoginDetails userLogin = null;
                    UserProfile profile = userProfileRepository.findByEmailId(normalizedEmail);
                    if (profile != null) {
                        userLogin = loginDetailsRepository.findByUserId(profile.getUserId()).orElse(null);
                    }

                    if (userLogin == null && !isEmpty(rec.getSignNo())) {
                        userLogin = loginDetailsRepository.findBySignNo(rec.getSignNo().trim()).orElse(null);
                        if (userLogin != null && profile == null) {
                            profile = userProfileRepository.findByUserId(userLogin.getUserId());
                        }
                    }

                    if (userLogin == null && !isEmpty(rec.getMobile())) {
                        profile = userProfileRepository.findByMobileNo(rec.getMobile().trim());
                        if (profile != null) {
                            userLogin = loginDetailsRepository.findByUserId(profile.getUserId()).orElse(null);
                        }
                    }

                    if (userLogin == null && !isEmpty(rec.getProviderUserId())) {
                        userLogin = loginDetailsRepository.findBySsoProviderUserId(rec.getProviderUserId().trim()).orElse(null);
                        if (userLogin != null && profile == null) {
                            profile = userProfileRepository.findByUserId(userLogin.getUserId());
                        }
                    }

                    if (userLogin == null) {
                        results.add(new ExcelRowResult(rowNum, email, rec.getSignNo(), ExcelRowStatus.FAILED, "User does not exist in ServicePlus"));
                        failedCount++;
                        continue;
                    }

                    if (userLogin.getSpdiStatus() == null || userLogin.getSpdiStatus() != UserStatus.ACTIVE.getCode()) {
                        results.add(new ExcelRowResult(rowNum, email, userLogin.getSignNo(), ExcelRowStatus.FAILED, "User account is inactive or suspended"));
                        failedCount++;
                        continue;
                    }

                    // Check if already in THIS user set
                    Optional<IdentifiedUserSetMember> existingInSet = memberRepository.findByUserSetIdAndUserIdAndTenantId(job.getUserSetId(), userLogin.getUserId(), job.getTenantId());
                    IdentifiedUserSetMember member;
                    if (existingInSet.isPresent()) {
                        member = existingInSet.get();
                        if (Boolean.TRUE.equals(member.getActive())) {
                            results.add(new ExcelRowResult(rowNum, email, userLogin.getSignNo(), ExcelRowStatus.FAILED, "User is already an active member of this Identified User Set"));
                            failedCount++;
                            continue;
                        }
                        member.setActive(true);
                        member.setUBy(job.getCrBy());
                        member.setUDate(new Date());
                        if (rec.getMembershipRole() != null) {
                            member.setMembershipRole(rec.getMembershipRole().trim());
                        }
                    } else {
                        member = new IdentifiedUserSetMember();
                        member.setUserSetId(job.getUserSetId());
                        member.setUserId(userLogin.getUserId());
                        member.setSignNo(userLogin.getSignNo());
                        member.setSsoProviderUserId(userLogin.getSsoProviderUserId());
                        member.setMembershipRole(rec.getMembershipRole() != null ? rec.getMembershipRole().trim() : "MEMBER");
                        member.setActive(true);
                        member.setCrBy(job.getCrBy());
                        member.setCDate(new Date());
                        member.setTenantId(job.getTenantId());
                    }

                    if (rec.getProviderUserId() != null && !rec.getProviderUserId().trim().isEmpty()) {
                        member.setSsoProviderUserId(rec.getProviderUserId().trim());
                        if (isEmpty(userLogin.getSsoProviderUserId())) {
                            userLogin.setSsoProviderUserId(rec.getProviderUserId().trim());
                            loginDetailsRepository.save(userLogin);
                        }
                    }

                    memberRepository.save(member);

                    // Assign ROLE_IDENTIFIED_USER (role_id 7) in role_assignment if not already assigned
                    assignIdentifiedUserRoleIfNotPresent(userLogin.getUserId(), job.getTenantId());

                    // Check if member already exists in ANOTHER Identified User Set
                    List<IdentifiedUserSetMember> otherMemberships = memberRepository.findByUserIdAndTenantIdAndActiveAndUserSetIdNot(
                            userLogin.getUserId(), job.getTenantId(), true, job.getUserSetId());

                    if (!otherMemberships.isEmpty()) {
                        Long otherSetId = otherMemberships.getFirst().getUserSetId();
                        Optional<IdentifiedUserSet> otherSetOpt = userSetRepository.findById(otherSetId);
                        String otherSetName = otherSetOpt.map(IdentifiedUserSet::getName).orElse(String.valueOf(otherSetId));
                        results.add(new ExcelRowResult(rowNum, email, userLogin.getSignNo(), ExcelRowStatus.WARNING,
                                "User added successfully. Warning: User already belongs to another Identified User Set: '" + otherSetName + "'"));
                        warningCount++;
                    } else {
                        results.add(new ExcelRowResult(rowNum, email, userLogin.getSignNo(), ExcelRowStatus.SUCCESS, "User added successfully"));
                        addedCount++;
                    }

                } catch (Exception e) {
                    log.error("Error processing row {}", rowNum, e);
                    results.add(new ExcelRowResult(rowNum, email, rec.getSignNo(), ExcelRowStatus.FAILED, "Error: " + e.getMessage()));
                    failedCount++;
                }
            }

            // Generate result Excel
            byte[] resultBytes = IdentifiedUserSetExcelUtil.generateResultExcel(results);
            File tempResultFile = File.createTempFile("result_job_" + job.getId() + "_", ".xlsx");
            Files.write(tempResultFile.toPath(), resultBytes);

            job.setStatus(JobStatus.COMPLETED);
            job.setTotalRecords(results.size());
            job.setAddedCount(addedCount);
            job.setWarningCount(warningCount);
            job.setFailedCount(failedCount);
            job.setResultFilePath(tempResultFile.getAbsolutePath());
            job.setFileData(null); // Clear Excel content from DB to avoid unnecessary space utilization
            job.setUDate(new Date());
            job.setUBy(job.getCrBy());
            uploadJobRepository.save(job);

            log.info("Job #{} completed: total={}, added={}, warnings={}, failed={}. Excel cleared from DB.",
                    job.getId(), results.size(), addedCount, warningCount, failedCount);

            // Send notification email using Alerts.properties templates
            sendUploadReportEmail(job, tempResultFile, results.size(), addedCount, warningCount, failedCount);

        } catch (Exception e) {
            log.error("Fatal failure processing upload job #{}", job.getId(), e);
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage("Fatal error: " + e.getMessage());
            job.setFileData(null); // Clear from DB on error
            job.setUDate(new Date());
            uploadJobRepository.save(job);
        }
    }

    private void sendUploadReportEmail(IdentifiedUserSetUploadJob job, File resultFile, int total, int added, int warnings, int failed) {
        if (notificationKafkaService == null || isEmpty(job.getAdminEmail())) {
            log.info("Kafka notification skipped for job #{}: adminEmail is empty or Kafka service unavailable", job.getId());
            return;
        }

        try {
            String subject = String.format(uploadEmailSubject, job.getId());
            String content = String.format(uploadEmailContent, job.getId(), total, added, warnings, failed);

            notificationKafkaService.sendNotification(
                    null,
                    job.getTenantId(),
                    job.getAdminEmail(),
                    subject,
                    "",
                    resultFile.getAbsolutePath(),
                    "xlsx",
                    content,
                    0,
                    0,
                    false,
                    true,
                    0,
                    0
            );
            log.info("Sent upload report email to {} for job #{}", job.getAdminEmail(), job.getId());
        } catch (Exception e) {
            log.error("Failed to send upload result email for job #{}", job.getId(), e);
            throw e;
        }
    }

    private void sendExportReportEmail(IdentifiedUserSetUploadJob job, String userSetName, File resultFile, int total) {
        if (notificationKafkaService == null || isEmpty(job.getAdminEmail())) {
            log.info("Kafka notification skipped for export job #{}: adminEmail is empty or Kafka service unavailable", job.getId());
            return;
        }

        try {
            String subject = String.format(exportEmailSubject, userSetName);
            String content = String.format(exportEmailContent, userSetName, job.getId(), total);

            notificationKafkaService.sendNotification(
                    null,
                    job.getTenantId(),
                    job.getAdminEmail(),
                    subject,
                    "",
                    resultFile.getAbsolutePath(),
                    "xlsx",
                    content,
                    0,
                    0,
                    false,
                    true,
                    0,
                    0
            );
            log.info("Sent export report email to {} for job #{}", job.getAdminEmail(), job.getId());
        } catch (Exception e) {
            log.error("Failed to send export result email for job #{}", job.getId(), e);
        }
    }

    public void assignIdentifiedUserRoleIfNotPresent(Long userId, String tenantId) {
        if (roleAssignmentRepository == null) {
            return;
        }
        try {
            boolean hasRole = roleAssignmentRepository.existsByUserIdAndRoleId(userId, ApplicationConstants.ROLE_IDENTIFIED_USER);
            if (!hasRole) {
                RoleAssignment assignment = new RoleAssignment();
                assignment.setUser_role_id(generateRoleAssignmentId());
                assignment.setUserId(userId);
                assignment.setRoleId(ApplicationConstants.ROLE_IDENTIFIED_USER);
                assignment.setTenantId(tenantId);

                if (userLocationRepository != null) {
                    List<UserLocation> locations = userLocationRepository.findByUserId(userId);
                    if (locations != null && !locations.isEmpty()) {
                        assignment.setUserLocId(locations.get(0).getUserLocId());
                    }
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

    @PreDestroy
    public void destroy() {
        log.info("Shutting down Identified User Set scheduler worker thread pool...");
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
