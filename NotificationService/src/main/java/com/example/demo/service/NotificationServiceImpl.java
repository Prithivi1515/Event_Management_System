package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.feignclient.TicketClient;
import com.example.demo.feignclient.UserClient;
import com.example.demo.dto.Event;
import com.example.demo.dto.User;
import com.example.demo.feignclient.EventClient;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    UserClient userClient;

    @Autowired
    EventClient eventClient;

    @Autowired
    TicketClient ticketClient;

    @Override
    public Notification sendNotification(Notification notification) {
        logger.info("Attempting to send notification: {}", notification);

        // Validate input parameters
        if (notification.getUserId() <= 0 || notification.getEventId() <= 0 || 
            notification.getMessage() == null || notification.getMessage().trim().isEmpty()) {
            logger.error("Invalid input parameters: {}", notification);
            throw new IllegalArgumentException("Invalid input parameters: userId, eventId, or message.");
        }

        // Check for duplicate notification
        if (notificationRepository.existsByUserIdAndEventIdAndMessage(
                notification.getUserId(), notification.getEventId(), notification.getMessage())) {
            logger.error("Duplicate notification exists for userId: {}, eventId: {}", 
                         notification.getUserId(), notification.getEventId());
            throw new RuntimeException("Duplicate notification already exists for userId: " + 
                                        notification.getUserId() + ", eventId: " + notification.getEventId());
        }

        try {
            // Fetch user and event details using Feign clients
            logger.info("Fetching user details for userId: {}", notification.getUserId());
            User user = userClient.getUserById(notification.getUserId());
            if (user == null) {
                logger.error("User not found with Id: {}", notification.getUserId());
                throw new RuntimeException("User not found with Id: " + notification.getUserId());
            }

            logger.info("Fetching event details for eventId: {}", notification.getEventId());
            Event event = eventClient.getEventById(notification.getEventId());
            if (event == null) {
                logger.error("Event not found with Id: {}", notification.getEventId());
                throw new RuntimeException("Event not found with Id: " + notification.getEventId());
            }

            // Add timestamp and save the notification
            notification.setTimestamp(LocalDateTime.now());
            Notification savedNotification = notificationRepository.save(notification);
            logger.info("Notification successfully sent: {}", savedNotification);
            return savedNotification;

        } catch (Exception e) {
            logger.error("Error while sending notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    @Override
    public List<Notification> getAllNotificationsByUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid userId: " + userId);
        }

        try {
            return notificationRepository.findByUserId(userId);
        } catch (Exception e) {
            // Log the error and rethrow it
            System.err.println("Error while fetching notifications for userId: " + userId + " - " + e.getMessage());
            throw new RuntimeException("Failed to fetch notifications", e);
        }
    }
}
