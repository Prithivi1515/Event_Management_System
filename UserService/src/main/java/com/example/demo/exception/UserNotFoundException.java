package com.example.demo.exception;

public class UserNotFoundException extends RuntimeException { // Changed to extend RuntimeException
    public UserNotFoundException(String message) {
        super(message);
    }
}
