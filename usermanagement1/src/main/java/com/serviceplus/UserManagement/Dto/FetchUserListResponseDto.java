package com.serviceplus.UserManagement.Dto;

import java.util.Date;
import java.util.List;

import com.serviceplus.UserManagement.Entity.UserSessionObject;

public class FetchUserListResponseDto {

	private List<Data> data;
	
	public static class Data{
		
		private String userName;
		private String userId;
		private String roles;
		private String location;
		private Date profileCreatedOn;
		private Integer status;
        private String eUserId;
		public String getUserName() {
			return userName;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
		public String getUserId() {
			return userId;
		}
		public void setUserId(String userId) {
			this.userId = userId;
		}
		public String getRoles() {
			return roles;
		}
		public void setRoles(String roles) {
			this.roles = roles;
		}
		public String getLocation() {
			return location;
		}
		public void setLocation(String location) {
			this.location = location;
		}
		public Date getProfileCreatedOn() {
			return profileCreatedOn;
		}
		public void setProfileCreatedOn(Date profileCreatedOn) {
			this.profileCreatedOn = profileCreatedOn;
		}
		public Integer getStatus() {
			return status;
		}
		public void setStatus(Integer status) {
			this.status = status;
		}

        public String geteUserId() {
            return eUserId;
        }

        public void seteUserId(String eUserId) {
            this.eUserId = eUserId;
        }
    }

	public List<Data> getData() {
		return data;
	}

	public void setData(List<Data> data) {
		this.data = data;
	}
	
}
