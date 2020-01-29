package com.appdeveloper.app.ws.ui.controller;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appdeveloper.app.ws.exception.UserServiceException;
import com.appdeveloper.app.ws.service.AddressService;
import com.appdeveloper.app.ws.service.UserService;
import com.appdeveloper.app.ws.shared.dto.AddressDto;
import com.appdeveloper.app.ws.shared.dto.UserDto;
import com.appdeveloper.app.ws.ui.model.request.PasswordRequestModel;
import com.appdeveloper.app.ws.ui.model.request.PasswordResetModel;
import com.appdeveloper.app.ws.ui.model.request.UserRequestModel;
import com.appdeveloper.app.ws.ui.model.response.AddressResponse;
import com.appdeveloper.app.ws.ui.model.response.ErrorMessages;
import com.appdeveloper.app.ws.ui.model.response.OperationStatusModel;
import com.appdeveloper.app.ws.ui.model.response.RequestOperationStatus;
import com.appdeveloper.app.ws.ui.model.response.UserResponse;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	UserService userService;
	
	@Autowired
	AddressService addressService;
	
	@GetMapping(path="/{id}")
	public UserResponse getUser(@PathVariable String id) {
		
		UserDto userDto = userService.getUserByUserId(id);
		ModelMapper modelmapper = new ModelMapper();
		UserResponse userResponse = modelmapper.map(userDto, UserResponse.class);
		return userResponse;
	}
	
	@GetMapping(path="/lastname={lastname}",
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public UserResponse getUserByLastName(@PathVariable String lastname) {
		
		UserDto userDto = userService.getUserByLastName(lastname);
		ModelMapper modelmapper = new ModelMapper();
		UserResponse userResponse = modelmapper.map(userDto, UserResponse.class);
		
		return userResponse;
	}
	
	@PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE}
	)
	public UserResponse createtUser(@RequestBody UserRequestModel userDetails) {
		
		System.out.println(userDetails.getEmail() + " AAAAAAAAAAAAAAAA");
		System.out.println(userDetails.getFirstname() + " BBBBBBBBBBBBBB");
		
		if(userDetails.getEmail() == "") {
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		}
		
		if(userDetails.getFirstname() == "") {
			throw new NullPointerException("object is null!!");
		}
		
		//UserDto userDto = new UserDto();
		//BeanUtils.copyProperties(userDetails, userDto);
		ModelMapper modelmapper = new ModelMapper();
		UserDto userDto = modelmapper.map(userDetails, UserDto.class);
		
		UserDto createdUser = userService.createUser(userDto);
		UserResponse userResponse = modelmapper.map(createdUser, UserResponse.class);
		return userResponse;
	}
	
	@PutMapping(path="/{id}", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE}
	)
	public UserResponse updateUser(@PathVariable String id, @RequestBody UserRequestModel userDetails) {
		
		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userDetails, userDto);
		
		UserDto createdUser = userService.updateUser(id, userDto);
		ModelMapper modelmapper = new ModelMapper();
		UserResponse userResponse = modelmapper.map(createdUser, UserResponse.class);
		return userResponse;
	}
	
	@DeleteMapping(path="/{id}",
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public UserResponse deleteUser(@PathVariable String id) {
		UserDto userDto = userService.deleteUser(id);
		ModelMapper modelmapper = new ModelMapper();
		UserResponse userResponse = modelmapper.map(userDto, UserResponse.class);
		return userResponse;
	}
	
	@GetMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public List<UserResponse> getUsers(@RequestParam(value="page", defaultValue="0") int page, 
			@RequestParam(value="limit" ,defaultValue="25") int limit){
		
		List<UserResponse> users = new ArrayList<>();
		
		List<UserDto> userDtos = userService.getUsers(page, limit);
		
		for(UserDto userDto : userDtos) {	
			ModelMapper modelmapper = new ModelMapper();
			UserResponse user = modelmapper.map(userDto, UserResponse.class);
			users.add(user);
			
		}

		return users;
	}
	
	@GetMapping(path="/{id}/addresses",
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
	public Resources<AddressResponse> getUserAddresses(@PathVariable String id){
		
		
		List<AddressDto> addressesDto = addressService.getUserAddresses(id);
		
		ModelMapper modelmapper = new ModelMapper();
		Type listType = new TypeToken<List<AddressResponse>>() {}.getType();
		List<AddressResponse> addresses = modelmapper.map(addressesDto, listType);
		
		
		for(AddressResponse address : addresses) {
			Link addressLink = linkTo(methodOn(UserController.class).getUserAddress(id, address.getAddressId())).withSelfRel();
			Link userLink = linkTo(UserController.class).slash(id).withRel("user");
			Link addressesLink = linkTo(methodOn(UserController.class).getUserAddresses(id)).withRel("addresses");
			
			address.add(addressLink);
			address.add(userLink);
			address.add(addressesLink);
		}
		
		
		
		return new Resources<>(addresses);
	}
	
	@GetMapping(path="/{userId}/addresses/{addressId}",
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
	public Resource<AddressResponse> getUserAddress(@PathVariable String userId, @PathVariable String addressId){
		
		
		AddressDto addressesDto = addressService.getAddress(addressId);
		Link addressLink = linkTo(methodOn(UserController.class).getUserAddress(userId, addressId)).withSelfRel();
		Link userLink = linkTo(UserController.class).slash(userId).withRel("user");
		Link addressesLink = linkTo(methodOn(UserController.class).getUserAddresses(userId)).withRel("addresses");

		ModelMapper modelmapper = new ModelMapper();
		AddressResponse returnValue = modelmapper.map(addressesDto, AddressResponse.class);
		
		returnValue.add(addressLink);
		returnValue.add(userLink);
		returnValue.add(addressesLink);
		
	
		return new Resource<>(returnValue);
	}
	
	 /*
     * http://localhost:8080/mobile-app-ws/users/email-verification?token=sdfsdf
     * */
    @GetMapping(path = "/email-verification", produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    public OperationStatusModel verifyEmailToken(@RequestParam(value = "token") String token) {

        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.VERIFY_EMAIL.name());
        
        boolean isVerified = userService.verifyEmailToken(token);
        
        if(isVerified)
        {
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        } else {
            returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
        }

        return returnValue;
    }
    
	 /*
     * http://localhost:8080/mobile-app-ws/users/password-reset-request
     * */
    @PostMapping(path = "/password-reset-request", 
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public OperationStatusModel requestReset(@RequestBody PasswordRequestModel passwordResetRequestModel) {
    	OperationStatusModel returnValue = new OperationStatusModel();
 
        boolean operationResult = userService.requestPasswordReset(passwordResetRequestModel.getEmail());
        
        returnValue.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());
        returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
 
        if(operationResult)
        {
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        }

        return returnValue;
    }
    
    @PostMapping(path = "/password-reset",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public OperationStatusModel resetPassword(@RequestBody PasswordResetModel passwordResetModel) {
    	OperationStatusModel returnValue = new OperationStatusModel();
 
        boolean operationResult = userService.resetPassword(
                passwordResetModel.getToken(),
                passwordResetModel.getPassword());
        
        returnValue.setOperationName(RequestOperationName.PASSWORD_RESET.name());
        returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
 
        if(operationResult)
        {
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        }

        return returnValue;
    }
    
    

}
