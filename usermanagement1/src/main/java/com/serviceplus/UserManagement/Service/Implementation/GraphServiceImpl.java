package com.serviceplus.UserManagement.Service.Implementation;

import static com.serviceplus.UserManagement.util.Utility.AESDecrypt;
import static com.serviceplus.UserManagement.util.Utility.getUserSessionDetails;
import static com.serviceplus.UserManagement.util.Utility.isEmpty;
import static com.serviceplus.UserManagement.util.Utility.isServicePlusInternalCall;
import static com.serviceplus.UserManagement.util.Utility.stringToEntity;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.serviceplus.UserManagement.Dto.User;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Repository.CustomQueryRepository;
import com.serviceplus.UserManagement.Service.GraphService;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import jakarta.servlet.http.HttpServletRequest;

@Service
@Primary
public class GraphServiceImpl implements GraphService{
	
	@Autowired
	private CustomQueryRepository userProfileRepository;
	
	@Override
	public User fetchProfile(String uid, DataFetchingEnvironment env, HttpServletRequest request) {
		try {
			String _uuid = request.getHeader("Atk");
			
			if(isEmpty(_uuid)) 
				throw new SPRuntimeError("GAPI/ME - 001",HttpStatus.FORBIDDEN);
			
			UserSessionObject user = getUserSessionDetails(request);
			
			Long loggedInUser = 0L;
			
			String decryptUid = AESDecrypt(uid);
			
			if(user == null) {
				
				if(!isServicePlusInternalCall(request)) {
					throw new SPRuntimeError("GAPI/ME - 002",HttpStatus.UNAUTHORIZED);
				}

				String clientFieldsStr = getSelectParams(env,true).toString();
				String params = clientFieldsStr.substring(1, clientFieldsStr.length() - 1);
				
				return fetchUserProfile(params,Long.parseLong(decryptUid),null);
			}
			else {
		    	loggedInUser = user.getUserID();

				if(!loggedInUser.equals(Long.parseLong(decryptUid)))
					throw new SPRuntimeError("GAPI/ME - 003",HttpStatus.UNAUTHORIZED);

                String clientFieldsStr = getSelectParams(env,true).toString();
                String params = clientFieldsStr.substring(1, clientFieldsStr.length() - 1);

                return fetchUserProfile(params,Long.parseLong(decryptUid),null);
			}
			

		} catch (Exception e) {
			if (e instanceof SPRuntimeError) {
				throw e;
			} else {
				e.printStackTrace();
				throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

    @Override
    public User specificProfile(String uid, DataFetchingEnvironment env, HttpServletRequest request) {
        String decryptUid = AESDecrypt(uid);
        String clientFieldsStr = getSelectParams(env,true).toString();
        String params = clientFieldsStr.substring(1, clientFieldsStr.length() - 1);
        UserSessionObject user = getUserSessionDetails(request);

        return fetchUserProfile(params,Long.parseLong(decryptUid),user.getUserID());
    }

    private String covertToJson(UserSessionObject user,Set<String> clientFields){
		  JSONObject jsonObject = new JSONObject();
		  try {
	        
	        for (Field field : UserSessionObject.class.getDeclaredFields()) {
	            field.setAccessible(true); // Allow access to private fields
	            
	            for(String cField : clientFields) {
		            if (cField.contains(field.getName())) {
		                // Get the field value from the userSession object
		                Object value = field.get(user);
		                jsonObject.put(cField, value);
		                break;
		            }
	            }
	           
	        }
		  }
		  catch(Exception e) {
			  e.printStackTrace();
		  }
		  
	      return jsonObject.toString();
	}

	//@Cacheable(value = "gAPIUserProfile" , key = "#uid")
	private User fetchUserProfile(String dbQuerySelectParams, Long uid, Long userID) {
		return  userProfileRepository.fetchProfile(dbQuerySelectParams, uid,userID);
	}
	
	private static Set<String> getSelectParams(DataFetchingEnvironment env,boolean fromDB) {
		List<SelectedField> fields = env.getSelectionSet().getFields();
		Set<String> clientFields = null;
		if(fromDB) {
			clientFields = fields.stream().map(f ->  f.getName().replaceAll("_", ".") + " as " + f.getName()).filter(f -> !f.equals("id")).collect(Collectors.toSet());
		}
		else {
			clientFields = fields.stream().map(SelectedField::getName).filter(f -> !f.equals("id")).collect(Collectors.toSet());
		}
		
		
		return clientFields;
	}
	
//	private static Set<String> getSelectParams(DataFetchingEnvironment env,boolean fromDB) {
//		List<SelectedField> fields = env.getSelectionSet().getFields();
//		Set<String> clientFields = null;
//		if(fromDB) {
//			clientFields = fields.stream().map(f -> "u." + f.getName() + " as " + f.getName()).filter(f -> !f.equals("id")).collect(Collectors.toSet());
//		}else {
//			clientFields = fields.stream().map(f -> f.getName()).filter(f -> !f.equals("id")).collect(Collectors.toSet());
//		}
//		
//		return clientFields;
//	}
}
