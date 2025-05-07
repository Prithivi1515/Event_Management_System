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

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    UserClient userClient;

    @Autowired
    EventClient eventClient;

    @Autowired
    TicketClient ticketClient;

    @Override
    public Notification sendNotification(int userId, int eventId, String message) {
        // Validate input parameters
        if (userId <= 0 || eventId <= 0 || message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid input parameters: userId, eventId, or message.");
        }

        try {
            // Fetch user and event details using Feign clients
            User user = userClient.getUserById(userId);
            if (user == null) {
                throw new RuntimeException("User not found with Id: " + userId);
            }

            Event event = eventClient.getEventById(eventId);
            if (event == null) {
                throw new RuntimeException("Event not found with Id: " + eventId);
            }

            // Create a new notification object
            Notification notification = Notification.builder()
                    .userId(userId)
                    .eventId(eventId)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();

            // Save the notification to the database
            return notificationRepository.save(notification);

        } catch (Exception e) {
            // Log the error and rethrow it
            System.err.println("Error while sending notification: " + e.getMessage());
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
