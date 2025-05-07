package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.Notification;
import com.example.demo.service.NotificationService;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    NotificationService service;

    @PostMapping("/sendNotification")
    public ResponseEntity<String> sendNotification(@RequestBody Notification notificationRequest) {
        try {
            Notification notification = service.sendNotification(notificationRequest);

            if (notification == null) {
                // Handle duplicate notification case
                String errorMessage = "Duplicate notification already exists for user ID: " + notificationRequest.getUserId() +
                                      " and event ID: " + notificationRequest.getEventId();
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
            }

            String responseMessage = "Notification sent to user with ID: " + notification.getUserId() +
                                     " for event with ID: " + notification.getEventId() +
                                     " with message: " + notification.getMessage();
            return ResponseEntity.ok(responseMessage);

        } catch (IllegalArgumentException e) {
            // Handle invalid input parameters
            String errorMessage = "Invalid input: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        } catch (Exception e) {
            // Handle unexpected errors
            String errorMessage = "An error occurred while sending the notification: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    @GetMapping("/getAllNotificationsByUserId")
    public ResponseEntity<List<Notification>> getAllNotificationsByUserId(@RequestParam(name = "userId") int userId) {
        try {
            if (userId <= 0) {
                throw new IllegalArgumentException("User ID must be greater than 0.");
            }

            List<Notification> notifications = service.getAllNotificationsByUserId(userId);
            return ResponseEntity.ok(notifications);

        } catch (IllegalArgumentException e) {
            // Handle invalid user ID
            String errorMessage = "Invalid user ID: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            // Handle unexpected errors
            String errorMessage = "An error occurred while fetching notifications: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
