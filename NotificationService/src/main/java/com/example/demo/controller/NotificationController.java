package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.EventNotFoundException;
import com.example.demo.exception.NotificationNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.Notification;
import com.example.demo.service.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService service;

    @PostMapping("/sendNotification")
    public ResponseEntity<String> sendNotification(@RequestBody @Valid Notification notificationRequest) {
        // Using @Valid annotation to leverage the validation constraints in Notification model
        // No need for manual validation here
        
        Notification notification = service.sendNotification(notificationRequest);
        
        String message = "Notification sent successfully to user ID: " + notification.getUserId() +
                         " for event ID: " + notification.getEventId() +
                         " with message: " + notification.getMessage();
        
        return ResponseEntity.ok(message);
    }

    @GetMapping("/getAllNotificationsByUserId")
    public ResponseEntity<List<Notification>> getAllNotificationsByUserId(@RequestParam(name = "userId") int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0.");
        }
        
        List<Notification> notifications = service.getAllNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }
    
    // Added missing endpoint to get notifications by event
    @GetMapping("/getAllNotificationsByEventId")
    public ResponseEntity<List<Notification>> getAllNotificationsByEventId(@RequestParam(name = "eventId") int eventId) {
        if (eventId <= 0) {
            throw new IllegalArgumentException("Event ID must be greater than 0.");
        }
        
        List<Notification> notifications = service.getAllNotificationsByEventId(eventId);
        return ResponseEntity.ok(notifications);
    }
    
    // Added missing endpoint to get notification by ID
    @GetMapping("/getNotificationById/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable("id") int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Notification ID must be greater than 0.");
        }
        
        Notification notification = service.getNotificationById(id);
        return ResponseEntity.ok(notification);
    }

    // Exception handlers to properly handle service exceptions
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    
    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<String> handleEventNotFoundException(EventNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<String> handleNotificationNotFoundException(NotificationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
