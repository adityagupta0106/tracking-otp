package com.serviceplus.UserManagement.SSO.Properties;

import com.serviceplus.UserManagement.SSO.Provider.SSOProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sso")
public class SsoProperties {

    /**
     * Default provider configured for the application.
     */
    private SSOProvider provider;

    public SSOProvider getProvider() {
        return provider;
    }

    public void setProvider(SSOProvider provider) {
        this.provider = provider;
    }
}
