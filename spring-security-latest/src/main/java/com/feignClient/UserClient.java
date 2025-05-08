package com.feignClient;



import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.entity.UserInfo;

@FeignClient(name="USERSERVICE",path="/user")
public interface UserClient
{
	@PostMapping("/save")
	public String saveUser(@RequestBody UserInfo userInfo);
	

}

