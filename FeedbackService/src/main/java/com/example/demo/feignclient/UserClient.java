package com.example.demo.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.dto.User;

@FeignClient(name="USERSERVICE",path="/user")
public interface UserClient
{
	@GetMapping("/getUserById/{id}")
	public User getUserById(@PathVariable("id") int userId);
	

}
