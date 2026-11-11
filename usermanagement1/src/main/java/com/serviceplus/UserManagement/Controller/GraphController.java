package com.serviceplus.UserManagement.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.serviceplus.UserManagement.Dto.User;
import com.serviceplus.UserManagement.Service.GraphService;

import graphql.schema.DataFetchingEnvironment;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class GraphController {
	
	  @Autowired
	  private GraphService graphService;	  
	
	  @QueryMapping
	  public User profile(@Argument String id,DataFetchingEnvironment env) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	    return graphService.fetchProfile(id,env,request);
	  }

      @QueryMapping
      public User specificProfile(@Argument String id,DataFetchingEnvironment env) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return graphService.specificProfile(id,env,request);
      }
	 
	
}
