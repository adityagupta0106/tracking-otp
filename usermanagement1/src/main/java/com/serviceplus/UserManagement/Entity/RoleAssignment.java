package com.serviceplus.UserManagement.Entity;

import com.serviceplus.UserManagement.util.ApplicationConstants;

import jakarta.persistence.*;

@Entity
@Table(name = "role_assignment",  schema = ApplicationConstants.SP_SCHEMA_NAME)
public class RoleAssignment extends BaseTenantEntity{
	

	@Id
	@Column(name="user_role_id")
	private Long user_role_id;

	@Column(name="user_loc_id")
	private Long userLocId;
	
	@Column(name="user_id")
	private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "user_id",
            insertable = false,
            updatable = false)
    private LoginDetails user;
	
	@Column(name="role_id")
	private Integer roleId;

	public Long getUser_role_id() {
		return user_role_id;
	}

	public void setUser_role_id(Long user_role_id) {
		this.user_role_id = user_role_id;
	}

    public Long getUserLocId() {
        return userLocId;
    }

    public void setUserLocId(Long userLocId) {
        this.userLocId = userLocId;
    }

    public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

    public LoginDetails getUser() {
        return user;
    }

    public void setUser(LoginDetails user) {
        this.user = user;
    }
}
