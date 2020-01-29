package com.appdeveloper.app.ws.io.repository;

import org.springframework.stereotype.Repository;

import com.appdeveloper.app.ws.io.entity.AddressEntity;
import com.appdeveloper.app.ws.io.entity.UserEntity;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

@Repository
public interface AddressRepository extends CrudRepository<AddressEntity, Long> {
	
	public List<AddressEntity> findAllByUserDetails(UserEntity userEntity);
	public AddressEntity findByAddressId(String addressId);

}
