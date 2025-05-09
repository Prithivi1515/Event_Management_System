package com.example.demo.service;

import java.util.List;

import com.example.demo.exception.EventNotFoundException;
import com.example.demo.exception.NotificationNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.Notification;

public interface NotificationService {


    Notification sendNotification(Notification notification) throws UserNotFoundException, EventNotFoundException;

    List<Notification> getAllNotificationsByUserId(int userId) throws NotificationNotFoundException, UserNotFoundException;
    

    List<Notification> getAllNotificationsByEventId(int eventId) throws NotificationNotFoundException, EventNotFoundException;
    
 
    Notification getNotificationById(int id) throws NotificationNotFoundException;
}
