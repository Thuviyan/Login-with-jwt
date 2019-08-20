package com.sgic.mobile.app.service;

import java.util.List;

import com.sgic.mobile.app.shared.dto.AddressDTO;

public interface AddressService {

	List<AddressDTO> getAddresses(String userId);
	AddressDTO getAddress(String addressId);
}
