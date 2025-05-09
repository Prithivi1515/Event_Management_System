package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.Event;
import com.example.demo.dto.User;
import com.example.demo.exception.EventNotFoundException;
import com.example.demo.exception.FeedbackNotFoundException;
import com.example.demo.exception.UserNotFoundException;
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
    public Feedback saveFeedback(Feedback feedback) {
        // Validate user
        User user = userClient.getUserById(feedback.getUserId());
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + feedback.getUserId());
        }

        // Ensure one user can give feedback for one event only once
        if (feedbackRepository.existsByUserIdAndEventId(feedback.getUserId(), feedback.getEventId())) {
            throw new FeedbackNotFoundException("User has already given feedback for this event.");
        }

        // Validate event
        Event event = eventClient.getEventById(feedback.getEventId());
        if (event == null) {
            throw new EventNotFoundException("Event not found with ID: " + feedback.getEventId());
        }

        // Save feedback
        feedback.setTimestamp(LocalDateTime.now());
        feedbackRepository.save(feedback);
        return feedback;
    }

    @Override
    public Feedback updateFeedback(int feedbackId, Feedback feedback) {
        Feedback existingFeedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found with ID: " + feedbackId));

        existingFeedback.setComments(feedback.getComments());
        existingFeedback.setRating(feedback.getRating());
        feedbackRepository.save(existingFeedback);
        return existingFeedback;
    }

    @Override
    public String deleteFeedback(int feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found with ID: " + feedbackId));
        feedbackRepository.delete(feedback);
        return "Feedback deleted successfully!";
    }

    @Override
    public Feedback getFeedbackById(int feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found with ID: " + feedbackId));
        return feedback;
    }

    @Override
    public List<Feedback> getAllFeedbacksByUser(int userId) {
        List<Feedback> feedbacks = feedbackRepository.findByUserId(userId);
        if (feedbacks.isEmpty()) {
            throw new UserNotFoundException("No feedback found for user with ID: " + userId);
        }
        return feedbacks;
    }

    @Override
    public List<Feedback> getAllFeedbacksByEvent(int eventId) {
        List<Feedback> feedbacks = feedbackRepository.findByEventId(eventId);
        if (feedbacks.isEmpty()) {
            throw new EventNotFoundException("No feedback found for event with ID: " + eventId);
        }
        return feedbacks;
    }

    @Override
    public float getAverageRatingByEvent(int eventId) {
        double averageRating = feedbackRepository.findAverageRatingByEventId(eventId);
        if (averageRating == 0) {
            throw new EventNotFoundException("No feedback available for the event with ID: " + eventId);
        }
        return (float) averageRating;
    }
}
