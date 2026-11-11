package com.serviceplus.UserManagement.Dto;


public class CaptchaDto {

	private String captchaCode;
	
	private String seed;

	public String getCaptchaCode() {
		return captchaCode;
	}

	public void setCaptchaCode(String captchaCode) {
		this.captchaCode = captchaCode;
	}

	public String getSeed() {
		return seed;
	}

	public void setSeed(String seed) {
		this.seed = seed;
	}
	
	
}
