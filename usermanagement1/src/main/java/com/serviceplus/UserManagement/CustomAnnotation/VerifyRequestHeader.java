package com.serviceplus.UserManagement.CustomAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;

@Target(ElementType.METHOD) 
@Retention(RetentionPolicy.RUNTIME) 
@Documented
@Constraint(validatedBy = VerifyHeader.class)
public @interface VerifyRequestHeader {

	 String message()  default "Malformed Request";
}
