package com.serviceplus.UserManagement.profile.dto;

public class ModifyMobileOtpDTO {

    private String otp;
    private String mobile;

    public ModifyMobileOtpDTO() {}

    public ModifyMobileOtpDTO(String otp, String mobile) {
        this.otp = otp;
        this.mobile = mobile;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
