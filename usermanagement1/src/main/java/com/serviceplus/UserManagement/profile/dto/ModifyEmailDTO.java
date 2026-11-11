package com.serviceplus.UserManagement.profile.dto;

import javax.validation.constraints.NotEmpty;

public class ModifyEmailDTO {

	@NotEmpty(message = "OTP can't be empty")
	private String otp;

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}
	
}
