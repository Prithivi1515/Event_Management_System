package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.User;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	UserService service;
	
	@PostMapping("/save")
	public String saveUser(@RequestBody User user) {
		return service.saveUser(user);
	}
	
	@PutMapping("/update/{uid}")
	public String updateUser(@PathVariable("uid") int userId,@RequestBody User user)throws UserNotFoundException {
		return service.updateUser(userId,user);
	}

	@GetMapping("/getUserById/{uid}")
	public User getUser(@PathVariable("uid") int userId) throws UserNotFoundException
	{
		return service.getUser(userId);
	}
	
	@GetMapping("/getAllUsers")
	public List<User> getAllUsers() {
		return service.getAllUsers();
	}

	@DeleteMapping("/deleteUserById/{uid}")
	public String deleteUser(@PathVariable("uid") int userId) throws UserNotFoundException
	{
		return service.deleteUser(userId);
	}

}
