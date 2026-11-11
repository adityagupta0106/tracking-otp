package com.serviceplus.UserManagement.Dto;

public class RegisterSsoUserResponse {

    private boolean success;
    private Long userId;
    private String signNo;
    private Boolean alreadyRegistered;
    private String password;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSignNo() {
        return signNo;
    }

    public void setSignNo(String signNo) {
        this.signNo = signNo;
    }

    public Boolean getAlreadyRegistered() {
        return alreadyRegistered;
    }

    public void setAlreadyRegistered(Boolean alreadyRegistered) {
        this.alreadyRegistered = alreadyRegistered;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
