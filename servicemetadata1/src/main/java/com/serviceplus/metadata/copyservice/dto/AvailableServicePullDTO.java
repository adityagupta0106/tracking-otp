package com.serviceplus.metadata.copyservice.dto;

import java.io.Serializable;
import java.util.Date;

public class AvailableServicePullDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer serviceId;
    private String serviceName;
    private String serviceAbbreviation;
    private Integer versionNo;
    private Integer minorVersionNo;
    private Long submittedApplicationsCount;
    private Date launchedDate;
    private boolean eligible;
    private boolean alreadyInCentralRepo;

    public AvailableServicePullDTO() {
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
}
