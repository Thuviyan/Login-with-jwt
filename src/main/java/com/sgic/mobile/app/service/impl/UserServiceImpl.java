package com.sgic.mobile.app.service.impl;

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

import com.sgic.mobile.app.exceptions.UserServiceException;
import com.sgic.mobile.app.io.entity.UserEntity;
import com.sgic.mobile.app.repository.UserRepository;
import com.sgic.mobile.app.service.UserService;
import com.sgic.mobile.app.shared.Utils;
import com.sgic.mobile.app.shared.dto.AddressDTO;
import com.sgic.mobile.app.shared.dto.UserDto;
import com.sgic.mobile.app.ui.model.response.ErrorMessages;

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
		
		for(int i=0; i<user.getAddresses().size();i++)
		{
			AddressDTO address = user.getAddresses().get(i);
			address.setUserDetails(user);
			address.setAddressId(utils.generateAddressId(30));
			user.getAddresses().set(i, address);
		}

		//BeanUtils.copyProperties(user, userEntity);
		ModelMapper modelMapper = new ModelMapper();
		UserEntity userEntity  = modelMapper.map(user, UserEntity.class);
		

		String publicUserId = utils.generateUserId(30);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		userEntity.setUserId(publicUserId);

		UserEntity storedUserDetails = userRepository.save(userEntity);

		//BeanUtils.copyProperties(storedUserDetails, returnValue);
		UserDto returnValue  = modelMapper.map(storedUserDetails, UserDto.class);

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

	@Override
	public UserDto getUserByUserId(String userId) {
		UserDto returnValue = new UserDto();
		UserEntity userEntity = userRepository.findByUserId(userId);
		if(userEntity==null) throw new UsernameNotFoundException("User with ID: " + userId + " not found");
		BeanUtils.copyProperties(userEntity, returnValue);
		return returnValue;
	}

	@Override
	public UserDto updateUser(String userId, UserDto user) {
		UserDto returnValue = new UserDto();
		UserEntity userEntity = userRepository.findByUserId(userId);
		
		if(userEntity==null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()); 
		
		userEntity.setFirstName(user.getFirstName());
		userEntity.setLastName(user.getLastName());
		
		UserEntity updatedUserDetails = userRepository.save(userEntity); 
		BeanUtils.copyProperties(updatedUserDetails, returnValue);
		return returnValue;
	}

	@Override
	public void deleteUser(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);
		
		if(userEntity == null) throw new UserServiceException (ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		
		userRepository.delete(userEntity);
		
	}

	@Override
	public List<UserDto> getUsers(int page, int limit) {
		
		List<UserDto> returnValue = new ArrayList<>();
		
		if(page>0) page = page -1;
		
		Pageable pageableRequest = PageRequest.of(page, limit);
		Page<UserEntity> userPage = userRepository.findAll(pageableRequest);
		List<UserEntity> users = userPage.getContent();
		
		for(UserEntity userEntity : users) {
			UserDto userDto = new UserDto();
			BeanUtils.copyProperties(userEntity, userDto);
			returnValue.add(userDto);
		}
		
		return returnValue;
	}

}
