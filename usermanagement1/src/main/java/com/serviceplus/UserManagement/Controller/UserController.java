package com.serviceplus.UserManagement.Controller;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.serviceplus.UserManagement.Dto.EntityLevelDetailsDto;
import com.serviceplus.UserManagement.Dto.UserCreationHistoryRequest;
import com.serviceplus.UserManagement.Service.LGDService;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.serviceplus.UserManagement.Dto.FetchRoleDto;
import com.serviceplus.UserManagement.Dto.FetchRoleResponseDto;
import com.serviceplus.UserManagement.Entity.AdminUserCreationAccess;
import com.serviceplus.UserManagement.Entity.RoleMaster;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.Entity.UserSessionObject.Roles;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Repository.AdminUserCreationAccessRepository;
import com.serviceplus.UserManagement.Service.UserService;

import jakarta.servlet.http.HttpServletRequest;

import static com.serviceplus.UserManagement.util.ApplicationConstants.*;
import static com.serviceplus.UserManagement.util.Utility.*;

@RestController
@RequestMapping("/a")
public class UserController {

	private final UserService userService;

	private final AdminUserCreationAccessRepository adminUserCreationAccessRepository;

    private final LGDService lgdService;

    @Autowired
    public UserController(UserService userService, AdminUserCreationAccessRepository adminUserCreationAccessRepository, LGDService lgdService) {
        this.userService = userService;
        this.adminUserCreationAccessRepository = adminUserCreationAccessRepository;
        this.lgdService = lgdService;
    }

    @PostMapping("roles")
	public FetchRoleResponseDto getRoles(@RequestBody FetchRoleDto dto,HttpServletRequest request) throws Exception {
		try {

			if(isEmpty(dto.getUserId()))
				throw new SPRuntimeError("Invalid Request -- 001", HttpStatus.BAD_REQUEST);			

			String receivedId = dto.getUserId();
			String aesDecrypt = AESDecrypt(receivedId);
			dto.setUserId(aesDecrypt);
			
			UserSessionObject user = getUserSessionDetails(request);
			
			if (user != null) {							
				
				if(!user.getUserID().toString().equals(aesDecrypt))
					throw new SPRuntimeError("Invalid Request -- 002", HttpStatus.FORBIDDEN);
				
				return checkUserRoles(dto,user.getRoles());
			}
			else {
				if(isServicePlusInternalCall(request)) {
					String atk = request.getHeader("Atk");
					return checkInMainDB(dto,atk);
				}
				else {
					throw new SPRuntimeError("UNAUTH-R[01]", HttpStatus.FORBIDDEN);
				}
			}		
			
		}
		catch(Exception e) {
			
			if(e instanceof SPRuntimeError) {
				throw e;
			}
			else {
				e.printStackTrace();
				throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	@PostMapping("createUserWithRole")
	public Object createUserWithRole(@RequestBody FetchRoleDto dto,HttpServletRequest request) throws Exception {
		FetchRoleResponseDto roles = getRoles(dto,request);
		List<Roles> role = roles.getData().getRoles();

		List<Integer> roleIds = role.stream().map(Roles::getRoleId).collect(Collectors.toList());
		List<AdminUserCreationAccess> roleAccess = adminUserCreationAccessRepository.findByLoggedInRoleIn(roleIds);

        UserSessionObject user = Objects.requireNonNull(getUserSessionDetails(request));

        if(hasRole(user,ROLE_DA)){
            EntityLevelDetailsDto levelDetails = lgdService.getEntityLevelDetails(user.getEntityId(), user.getEntityLevelId());
            if(ENTITY_LEVEL_TOP != levelDetails.getSortOrder()){
                roleAccess = roleAccess.stream().filter(ra -> ra.getRoleId() != ROLE_DEFINER).toList();
            }
        }
        
        roleAccess = roleAccess.stream().filter(ra -> ra.getRoleId() != ROLE_EXTERNAL_APP_DEO).toList();
        
		
		return roleAccess;
	}
	
	@PostMapping("fetchUserList")
	public ResponseEntity<?> fetchUserList(@RequestBody FetchRoleDto dto,HttpServletRequest request){
		return userService.fetchUserList(dto,request);
	}
	
	private FetchRoleResponseDto checkInMainDB(FetchRoleDto dto,String uuid){
		List<Roles> roles = userService.getRoles(Integer.parseInt(dto.getUserId()));
		return checkUserRoles(dto,roles);
	}
	
	
	private FetchRoleResponseDto checkUserRoles(FetchRoleDto dto,List<Roles> roles) {
		FetchRoleResponseDto.Data data = new FetchRoleResponseDto.Data(roles);
		return new FetchRoleResponseDto(data);
	}

    @PostMapping("/createdUser/history")
    public ResponseEntity<?> fetchUserCreationHistory(HttpServletRequest request, @RequestBody UserCreationHistoryRequest historyRequest) {

        UserSessionObject loggedInUser = getUserSessionDetails(request);

        return ResponseEntity.ok(userService.fetchHistory(loggedInUser, historyRequest));
    }

    @PostMapping("/createdUser/history/download")
    public ResponseEntity<?> downloadUserCreationExcel(@RequestParam Long historyId, HttpServletRequest request) {

        UserSessionObject loggedInUser = Objects.requireNonNull(getUserSessionDetails(request));

        return ResponseEntity.ok(userService.downloadExcel(historyId, loggedInUser));
    }
}
