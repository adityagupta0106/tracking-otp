package com.serviceplus.UserManagement.identifieduserset.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.serviceplus.UserManagement.identifieduserset.enums.UserAccountStatus;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSearchResultDto {

    private Long userId;
    private String signNo;
    private String userName;
    private String emailId;
    private String mobileNo;
    private String ssoProviderUserId;
    private UserAccountStatus status;
    private String tenantId;
    private List<String> roles = new ArrayList<>();
    private List<String> identifiedUserSets = new ArrayList<>();

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getSsoProviderUserId() {
        return ssoProviderUserId;
    }

    public void setSsoProviderUserId(String ssoProviderUserId) {
        this.ssoProviderUserId = ssoProviderUserId;
    }

    public String getProviderUserId() {
        return ssoProviderUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.ssoProviderUserId = providerUserId;
    }

    public UserAccountStatus getStatus() {
        return status;
    }

    public void setStatus(UserAccountStatus status) {
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = UserAccountStatus.fromString(status);
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getIdentifiedUserSets() {
        return identifiedUserSets;
    }

    public void setIdentifiedUserSets(List<String> identifiedUserSets) {
        this.identifiedUserSets = identifiedUserSets;
    }
}
