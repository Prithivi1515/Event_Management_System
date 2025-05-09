package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.Event;
import com.example.demo.dto.User;
import com.example.demo.exception.EventNotFoundException;
import com.example.demo.exception.NotificationNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.feignclient.EventClient;
import com.example.demo.feignclient.UserClient;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private EventClient eventClient;

    @Override
    public Notification sendNotification(Notification notification) {
        // Validate input parameters
        if (notification.getUserId() <= 0 || notification.getEventId() <= 0 || 
            notification.getMessage() == null || notification.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid input parameters: userId, eventId, or message.");
        }

        // Fetch user details
        User user = userClient.getUserById(notification.getUserId());
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + notification.getUserId());
        }

        // Fetch event details
        Event event = eventClient.getEventById(notification.getEventId());
        if (event == null) {
            throw new EventNotFoundException("Event not found with ID: " + notification.getEventId());
        }

        // Check for duplicate notification
        if (notificationRepository.existsByUserIdAndEventIdAndMessage(
                notification.getUserId(), notification.getEventId(), notification.getMessage())) {
            throw new IllegalArgumentException("Duplicate notification already exists for user ID: " + 
                                               notification.getUserId() + ", event ID: " + notification.getEventId());
        }

        // Add timestamp and save the notification
        notification.setTimestamp(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getAllNotificationsByUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID: " + userId);
        }

        // Verify user exists
        User user = userClient.getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        List<Notification> notifications = notificationRepository.findByUserId(userId);
        if (notifications.isEmpty()) {
            throw new NotificationNotFoundException("No notifications found for user ID: " + userId);
        }

        return notifications;
    }

    @Override
    public List<Notification> getAllNotificationsByEventId(int eventId) throws NotificationNotFoundException, EventNotFoundException {
        if (eventId <= 0) {
            throw new IllegalArgumentException("Invalid event ID: " + eventId);
        }

        // Verify event exists
        Event event = eventClient.getEventById(eventId);
        if (event == null) {
            throw new EventNotFoundException("Event not found with ID: " + eventId);
        }

        List<Notification> notifications = notificationRepository.findByEventId(eventId);
        if (notifications.isEmpty()) {
            throw new NotificationNotFoundException("No notifications found for event ID: " + eventId);
        }

        return notifications;
    }

    @Override
    public Notification getNotificationById(int id) throws NotificationNotFoundException {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid notification ID: " + id);
        }

        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));
    }
}
