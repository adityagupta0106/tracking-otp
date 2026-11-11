package com.serviceplus.UserManagement.SSO.Provider;

import com.serviceplus.UserManagement.Dto.ProvisionedUserResult;
import jakarta.servlet.http.HttpServletRequest;

public interface IdentityProvider {

    /**
     * Unique provider.
     * Example:
     * JANPARICHAY
     * DIGILOCKER
     */
    SSOProvider getProvider();

    Boolean registerUser(HttpServletRequest request, ProvisionedUserResult requestDto);
}