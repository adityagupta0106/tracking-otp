package com.serviceplus.UserManagement.identifieduserset.entity;

import com.serviceplus.UserManagement.Entity.BaseTenantEntity;
import com.serviceplus.UserManagement.identifieduserset.enums.JobStatus;
import com.serviceplus.UserManagement.identifieduserset.enums.JobType;
import com.serviceplus.UserManagement.util.ApplicationConstants;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "identified_user_set_upload_jobs", schema = ApplicationConstants.SP_SCHEMA_NAME)
public class IdentifiedUserSetUploadJob extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_set_id", nullable = false)
    private Long userSetId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARBINARY)
    @Column(name = "file_data", columnDefinition = "bytea")
    private byte[] fileData;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private JobType jobType = JobType.UPLOAD;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "admin_email")
    private String adminEmail;

    @Column(name = "total_records")
    private Integer totalRecords = 0;

    @Column(name = "added_count")
    private Integer addedCount = 0;

    @Column(name = "failed_count")
    private Integer failedCount = 0;

    @Column(name = "warning_count")
    private Integer warningCount = 0;

    @Column(name = "result_file_path")
    private String resultFilePath;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "cr_by", nullable = false)
    private Long crBy;

    @Column(name = "u_by")
    private Long uBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "c_date")
    private Date cDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "u_date")
    private Date uDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserSetId() {
        return userSetId;
    }

    public void setUserSetId(Long userSetId) {
        this.userSetId = userSetId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = JobStatus.fromString(status);
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Integer getAddedCount() {
        return addedCount;
    }

    public void setAddedCount(Integer addedCount) {
        this.addedCount = addedCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public Integer getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(Integer warningCount) {
        this.warningCount = warningCount;
    }

    public String getResultFilePath() {
        return resultFilePath;
    }

    public void setResultFilePath(String resultFilePath) {
        this.resultFilePath = resultFilePath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getCrBy() {
        return crBy;
    }

    public void setCrBy(Long crBy) {
        this.crBy = crBy;
    }

    public Long getUBy() {
        return uBy;
    }

    public void setUBy(Long uBy) {
        this.uBy = uBy;
    }

    public Date getCDate() {
        return cDate;
    }

    public void setCDate(Date cDate) {
        this.cDate = cDate;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = JobType.fromString(jobType);
    }

    public Date getUDate() {
        return uDate;
    }

    public void setUDate(Date uDate) {
        this.uDate = uDate;
    }
}
