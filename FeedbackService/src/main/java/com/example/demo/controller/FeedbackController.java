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

import com.example.demo.model.Feedback;
import com.example.demo.service.FeedbackService;

import java.util.List;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    FeedbackService service;

    @PostMapping("/save")
    public Feedback saveFeedback(@RequestBody Feedback feedback) {
        return service.saveFeedback(feedback);
    }

    @PutMapping("/update/{fid}")
    public Feedback updateFeedback(@PathVariable("fid") int feedbackId, @RequestBody Feedback feedback) {
        return service.updateFeedback(feedbackId, feedback);
    }

    @DeleteMapping("/delete/{fid}")
    public String deleteFeedback(@PathVariable("fid") int feedbackId) {
        service.deleteFeedback(feedbackId);
        return "Feedback deleted successfully!";
    }

    @GetMapping("/get/{fid}")
    public Feedback getFeedbackById(@PathVariable("fid") int feedbackId) {
        return service.getFeedbackById(feedbackId);
    }

    @GetMapping("/getAllFeedbacksByUser/{uid}")
    public List<Feedback> getAllFeedbacksByUser(@PathVariable("uid") int userId) {
        return service.getAllFeedbacksByUser(userId);
    }

    @GetMapping("/getAllFeedbacksByEvent/{eid}")
    public List<Feedback> getAllFeedbacksByEvent(@PathVariable("eid") int eventId) {
        return service.getAllFeedbacksByEvent(eventId);
    }

    @GetMapping("/getAverageRatingByEvent/{eid}")
    public String getAverageRatingByEvent(@PathVariable("eid") int eventId) {
        float averageRating = service.getAverageRatingByEvent(eventId);
        return "The average rating for the event with ID " + eventId + " is: " + averageRating;
    }
}
