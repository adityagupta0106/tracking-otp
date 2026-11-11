package com.serviceplus.UserManagement.Repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.serviceplus.UserManagement.Dto.CreateUserDto.SpecificLocation;
import com.serviceplus.UserManagement.Entity.BulkUserCreatedDetails;
import com.serviceplus.UserManagement.Entity.LoginDetails;
import com.serviceplus.UserManagement.Entity.RoleAssignment;
import com.serviceplus.UserManagement.Entity.UserLocation;
import com.serviceplus.UserManagement.Entity.UserProfile;
import com.serviceplus.UserManagement.Enum.ApplicationConstants;
import com.serviceplus.UserManagement.util.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserRepository extends JPADao<Long, LoginDetails>{

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @Transactional
	public List<BulkUserCreatedDetails> saveUser(Integer stateId, Integer createdbyUserId, Integer subOrgCode, Integer deptId,
                                                 Integer locationId, Integer locationTypeId, Integer role, SpecificLocation location, String stateName,
                                                 String deptName, Integer designationId, String kioskType, Boolean approvalRequired, boolean isSGA, Boolean topLevelAdmin) {

//		List<BulkUserCreatedDetails> resultList = new ArrayList<>();
//		BulkUserCreatedDetails user = new BulkUserCreatedDetails();
//
//		try {
//
//			Integer deptLocalBody = 0;
//			String areaType = "";
//
//			String query14 = "select fnc_generate_seq from schm_sp.fnc_generate_seq(16)";
//			final Query userIdSeq = em.createNativeQuery(query14);
//			BigDecimal userIdDecimal = (BigDecimal) userIdSeq.getSingleResult();
//			BigInteger userId = userIdDecimal.toBigInteger();
//
//			String query13 = "SELECT fnc_generate_user_id FROM schm_sp.fnc_generate_user_id('" + stateId + "')";
//			final Query signNoQuery = em.createNativeQuery(query13);
//			String signNo = (String) signNoQuery.getSingleResult();
//
//			String password = Utility.generateRandomString(ApplicationConstants.PASS_LIST.getString(), 8);
//
//			final String query6 = "SELECT encode(digest(md5('" + password + "'), 'sha256'), 'hex')";
//			final Query pass = em.createNativeQuery(query6);
//			String passwd = (String) pass.getSingleResult();
//
//			LoginDetails loginDetails = new LoginDetails();
//			loginDetails.setCreatedDate(new Date());
//			loginDetails.setPasswd(passwd);
//			loginDetails.setSignNo(signNo);
//			loginDetails.setSpdiStatus(0);
//			loginDetails.setUserId(userId.intValue());
//			loginDetails.setCreatedBy(createdbyUserId);
//            loginDetails.setStateId(stateId);
//            loginDetails.setTopLevelAdmin(topLevelAdmin);
//
//			loginDetails = em.merge(loginDetails);
//
//			UserProfile userProfile = new UserProfile();
//			userProfile.setDesignationId(designationId);
//			userProfile.setStateId(stateId);
//			userProfile.setUserId(loginDetails.getUserId());
//			userProfile.setStateName(stateName);
//			userProfile.setKisokType(kioskType);
//			userProfile.setApprovalRequired(approvalRequired);
//
//			em.merge(userProfile);
//
//			/*
//			 * final String query8 =
//			 * "insert into schm_sp.spe_create_user(spdi_create_user_id,spdv_first_user_name,spdv_user_email_id,spdv_user_name,spdv_user_password,spdi_state_id, "
//			 * +
//			 * "spdi_location_type_id,spdi_location_id,spdi_created_by,spdi_department_id,spdv_department_name,spdi_designation_id,spdv_designation_name,kiosk_privilege) "
//			 * + "VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,0,'','')"; final Query
//			 * createUser = em.createNativeQuery(query8); createUser.setParameter(1,
//			 * userId); createUser.setParameter(2, signNo); createUser.setParameter(3,
//			 * signNo); createUser.setParameter(4, signNo); createUser.setParameter(5,
//			 * passwd); createUser.setParameter(6, stateId); createUser.setParameter(7,
//			 * locationTypeId); createUser.setParameter(8, locationId);
//			 * createUser.setParameter(9, createdbyUserId); createUser.setParameter(10,
//			 * deptId); createUser.setParameter(11, deptName); createUser.executeUpdate();
//			 */
//
//			/*
//			 * final String query9 =
//			 * "insert into schm_sp.spe_user_department(spdv_user_name,spdi_department_localbody,spdi_departmentlevel_localbodytype,"
//			 * +
//			 * " spdi_departmentid_localbodyid,spdi_designationid,spdv_local_body_area_type,spdi_department_locatedat )  "
//			 * + "VALUES (?1,?2,?3,?4,?5,?6,?7)"; final Query userDept =
//			 * em.createNativeQuery(query9); userDept.setParameter(1, signNo);
//			 * userDept.setParameter(2, deptLocalBody); userDept.setParameter(3,
//			 * locationTypeId); userDept.setParameter(4, locationId);
//			 * userDept.setParameter(5, 0); userDept.setParameter(6, areaType);
//			 * userDept.setParameter(7, subOrgCode); userDept.executeUpdate();
//			 */
//
//			final String query3 = "SELECT nextval( 'schm_sp.user_location_user_loc_id_seq' )";
//			final Query userLocQuery = em.createNativeQuery(query3);
//			Long locIdSeq = (Long) userLocQuery.getSingleResult();
//
//			UserLocation userLocation = new UserLocation();
//			userLocation.setUserLocId(locIdSeq);
//			userLocation.setUserId(loginDetails.getUserId());
//			userLocation.setLocationId(location.getId());
//			userLocation.setLocationTypeId(locationTypeId);
//			userLocation.setDepartmentLevelId(subOrgCode);
//			userLocation.setDepartmentId(deptId);
//			userLocation.setDepartmentLevelName(deptName);
//			userLocation.setLocationName(location.getName());
//
//			em.merge(userLocation);
//
//			final String query5 = "SELECT nextval( 'schm_sp.role_assignment_user_role_id_seq' )";
//			final Query roleIdSeqQuery = em.createNativeQuery(query5);
//			Long roleIdSeq = (Long) roleIdSeqQuery.getSingleResult();
//
//			RoleAssignment roles = new RoleAssignment();
//			roles.setRoleId(role);
//			roles.setUserLocId(locIdSeq);
//			roles.setUserId(userId.intValue());
//			roles.setUser_role_id(roleIdSeq);
//
//			em.merge(roles);
//
//			user.setUserName(signNo);
//			user.setPassword(password);
//			user.setLocTypeName(location.getName());
//			user.setParentLoctationName(location.getParentName());
//			resultList.add(user);
//			return resultList;
//		} catch (Exception re) {
//		re.printStackTrace();
//	}
	return null;
}
}
