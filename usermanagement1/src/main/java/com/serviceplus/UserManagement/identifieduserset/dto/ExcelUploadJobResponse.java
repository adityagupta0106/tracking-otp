package com.serviceplus.UserManagement.identifieduserset.dto;

import com.serviceplus.UserManagement.identifieduserset.enums.JobStatus;
import com.serviceplus.UserManagement.identifieduserset.enums.JobType;

public class ExcelUploadJobResponse {

    private Long jobId;
    private JobStatus status;
    private JobType jobType;
    private String message;
    private String fileName;

    public ExcelUploadJobResponse() {}

    public ExcelUploadJobResponse(Long jobId, JobStatus status, String message, String fileName) {
        this.jobId = jobId;
        this.status = status;
        this.message = message;
        this.fileName = fileName;
    }

    public ExcelUploadJobResponse(Long jobId, String status, String message, String fileName) {
        this(jobId, JobStatus.fromString(status), message, fileName);
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
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

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = JobType.fromString(jobType);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
