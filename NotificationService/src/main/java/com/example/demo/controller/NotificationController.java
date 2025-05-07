package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
        Notification notification = service.sendNotification(
            notificationRequest.getUserId(),
            notificationRequest.getEventId(),
            notificationRequest.getMessage()
        );
        String responseMessage = "Notification sent to user with ID: " + notification.getUserId() + 
                                 " for event with ID: " + notification.getEventId() + 
                                 " with message: " + notification.getMessage();
        return ResponseEntity.ok(responseMessage);
    }

    @GetMapping("/getAllNotificationsByUserId")
    public ResponseEntity<List<Notification>> getAllNotificationsByUserId(@RequestParam(name = "userId") int userId) {
        List<Notification> notifications = service.getAllNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }
}
