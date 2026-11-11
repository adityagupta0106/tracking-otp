package com.serviceplus.UserManagement.SSO.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sso.providers.janparichay")
public class JanParichayProperties {

    private String userOnboardingUrl;

    private String restAuthId;

    private String hmacSecret;

    public String getUserOnboardingUrl() {
        return userOnboardingUrl;
    }

    public void setUserOnboardingUrl(String userOnboardingUrl) {
        this.userOnboardingUrl = userOnboardingUrl;
    }

    public String getRestAuthId() {
        return restAuthId;
    }

    public void setRestAuthId(String restAuthId) {
        this.restAuthId = restAuthId;
    }

    public String getHmacSecret() {
        return hmacSecret;
    }

    public void setHmacSecret(String hmacSecret) {
        this.hmacSecret = hmacSecret;
    }
}