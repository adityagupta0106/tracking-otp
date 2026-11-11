package com.serviceplus.UserManagement.Strategy.Impl;

import com.serviceplus.UserManagement.Dto.CreateUserDto;
import com.serviceplus.UserManagement.Dto.CreateUserDto.SpecificLocation;
import com.serviceplus.UserManagement.Dto.EntityLevelDetailsDto;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Service.LGDService;
import com.serviceplus.UserManagement.Strategy.UserCreationStrategy;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.serviceplus.UserManagement.util.ApplicationConstants.ROLE_DA;
import static com.serviceplus.UserManagement.util.ApplicationConstants.ROLE_TENANT_ADMIN;
import static com.serviceplus.UserManagement.util.Utility.hasRole;
import static java.util.Objects.isNull;

@Component("ROLE_ORGANIZATION")
public class OrganizationHierarchyUserCreationStrategy implements UserCreationStrategy {

    private static final Logger UserCreationLogger = LogManager.getLogger("userCreationLogger");

    private final LGDService lgdService;

    @Autowired
    public OrganizationHierarchyUserCreationStrategy(LGDService lgdService) {
        this.lgdService = lgdService;
    }

    @Override
    public void process(UserSessionObject loggedInUser, CreateUserDto request, HttpServletRequest httpRequest) {

        boolean tenantAdmin = hasRole(loggedInUser, ROLE_TENANT_ADMIN);

        boolean departmentAdmin = hasRole(loggedInUser, ROLE_DA);

        validateHierarchy(loggedInUser, request, tenantAdmin, departmentAdmin);

        resolveLocations(loggedInUser, request, tenantAdmin);
    }

    private void validateHierarchy(UserSessionObject loggedInUser, CreateUserDto request, boolean tenantAdmin, boolean departmentAdmin) {

        UserCreationLogger.info(
                "Resolving hierarchy details categoryId={} entityId={} levelId={} clcId={}",
                request.getCategoryId(),
                request.getEntityId(),
                request.getLevelId(),
                request.getClcId());

        boolean IS_CREATING_DA = request.getCreatedRole().equals(ROLE_DA);

        if (tenantAdmin) {

            if (request.getCategoryId() == null) {
                throw new SPRuntimeError("Category is required", HttpStatus.BAD_REQUEST);
            }

            if (request.getEntityId() == null) {
                throw new SPRuntimeError("Entity is required", HttpStatus.BAD_REQUEST);
            }

            if (request.getLevelId() == null) {
                throw new SPRuntimeError("Level is required", HttpStatus.BAD_REQUEST);
            }
        }

        if (departmentAdmin) {
            request.setEntityId(loggedInUser.getEntityId());

            if(isNull(request.getLevelId())){
                if(!IS_CREATING_DA) {
                    request.setLevelId(loggedInUser.getEntityLevelId());
                }
                else {
                    throw new SPRuntimeError("Level is required 01", HttpStatus.BAD_REQUEST);
                }
            }
            else if(IS_CREATING_DA && loggedInUser.getEntityLevelId().equals(request.getLevelId())){
                    throw new SPRuntimeError("Cannot create department admin of same level", HttpStatus.BAD_REQUEST);
            }
        }

        EntityLevelDetailsDto hierarchy = lgdService.getEntityLevelDetails(request.getEntityId(), request.getLevelId());

        request.setEntityName(hierarchy.getEntityName());
        request.setEntityLevelName(hierarchy.getLevelName());
    }

    private void resolveLocations(UserSessionObject loggedInUser, CreateUserDto request, boolean tenantAdmin) {

        UserCreationLogger.info(
                "Fetching locations categoryId={} entityId={} levelId={} parentUnitId={} allOrSpecific={}",
                request.getCategoryId(),
                request.getEntityId(),
                request.getLevelId(),
                request.getParentUnitId(),
                request.getAllOrSpecific());

        List<SpecificLocation> locations = lgdService.fetchLocations(
                                                        request.getCategoryId(),
                                                        request.getEntityId(),
                                                        request.getLevelId(),
                                                        request.getParentUnitId(),
                                                        loggedInUser.getLocationId(),
                                                        request.getClcId(),
                                                        request.getAllOrSpecific(),
                                                        request.getUnitId(),
                                                        tenantAdmin
                                                );

        if (locations == null || locations.isEmpty()) {
            throw new SPRuntimeError("No locations found (LGD)", HttpStatus.BAD_REQUEST);
        }

        request.setUnitId(locations);

        UserCreationLogger.info(
                "Resolved {} locations for entityId={}",
                locations.size(),
                request.getEntityId());
    }

}