package com.appdeveloper.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.appdeveloper.app.ws.io.entity.AddressEntity;
import com.appdeveloper.app.ws.io.entity.UserEntity;
import com.appdeveloper.app.ws.io.repository.AddressRepository;
import com.appdeveloper.app.ws.io.repository.UserRepository;
import com.appdeveloper.app.ws.service.AddressService;
import com.appdeveloper.app.ws.shared.dto.AddressDto;

@Service
public class AddressServiceImpl implements AddressService {

	@Autowired
	UserRepository userRepository;
	@Autowired
	AddressRepository addressRepository;
	
	@Override
	public List<AddressDto> getUserAddresses(String userId) {
		
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UsernameNotFoundException(userId);
		
		List<AddressEntity> addressEntityList =  addressRepository.findAllByUserDetails(userEntity);
		
		ModelMapper modelmapper = new ModelMapper();
		List<AddressDto> returnValue = new ArrayList<AddressDto>();
		
		for(AddressEntity addressEntity : addressEntityList) {
			AddressDto addressDto = modelmapper.map(addressEntity, AddressDto.class);
			returnValue.add(addressDto);
		}
		
		return returnValue;
		
	}
	
	@Override
	public AddressDto getAddress(String addressId) {
		
		AddressEntity addressEntity=  addressRepository.findByAddressId(addressId);
		
		ModelMapper modelmapper = new ModelMapper();
		AddressDto returnValue = modelmapper.map(addressEntity, AddressDto.class);
		
		return returnValue;
		
	}

}
