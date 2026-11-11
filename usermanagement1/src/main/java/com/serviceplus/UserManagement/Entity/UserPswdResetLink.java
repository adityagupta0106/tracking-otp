package com.serviceplus.UserManagement.Entity;

import java.util.Date;

import com.serviceplus.UserManagement.util.ApplicationConstants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_pwd_reset_link", schema = ApplicationConstants.SP_SCHEMA_NAME)
public class UserPswdResetLink  extends BaseTenantEntity{
	
	@Column(name = "token")
	private String token;

	@Id
	@Column(name = "user_id")
	private Long userId;
	
	@Column(name = "creation_time")
	private Date creationTime;
	
	@Column(name = "last_acces_time")
	private Date lastAccessTime;
	
	@Column(name = "access_count")
	private Integer accessCount;
	
	@Column(name = "attempt_count")
	private Integer attemptCount;
	
	@Column(name = "last_hit_ip_address")
	private String lastHitIPAddress;
	
	@Column(name = "is_active")
	private Boolean isActive;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public Date getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(Date lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public Integer getAccessCount() {
		return accessCount;
	}

	public void setAccessCount(Integer accessCount) {
		this.accessCount = accessCount;
	}

	public Integer getAttemptCount() {
		return attemptCount;
	}

	public void setAttemptCount(Integer attemptCount) {
		this.attemptCount = attemptCount;
	}

	public String getLastHitIPAddress() {
		return lastHitIPAddress;
	}

	public void setLastHitIPAddress(String lastHitIPAddress) {
		this.lastHitIPAddress = lastHitIPAddress;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
}
