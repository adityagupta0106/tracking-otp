package com.serviceplus.UserManagement.Service;

import com.serviceplus.UserManagement.Dto.User;

import graphql.schema.DataFetchingEnvironment;
import jakarta.servlet.http.HttpServletRequest;

public interface GraphService {

	User fetchProfile(String id,DataFetchingEnvironment env, HttpServletRequest request);

    User specificProfile(String id, DataFetchingEnvironment env, HttpServletRequest request);
}
