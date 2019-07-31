package com.sgic.mobile.app.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.sgic.mobile.app.shared.dto.UserDto;

public interface UserService extends UserDetailsService {
	
	UserDto createUser(UserDto user);
	UserDto getUser(String email);
	UserDto getUserByUserId(String userId);
}
