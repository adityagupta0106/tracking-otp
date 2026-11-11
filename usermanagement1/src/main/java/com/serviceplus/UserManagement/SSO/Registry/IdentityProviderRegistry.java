package com.serviceplus.UserManagement.SSO.Registry;

import com.serviceplus.UserManagement.SSO.Provider.IdentityProvider;
import com.serviceplus.UserManagement.SSO.Provider.SSOProvider;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class IdentityProviderRegistry {

    private final List<IdentityProvider> identityProviders;

    private final Map<SSOProvider, IdentityProvider> registry = new EnumMap<>(SSOProvider.class);

    public IdentityProviderRegistry(List<IdentityProvider> identityProviders) {
        this.identityProviders = identityProviders;
    }

    @PostConstruct
    public void initialize() {

        for (IdentityProvider provider : identityProviders) {
            registry.put(provider.getProvider(), provider);
        }
    }

    public IdentityProvider getProvider(SSOProvider provider) {

        IdentityProvider identityProvider = registry.get(provider);

        if (identityProvider == null) {
            throw new IllegalArgumentException("Unsupported SSO Provider : " + provider);
        }

        return identityProvider;
    }
}
