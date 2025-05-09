package com.example.demo.service;

import java.util.List;

import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.User;

public interface UserService {
    
    String saveUser(User user);
    
    User getUser(int userId) throws UserNotFoundException;
    
    List<User> getAllUsers() throws UserNotFoundException;
    
    String deleteUser(int userId) throws UserNotFoundException;

    String updateUser(int userId, User user) throws UserNotFoundException;
    
    User getUserByEmail(String email) throws UserNotFoundException;
    
    List<User> getUsersByRole(String role) throws UserNotFoundException;
    
    List<User> searchUsersByName(String name) throws UserNotFoundException;
}
