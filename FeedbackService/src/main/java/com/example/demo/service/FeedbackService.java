package com.example.demo.service;

import com.example.demo.model.Feedback;

public interface FeedbackService {
    public abstract String saveFeedback(Feedback feedback);
    
    public abstract String updateFeedback(int feedbackId, Feedback feedback);
    
    public abstract String deleteFeedback(int feedbackId);
    
    public abstract String getFeedbackById(int feedbackId);
    
    public abstract String getAllFeedbacksByUser(int userId);
    
    public abstract String getAllFeedbacksByEvent(int eventId);
}
