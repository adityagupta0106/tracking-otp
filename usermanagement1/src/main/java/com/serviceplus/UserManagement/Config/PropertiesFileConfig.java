package com.serviceplus.UserManagement.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({
    "classpath:Alerts.properties"
})
public class PropertiesFileConfig {

}
