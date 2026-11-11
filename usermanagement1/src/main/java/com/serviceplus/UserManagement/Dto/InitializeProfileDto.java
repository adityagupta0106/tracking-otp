package com.serviceplus.UserManagement.Dto;

public class InitializeProfileDto {

	private String uid;
	private String userName;
	private String email;
	private String mobileNo;
	private Integer loginPreference;
	private Integer designationId;
    private String designationName;
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobileNo() {
		return mobileNo;
	}
	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}
	public Integer getLoginPreference() {
		return loginPreference;
	}
	public void setLoginPreference(Integer loginPreference) {
		this.loginPreference = loginPreference;
	}
	public Integer getDesignationId() {
		return designationId;
	}
	public void setDesignationId(Integer designationId) {
		this.designationId = designationId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

    public String getDesignationName() {
        return designationName;
    }

    public void setDesignationName(String designationName) {
        this.designationName = designationName;
    }
}
