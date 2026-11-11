package com.serviceplus.UserManagement.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.serviceplus.UserManagement.util.ApplicationConstants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_user_creation_access", schema = ApplicationConstants.SP_SCHEMA_NAME)
public class AdminUserCreationAccess  extends BaseTenantEntity{

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonIgnore
	private Long id;
	
	@Column(name = "logged_in_role")
	@JsonIgnore
	private Integer loggedInRole;
	
	@ManyToOne(fetch = FetchType.EAGER) // SINCE VERY FEW API CALLS SO, USING EAGER
    @JoinColumn(name = "can_create_role", referencedColumnName = "role_id", insertable = false, updatable = false)
	@JsonIgnore
	private RoleMaster master;
	
	@JsonProperty("roleId")
	public Integer getRoleId() {
	    return master != null ? master.getRoleId() : null;
	}

	@JsonProperty("roleName")
	public String getRoleName() {
	    return master != null ? master.getRoleName() : null;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getLoggedInRole() {
		return loggedInRole;
	}

	public void setLoggedInRole(Integer loggedInRole) {
		this.loggedInRole = loggedInRole;
	}

	public RoleMaster getMaster() {
		return master;
	}

	public void setMaster(RoleMaster master) {
		this.master = master;
	}

}
