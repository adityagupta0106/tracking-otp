package com.serviceplus.UserManagement.CustomAnnotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SanitizeRequest {
}

