package com.serviceplus.UserManagement.Controller;

import com.serviceplus.UserManagement.Dto.ListUserAssignment;
import com.serviceplus.UserManagement.Dto.ServerSidePaginationRecord;
import com.serviceplus.UserManagement.Dto.UserAssignmentSearchRequest;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.Service.UserDirectoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

import static com.serviceplus.UserManagement.util.Utility.getUserSessionDetails;

@RestController
@RequestMapping("/a/user-directory")
public class UserDirectoryController {

    @Autowired
    private UserDirectoryService userDirectoryService;

    @PostMapping("/executives")
    public ResponseEntity<?> getUsers(
            HttpServletRequest request,
            @RequestBody UserAssignmentSearchRequest searchRequest) {

        UserSessionObject loggedInUser = Objects.requireNonNull(getUserSessionDetails(request));

        ServerSidePaginationRecord<ListUserAssignment> response =
                userDirectoryService.getUsersByLocation(
                        loggedInUser,
                        searchRequest);

        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/users")
    public ResponseEntity<?> getUsersByRoles(
            HttpServletRequest request,
            @RequestBody UserAssignmentSearchRequest searchRequest) {

        UserSessionObject loggedInUser =
                Objects.requireNonNull(getUserSessionDetails(request));

        ServerSidePaginationRecord<ListUserAssignment> response =
                userDirectoryService.getUsersByRoles(
                        loggedInUser,
                        searchRequest);

        return ResponseEntity.ok(response);
    }
}
