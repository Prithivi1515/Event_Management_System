package com.example.demo.service;

import java.util.List;

import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.User;

public interface UserService {
	
	public abstract String saveUser(User user);
	
//	public abstract User updateUser(User user);
	
	public abstract User getUser(int userId) throws UserNotFoundException;
	
	public abstract List<User> getAllUsers();
	
	public abstract String deleteUser(int userId) throws UserNotFoundException ;

	public abstract String updateUser(int userid,User user) throws UserNotFoundException;

}
