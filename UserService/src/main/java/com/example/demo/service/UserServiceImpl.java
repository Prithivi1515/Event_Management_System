package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Override
    public String saveUser(User user) {
        // Check for duplicate email using the correct method name
        if (repository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        repository.save(user);
        return "User saved successfully!";
    }

    @Override
    public String updateUser(int userId, User user) throws UserNotFoundException {
        User existingUser = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Check for email uniqueness with the correct method
        repository.findByEmailIgnoreCase(user.getEmail()).ifPresent(existing -> {
            if (existing.getUserId() != userId) {
                throw new IllegalArgumentException("Email already exists: " + user.getEmail());
            }
        });

        // Update all fields, not just name and email
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword()); // Consider password hashing
        existingUser.setContactNumber(user.getContactNumber());
        existingUser.setRoles(user.getRoles());
        
        repository.save(existingUser);
        return "User updated successfully!";
    }

    @Override
    public User getUser(int userId) throws UserNotFoundException {
        return repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    @Override
    public List<User> getAllUsers() throws UserNotFoundException {
        List<User> users = repository.findAll();
        if (users.isEmpty()) {
            throw new UserNotFoundException("No users found in the system.");
        }
        return users;
    }

    @Override
    public String deleteUser(int userId) throws UserNotFoundException {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Cannot delete, user not found with ID: " + userId));
        repository.delete(user);
        return "User deleted successfully!";
    }
    
    @Override
    public User getUserByEmail(String email) throws UserNotFoundException {
        return repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }
    
    @Override
    public List<User> getUsersByRole(String role) throws UserNotFoundException {
        List<User> users = repository.findByRoles(role);
        if (users.isEmpty()) {
            throw new UserNotFoundException("No users found with role: " + role);
        }
        return users;
    }
    
    @Override
    public List<User> searchUsersByName(String name) throws UserNotFoundException {
        List<User> users = repository.findByNameContainingIgnoreCase(name);
        if (users.isEmpty()) {
            throw new UserNotFoundException("No users found matching name: " + name);
        }
        return users;
    }
}
