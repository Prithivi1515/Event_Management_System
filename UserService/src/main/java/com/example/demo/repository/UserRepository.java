package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    // Find user by email (case-insensitive) using method name derivation
    Optional<User> findByEmailIgnoreCase(String email);
    
    // Check if email exists 
    boolean existsByEmailIgnoreCase(String email);
    
    // Find users by role
    List<User> findByRoles(String roles);
    
    // Find users by name containing (case-insensitive)
    List<User> findByNameContainingIgnoreCase(String name);
}
