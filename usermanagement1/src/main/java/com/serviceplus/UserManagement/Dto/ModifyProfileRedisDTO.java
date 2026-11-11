package com.serviceplus.UserManagement.Dto;

import java.util.Date;

public class ModifyProfileRedisDTO {
	private String mobileNo;
	private String email;
	private Integer mobileOtp;
	private Integer emailOtp;
	private Integer mobileOtpAttempts;
	private Integer emailOtpAttempts;
	private Date mobileOtpGenTime;
	private Date emailOtpGenTime;
	private Integer mobileOtpSendCount;
	private Integer emailOtpSendCount;
	private Date mobileOtpAttemptsBlockTime;
	private Date emailOtpAttemptsBlockTime;

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getMobileOtp() {
		return mobileOtp;
	}

	public void setMobileOtp(Integer mobileOtp) {
		this.mobileOtp = mobileOtp;
	}

	public Integer getEmailOtp() {
		return emailOtp;
	}

	public void setEmailOtp(Integer emailOtp) {
		this.emailOtp = emailOtp;
	}

	public Integer getMobileOtpAttempts() {
		return mobileOtpAttempts;
	}

	public void setMobileOtpAttempts(Integer mobileOtpAttemps) {
		this.mobileOtpAttempts = mobileOtpAttemps;
	}

	public Integer getEmailOtpAttempts() {
		return emailOtpAttempts;
	}

	public void setEmailOtpAttempts(Integer emailOtpAttemps) {
		this.emailOtpAttempts = emailOtpAttemps;
	}

	public Date getMobileOtpGenTime() {
		return mobileOtpGenTime;
	}

	public void setMobileOtpGenTime(Date mobileOtpGenTime) {
		this.mobileOtpGenTime = mobileOtpGenTime;
	}

	public Date getEmailOtpGenTime() {
		return emailOtpGenTime;
	}

	public void setEmailOtpGenTime(Date emailOtpGenTime) {
		this.emailOtpGenTime = emailOtpGenTime;
	}

	public Integer getMobileOtpSendCount() {
		return mobileOtpSendCount;
	}

	public void setMobileOtpSendCount(Integer mobileOtpSendCount) {
		this.mobileOtpSendCount = mobileOtpSendCount;
	}

	public Integer getEmailOtpSendCount() {
		return emailOtpSendCount;
	}

	public void setEmailOtpSendCount(Integer emailOtpSendCount) {
		this.emailOtpSendCount = emailOtpSendCount;
	}

	public Date getMobileOtpAttemptsBlockTime() {
		return mobileOtpAttemptsBlockTime;
	}

	public void setMobileOtpAttemptsBlockTime(Date mobileOtpAttempsBlockTime) {
		this.mobileOtpAttemptsBlockTime = mobileOtpAttempsBlockTime;
	}

	public Date getEmailOtpAttemptsBlockTime() {
		return emailOtpAttemptsBlockTime;
	}

	public void setEmailOtpAttemptsBlockTime(Date emailOtpAttempsBlockTime) {
		this.emailOtpAttemptsBlockTime = emailOtpAttempsBlockTime;
	}

}
