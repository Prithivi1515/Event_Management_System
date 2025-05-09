package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.EventNotFoundException;
import com.example.demo.exception.FeedbackNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.Feedback;
import com.example.demo.service.FeedbackService;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    FeedbackService service;

    @PostMapping("/save")
    public String saveFeedback(@RequestBody Feedback feedback) {
        return service.saveFeedback(feedback);
    }

    @PutMapping("/update/{fid}")
    public String updateFeedback(@PathVariable("fid") int feedbackId, @RequestBody Feedback feedback) {
        String result = service.updateFeedback(feedbackId, feedback);
        if (result == null) {
            throw new FeedbackNotFoundException("Feedback not found with ID: " + feedbackId);
        }
        return result;
    }

    @DeleteMapping("/delete/{fid}")
    public String deleteFeedback(@PathVariable("fid") int feedbackId) {
        String result = service.deleteFeedback(feedbackId);
        if (result == null) {
            throw new FeedbackNotFoundException("Feedback not found with ID: " + feedbackId);
        }
        return result;
    }

    @GetMapping("/get/{fid}")
    public String getFeedbackById(@PathVariable("fid") int feedbackId) {
        String result = service.getFeedbackById(feedbackId);
        if (result == null) {
            throw new FeedbackNotFoundException("Feedback not found with ID: " + feedbackId);
        }
        return result;
    }

    @GetMapping("/getAllFeedbacksByUser/{uid}")
    public String getAllFeedbacksByUser(@PathVariable("uid") int userId) {
        String result = service.getAllFeedbacksByUser(userId);
        if (result == null || result.isEmpty()) {
            throw new UserNotFoundException("No feedback found for user with ID: " + userId);
        }
        return result;
    }

    @GetMapping("/getAllFeedbacksByEvent/{eid}")
    public String getAllFeedbacksByEvent(@PathVariable("eid") int eventId) {
        String result = service.getAllFeedbacksByEvent(eventId);
        if (result == null || result.isEmpty()) {
            throw new EventNotFoundException("No feedback found for event with ID: " + eventId);
        }
        return result;
    }

    @GetMapping("/getAverageRatingByEvent/{eid}")
    public String getAverageRatingByEvent(@PathVariable("eid") int eventId) {
        float averageRating = service.getAverageRatingByEvent(eventId);
        if (averageRating == 0) {
            throw new EventNotFoundException("No feedback available for the event with ID: " + eventId);
        }
        return "The average rating for the event with ID " + eventId + " is: " + averageRating;
    }
}
