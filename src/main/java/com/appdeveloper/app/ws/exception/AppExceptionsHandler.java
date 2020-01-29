package com.appdeveloper.app.ws.exception;

import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.appdeveloper.app.ws.ui.model.response.ErrorMessage;

@ControllerAdvice
public class AppExceptionsHandler {
	
	@ExceptionHandler(value= {UserServiceException.class})
	public ResponseEntity<Object> handleUserServiceException(UserServiceException ex) {
		
		ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage());
		return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);	
	}
	
	
	@ExceptionHandler(value= {Exception.class})
	public ResponseEntity<Object> handleUserServiceException(Exception ex) {
		
		ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage());
		return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);	
	}

}
