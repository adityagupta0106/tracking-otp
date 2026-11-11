package com.serviceplus.UserManagement.Service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

	public boolean sendNotification() {
		sendEmailNotification();
		sendMobileNotification();
		return true;
	}
	
	public boolean sendEmailNotification() {
		return true;
	}
	
	public boolean sendMobileNotification() {
		return true;
	}
}
