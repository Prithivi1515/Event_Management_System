package com.example.demo.service;

import java.util.List;

import com.example.demo.model.Notification;

public interface NotificationService {

    public abstract Notification sendNotification(int userId, int eventId,String message);

    public abstract List<Notification> getAllNotificationsByUserId(int userId);
        
}
