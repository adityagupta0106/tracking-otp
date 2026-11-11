package com.serviceplus.UserManagement.Dto;

public class UserProfileApiDto {

	private Data data;

	public static class Data {
		private Profile profile;

		public static class Profile {
			private String mobileNo;
			private String emailId;
			private Integer stateId;
			private String userName;

			public String getEmailId() {
				return emailId;
			}

			public void setEmailId(String emailId) {
				this.emailId = emailId;
			}

			public String getMobileNo() {
				return mobileNo;
			}

			public void setMobileNo(String mobileNo) {
				this.mobileNo = mobileNo;
			}

			public Integer getStateId() {
				return stateId;
			}

			public void setStateId(Integer stateId) {
				this.stateId = stateId;
			}

			public String getUserName() {
				return userName;
			}

			public void setUserName(String userName) {
				this.userName = userName;
			}
		}

		public Profile getProfile() {
			return profile;
		}

		public void setProfile(Profile profile) {
			this.profile = profile;
		}

		
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}
}
