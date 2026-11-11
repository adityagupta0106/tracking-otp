package com.serviceplus.UserManagement.Service;

import static com.serviceplus.UserManagement.util.ApplicationConstants.ROLE_DA;
import static com.serviceplus.UserManagement.util.ApplicationConstants.ROLE_EXECUTIVE;
import static com.serviceplus.UserManagement.util.Utility.isEmpty;
import static java.util.Objects.isNull;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;
import com.serviceplus.UserManagement.ApiExecutor.ApiExecutor;
import com.serviceplus.UserManagement.Dto.CreateUserDto;
import com.serviceplus.UserManagement.Dto.CreateUserDto.SpecificLocation;
import com.serviceplus.UserManagement.Dto.LevelWiseUnitRequest;
import com.serviceplus.UserManagement.Dto.ListUserAssignment;
import com.serviceplus.UserManagement.Dto.ServerSidePaginationRecord;
import com.serviceplus.UserManagement.Dto.UserAssignmentSearchRequest;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Repository.RoleAssignmentRepository;
import com.serviceplus.UserManagement.Repository.UserLocationDesignationRepository;
import com.serviceplus.UserManagement.Repository.UserLocationRepository;
import com.serviceplus.UserManagement.Repository.UserProfileRepository;
import com.serviceplus.UserManagement.util.Utility;

@Service
public class UserDirectoryService {

	@Value("${lgd.service}")
	private String LGD_SERVICE;

	private static final Logger Logger = LogManager.getLogger("userDirectoryService");

    private static final int MAX_PER_PAGE_COUNT = 50;

    private final UserLocationRepository userLocationRepository;

    private final RoleAssignmentRepository roleAssignmentRepository;

    private final UserLocationDesignationRepository userLocationDesignationRepository;

    private final UserProfileRepository userProfileRepository;

    @Autowired
	private ApiExecutor<?> apiExecutor;
    
    @Autowired
    public UserDirectoryService(
            UserLocationRepository userLocationRepository,
            RoleAssignmentRepository roleAssignmentRepository,
            UserLocationDesignationRepository userLocationDesignationRepository,
            UserProfileRepository userProfileRepository) {

        this.userLocationRepository = userLocationRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.userLocationDesignationRepository = userLocationDesignationRepository;
        this.userProfileRepository = userProfileRepository;
    }

	public ServerSidePaginationRecord<ListUserAssignment> getUsersByLocation(UserSessionObject loggedInUser,
			UserAssignmentSearchRequest request) {
		return getUsers(loggedInUser, request, Collections.singletonList(ROLE_EXECUTIVE));
	}
	public ServerSidePaginationRecord<ListUserAssignment> getUsersByRoles(UserSessionObject loggedInUser,
			UserAssignmentSearchRequest request) {
		return getUsers(loggedInUser, request, Arrays.asList(ROLE_EXECUTIVE, ROLE_DA));
	}

	private ServerSidePaginationRecord<ListUserAssignment> getUsers(UserSessionObject loggedInUser,
			UserAssignmentSearchRequest request, List<Integer> roleIds) {

		try {
			if(request.getLevelId() == null){
				request.setLevelId(loggedInUser.getEntityLevelId());
			}
			if(request.getLocationId() == null){
				request.setLevelId(loggedInUser.getLocationId());
			}
			
			Boolean validLocationId = validateLocationId(request, loggedInUser);
			if(!validLocationId) {
	        	throw new SPRuntimeError("Invalid Location", HttpStatus.BAD_REQUEST);
	        }
			
			request.setPageNo(isNull(request.getPageNo()) ? 0 : request.getPageNo());
			request.setPageSize(Math.min(request.getPageSize(), MAX_PER_PAGE_COUNT));

			Pageable pageable = PageRequest.of(request.getPageNo(), request.getPageSize());

			List<String> signNos = isEmpty(request.getSignNo()) ? null
					: Arrays.stream(request.getSignNo().split(",")).map(String::trim).filter(s -> !s.isBlank())
							.toList();

			boolean applySignFilter = signNos != null && !signNos.isEmpty();

			Page<ListUserAssignment> page = userLocationRepository.findUsersByRoles(loggedInUser.getLocationId(),
					roleIds, signNos, applySignFilter, pageable);

			ServerSidePaginationRecord<ListUserAssignment> response = new ServerSidePaginationRecord<>();

			response.setData(page.getContent());
			response.setTotalRecords(page.getTotalElements());
			response.setCurrentPage(page.getNumber());
			response.setPageSize(page.getSize());
			response.setTotalPages(page.getTotalPages());

			Logger.info("Fetched {} users for locationId={} roleIds={}", page.getTotalElements(),
					loggedInUser.getLocationId(), roleIds);

			return response;

		} catch (Exception ex) {

			Logger.error("Failed to fetch users for locationId={} tenantId={} roleIds={}", loggedInUser.getLocationId(),
					loggedInUser.getTenantId(), roleIds, ex);

			throw ex;
		}
	}

	private Boolean validateLocationId(UserAssignmentSearchRequest request, UserSessionObject sessionObject) {
		boolean flag = false;
		String url = LGD_SERVICE.concat("levelWiseUnits");

		LevelWiseUnitRequest body = new LevelWiseUnitRequest();

		body.setLevelCode(request.getLevelId());
		body.setEntityCode(sessionObject.getEntityId());
		body.setClc(sessionObject.getClcId() == null ? 0 : sessionObject.getClcId());
		body.setParentUnitCode(sessionObject.getLocationId() == null ? 0 : sessionObject.getLocationId());
		body.setLocationCode(request.getLocationId());

		if (!body.getParentUnitCode().equals(body.getLocationCode())) {			
			ResponseEntity<?> apiResponse = apiExecutor.callExternalEndpoint(String.class, HttpMethod.POST, new HashMap<>(),
					new HashMap<>(), url, Utility.entityToString(body), MediaType.APPLICATION_JSON);
			if (apiResponse == null || apiResponse.getBody() == null) {
				Type listType = new TypeToken<List<CreateUserDto.SpecificLocation>>() {
				}.getType();
				List<CreateUserDto.SpecificLocation> locations = (List<CreateUserDto.SpecificLocation>) Utility
						.stringToEntityUsingType((String) apiResponse.getBody(), listType);
				
				for (SpecificLocation location : locations) {
					if (request.getLocationId().equals(location.getEntityUnitCode())) {
						flag = true;
						break;
					}
				}
			}
		}else {
			flag = true;
		}
		
		return flag;
	}
}