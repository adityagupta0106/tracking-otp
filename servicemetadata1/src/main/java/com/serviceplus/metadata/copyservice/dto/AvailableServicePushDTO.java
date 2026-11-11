package com.serviceplus.metadata.copyservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailableServicePushDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer serviceId;
    private Integer baseServiceId;
    private String serviceName;
    private String serviceAbbreviation;
    private Long departmentId;
    private Integer versionNo;
    private Integer minorVersionNo;
    private Integer serviceStatus;
    private String statusLabel;
    private Long submittedApplicationsCount;
    private Date launchedDate;
    private boolean eligible;
    private boolean alreadyInCentralRepo;
    private String message;
    private List<String> additionalConfigurations;
    private List<String> maskedModules;

    public AvailableServicePushDTO() {
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
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

    public Long getSubmittedApplicationsCount() {
        return submittedApplicationsCount;
    }

    public void setSubmittedApplicationsCount(Long submittedApplicationsCount) {
        this.submittedApplicationsCount = submittedApplicationsCount;
    }

    public Date getLaunchedDate() {
        return launchedDate;
    }

    public void setLaunchedDate(Date launchedDate) {
        this.launchedDate = launchedDate;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public boolean isAlreadyInCentralRepo() {
        return alreadyInCentralRepo;
    }

    public void setAlreadyInCentralRepo(boolean alreadyInCentralRepo) {
        this.alreadyInCentralRepo = alreadyInCentralRepo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getAdditionalConfigurations() {
        return additionalConfigurations;
    }

    public void setAdditionalConfigurations(List<String> additionalConfigurations) {
        this.additionalConfigurations = additionalConfigurations;
    }

    public List<String> getMaskedModules() {
        return maskedModules;
    }

    public void setMaskedModules(List<String> maskedModules) {
        this.maskedModules = maskedModules;
    }
}
