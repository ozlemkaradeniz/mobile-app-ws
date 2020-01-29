package com.appdeveloper.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.appdeveloper.app.ws.exception.UserServiceException;
import com.appdeveloper.app.ws.io.entity.PasswordResetTokenEntity;
import com.appdeveloper.app.ws.io.entity.UserEntity;
import com.appdeveloper.app.ws.io.repository.PasswordResetTokenRepository;
import com.appdeveloper.app.ws.io.repository.UserRepository;
import com.appdeveloper.app.ws.service.UserService;
import com.appdeveloper.app.ws.shared.AmazonSES;
import com.appdeveloper.app.ws.shared.Utils;
import com.appdeveloper.app.ws.shared.dto.AddressDto;
import com.appdeveloper.app.ws.shared.dto.UserDto;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;
	@Autowired
	Utils utils;
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	PasswordResetTokenRepository passwordResetTokenRepository;
	
	@Autowired
	AmazonSES amazonSES;

	@Override
	public UserDto createUser(UserDto userDto) {
		
		if (userRepository.findByEmail(userDto.getEmail()) != null)
			throw new UserServiceException("Record already exist!");
		
		
		
		for(int i=0; i < userDto.getAddresses().size(); i++) {
			AddressDto addressDto = userDto.getAddresses().get(i);
			addressDto.setUserDetails(userDto);
			addressDto.setAddressId(utils.generateAddressId(30));
			userDto.getAddresses().set(i, addressDto);
		}
		
		ModelMapper modelmapper = new ModelMapper();
		UserEntity userEntity = modelmapper.map(userDto, UserEntity.class);

		String publicUserId = utils.generateUserId(30);
		userEntity.setUserId(publicUserId);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
		userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(userDto.getUserId()));
		userEntity.setEmailVerificationStatus(Boolean.FALSE);

		UserEntity storedUserEntity = (UserEntity) userRepository.save(userEntity);
		UserDto returnValue = modelmapper.map(storedUserEntity, UserDto.class);
		
		amazonSES.verifyEmail(returnValue);
		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub

		UserEntity userEntity = userRepository.findByEmail(username);
		if (userEntity == null)
			throw new UsernameNotFoundException(username);
		
		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), 
				userEntity.getEmailVerificationStatus(),
				true, true,
				true, new ArrayList<>());

		//return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList());

	}

	@Override
	public UserDto getUser(String email) {
		// TODO Auto-generated method stub
		UserEntity userEntity = userRepository.findByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userEntity, userDto);

		return userDto;
	}

	@Override
	public UserDto getUserByUserId(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UsernameNotFoundException(userId);

		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userEntity, userDto);

		return userDto;
	}

	@Override
	public UserDto getUserByLastName(String lastName) {
		// TODO Auto-generated method stub
		UserEntity userEntity = userRepository.findByLastname(lastName);
		if (userEntity == null)
			throw new UsernameNotFoundException(lastName);

		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userEntity, userDto);

		return userDto;
	}

	@Override
	public UserDto updateUser(String userId, UserDto userRequest) {

		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UsernameNotFoundException(userId);

		userEntity.setFirstname(userRequest.getFirstname());
		userEntity.setLastname(userRequest.getLastname());

		UserEntity storedUserEntity = (UserEntity) userRepository.save(userEntity);
		UserDto returnUser = new UserDto();
		BeanUtils.copyProperties(storedUserEntity, returnUser);
		return returnUser;

	}

	@Override
	public UserDto deleteUser(String userId) {

		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UsernameNotFoundException(userId);

		userRepository.delete(userEntity);
		UserDto returnUser = new UserDto();
		BeanUtils.copyProperties(userEntity, returnUser);
		return returnUser;
	}

	@Override
	public List<UserDto> getUsers(int page, int limit) {
		Pageable pageable = PageRequest.of(page, limit);

		Page pages = userRepository.findAll(pageable);
		List<UserEntity> userEntities = pages.getContent();

		List<UserDto> userDtos = new ArrayList<>();

		for (UserEntity userEntity : userEntities) {
			UserDto userDto = new UserDto();
			BeanUtils.copyProperties(userEntity, userDto);
			userDtos.add(userDto);

		}

		return userDtos;

	}
	
	@Override
	public boolean verifyEmailToken(String token) {
	    boolean returnValue = false;

        // Find user by token
        UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);
        
        System.out.println("OZLEM  11İŞ1İŞ1İŞ1İ");

        if (userEntity != null) {
        	System.out.println("OZLEM 2  11İŞ1İŞ1İŞ1İ");
            boolean hastokenExpired = Utils.hasTokenExpired(token);
            if (!hastokenExpired) {
                userEntity.setEmailVerificationToken(null);
                userEntity.setEmailVerificationStatus(Boolean.TRUE);
                System.out.println("OZLEM 3  11İŞ1İŞ1İŞ1İ");
                userRepository.save(userEntity);
                returnValue = true;
                
            }
        }
        

        return returnValue;
	}

	@Override
	public boolean requestPasswordReset(String email) {
		
		boolean returnValue = false;
		UserEntity userEntity = userRepository.findByEmail(email);
		
		if (userEntity == null)
			return returnValue;
		
	    String token = utils.generatePasswordResetToken(userEntity.getUserId());
	        
	    PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
	    passwordResetTokenEntity.setToken(token);
	    passwordResetTokenEntity.setUserDetails(userEntity);
	    passwordResetTokenRepository.save(passwordResetTokenEntity);
	        
	    returnValue = new AmazonSES().sendPasswordResetRequest(
	            userEntity.getFirstname(), 
	             userEntity.getEmail(),
	            token);
	        
			return returnValue;
		
		
	}
	
	@Override
	public boolean resetPassword(String token, String password) {
        boolean returnValue = false;
        
        if( Utils.hasTokenExpired(token) )
        {
            return returnValue;
        }
 
        PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByToken(token);

        if (passwordResetTokenEntity == null) {
            return returnValue;
        }

        // Prepare new password
        String encodedPassword = bCryptPasswordEncoder.encode(password);
        
        // Update User password in database
        UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
        userEntity.setEncryptedPassword(encodedPassword);
        UserEntity savedUserEntity = userRepository.save(userEntity);
 
        // Verify if password was saved successfully
        if (savedUserEntity != null && savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPassword)) {
            returnValue = true;
        }
   
        // Remove Password Reset token from database
        passwordResetTokenRepository.delete(passwordResetTokenEntity);
        
        return returnValue;
	}
	
	

}
