package com.serviceplus.UserManagement.Service;

import java.util.Map;

import com.serviceplus.UserManagement.Entity.UserSessionObject;

public interface RedisService {

	Object fetchFromRedisCache(String uuid,Object obj);
	
	void addToRedisCache(UserSessionObject user,String uuid);
	
	String fetchFromNonActivatedUsersList(String value);

	void removeFromList(String listName,String value);

	void revokeLoginKey(String key);
	
	void updateCheckTime(String uuid);
}
