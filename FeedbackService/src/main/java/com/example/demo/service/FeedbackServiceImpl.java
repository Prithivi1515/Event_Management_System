package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(FeedbackServiceImpl.class);

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private EventClient eventClient;

    @Autowired
    private UserClient userClient;

    @Override
    public Feedback saveFeedback(Feedback feedback) {
        logger.info("Attempting to save feedback from user ID: {} for event ID: {}", 
                feedback.getUserId(), feedback.getEventId());
        
        if (feedback.getUserId() <= 0 || feedback.getEventId() <= 0) {
            logger.warn("Invalid feedback data: userId={}, eventId={}", 
                    feedback.getUserId(), feedback.getEventId());
            throw new IllegalArgumentException("User ID and Event ID must be greater than 0");
        }
        
        // Validate user
        logger.debug("Validating user ID: {}", feedback.getUserId());
        User user = userClient.getUserById(feedback.getUserId());
        if (user == null) {
            logger.error("User not found with ID: {}", feedback.getUserId());
            throw new UserNotFoundException("User not found with ID: " + feedback.getUserId());
        }
        logger.debug("User validated successfully: ID={}, name={}", user.getUserId(), user.getName());

        // Ensure one user can give feedback for one event only once
        logger.debug("Checking if user ID: {} has already given feedback for event ID: {}", 
                feedback.getUserId(), feedback.getEventId());
        if (feedbackRepository.existsByUserIdAndEventId(feedback.getUserId(), feedback.getEventId())) {
            logger.warn("Duplicate feedback attempt from user ID: {} for event ID: {}", 
                    feedback.getUserId(), feedback.getEventId());
            throw new FeedbackNotFoundException("User has already given feedback for this event.");
        }

        // Validate event
        logger.debug("Validating event ID: {}", feedback.getEventId());
        Event event = eventClient.getEventById(feedback.getEventId());
        if (event == null) {
            logger.error("Event not found with ID: {}", feedback.getEventId());
            throw new EventNotFoundException("Event not found with ID: " + feedback.getEventId());
        }
        logger.debug("Event validated successfully: ID={}, name={}", event.getEventId(), event.getName());

        // Save feedback
        logger.debug("Setting timestamp and saving feedback");
        feedback.setTimestamp(LocalDateTime.now());
        Feedback savedFeedback = feedbackRepository.save(feedback);
        logger.info("Feedback saved successfully with ID: {}, rating: {}", 
                savedFeedback.getFeedbackId(), savedFeedback.getRating());
        return savedFeedback;
    }

    @Override
    public Feedback updateFeedback(int feedbackId, Feedback feedback) {
        logger.info("Attempting to update feedback with ID: {}", feedbackId);
        
        if (feedbackId <= 0) {
            logger.warn("Invalid feedback ID: {}", feedbackId);
            throw new IllegalArgumentException("Feedback ID must be greater than 0");
        }
        
        logger.debug("Fetching existing feedback with ID: {}", feedbackId);
        Feedback existingFeedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> {
                    logger.error("Feedback not found with ID: {}", feedbackId);
                    return new FeedbackNotFoundException("Feedback not found with ID: " + feedbackId);
                });
        
        logger.debug("Updating feedback fields: comments and rating");
        existingFeedback.setComments(feedback.getComments());
        existingFeedback.setRating(feedback.getRating());
        
        Feedback updatedFeedback = feedbackRepository.save(existingFeedback);
        logger.info("Feedback updated successfully: ID={}, new rating={}", 
                updatedFeedback.getFeedbackId(), updatedFeedback.getRating());
        return updatedFeedback;
    }

    @Override
    public String deleteFeedback(int feedbackId) {
        logger.info("Attempting to delete feedback with ID: {}", feedbackId);
        
        if (feedbackId <= 0) {
            logger.warn("Invalid feedback ID: {}", feedbackId);
            throw new IllegalArgumentException("Feedback ID must be greater than 0");
        }
        
        logger.debug("Fetching feedback with ID: {} for deletion", feedbackId);
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> {
                    logger.error("Cannot delete - feedback not found with ID: {}", feedbackId);
                    return new FeedbackNotFoundException("Feedback not found with ID: " + feedbackId);
                });
                
        logger.debug("Deleting feedback from database, ID: {}", feedbackId);
        feedbackRepository.delete(feedback);
        logger.info("Feedback deleted successfully, ID: {}", feedbackId);
        return "Feedback deleted successfully!";
    }

    @Override
    public Feedback getFeedbackById(int feedbackId) {
        logger.info("Retrieving feedback with ID: {}", feedbackId);
        
        if (feedbackId <= 0) {
            logger.warn("Invalid feedback ID: {}", feedbackId);
            throw new IllegalArgumentException("Feedback ID must be greater than 0");
        }
        
        logger.debug("Fetching feedback from database with ID: {}", feedbackId);
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> {
                    logger.error("Feedback not found with ID: {}", feedbackId);
                    return new FeedbackNotFoundException("Feedback not found with ID: " + feedbackId);
                });
                
        logger.debug("Feedback retrieved successfully: ID={}, eventId={}, userId={}, rating={}",
                feedback.getFeedbackId(), feedback.getEventId(), feedback.getUserId(), feedback.getRating());
        return feedback;
    }

    @Override
    public List<Feedback> getAllFeedbacksByUser(int userId) {
        logger.info("Retrieving all feedback for user ID: {}", userId);
        
        if (userId <= 0) {
            logger.warn("Invalid user ID: {}", userId);
            throw new IllegalArgumentException("User ID must be greater than 0");
        }
        
        logger.debug("Fetching feedback from database for user ID: {}", userId);
        List<Feedback> feedbacks = feedbackRepository.findByUserId(userId);
        if (feedbacks.isEmpty()) {
            logger.warn("No feedback found for user with ID: {}", userId);
            throw new UserNotFoundException("No feedback found for user with ID: " + userId);
        }
        
        logger.info("Retrieved {} feedback entries for user ID: {}", feedbacks.size(), userId);
        return feedbacks;
    }

    @Override
    public List<Feedback> getAllFeedbacksByEvent(int eventId) {
        logger.info("Retrieving all feedback for event ID: {}", eventId);
        
        if (eventId <= 0) {
            logger.warn("Invalid event ID: {}", eventId);
            throw new IllegalArgumentException("Event ID must be greater than 0");
        }
        
        logger.debug("Fetching feedback from database for event ID: {}", eventId);
        List<Feedback> feedbacks = feedbackRepository.findByEventId(eventId);
        if (feedbacks.isEmpty()) {
            logger.warn("No feedback found for event with ID: {}", eventId);
            throw new EventNotFoundException("No feedback found for event with ID: " + eventId);
        }
        
        logger.info("Retrieved {} feedback entries for event ID: {}", feedbacks.size(), eventId);
        return feedbacks;
    }

    @Override
    public float getAverageRatingByEvent(int eventId) {
        logger.info("Calculating average rating for event ID: {}", eventId);
        
        if (eventId <= 0) {
            logger.warn("Invalid event ID: {}", eventId);
            throw new IllegalArgumentException("Event ID must be greater than 0");
        }
        
        logger.debug("Querying database for average rating for event ID: {}", eventId);
        double averageRating = feedbackRepository.findAverageRatingByEventId(eventId);
        if (averageRating == 0) {
            logger.warn("No feedback available for calculating average rating for event ID: {}", eventId);
            throw new EventNotFoundException("No feedback available for the event with ID: " + eventId);
        }
        
        logger.info("Average rating for event ID: {} is {}", eventId, averageRating);
        return (float) averageRating;
    }
}
