package com.serviceplus.UserManagement.identifieduserset.service.impl;

import com.serviceplus.UserManagement.Entity.LoginDetails;
import com.serviceplus.UserManagement.Entity.UserProfile;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Repository.LoginDetailsRepository;
import com.serviceplus.UserManagement.Repository.UserDetailsRepository;
import com.serviceplus.UserManagement.Repository.UserProfileRepository;
import com.serviceplus.UserManagement.identifieduserset.dto.UserSearchRequest;
import com.serviceplus.UserManagement.identifieduserset.dto.UserSearchResultDto;
import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSetMember;
import com.serviceplus.UserManagement.identifieduserset.enums.UserAccountStatus;
import com.serviceplus.UserManagement.identifieduserset.repository.IdentifiedUserSetMemberRepository;
import com.serviceplus.UserManagement.identifieduserset.repository.IdentifiedUserSetRepository;
import com.serviceplus.UserManagement.identifieduserset.service.UserSearchService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.serviceplus.UserManagement.util.Utility.getUserSessionDetails;
import static com.serviceplus.UserManagement.util.Utility.isEmpty;

@Service
public class UserSearchServiceImpl implements UserSearchService {

    private static final Logger log = LogManager.getLogger(UserSearchServiceImpl.class);

    private final UserProfileRepository userProfileRepository;
    private final LoginDetailsRepository loginDetailsRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final IdentifiedUserSetMemberRepository memberRepository;
    private final IdentifiedUserSetRepository userSetRepository;

    @Autowired
    public UserSearchServiceImpl(UserProfileRepository userProfileRepository,
                                 LoginDetailsRepository loginDetailsRepository,
                                 UserDetailsRepository userDetailsRepository,
                                 IdentifiedUserSetMemberRepository memberRepository,
                                 IdentifiedUserSetRepository userSetRepository) {
        this.userProfileRepository = userProfileRepository;
        this.loginDetailsRepository = loginDetailsRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.memberRepository = memberRepository;
        this.userSetRepository = userSetRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserSearchResultDto searchUser(UserSearchRequest request, HttpServletRequest httpRequest) {
        List<UserSearchResultDto> results = searchUsers(request, httpRequest);
        if (results.isEmpty()) {
            throw new SPRuntimeError("User not found matching search criteria", HttpStatus.NOT_FOUND);
        }
        return results.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSearchResultDto> searchUsers(UserSearchRequest request, HttpServletRequest httpRequest) {
        if (request == null || isEmpty(request.getQuery())) {
            throw new SPRuntimeError("Search query is mandatory and cannot be empty", HttpStatus.BAD_REQUEST);
        }

        String query = request.getQuery().trim();

        UserSessionObject sessionUser = getUserSessionDetails(httpRequest);
        String tenantFilter = sessionUser != null ? sessionUser.getTenantId() : null;

        LoginDetails login = null;
        UserProfile profile = null;

        // 1. Search directly by email in DB
        profile = userProfileRepository.findByEmailId(query);
        if (profile != null) {
            login = loginDetailsRepository.findByUserId(profile.getUserId()).orElse(null);
        }

        // 2. Search directly by mobile in DB (if not found by email)
        if (login == null) {
            profile = userProfileRepository.findByMobileNo(query);
            if (profile != null) {
                login = loginDetailsRepository.findByUserId(profile.getUserId()).orElse(null);
            }
        }

        // 3. Search directly by providerUserId (sso_provider_user_id) in DB (if not found by email or mobile)
        if (login == null) {
            login = loginDetailsRepository.findBySsoProviderUserId(query).orElse(null);
            if (login != null && profile == null) {
                profile = userProfileRepository.findByUserId(login.getUserId());
            }
        }

        // Do NOT search by signNo!

        if (login == null) {
            return Collections.emptyList();
        }

        if (tenantFilter != null && !tenantFilter.trim().isEmpty() && login.getTenantId() != null && !tenantFilter.equals(login.getTenantId())) {
            return Collections.emptyList();
        }

        UserSearchResultDto dto = buildUserSearchResult(profile, login);
        return List.of(dto);
    }

    private UserSearchResultDto buildUserSearchResult(UserProfile profile, LoginDetails login) {
        UserSearchResultDto dto = new UserSearchResultDto();
        dto.setUserId(login.getUserId());
        dto.setSignNo(login.getSignNo());
        dto.setSsoProviderUserId(login.getSsoProviderUserId());
        dto.setStatus(login.getSpdiStatus() != null && (login.getSpdiStatus() == 3 || login.getSpdiStatus() == 1) ? UserAccountStatus.ACTIVE : UserAccountStatus.INACTIVE);

        if (profile != null) {
            dto.setUserName(profile.getUserName());
            dto.setEmailId(profile.getEmailId());
            dto.setMobileNo(profile.getMobileNo());
        }

        try {
            List<Object[]> rolesData = userDetailsRepository.fetchUserRoles(login.getUserId().intValue());
            List<String> roleNames = new ArrayList<>();
            for (Object[] r : rolesData) {
                if (r.length > 1 && r[1] != null) {
                    roleNames.add(r[1].toString());
                }
            }
            dto.setRoles(roleNames);
        } catch (Exception e) {
            log.warn("Could not fetch roles for user {}: {}", login.getUserId(), e.getMessage());
        }

        try {
            List<IdentifiedUserSetMember> memberships;
            if (login.getTenantId() != null && !login.getTenantId().trim().isEmpty()) {
                memberships = memberRepository.findByUserIdAndTenantIdAndActive(login.getUserId(), login.getTenantId(), true);
            } else {
                memberships = memberRepository.findByUserIdAndActive(login.getUserId(), true);
            }

            List<String> setNames = new ArrayList<>();
            for (IdentifiedUserSetMember m : memberships) {
                userSetRepository.findById(m.getUserSetId()).ifPresent(s -> setNames.add(s.getName()));
            }
            dto.setIdentifiedUserSets(setNames);
        } catch (Exception e) {
            log.warn("Could not fetch user set memberships for user {}: {}", login.getUserId(), e.getMessage());
        }

        return dto;
    }
}
