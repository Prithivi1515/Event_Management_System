package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Constants for response keys
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_STATUS = "status";
    private static final String KEY_ERROR = "error";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_PATH = "path";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_TIMESTAMP, LocalDateTime.now());
        response.put(KEY_STATUS, HttpStatus.BAD_REQUEST.value());
        response.put(KEY_ERROR, "Validation Error");
        response.put(KEY_MESSAGE, ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        response.put(KEY_PATH, request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEventNotFoundException(EventNotFoundException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_TIMESTAMP, LocalDateTime.now());
        response.put(KEY_STATUS, HttpStatus.NOT_FOUND.value());
        response.put(KEY_ERROR, "Event Not Found");
        response.put(KEY_MESSAGE, ex.getMessage());
        response.put(KEY_PATH, request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_TIMESTAMP, LocalDateTime.now());
        response.put(KEY_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put(KEY_ERROR, "Internal Server Error");
        response.put(KEY_MESSAGE, ex.getMessage());
        response.put(KEY_PATH, request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
