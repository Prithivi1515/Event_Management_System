package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.NotificationService;
import org.springframework.web.bind.annotation.PostMapping;
import com.example.demo.model.Notification;



@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    NotificationService service;

    @PostMapping("/sendNotification")
    public String sendNotification(@RequestParam int userId,@RequestParam int eventId,@RequestParam String message) {
        Notification notification = service.sendNotification(userId, eventId, message);
        return "Notification sent to user with ID: " + notification.getUserId() + " for event with ID: " + notification.getEventId() + " with message: " + notification.getMessage();
    }

    @GetMapping("/getAllNotificationsByUserId")
    public List<Notification> getAllNotificationsByUserId(@RequestParam int userId) {
        return service.getAllNotificationsByUserId(userId);
    }
    
   
    
    
}
