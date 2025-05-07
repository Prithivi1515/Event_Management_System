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
    EventClient eventClient;
    
    @Autowired
    UserClient userClient;
    @Override
    public String saveFeedback(Feedback feedback) {
    	
    	User userid =userClient.getUserById(feedback.getUserId());
		if (userid == null) {
			throw new RuntimeException("Event not found with Id: " + feedback.getUserId());
		}

		Event event = eventClient.getEventById(feedback.getEventId());
		if (event == null) {
			throw new RuntimeException("Event not found with Id: " + feedback.getEventId());
		}
		feedback.setTimestamp(LocalDateTime.now());
        feedbackRepository.save(feedback);
        return "Feedback saved successfully!";
    }

    @Override
    public String updateFeedback(int feedbackId, Feedback feedback) {
        Feedback existingFeedback = feedbackRepository.findById(feedbackId).orElse(null);
        if (existingFeedback != null) {
            existingFeedback.setComments(feedback.getComments());
            existingFeedback.setRating(feedback.getRating());
            feedbackRepository.save(existingFeedback);
            return "Feedback updated successfully!";
        } else {
            return "Feedback not found!";
        }
    }

    @Override
    public String deleteFeedback(int feedbackId) {
        feedbackRepository.deleteById(feedbackId);
        return "Feedback deleted successfully!";
    }

    @Override
    public String getFeedbackById(int feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId).orElse(null);
        if (feedback != null) {
             return feedback.toString();
         } else {
             return "Feedback not found!";
         }
    }

    @Override
    public String getAllFeedbacksByUser(int userId) {
        List<Feedback> feedbacks = feedbackRepository.findByUserId(userId);
        return feedbacks.toString();
    }

    @Override
    public String getAllFeedbacksByEvent(int eventId) {
        List<Feedback> feedbacks = feedbackRepository.findByEventId(eventId);
        return feedbacks.toString();
    }

    @Override
    public float getAverageRatingByEvent(int eventId) {
        List<Feedback> feedbacks = feedbackRepository.findByEventId(eventId);
        if (feedbacks.isEmpty()) {
            return 0;
        }
        float totalRating = 0;
        for (Feedback feedback : feedbacks) {
            totalRating += feedback.getRating();
        }
        return totalRating / feedbacks.size();
    }
    
}
