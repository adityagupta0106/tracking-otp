package com.serviceplus.metadata.copyservice.dto;

import java.io.Serializable;
import java.util.List;

public class CopyServiceResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private Integer newServiceId;
    private Integer newBaseServiceId;
    private String newServiceName;
    private String newServiceAbbreviation;
    private Integer serviceStatus; // 0 (DEFINED)
    private boolean crossDepartment;
    private String message;
    private List<String> pendingValidationModules;

    public CopyServiceResponseDTO() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getNewServiceId() {
        return newServiceId;
    }

    public void setNewServiceId(Integer newServiceId) {
        this.newServiceId = newServiceId;
    }

    public Integer getNewBaseServiceId() {
        return newBaseServiceId;
    }

    public void setNewBaseServiceId(Integer newBaseServiceId) {
        this.newBaseServiceId = newBaseServiceId;
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

    public Integer getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(Integer serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public boolean isCrossDepartment() {
        return crossDepartment;
    }

    public void setCrossDepartment(boolean crossDepartment) {
        this.crossDepartment = crossDepartment;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getPendingValidationModules() {
        return pendingValidationModules;
    }

    public void setPendingValidationModules(List<String> pendingValidationModules) {
        this.pendingValidationModules = pendingValidationModules;
    }
}
