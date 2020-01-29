package com.appdeveloper.app.ws.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.appdeveloper.app.ws.exception.UserServiceException;
import com.appdeveloper.app.ws.io.entity.AddressEntity;
import com.appdeveloper.app.ws.io.entity.UserEntity;
import com.appdeveloper.app.ws.io.repository.UserRepository;
import com.appdeveloper.app.ws.service.impl.UserServiceImpl;
import com.appdeveloper.app.ws.shared.AmazonSES;
import com.appdeveloper.app.ws.shared.Utils;
import com.appdeveloper.app.ws.shared.dto.AddressDto;
import com.appdeveloper.app.ws.shared.dto.UserDto;

class UserServiceImplTest {

	@InjectMocks
	UserServiceImpl userService;

	@Mock
	UserRepository userRepository;
 
	@Mock
	Utils utils;
	
	@Mock
	AmazonSES amazonSES;

	@Mock
	BCryptPasswordEncoder bCryptPasswordEncoder;
 
	String userId = "hhty57ehfy";
	String encryptedPassword = "74hghd8474jf";
	
	UserEntity userEntity;
 
	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setFirstname("ozlem");
		userEntity.setLastname("karadeniz");
		userEntity.setUserId(userId);
		userEntity.setEncryptedPassword(encryptedPassword);
		userEntity.setEmail("ozlemkaradeniz@gmail.com");
		userEntity.setEmailVerificationToken("7htnfhr758");
		userEntity.setAddresses(getAddressesEntity());
	}

	@Test
	final void testGetUser() {
 
		when(userRepository.findByEmail(anyString())).thenReturn(userEntity);

		UserDto userDto = userService.getUser("ozlemkaradeniz@gmail.com");

		assertNotNull(userDto);
		assertEquals("ozlem", userDto.getFirstname());

	}

	@Test
	final void testGetUser_UsernameNotFoundException() {
		when(userRepository.findByEmail(anyString())).thenReturn(null);

		assertThrows(UsernameNotFoundException.class,

				() -> {
					userService.getUser("test@test.com");
				}

		);
	}
	
	@Test
	final void testCreateUser_CreateUserServiceException()
	{
		when(userRepository.findByEmail(anyString())).thenReturn(userEntity);
		UserDto userDto = new UserDto();
		userDto.setAddresses(getAddressesDto());
		userDto.setFirstname("ozlem");
		userDto.setLastname("karadeniz");
		userDto.setPassword("12345678");
		userDto.setEmail("test@test.com");
 	
		assertThrows(UserServiceException.class,

				() -> {
					userService.createUser(userDto);
				}

		);
	}
	
	@Test
	final void testCreateUser()
	{
		when(userRepository.findByEmail(anyString())).thenReturn(null);
		when(utils.generateAddressId(anyInt())).thenReturn("hgfnghtyrir884");
		when(utils.generateUserId(anyInt())).thenReturn(userId);
		when(bCryptPasswordEncoder.encode(anyString())).thenReturn(encryptedPassword);
		when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
		Mockito.doNothing().when(amazonSES).verifyEmail(any(UserDto.class));
 		
		UserDto userDto = new UserDto();
		userDto.setAddresses(getAddressesDto());
		userDto.setFirstname("ARTUR");
		userDto.setLastname("Kargopolov");
		userDto.setPassword("12345678");
		userDto.setEmail("ozlemkaradeniz@gmail.com");

		UserDto storedUserDetails = userService.createUser(userDto);
		assertNotNull(storedUserDetails);
		assertEquals(userEntity.getFirstname(), storedUserDetails.getFirstname());
		assertEquals(userEntity.getLastname(), storedUserDetails.getLastname());
		assertNotNull(storedUserDetails.getUserId());
		assertEquals(storedUserDetails.getAddresses().size(), userEntity.getAddresses().size());
		verify(utils,times(storedUserDetails.getAddresses().size())).generateAddressId(30);
		verify(bCryptPasswordEncoder, times(1)).encode("12345678");
		verify(userRepository,times(1)).save(any(UserEntity.class));
	}
	
	private List<AddressDto> getAddressesDto() {
		AddressDto addressDto = new AddressDto();
		addressDto.setType("shipping");
		addressDto.setCity("Vancouver");
		addressDto.setCountry("Canada");
		addressDto.setPostalCode("ABC123");
		addressDto.setStreetName("123 Street name");

		AddressDto billingAddressDto = new AddressDto();
		billingAddressDto.setType("billling");
		billingAddressDto.setCity("Vancouver");
		billingAddressDto.setCountry("Canada");
		billingAddressDto.setPostalCode("ABC123");
		billingAddressDto.setStreetName("123 Street name");

		List<AddressDto> addresses = new ArrayList<>();
		addresses.add(addressDto);
		addresses.add(billingAddressDto);

		return addresses;

	}
	
	private List<AddressEntity> getAddressesEntity()
	{
		List<AddressDto> addresses = getAddressesDto();
		
	    Type listType = new TypeToken<List<AddressEntity>>() {}.getType();
	    
	    return new ModelMapper().map(addresses, listType);
	}


}
