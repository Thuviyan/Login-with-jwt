package com.sgic.mobile.app.service.impl;

import java.util.ArrayList;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.sgic.mobile.app.io.entity.UserEntity;
import com.sgic.mobile.app.repository.UserRepository;
import com.sgic.mobile.app.service.UserService;
import com.sgic.mobile.app.shared.Utils;
import com.sgic.mobile.app.shared.dto.UserDto;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	Utils utils;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public UserDto createUser(UserDto user) {

		if (userRepository.findByEmail(user.getEmail()) != null)
			throw new RuntimeException("Record already exists.");

		UserEntity userEntity = new UserEntity();
		BeanUtils.copyProperties(user, userEntity);

		String publicUserId = utils.generateUserId(30);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		userEntity.setUserId(publicUserId);

		UserEntity storedUserDetails = userRepository.save(userEntity);

		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(storedUserDetails, returnValue);

		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(email);
		
		if(userEntity==null) throw new UsernameNotFoundException(email);
		
		return new User(userEntity.getEmail(),userEntity.getEncryptedPassword(),new ArrayList<>());
	}

	@Override
	public UserDto getUser(String email) {
		UserEntity userEntity = userRepository.findByEmail(email);
		if(userEntity==null) throw new UsernameNotFoundException(email);
		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(userEntity, returnValue);
		
		return returnValue;
	}

}