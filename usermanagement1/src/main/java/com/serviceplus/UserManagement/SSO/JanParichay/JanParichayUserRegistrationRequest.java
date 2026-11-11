package com.serviceplus.UserManagement.SSO.JanParichay;

public class JanParichayUserRegistrationRequest {

    private String UserId;
    private String Password;
    private String ServiceId;

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getServiceId() {
        return ServiceId;
    }

    public void setServiceId(String serviceId) {
        ServiceId = serviceId;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
