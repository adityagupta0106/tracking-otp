package com.serviceplus.UserManagement.identifieduserset.controller;

import com.serviceplus.UserManagement.identifieduserset.dto.UserSearchRequest;
import com.serviceplus.UserManagement.identifieduserset.dto.UserSearchResultDto;
import com.serviceplus.UserManagement.identifieduserset.service.UserSearchService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/a/user-search")
public class UserSearchController {

    private final UserSearchService userSearchService;

    @Autowired
    public UserSearchController(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }

    @PostMapping
    public ResponseEntity<UserSearchResultDto> searchUser(@RequestBody UserSearchRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(userSearchService.searchUser(request, httpRequest));
    }

    @GetMapping
    public ResponseEntity<List<UserSearchResultDto>> searchUsers(@RequestParam(required = false) String query,
                                                                 @RequestParam(required = false) String email,
                                                                 @RequestParam(required = false) String mobile,
                                                                 @RequestParam(required = false) String providerUserId,
                                                                 HttpServletRequest httpRequest) {
        String searchQuery = query;
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            if (email != null && !email.trim().isEmpty()) {
                searchQuery = email;
            } else if (mobile != null && !mobile.trim().isEmpty()) {
                searchQuery = mobile;
            } else if (providerUserId != null && !providerUserId.trim().isEmpty()) {
                searchQuery = providerUserId;
            }
        }
        UserSearchRequest req = new UserSearchRequest(searchQuery);
        return ResponseEntity.ok(userSearchService.searchUsers(req, httpRequest));
    }
}
