package com.serviceplus.metadata.copyservice.dto;

import java.io.Serializable;

public class CopyServiceRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Service ID (accepts both serviceId and sourceServiceId)
    private Integer serviceId;
    private Integer sourceServiceId;

    // For Global copy
    private Boolean isGlobal;
    private String centralRepoId;

    // Destination Department
    private Long targetDepartmentId;

    // Optional name and abbreviation overrides
    private String newServiceName;
    private String newServiceAbbreviation;

    public CopyServiceRequestDTO() {
    }

    public Integer getServiceId() {
        return (serviceId != null) ? serviceId : sourceServiceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
        if (this.sourceServiceId == null) {
            this.sourceServiceId = serviceId;
        }
    }

    public Integer getSourceServiceId() {
        return (sourceServiceId != null) ? sourceServiceId : serviceId;
    }

    public void setSourceServiceId(Integer sourceServiceId) {
        this.sourceServiceId = sourceServiceId;
        if (this.serviceId == null) {
            this.serviceId = sourceServiceId;
        }
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

    public Long getTargetDepartmentId() {
        return targetDepartmentId;
    }

    public void setTargetDepartmentId(Long targetDepartmentId) {
        this.targetDepartmentId = targetDepartmentId;
    }

    public String getNewServiceName() {
        return newServiceName;
    }

    public void setNewServiceName(String newServiceName) {
        this.newServiceName = newServiceName;
    }

    public String getNewServiceAbbreviation() {
        return newServiceAbbreviation;
    }

    public void setNewServiceAbbreviation(String newServiceAbbreviation) {
        this.newServiceAbbreviation = newServiceAbbreviation;
    }
}
