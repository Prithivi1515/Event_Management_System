package com.example.demo.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserRepository repository;

    @Override
    public String saveUser(User user) {
        logger.info("Attempting to save new user with email: {}", user.getEmail());
        
        // Check for duplicate email using the correct method name
        if (repository.existsByEmailIgnoreCase(user.getEmail())) {
            logger.warn("Registration failed: Email already exists: {}", user.getEmail());
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        repository.save(user);
        logger.info("User saved successfully with ID: {}", user.getUserId());
        return "User saved successfully!";
    }

    @Override
    public String updateUser(int userId, User user) throws UserNotFoundException {
        logger.info("Attempting to update user with ID: {}", userId);
        
        User existingUser = repository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Update failed: User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });

        // Check for email uniqueness with the correct method
        repository.findByEmailIgnoreCase(user.getEmail()).ifPresent(existing -> {
            if (existing.getUserId() != userId) {
                logger.warn("Update failed: Email {} already in use by another user", user.getEmail());
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
        logger.info("User updated successfully: ID={}, Name={}", userId, existingUser.getName());
        return "User updated successfully!";
    }

    @Override
    public User getUser(int userId) throws UserNotFoundException {
        logger.debug("Fetching user with ID: {}", userId);
        
        return repository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });
    }

    @Override
    public List<User> getAllUsers() throws UserNotFoundException {
        logger.debug("Fetching all users");
        
        List<User> users = repository.findAll();
        if (users.isEmpty()) {
            logger.warn("No users found in the system");
            throw new UserNotFoundException("No users found in the system.");
        }
        
        logger.info("Retrieved {} users from database", users.size());
        return users;
    }

    @Override
    public String deleteUser(int userId) throws UserNotFoundException {
        logger.info("Attempting to delete user with ID: {}", userId);
        
        User user = repository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Delete failed: User not found with ID: {}", userId);
                    return new UserNotFoundException("Cannot delete, user not found with ID: " + userId);
                });
                
        repository.delete(user);
        logger.info("User deleted successfully: ID={}, Email={}", userId, user.getEmail());
        return "User deleted successfully!";
    }
    
    @Override
    public User getUserByEmail(String email) throws UserNotFoundException {
        logger.debug("Fetching user with email: {}", email);
        
        return repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });
    }
    
    @Override
    public List<User> getUsersByRole(String role) throws UserNotFoundException {
        logger.debug("Fetching users with role: {}", role);
        
        List<User> users = repository.findByRoles(role);
        if (users.isEmpty()) {
            logger.warn("No users found with role: {}", role);
            throw new UserNotFoundException("No users found with role: " + role);
        }
        
        logger.info("Found {} users with role: {}", users.size(), role);
        return users;
    }
    
    @Override
    public List<User> searchUsersByName(String name) throws UserNotFoundException {
        logger.debug("Searching users with name containing: {}", name);
        
        List<User> users = repository.findByNameContainingIgnoreCase(name);
        if (users.isEmpty()) {
            logger.warn("No users found matching name: {}", name);
            throw new UserNotFoundException("No users found matching name: " + name);
        }
        
        logger.info("Found {} users matching name search: {}", users.size(), name);
        return users;
    }
}
