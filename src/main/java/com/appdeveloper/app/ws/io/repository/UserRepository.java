package com.appdeveloper.app.ws.io.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import org.springframework.stereotype.Repository;

import com.appdeveloper.app.ws.io.entity.UserEntity;

@Repository
public interface UserRepository extends  PagingAndSortingRepository<UserEntity, Long>{

	public UserEntity findByEmail(String email);
	public UserEntity findByUserId(String userId);
	public UserEntity findByLastname(String lastName);	
	public UserEntity findUserByEmailVerificationToken(String token);
	
}
