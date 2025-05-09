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
        // Check for duplicate email
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        repository.save(user);
        return "User saved successfully!";
    }

    @Override
    public String updateUser(int userId, User user) throws UserNotFoundException {
        User existingUser = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        repository.findByEmail(user.getEmail()).ifPresent(existing -> {
            if (existing.getUserId() != userId) {
                throw new IllegalArgumentException("Email already exists: " + user.getEmail());
            }
        });

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        repository.save(existingUser);
        return "User updated successfully!";
    }

    @Override
    public User getUser(int userId) throws UserNotFoundException {
        return repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    @Override
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    @Override
    public String deleteUser(int userId) throws UserNotFoundException {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Cannot delete, user not found with ID: " + userId));
        repository.delete(user);
        return "User deleted successfully!";
    }
}
