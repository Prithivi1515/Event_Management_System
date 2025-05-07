package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Find notifications by userId
    List<Notification> findByUserId(int userId);

    // Check if a notification exists for a specific user, event, and message
    boolean existsByUserIdAndEventIdAndMessage(int userId, int eventId, String message);

    // Uncomment or add additional methods if needed
    // List<Notification> findByEventId(int eventId);
}
