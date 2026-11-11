package com.serviceplus.metadata.copyservice.dto;

import java.io.Serializable;

public class ServicePullInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer serviceId;
    private String serviceName;
    private String serviceAbbreviation;
    private String sourceTenantId;
    private String sourceTenantName;
    private Long sourceDepartmentId;
    private String sourceDepartmentName;
    private Integer versionNo;
    private Integer minorVersionNo;
    private Integer serviceStatus;
    private Long submittedApplicationsCount;
    private Integer slaDays;

    public ServicePullInfoDTO() {
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

    public String getSourceTenantId() {
        return sourceTenantId;
    }

    public void setSourceTenantId(String sourceTenantId) {
        this.sourceTenantId = sourceTenantId;
    }

    public String getSourceTenantName() {
        return sourceTenantName;
    }

    public void setSourceTenantName(String sourceTenantName) {
        this.sourceTenantName = sourceTenantName;
    }

    public Long getSourceDepartmentId() {
        return sourceDepartmentId;
    }

    public void setSourceDepartmentId(Long sourceDepartmentId) {
        this.sourceDepartmentId = sourceDepartmentId;
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

    public Integer getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(Integer serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public Long getSubmittedApplicationsCount() {
        return submittedApplicationsCount;
    }

    public void setSubmittedApplicationsCount(Long submittedApplicationsCount) {
        this.submittedApplicationsCount = submittedApplicationsCount;
    }

    public Integer getSlaDays() {
        return slaDays;
    }

    public void setSlaDays(Integer slaDays) {
        this.slaDays = slaDays;
    }
}
