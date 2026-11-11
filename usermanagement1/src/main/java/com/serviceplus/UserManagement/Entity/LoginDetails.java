package com.serviceplus.UserManagement.Entity;


import java.util.Date;

import com.serviceplus.UserManagement.util.ApplicationConstants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "login_details", schema = ApplicationConstants.SP_SCHEMA_NAME)
public class LoginDetails extends BaseTenantEntity implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "sign_no")
	private String signNo;
	
	@Column(name = "passwd")
	private String passwd;

	@Id
	@Column(name = "user_id")
	private Long  userId;

	@Column(name = "spdi_status")
	private Integer spdiStatus;
	
	@Column(name = "c_date")
	private Date createdDate;
	
	@Column(name = "created_by")
	private Long createdBy;

    @Column(name = "clc_id")
    private Integer clcId;

	@Column(name = "sso_provider_user_id")
	private String ssoProviderUserId;
	
	public String getSsoProviderUserId() {
		return ssoProviderUserId;
	}
	public void setSsoProviderUserId(String ssoProviderUserId) {
		this.ssoProviderUserId = ssoProviderUserId;
	}
	public String getProviderUserId() {
		return ssoProviderUserId;
	}
	public void setProviderUserId(String providerUserId) {
		this.ssoProviderUserId = providerUserId;
	}
	
	public String getSignNo() {
		return signNo;
	}
	public void setSignNo(String signNo) {
		this.signNo = signNo;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public Integer getSpdiStatus() {
		return spdiStatus;
	}
	public void setSpdiStatus(Integer spdiStatus) {
		this.spdiStatus = spdiStatus;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Long getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

    public Long  getUserId() {
        return userId;
    }

    public void setUserId(Long  userId) {
        this.userId = userId;
    }

    public Integer getClcId() {
        return clcId;
    }

    public void setClcId(Integer clcId) {
        this.clcId = clcId;
    }
}
