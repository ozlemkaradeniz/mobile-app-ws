package com.appdeveloper.app.ws.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.appdeveloper.app.ws.shared.dto.UserDto;

public interface UserService extends UserDetailsService{
	
	UserDto createUser(UserDto userRequest);
	UserDto updateUser(String userId, UserDto userRequest);
	UserDto getUser(String email);
	UserDto getUserByUserId(String userId);
	UserDto getUserByLastName(String lastName);
	UserDto deleteUser(String userId);
	List<UserDto> getUsers(int page, int limit);
	boolean verifyEmailToken(String token);
	boolean requestPasswordReset(String email);
	boolean resetPassword(String token, String password);
}
