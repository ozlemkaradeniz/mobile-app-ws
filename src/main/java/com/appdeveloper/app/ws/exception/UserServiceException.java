package com.appdeveloper.app.ws.exception;

public class UserServiceException extends RuntimeException{

	private static final long serialVersionUID = -6329234461989660842L;
	
	public UserServiceException(String errorMessage) {
		super(errorMessage);
	}
}
