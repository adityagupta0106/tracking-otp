package com.serviceplus.UserManagement.Dto;

public class DSCResponse {

    private boolean success;

    private String message;

    private Long dscId;

    private String status;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getDscId() {
        return dscId;
    }

    public void setDscId(Long dscId) {
        this.dscId = dscId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
