package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Find notifications by userId
    List<Notification> findByUserId(int userId);

    // Check if a notification exists for a specific user, event, and message (case-insensitive)
    @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE n.userId = :userId AND n.eventId = :eventId AND LOWER(TRIM(n.message)) = LOWER(TRIM(:message))")
    boolean existsByUserIdAndEventIdAndMessage(@Param("userId") int userId, @Param("eventId") int eventId, @Param("message") String message);

    // Find notifications by eventId (optional additional method)
    List<Notification> findByEventId(int eventId);
}
