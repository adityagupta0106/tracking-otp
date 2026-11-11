package com.serviceplus.UserManagement.Service.Implementation;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Service.RedisService;
import static com.serviceplus.UserManagement.util.ApplicationConstants.REDIS_KEY_EXIPRATION_DURATION_IN_MINUTES;

@Service
@Primary
public class RedisServiceImpl implements RedisService{

	@Autowired
	private RedisTemplate<String, String> template;
	
	@Override
	public Object fetchFromRedisCache(String uuid,Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = template.opsForValue().get(uuid);
			
			//Dump data
			RedisConnection connection = template.getConnectionFactory().getConnection();
			//connection.save();    
			//Dump data */
			
			RedisServerCommands serverCommands = connection.serverCommands();
			serverCommands.save();
			
			Object map = mapper.readValue(json, obj.getClass());
			return map;
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public void addToRedisCache(UserSessionObject user,String uuid) {
		ObjectMapper mapper = new ObjectMapper();
		String finalJson;
		try {
			finalJson = mapper.writeValueAsString(user);
			template.opsForValue().set(uuid, finalJson,Duration.ofMinutes(REDIS_KEY_EXIPRATION_DURATION_IN_MINUTES));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String fetchFromNonActivatedUsersList(String value) {
		String returnValue = "";
		try {
			List<String> listValues = template.opsForList().range("NonActivatedUsers", 0, -1);
			if(listValues.contains(value)) {
				returnValue = value; 
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new SPRuntimeError("RDERROR -- 002" , HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return returnValue;
	}

	@Override
	public void removeFromList(String listName,String value) {
		try {
			template.opsForList().remove(listName, 0, value);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new SPRuntimeError("RDERROR -- 003" , HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void revokeLoginKey(String key) {
		try {
			template.opsForValue().getAndDelete(key);
		}
		catch(Exception e) {
			e.printStackTrace();
			//throw new SPRuntimeError("RDERROR -- 004" , HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Override
	public void updateCheckTime(String uuid) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			UserSessionObject data = (UserSessionObject) fetchFromRedisCache(uuid,new UserSessionObject());
			if(data != null) {
			  data.setLastCheck(new Date());
			  String json = mapper.writeValueAsString(data);
			  template.opsForValue().set(uuid,json);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
