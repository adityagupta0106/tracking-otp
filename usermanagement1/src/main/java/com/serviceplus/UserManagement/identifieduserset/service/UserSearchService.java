package com.serviceplus.UserManagement.identifieduserset.service;

import com.serviceplus.UserManagement.identifieduserset.dto.UserSearchRequest;
import com.serviceplus.UserManagement.identifieduserset.dto.UserSearchResultDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserSearchService {

    UserSearchResultDto searchUser(UserSearchRequest request, HttpServletRequest httpRequest);

    List<UserSearchResultDto> searchUsers(UserSearchRequest request, HttpServletRequest httpRequest);
}
