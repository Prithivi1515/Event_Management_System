package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping("/save")
    public ResponseEntity<String> saveUser(@RequestBody @Valid User user) {
        String response = service.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update/{uid}")
    public ResponseEntity<String> updateUser(@PathVariable("uid") int userId, @RequestBody @Valid User user) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0.");
        }
        String response = service.updateUser(userId, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getUserById/{uid}")
    public ResponseEntity<?> getUser(@PathVariable("uid") int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0.");
        }
        try {
            User user = service.getUser(userId);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = service.getAllUsers();
        if (users.isEmpty()) {
            throw new UserNotFoundException("No users found in the system.");
        }
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/deleteUserById/{uid}")
    public ResponseEntity<String> deleteUser(@PathVariable("uid") int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0.");
        }
        try {
            String response = service.deleteUser(userId);
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
    }
}
