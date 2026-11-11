package com.serviceplus.UserManagement.Strategy;

import com.serviceplus.UserManagement.Dto.CreateUserDto;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import jakarta.servlet.http.HttpServletRequest;

public interface UserCreationStrategy {

    void process(
            UserSessionObject loggedInUser,
            CreateUserDto request,
            HttpServletRequest httpRequest
    );
}