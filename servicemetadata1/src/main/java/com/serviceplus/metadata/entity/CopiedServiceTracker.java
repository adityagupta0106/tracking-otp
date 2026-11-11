package com.serviceplus.metadata.entity;

import com.serviceplus.metadata.utility.ApplicationConstants;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "service_copy_tracker", schema = ApplicationConstants.SP_SCHEMA_NAME)
public class CopiedServiceTracker implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_id", nullable = false)
    private Integer serviceId;

    @Column(name = "base_service_id", nullable = false)
    private Integer baseServiceId;

    @Column(name = "source_service_id")
    private Integer sourceServiceId;

    @Column(name = "source_tenant_id")
    private String sourceTenantId;

    @Column(name = "source_department_id")
    private Long sourceDepartmentId;

    @Column(name = "target_tenant_id", nullable = false)
    private String targetTenantId;

    @Column(name = "target_department_id", nullable = false)
    private Long targetDepartmentId;

    @Column(name = "is_cross_department", nullable = false)
    private Boolean isCrossDepartment;

    @Column(name = "is_global", nullable = false)
    private Boolean isGlobal;

    @Column(name = "central_repo_id")
    private String centralRepoId;

    // PENDING_VERIFICATION, VERIFIED
    @Column(name = "verification_status")
    private String verificationStatus;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    public CopiedServiceTracker() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getBaseServiceId() {
        return baseServiceId;
    }

    public void setBaseServiceId(Integer baseServiceId) {
        this.baseServiceId = baseServiceId;
    }

    public Integer getSourceServiceId() {
        return sourceServiceId;
    }

    public void setSourceServiceId(Integer sourceServiceId) {
        this.sourceServiceId = sourceServiceId;
    }

    public String getSourceTenantId() {
        return sourceTenantId;
    }

    public void setSourceTenantId(String sourceTenantId) {
        this.sourceTenantId = sourceTenantId;
    }

    public Long getSourceDepartmentId() {
        return sourceDepartmentId;
    }

    public void setSourceDepartmentId(Long sourceDepartmentId) {
        this.sourceDepartmentId = sourceDepartmentId;
    }

    public String getTargetTenantId() {
        return targetTenantId;
    }

    public void setTargetTenantId(String targetTenantId) {
        this.targetTenantId = targetTenantId;
    }

    public Long getTargetDepartmentId() {
        return targetDepartmentId;
    }

    public void setTargetDepartmentId(Long targetDepartmentId) {
        this.targetDepartmentId = targetDepartmentId;
    }

    public Boolean getCrossDepartment() {
        return isCrossDepartment;
    }

    public void setCrossDepartment(Boolean crossDepartment) {
        isCrossDepartment = crossDepartment;
    }

    public Boolean getGlobal() {
        return isGlobal;
    }

    public void setGlobal(Boolean global) {
        isGlobal = global;
    }

    public String getCentralRepoId() {
        return centralRepoId;
    }

    public void setCentralRepoId(String centralRepoId) {
        this.centralRepoId = centralRepoId;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
