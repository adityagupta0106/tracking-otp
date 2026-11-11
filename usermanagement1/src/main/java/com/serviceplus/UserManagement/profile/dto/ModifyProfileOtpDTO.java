package com.serviceplus.UserManagement.profile.dto;

public class ModifyProfileOtpDTO {

    private String otp;
    private String email;

    public ModifyProfileOtpDTO() {}

    public ModifyProfileOtpDTO(String otp, String email) {
        this.otp = otp;
        this.email = email;
    }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
