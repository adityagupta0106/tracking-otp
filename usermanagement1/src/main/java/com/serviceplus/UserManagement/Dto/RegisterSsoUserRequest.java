package com.serviceplus.UserManagement.Dto;


public class RegisterSsoUserRequest {

    private String issuer;

    private String firstName;

    private String lastName;

    private String fullName;

    private String issueUserId;

    private String tenantId;

    private String  identity;

    private String ssoProviderId;

    private CommonVerification verification;

    public static class CommonVerification {

        private String servicePlusId;
        private Long cscId;
        private String email;
        private String mobile;

        public String getServicePlusId() {
            return servicePlusId;
        }

        public void setServicePlusId(String servicePlusId) {
            this.servicePlusId = servicePlusId;
        }

        public Long getCscId() {
            return cscId;
        }

        public void setCscId(Long cscId) {
            this.cscId = cscId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }
    }


    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public CommonVerification getVerification() {
        return verification;
    }

    public void setVerification(CommonVerification verification) {
        this.verification = verification;
    }

    public String getIssueUserId() {
        return issueUserId;
    }

    public void setIssueUserId(String issueUserId) {
        this.issueUserId = issueUserId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getSsoProviderId() {
        return ssoProviderId;
    }

    public void setSsoProviderId(String ssoProviderId) {
        this.ssoProviderId = ssoProviderId;
    }
}
