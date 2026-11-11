package com.serviceplus.UserManagement.CustomAnnotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.serviceplus.UserManagement.util.Utility;

import jakarta.servlet.http.HttpServletRequest;

public class VerifyHeader implements ConstraintValidator<VerifyRequestHeader,HttpServletRequest> {

	@Override
	public boolean isValid(HttpServletRequest request, ConstraintValidatorContext context) {
		String sid = request.getHeader("Atk");
		if(Utility.isEmpty(sid))
			return false;
			
		return true;
	}

}
