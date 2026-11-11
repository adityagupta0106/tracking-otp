package com.serviceplus.UserManagement.Repository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import com.serviceplus.UserManagement.Entity.UserSessionObject;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;

import com.serviceplus.UserManagement.Dto.FetchUserListResponseDto;
import com.serviceplus.UserManagement.Dto.User;
import org.springframework.transaction.annotation.Transactional;

import static com.serviceplus.UserManagement.util.ApplicationConstants.SP_SCHEMA_NAME;
import static com.serviceplus.UserManagement.util.Utility.AESEncrypt;

@Repository
public class CustomQueryRepository  extends JPADao<Long, Object>{

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
	public User fetchProfile(String selectFields, Long uid, Long userID) {
		try {
            String condition = userID == null ? "" : " AND ld.createdBy = :loggedInUser";

			String queryString = "SELECT "+ selectFields +" FROM UserProfile u  "
					+ " INNER JOIN UserLocation ul on u.userId = ul.userId "
                    + " INNER JOIN LoginDetails ld on ld.userId = ul.userId "
                    + " where u.userId=:uid ".concat(condition);
			final TypedQuery<Tuple> query = em.createQuery(queryString,Tuple.class);
			query.setParameter("uid", uid);
            if(userID != null){
                query.setParameter("loggedInUser", userID);
            }
			List<Tuple> resultList = query.getResultList();
			
			if (resultList.isEmpty()) {
		        return new User();
		    }

		    User userProfile = new User();
		    
		    Tuple tuple = resultList.get(0);

		    for (int i = 0; i < tuple.getElements().size(); i++) {
		        String alias = tuple.getElements().get(i).getAlias();
		        Object value = tuple.get(i);
		        if(alias.equals("u_profilePic")) {
		        	if(value != null) {
		        		byte[] imageBytes = (byte[]) value;
		        		value = Base64.getEncoder().encodeToString(imageBytes);
		        	}
		        }
		        Field field = User.class.getDeclaredField(alias);
		        field.setAccessible(true); //To access private fields
		        field.set(userProfile, value);
		    }
		    
		    
		    return userProfile;//return list
		}
		catch(Exception e) {
			e.printStackTrace();
			return new User();
		}
	}

	@SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
	public FetchUserListResponseDto fetchUserList(Long userID) {
	    FetchUserListResponseDto dto = new FetchUserListResponseDto();
		try {	
			String queryString = "SELECT ld.sign_no, " +
			                    "up.user_name, " +
			                    "ul.location_name, " +
			                    "ld.c_date, " +
			                    "array_to_string(array_agg(rm.role_name), ',') AS role_names, " +
			                    "ld.spdi_status ,ld.user_id " +
			                    "FROM  " + SP_SCHEMA_NAME + ".login_details ld " +
			                    "LEFT JOIN " + SP_SCHEMA_NAME + ".user_profile up ON up.user_id = ld.user_id " +
			                    "LEFT JOIN " + SP_SCHEMA_NAME + ".user_location ul ON up.user_id = ul.user_id " +
			                    "LEFT JOIN " + SP_SCHEMA_NAME + ".role_assignment rl ON rl.user_id = ul.user_id " +
			                    "LEFT JOIN " + SP_SCHEMA_NAME + ".role_master rm ON rl.role_id = rm.role_id " +
			                    "WHERE ld.created_by = :uid " +
			                    "GROUP BY ld.sign_no, up.user_name, ul.location_name, ld.c_date, ld.spdi_status,ld.user_id";
			Query query = em.createNativeQuery(queryString);
			query.setParameter("uid", userID);
			
		    List<Object[]> resultList = query.getResultList();
		    
		    List<FetchUserListResponseDto.Data> data = new ArrayList<>();
		    
		    resultList.forEach(result -> {
		    	FetchUserListResponseDto.Data user = new FetchUserListResponseDto.Data();
		    	user.setUserId((String) result[0]);
		    	user.setUserName((String) result[1]);
		    	user.setLocation((String) result[2]);
		    	user.setProfileCreatedOn((Date) result[3]);
		    	user.setRoles((String) result[4]);
		    	user.setStatus((Integer) result[5]);
                user.seteUserId(AESEncrypt(String.valueOf(result[6])));
		    	data.add(user);
		    });
		    
		    dto.setData(data);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	    return dto;
	}

//    public boolean ifSGAExist(UserSessionObject user,Integer roleToCheck) {
//        EntityManager em = getEntityManager();
//        try {
//            String queryString = "SELECT * FROM LoginDetails ld INNER JOIN RoleAssignment rl ON" +
//                                " ld.userId = rl.";
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        } finally {
//            em.close();
//        }
//    }
}
