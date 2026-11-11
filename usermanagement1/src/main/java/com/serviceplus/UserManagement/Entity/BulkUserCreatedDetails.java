package com.serviceplus.UserManagement.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

////@Entity
////@Table(name = "save_bulk_user_typewise", schema = "${ApplicationConstants.SP_SCHEMA_NAME.getString()}")
public class BulkUserCreatedDetails {
	
	//@Id
	//@Column(name="user_name")
	private String userName;
	
	//@Column(name = "password")
	private String password;
	
	//@Column(name = "location_name")
	private String locTypeName;
	
	//@Column(name = "parent_name")
	private String parentLoctationName;
	
	//@Column(name = "super_parent_name")
	private String superParentLoctationName;

    private String roleName;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLocTypeName() {
		return locTypeName;
	}

	public void setLocTypeName(String locTypeName) {
		this.locTypeName = locTypeName;
	}

	public String getParentLoctationName() {
		return parentLoctationName;
	}

	public void setParentLoctationName(String parentLoctationName) {
		this.parentLoctationName = parentLoctationName;
	}

	public String getSuperParentLoctationName() {
		return superParentLoctationName;
	}

	public void setSuperParentLoctationName(String superParentLoctationName) {
		this.superParentLoctationName = superParentLoctationName;
	}

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
