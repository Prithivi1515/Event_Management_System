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
    UserRepository repository;

    @Override
    public String saveUser(User user) {
        repository.save(user);
        return "Employee Saved !!!";
    }

    // Update user details
    @Override
    public String updateUser(int userId, User user) throws UserNotFoundException {
        User existingUser = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        repository.save(existingUser);
        return "User updated successfully";
    }

    @Override
    public User getUser(int userId) throws UserNotFoundException {
        return repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    @Override
    public String deleteUser(int userId) throws UserNotFoundException {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Cannot delete, User not found"));
        repository.delete(user);
        return "User deleted";
    }
}
