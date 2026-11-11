package com.serviceplus.UserManagement.TenantFilter;

import com.serviceplus.UserManagement.Entity.UserSessionObject;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.serviceplus.UserManagement.util.Utility.getUserSessionDetails;

@Component
@Order(1)
public class TenantRequestFilter implements Filter{

    @Autowired
    private TenantInterceptor tenantInterceptor;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        UserSessionObject user = getUserSessionDetails(httpRequest);

        if(user != null) {
            String tenantId = user.getTenantId();

            if (tenantId != null) {
              //  tenantInterceptor.enableTenantFilter(tenantId);
            }
        }

        chain.doFilter(request, response);
    }
}
