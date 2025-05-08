package com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.entity.UserInfo;
import com.feignClient.UserClient;
import com.repository.UserInfoRepository;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserInfoRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserClient userClient;

    public String addUser(UserInfo userInfo) {
        if (userInfo == null || userInfo.getName() == null || userInfo.getPassword() == null) {
            return "Invalid user information provided.";
        }

        String name = userInfo.getName().trim().toLowerCase();
        Optional<UserInfo> existingUser = repository.findByName(name);

        if (existingUser.isPresent()) {
            return "This username is already registered.";
        }

        userClient.saveUser(userInfo);
        userInfo.setName(name); // Normalize username to lowercase
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        repository.save(userInfo);
        return "Registration successful.";
    }

    public String getRoles(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "Invalid username provided.";
        }

        Optional<UserInfo> userInfo = repository.findByName(username.trim().toLowerCase());
        return userInfo.map(UserInfo::getRoles).orElse("Roles not found for the given username.");
    }
}
