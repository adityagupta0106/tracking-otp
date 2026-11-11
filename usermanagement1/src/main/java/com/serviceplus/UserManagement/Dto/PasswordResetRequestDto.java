package com.serviceplus.UserManagement.Dto;

public class PasswordResetRequestDto {

	private String authString;
	private String token;

	public String getAuthString() {
		return authString;
	}

	public void setAuthString(String authString) {
		this.authString = authString;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}	
}
