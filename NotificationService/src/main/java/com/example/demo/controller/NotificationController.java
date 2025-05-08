package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Notification;
import com.example.demo.service.NotificationService;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService service;

    @PostMapping("/sendNotification")
    public ResponseEntity<String> sendNotification(@RequestBody Notification notificationRequest) {
        try {
            Notification notification = service.sendNotification(notificationRequest);

            if (notification == null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Duplicate notification already exists for user ID: " + notificationRequest.getUserId() +
                              " and event ID: " + notificationRequest.getEventId());
            }

            return ResponseEntity.ok("Notification sent successfully to user ID: " + notification.getUserId() +
                                     " for event ID: " + notification.getEventId() +
                                     " with message: " + notification.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while sending the notification: " + e.getMessage());
        }
    }

    @GetMapping("/getAllNotificationsByUserId")
    public ResponseEntity<?> getAllNotificationsByUserId(@RequestParam(name = "userId") int userId) {
        try {
            if (userId <= 0) {
                throw new IllegalArgumentException("User ID must be greater than 0.");
            }

            List<Notification> notifications = service.getAllNotificationsByUserId(userId);
            if (notifications.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No notifications found for user ID: " + userId);
            }

            return ResponseEntity.ok(notifications);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user ID: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching notifications: " + e.getMessage());
        }
    }
}
