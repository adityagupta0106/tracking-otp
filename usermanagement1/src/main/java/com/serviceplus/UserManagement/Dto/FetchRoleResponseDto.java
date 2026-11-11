package com.serviceplus.UserManagement.Dto;

import java.util.List;

import com.serviceplus.UserManagement.Entity.UserSessionObject;

public class FetchRoleResponseDto {

	
	private Data data;
	
	public FetchRoleResponseDto(Data data) {
		this.data = data;
	}
	
	public FetchRoleResponseDto() {
		
	}
	
	public static class Data{
		
		private List<UserSessionObject.Roles> roles;

		public Data(List<UserSessionObject.Roles> roles) {
			this.roles = roles;
		}
		
		public Data() {
			
		}
		
		public List<UserSessionObject.Roles> getRoles() {
			return roles;
		}

		public void setRoles(List<UserSessionObject.Roles> roles) {
			this.roles = roles;
		}
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}
}
