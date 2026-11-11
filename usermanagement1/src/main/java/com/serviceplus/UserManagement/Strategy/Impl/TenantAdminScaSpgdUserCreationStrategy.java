package com.serviceplus.UserManagement.Strategy.Impl;

import com.serviceplus.UserManagement.Dto.CreateUserDto;
import com.serviceplus.UserManagement.Dto.CreateUserDto.SpecificLocation;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Strategy.UserCreationStrategy;
import com.serviceplus.UserManagement.identifieduserset.dto.UserSetResponse;
import com.serviceplus.UserManagement.identifieduserset.service.IdentifiedUserSetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.serviceplus.UserManagement.util.ApplicationConstants.*;
import static com.serviceplus.UserManagement.util.Utility.isEmpty;

@Component("ROLE_TENANT_ADMIN_SCA_SPGD")
public class TenantAdminScaSpgdUserCreationStrategy implements UserCreationStrategy {

    private final IdentifiedUserSetService identifiedUserSetService;

    @Autowired
    public TenantAdminScaSpgdUserCreationStrategy(IdentifiedUserSetService identifiedUserSetService) {
        this.identifiedUserSetService = identifiedUserSetService;
    }

    @Override
    public void process(UserSessionObject loggedInUser, CreateUserDto request, HttpServletRequest httpRequest) {

        Integer targetRole = request.getCreatedRole();

        request.setCategoryId(0);
        request.setEntityId(loggedInUser.getEntityId());
        request.setLevelId(1);
        request.setClcId(loggedInUser.getClcId());
        request.setParentUnitId(0);
        request.setAllOrSpecific(0);

        List<SpecificLocation> locations = new ArrayList<>();

        SpecificLocation location = new SpecificLocation();
        location.setEntityUnitCode(loggedInUser.getLocationId());
        location.setEntityUnitName(loggedInUser.getLocationName());
        locations.add(location);

        request.setUnitId(locations);

        if (targetRole == ROLE_SCA) {
            request.setClcId(loggedInUser.getClcId());
            validateUserSets(request, httpRequest);
        }
    }

    private void validateUserSets(CreateUserDto request, HttpServletRequest httpRequest) {

        String userSets = request.getCugTypes();

        List<UserSetResponse> allSets = identifiedUserSetService.getUserSets(httpRequest);

        Set<String> validSetIds = allSets.stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .map(type -> String.valueOf(type.getId()))
                .collect(Collectors.toSet());

        String filteredUserSets = Arrays.stream(userSets.split(","))
                                        .filter(validSetIds::contains)
                                        .collect(Collectors.joining(",")
                                        );

        if (isEmpty(filteredUserSets)) {
            throw new SPRuntimeError("Invalid identified user set", HttpStatus.BAD_REQUEST);
        }

        request.setCugTypes(filteredUserSets);
    }
}