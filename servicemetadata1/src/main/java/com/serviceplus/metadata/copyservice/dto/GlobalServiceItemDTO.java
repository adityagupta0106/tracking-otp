package com.serviceplus.metadata.copyservice.dto;

import java.io.Serializable;
import java.util.Date;

public class GlobalServiceItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private Integer serviceId;
    private String serviceName;
    private String serviceAbbreviation;
    private String sourceTenantName;
    private String sourceDepartmentName;
    private Integer versionNo;
    private Integer minorVersionNo;
    private Long submittedApplicationsCount;
    private String status;
    private Integer slaDays;
    private Date createdAt;

    public GlobalServiceItemDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceAbbreviation() {
        return serviceAbbreviation;
    }

    public void setServiceAbbreviation(String serviceAbbreviation) {
        this.serviceAbbreviation = serviceAbbreviation;
    }

    public String getSourceTenantName() {
        return sourceTenantName;
    }

    public void setSourceTenantName(String sourceTenantName) {
        this.sourceTenantName = sourceTenantName;
    }

    public String getSourceDepartmentName() {
        return sourceDepartmentName;
    }

    public void setSourceDepartmentName(String sourceDepartmentName) {
        this.sourceDepartmentName = sourceDepartmentName;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public Integer getMinorVersionNo() {
        return minorVersionNo;
    }

    public void setMinorVersionNo(Integer minorVersionNo) {
        this.minorVersionNo = minorVersionNo;
    }

    public Long getSubmittedApplicationsCount() {
        return submittedApplicationsCount;
    }

    public void setSubmittedApplicationsCount(Long submittedApplicationsCount) {
        this.submittedApplicationsCount = submittedApplicationsCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSlaDays() {
        return slaDays;
    }

    public void setSlaDays(Integer slaDays) {
        this.slaDays = slaDays;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
