package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.dto.Event;
import com.example.demo.dto.User;
import com.example.demo.exception.EventNotFoundException;
import com.example.demo.exception.NotificationNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;

import lombok.AllArgsConstructor;

import com.example.demo.feignclient.EventClient;
import com.example.demo.feignclient.UserClient;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private NotificationRepository notificationRepository;

    private UserClient userClient;

    private EventClient eventClient;

    @Override
    public Notification sendNotification(Notification notification) {
        logger.info("Attempting to send notification for user ID: {} and event ID: {}", 
                notification.getUserId(), notification.getEventId());
        
        // Validate input parameters
        if (notification.getUserId() <= 0 || notification.getEventId() <= 0 || 
            notification.getMessage() == null || notification.getMessage().trim().isEmpty()) {
            logger.warn("Invalid notification parameters: userId={}, eventId={}, message={}",
                    notification.getUserId(), notification.getEventId(), 
                    notification.getMessage() == null ? "null" : "empty or invalid");
            throw new IllegalArgumentException("Invalid input parameters: userId, eventId, or message.");
        }

        // Fetch user details
        logger.debug("Fetching user details for ID: {}", notification.getUserId());
        User user = userClient.getUserById(notification.getUserId());
        if (user == null) {
            logger.error("User not found for notification, user ID: {}", notification.getUserId());
            throw new UserNotFoundException("User not found with ID: " + notification.getUserId());
        }
        logger.debug("User found: ID={}, name={}", user.getUserId(), user.getName());

        // Fetch event details
        logger.debug("Fetching event details for ID: {}", notification.getEventId());
        Event event = eventClient.getEventById(notification.getEventId());
        if (event == null) {
            logger.error("Event not found for notification, event ID: {}", notification.getEventId());
            throw new EventNotFoundException("Event not found with ID: " + notification.getEventId());
        }
        logger.debug("Event found: ID={}, name={}", event.getEventId(), event.getName());

        // Check for duplicate notification
        logger.debug("Checking for duplicate notifications");
        if (notificationRepository.existsByUserIdAndEventIdAndMessage(
                notification.getUserId(), notification.getEventId(), notification.getMessage())) {
            logger.warn("Duplicate notification detected for user ID: {} and event ID: {}", 
                    notification.getUserId(), notification.getEventId());
            throw new IllegalArgumentException("Duplicate notification already exists for user ID: " + 
                                               notification.getUserId() + ", event ID: " + notification.getEventId());
        }

        // Add timestamp and save the notification
        notification.setTimestamp(LocalDateTime.now());
        logger.debug("Saving notification to database");
        Notification savedNotification = notificationRepository.save(notification);
        logger.info("Notification sent successfully, ID: {}, user ID: {}, event ID: {}", 
                savedNotification.getNotificationId(), savedNotification.getUserId(), savedNotification.getEventId());
        
        return savedNotification;
    }

    @Override
    public List<Notification> getAllNotificationsByUserId(int userId) {
        logger.info("Retrieving all notifications for user ID: {}", userId);
        
        if (userId <= 0) {
            logger.warn("Invalid user ID provided: {}", userId);
            throw new IllegalArgumentException("Invalid user ID: " + userId);
        }

        // Verify user exists
        logger.debug("Verifying user exists with ID: {}", userId);
        User user = userClient.getUserById(userId);
        if (user == null) {
            logger.error("User not found with ID: {}", userId);
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        logger.debug("Fetching notifications from database for user ID: {}", userId);
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        if (notifications.isEmpty()) {
            logger.warn("No notifications found for user ID: {}", userId);
            throw new NotificationNotFoundException("No notifications found for user ID: " + userId);
        }

        logger.info("Retrieved {} notifications for user ID: {}", notifications.size(), userId);
        return notifications;
    }

    @Override
    public List<Notification> getAllNotificationsByEventId(int eventId) throws NotificationNotFoundException, EventNotFoundException {
        logger.info("Retrieving all notifications for event ID: {}", eventId);
        
        if (eventId <= 0) {
            logger.warn("Invalid event ID provided: {}", eventId);
            throw new IllegalArgumentException("Invalid event ID: " + eventId);
        }

        // Verify event exists
        logger.debug("Verifying event exists with ID: {}", eventId);
        Event event = eventClient.getEventById(eventId);
        if (event == null) {
            logger.error("Event not found with ID: {}", eventId);
            throw new EventNotFoundException("Event not found with ID: " + eventId);
        }

        logger.debug("Fetching notifications from database for event ID: {}", eventId);
        List<Notification> notifications = notificationRepository.findByEventId(eventId);
        if (notifications.isEmpty()) {
            logger.warn("No notifications found for event ID: {}", eventId);
            throw new NotificationNotFoundException("No notifications found for event ID: " + eventId);
        }

        logger.info("Retrieved {} notifications for event ID: {}", notifications.size(), eventId);
        return notifications;
    }

    @Override
    public Notification getNotificationById(int id) throws NotificationNotFoundException {
        logger.info("Retrieving notification by ID: {}", id);
        
        if (id <= 0) {
            logger.warn("Invalid notification ID provided: {}", id);
            throw new IllegalArgumentException("Invalid notification ID: " + id);
        }

        logger.debug("Fetching notification from database with ID: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Notification not found with ID: {}", id);
                    return new NotificationNotFoundException("Notification not found with ID: " + id);
                });

        logger.info("Successfully retrieved notification ID: {}", id);
        return notification;
    }
}
