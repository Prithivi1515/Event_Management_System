package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    // Custom query methods can be defined here if needed
    // For example, to find feedback by userId or eventId, you can add:
    List<Feedback> findByUserId(int userId);
    List<Feedback> findByEventId(int eventId);
    
}
