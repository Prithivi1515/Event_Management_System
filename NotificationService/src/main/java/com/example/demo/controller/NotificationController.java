package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.NotificationNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.Notification;
import com.example.demo.service.NotificationService;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService service;

    @PostMapping("/sendNotification")
    public String sendNotification(@RequestBody Notification notificationRequest) {
        Notification notification = service.sendNotification(notificationRequest);

        if (notification == null) {
            throw new IllegalArgumentException("Duplicate notification already exists for user ID: " 
                    + notificationRequest.getUserId() + " and event ID: " + notificationRequest.getEventId());
        }

        return "Notification sent successfully to user ID: " + notification.getUserId() +
               " for event ID: " + notification.getEventId() +
               " with message: " + notification.getMessage();
    }

    @GetMapping("/getAllNotificationsByUserId")
    public List<Notification> getAllNotificationsByUserId(@RequestParam(name = "userId") int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0.");
        }

        List<Notification> notifications = service.getAllNotificationsByUserId(userId);
        if (notifications.isEmpty()) {
            throw new NotificationNotFoundException("No notifications found for user ID: " + userId);
        }

        return notifications;
    }
}
