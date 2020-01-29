package com.appdeveloper.app.ws.service;

import java.util.List;

import com.appdeveloper.app.ws.shared.dto.AddressDto;

public interface AddressService {
	public List<AddressDto >getUserAddresses(String userId);
	public AddressDto getAddress(String addressId);
}
