package com.serviceplus.UserManagement.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serviceplus.UserManagement.util.ApplicationConstants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "role_master",  schema = ApplicationConstants.SP_SCHEMA_NAME)
public class RoleMaster  extends BaseTenantEntity{

	private Integer roleId;
	private String roleName;
	@JsonIgnore
	private String signRole;

	public RoleMaster() {
	}

	public RoleMaster(Integer roleId) {
		this.roleId = roleId;
	}

	public RoleMaster(Integer roleId, String roleName) {
		this.roleId = roleId;
		this.roleName = roleName;
	}

	@Id
	@Column(name = "role_id", unique = true, nullable = false)
	public Integer getRoleId() {
		return this.roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	@Column(name = "role_name", length = 100)
	public String getRoleName() {
		return this.roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	@Column(name = "sign_role", length = 100)
	public String getSignRole() {
		return signRole;
	}

	public void setSignRole(String signRole) {
		this.signRole = signRole;
	}

}
