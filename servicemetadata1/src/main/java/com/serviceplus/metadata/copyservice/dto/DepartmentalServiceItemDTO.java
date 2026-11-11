package com.serviceplus.metadata.copyservice.dto;

import java.io.Serializable;
import java.util.Date;

public class DepartmentalServiceItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer serviceId;
    private Integer baseServiceId;
    private String serviceName;
    private String serviceAbbreviation;
    private Integer versionNo;
    private Integer minorVersionNo;
    private Integer serviceStatus;
    private String statusLabel;
    private String departmentName;
    private Long submittedApplicationsCount;
    private boolean eligibleForCopy;
    private boolean eligibleForPush;
    private boolean alreadyInCentralRepo;
    private Integer slaDays;
    private String hierarchy;
    private Date launchedDate;

    public DepartmentalServiceItemDTO() {
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

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Long getSubmittedApplicationsCount() {
        return submittedApplicationsCount;
    }

    public void setSubmittedApplicationsCount(Long submittedApplicationsCount) {
        this.submittedApplicationsCount = submittedApplicationsCount;
    }

    public boolean isEligibleForCopy() {
        return eligibleForCopy;
    }

    public void setEligibleForCopy(boolean eligibleForCopy) {
        this.eligibleForCopy = eligibleForCopy;
    }

    public boolean isEligibleForPush() {
        return eligibleForPush;
    }

    public void setEligibleForPush(boolean eligibleForPush) {
        this.eligibleForPush = eligibleForPush;
    }

    public boolean isAlreadyInCentralRepo() {
        return alreadyInCentralRepo;
    }

    public void setAlreadyInCentralRepo(boolean alreadyInCentralRepo) {
        this.alreadyInCentralRepo = alreadyInCentralRepo;
    }

    public Integer getSlaDays() {
        return slaDays;
    }

    public void setSlaDays(Integer slaDays) {
        this.slaDays = slaDays;
    }

    public String getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(String hierarchy) {
        this.hierarchy = hierarchy;
    }

    public Date getLaunchedDate() {
        return launchedDate;
    }

    public void setLaunchedDate(Date launchedDate) {
        this.launchedDate = launchedDate;
    }
}
