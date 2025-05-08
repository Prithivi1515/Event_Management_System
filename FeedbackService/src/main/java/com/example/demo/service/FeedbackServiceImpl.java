package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.Event;
import com.example.demo.dto.User;
import com.example.demo.feignclient.EventClient;
import com.example.demo.feignclient.UserClient;
import com.example.demo.model.Feedback;
import com.example.demo.repository.FeedbackRepository;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private EventClient eventClient;

    @Autowired
    private UserClient userClient;

    @Override
    public String saveFeedback(Feedback feedback) {
        // Validate user
        User user = userClient.getUserById(feedback.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + feedback.getUserId());
        }

        // Validate event
        Event event = eventClient.getEventById(feedback.getEventId());
        if (event == null) {
            throw new IllegalArgumentException("Event not found with ID: " + feedback.getEventId());
        }

        // Save feedback
        feedback.setTimestamp(LocalDateTime.now());
        feedbackRepository.save(feedback);
        return "Feedback saved successfully!";
    }

    @Override
    public String updateFeedback(int feedbackId, Feedback feedback) {
        Feedback existingFeedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found with ID: " + feedbackId));

        existingFeedback.setComments(feedback.getComments());
        existingFeedback.setRating(feedback.getRating());
        feedbackRepository.save(existingFeedback);
        return "Feedback updated successfully!";
    }

    @Override
    public String deleteFeedback(int feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found with ID: " + feedbackId));
        feedbackRepository.delete(feedback);
        return "Feedback deleted successfully!";
    }

    @Override
    public String getFeedbackById(int feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found with ID: " + feedbackId));
        return feedback.toString();
    }

    @Override
    public String getAllFeedbacksByUser(int userId) {
        List<Feedback> feedbacks = feedbackRepository.findByUserId(userId);
        if (feedbacks.isEmpty()) {
            return "No feedback found for user with ID: " + userId;
        }
        return feedbacks.toString();
    }

    @Override
    public String getAllFeedbacksByEvent(int eventId) {
        List<Feedback> feedbacks = feedbackRepository.findByEventId(eventId);
        if (feedbacks.isEmpty()) {
            return "No feedback found for event with ID: " + eventId;
        }
        return feedbacks.toString();
    }

    @Override
    public float getAverageRatingByEvent(int eventId) {
        double averageRating = feedbackRepository.findAverageRatingByEventId(eventId);
        return (float) averageRating;
    }
}
