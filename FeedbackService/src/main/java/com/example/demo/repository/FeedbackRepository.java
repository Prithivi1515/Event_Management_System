package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    // Find feedback by userId
    List<Feedback> findByUserId(int userId);

    // Find feedback by eventId
    List<Feedback> findByEventId(int eventId);

    // Calculate the average rating for a given eventId
    @Query("SELECT COALESCE(AVG(f.rating), 0) FROM Feedback f WHERE f.eventId = :eventId")
    double findAverageRatingByEventId(@Param("eventId") int eventId);

    boolean existsByUserIdAndEventId(int userId, int eventId);
}
