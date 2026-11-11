package com.serviceplus.UserManagement.Factory;

import com.serviceplus.UserManagement.Strategy.UserCreationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.serviceplus.UserManagement.util.ApplicationConstants.*;

@Component
public class UserCreationStrategyFactory {

    private final Map<String, UserCreationStrategy> strategies;

    @Autowired
    public UserCreationStrategyFactory(Map<String, UserCreationStrategy> strategies) {
        this.strategies = strategies;
    }

    public UserCreationStrategy getStrategy(
            Integer roleId
    ) {

        return switch (roleId) {

            case ROLE_TENANT_ADMIN,
                 ROLE_SPGD_ADMIN,
                 ROLE_SCA -> strategies.get("ROLE_TENANT_ADMIN_SCA_SPGD");

            case ROLE_DA,
                 ROLE_DEFINER,
                 ROLE_WORK_FLOW_PLAYER,
                 ROLE_EXTERNAL_APP_DEO,
                 ROLE_EXECUTIVE,
                 ROLE_CREDENTIAL_ADMIN -> strategies.get("ROLE_ORGANIZATION");

            default -> throw new RuntimeException(
                    "No strategy found"
            );
        };
    }
}