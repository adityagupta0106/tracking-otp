package com.serviceplus.UserManagement.Dto;

public class TenantProfilePolicy {

    private boolean emailMandatory;
    private boolean mobileMandatory;
    private boolean userNameMandatory;
    private boolean emailEnabled;
    private boolean mobileEnabled;

    public boolean isEmailMandatory() {
        return emailMandatory;
    }

    public void setEmailMandatory(boolean emailMandatory) {
        this.emailMandatory = emailMandatory;
    }

    public boolean isMobileMandatory() {
        return mobileMandatory;
    }

    public void setMobileMandatory(boolean mobileMandatory) {
        this.mobileMandatory = mobileMandatory;
    }

    public boolean isUserNameMandatory() {
        return userNameMandatory;
    }

    public void setUserNameMandatory(boolean userNameMandatory) {
        this.userNameMandatory = userNameMandatory;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public boolean isMobileEnabled() {
        return mobileEnabled;
    }

    public void setMobileEnabled(boolean mobileEnabled) {
        this.mobileEnabled = mobileEnabled;
    }
}
