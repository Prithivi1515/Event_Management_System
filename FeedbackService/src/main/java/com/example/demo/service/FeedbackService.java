package com.example.demo.service;

import java.util.List;

import com.example.demo.model.Feedback;

public interface FeedbackService {
    public abstract Feedback saveFeedback(Feedback feedback);
    
    public abstract Feedback updateFeedback(int feedbackId, Feedback feedback);
    
    public abstract String deleteFeedback(int feedbackId);
    
    public abstract Feedback getFeedbackById(int feedbackId);
    
    public abstract List<Feedback> getAllFeedbacksByUser(int userId);
    
    public abstract List<Feedback> getAllFeedbacksByEvent(int eventId);

    public abstract float getAverageRatingByEvent(int eventId);
}
