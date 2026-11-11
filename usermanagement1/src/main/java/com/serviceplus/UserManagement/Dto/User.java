package com.serviceplus.UserManagement.Dto;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	private String u_userId;

	private String u_emailId;

	private String u_mobileNo;

	private Integer u_clcId;

	private String u_clcName;
 
	private String u_userName;

	private Integer u_designationId;

	private String u_cugType;

	private Boolean u_approvalRequired;

	private Integer ul_categoryId;

	private Integer ul_entityId;

	private Integer ul_entityLevelId;

	private String ul_entityLevelName;

	private Integer ul_locationId;

	private String ul_locationName;

	private String ld_signNo;

	private String u_middleName;

	private String u_lastName;

	private Date u_dob;

	private String u_address;

	private String u_postalCode;

	private String u_profilePic;

	private Date u_cDate;

	private Date u_uDate;

	public User() {
	}

	public User(Integer u_clcId, String u_userId, String u_emailId, String u_mobileNo, String u_clcName,
			String u_userName, Integer u_designationId, String u_cugType, Boolean u_approvalRequired,
			Integer ul_categoryId, Integer ul_entityId, Integer ul_entityLevelId, String ul_entityLevelName,
			Integer ul_locationId, String ul_locationName, String ld_signNo, String u_middleName, String u_lastName,
			Date u_dob, String u_address, String u_postalCode, String u_profilePic, Date u_cDate, Date u_uDate) {
		this.u_clcId = u_clcId;
		this.u_userId = u_userId;
		this.u_emailId = u_emailId;
		this.u_mobileNo = u_mobileNo;
		this.u_clcName = u_clcName;
		this.u_userName = u_userName;
		this.u_designationId = u_designationId;
		this.u_cugType = u_cugType;
		this.u_approvalRequired = u_approvalRequired;
		this.ul_categoryId = ul_categoryId;
		this.ul_entityId = ul_entityId;
		this.ul_entityLevelId = ul_entityLevelId;
		this.ul_entityLevelName = ul_entityLevelName;
		this.ul_locationId = ul_locationId;
		this.ul_locationName = ul_locationName;
		this.ld_signNo = ld_signNo;
		this.u_middleName = u_middleName;
		this.u_lastName = u_lastName;
		this.u_dob = u_dob;
		this.u_address = u_address;
		this.u_profilePic = u_profilePic;
		this.u_cDate = u_cDate;
		this.u_uDate = u_uDate;
	}

	public String getU_userId() {
		return u_userId;
	}

	public void setU_userId(String u_userId) {
		this.u_userId = u_userId;
	}

	public String getU_emailId() {
		return u_emailId;
	}

	public void setU_emailId(String u_emailId) {
		this.u_emailId = u_emailId;
	}

	public String getU_mobileNo() {
		return u_mobileNo;
	}

	public void setU_mobileNo(String u_mobileNo) {
		this.u_mobileNo = u_mobileNo;
	}

	public Integer getU_clcId() {
		return u_clcId;
	}

	public void setU_clcId(Integer u_clcId) {
		this.u_clcId = u_clcId;
	}

	public String getU_clcName() {
		return u_clcName;
	}

	public void setU_clcName(String u_clcName) {
		this.u_clcName = u_clcName;
	}

	public String getU_userName() {
		return u_userName;
	}

	public void setU_userName(String u_userName) {
		this.u_userName = u_userName;
	}

	public Integer getU_designationId() {
		return u_designationId;
	}

	public void setU_designationId(Integer u_designationId) {
		this.u_designationId = u_designationId;
	}

	public String getU_cugType() {
		return u_cugType;
	}

	public void setU_cugType(String u_cugType) {
		this.u_cugType = u_cugType;
	}

	public Boolean getU_approvalRequired() {
		return u_approvalRequired;
	}

	public void setU_approvalRequired(Boolean u_approvalRequired) {
		this.u_approvalRequired = u_approvalRequired;
	}

	public Integer getUl_categoryId() {
		return ul_categoryId;
	}

	public void setUl_categoryId(Integer ul_categoryId) {
		this.ul_categoryId = ul_categoryId;
	}

	public Integer getUl_entityId() {
		return ul_entityId;
	}

	public void setUl_entityId(Integer ul_entityId) {
		this.ul_entityId = ul_entityId;
	}

	public Integer getUl_entityLevelId() {
		return ul_entityLevelId;
	}

	public void setUl_entityLevelId(Integer ul_entityLevelId) {
		this.ul_entityLevelId = ul_entityLevelId;
	}

	public String getUl_entityLevelName() {
		return ul_entityLevelName;
	}

	public void setUl_entityLevelName(String ul_entityLevelName) {
		this.ul_entityLevelName = ul_entityLevelName;
	}

	public Integer getUl_locationId() {
		return ul_locationId;
	}

	public void setUl_locationId(Integer ul_locationId) {
		this.ul_locationId = ul_locationId;
	}

	public String getUl_locationName() {
		return ul_locationName;
	}

	public void setUl_locationName(String ul_locationName) {
		this.ul_locationName = ul_locationName;
	}

	public String getLd_signNo() {
		return ld_signNo;
	}

	public void setLd_signNo(String ld_signNo) {
		this.ld_signNo = ld_signNo;
	}

	public String getU_middleName() {
		return u_middleName;
	}

	public void setU_middleName(String u_middleName) {
		this.u_middleName = u_middleName;
	}

	public String getU_lastName() {
		return u_lastName;
	}

	public void setU_lastName(String u_lastName) {
		this.u_lastName = u_lastName;
	}

	public Date getU_dob() {
		return u_dob;
	}

	public void setU_dob(Date u_dob) {
		this.u_dob = u_dob;
	}

	public String getU_address() {
		return u_address;
	}

	public void setU_address(String u_address) {
		this.u_address = u_address;
	}

	public String getU_postalCode() {
		return u_postalCode;
	}

	public void setU_postalCode(String u_postalCode) {
		this.u_postalCode = u_postalCode;
	}

	public String getU_profilePic() {
		return u_profilePic;
	}

	public void setU_profilePic(String u_profilePic) {
		this.u_profilePic = u_profilePic;
	}

	public Date getU_cDate() {
		return u_cDate;
	}

	public void setU_cDate(Date u_cDate) {
		this.u_cDate = u_cDate;
	}

	public Date getU_uDate() {
		return u_uDate;
	}

	public void setU_uDate(Date u_uDate) {
		this.u_uDate = u_uDate;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}