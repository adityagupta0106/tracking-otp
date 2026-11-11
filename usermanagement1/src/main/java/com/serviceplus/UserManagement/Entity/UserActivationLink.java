package com.serviceplus.UserManagement.Entity;

import java.io.Serializable;
import java.util.Date;

import com.serviceplus.UserManagement.util.ApplicationConstants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

////@Entity
////@Table(name = "user_activation_link", schema = ApplicationConstants.SP_SCHEMA_NAME)
public class UserActivationLink  extends BaseTenantEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	//@Id
	//@Column(name = "user_id")
	private Integer userId;

	//@Column(name = "mobile_code")
	private String mobileCode;
	
	//@Column(name = "link_creation_date")
	private Date linkCreationDate;

	//@Column(name = "ref_number")
	private String refNumber;
	
	//@Column(name = "mobile_no")
	private String mobileNo;

	public UserActivationLink() {

	}
	
	
	public UserActivationLink(Integer userId, String mobileCode,
			Date linkCreationDate, String refNumber, String mobileNo) {
		super();
		this.userId = userId;
		this.mobileCode = mobileCode;
		this.linkCreationDate = linkCreationDate;
		this.refNumber = refNumber;
		this.mobileNo=mobileNo;
	}



	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getMobileCode() {
		return mobileCode;
	}

	public void setMobileCode(String mobileCode) {
		this.mobileCode = mobileCode;
	}

	public Date getLinkCreationDate() {
		return linkCreationDate;
	}

	public void setLinkCreationDate(Date linkCreationDate) {
		this.linkCreationDate = linkCreationDate;
	}

	public String getRefNumber() {
		return refNumber;
	}

	public void setRefNumber(String refNumber) {
		this.refNumber = refNumber;
	}


	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	

}
