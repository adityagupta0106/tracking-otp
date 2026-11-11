package com.serviceplus.UserManagement.ExceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(value = SPRuntimeError.class)
	public ResponseEntity<ErrorDetails> handler(SPRuntimeError  ex, HttpServletRequest request){
		ErrorDetails err = new ErrorDetails(ex.getMessage(),ex.getErrorCode(),ex.getErrorCode().value());
		return ResponseEntity.status(ex.getErrorCode()).body(err);
	}
	
	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ErrorDetails> genericMessage(Exception  ex, HttpServletRequest request){
        ex.printStackTrace();
		ErrorDetails err = new ErrorDetails("Something went wrong",HttpStatus.INTERNAL_SERVER_ERROR,HttpStatus.INTERNAL_SERVER_ERROR.value());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
	}
}
