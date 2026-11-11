package com.serviceplus.UserManagement.Service;

import com.serviceplus.UserManagement.Dto.CaptchaDto;

import jakarta.servlet.http.HttpServletRequest;

public interface CaptchaService {

	CaptchaDto fetchCaptcha(HttpServletRequest request);
}
