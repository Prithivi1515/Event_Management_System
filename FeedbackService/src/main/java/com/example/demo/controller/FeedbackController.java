package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    FeedbackService service;

    @PostMapping("/save")
    public ResponseEntity<String> saveFeedback(@RequestBody Feedback feedback) {
        String result = service.saveFeedback(feedback);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update/{fid}")
    public ResponseEntity<String> updateFeedback(@PathVariable("fid") int feedbackId, @RequestBody Feedback feedback) {
        String result = service.updateFeedback(feedbackId, feedback);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete/{fid}")
    public ResponseEntity<String> deleteFeedback(@PathVariable("fid") int feedbackId) {
        String result = service.deleteFeedback(feedbackId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get/{fid}")
    public ResponseEntity<String> getFeedbackById(@PathVariable("fid") int feedbackId) {
        String result = service.getFeedbackById(feedbackId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getAllFeedbacksByUser/{uid}")
    public ResponseEntity<String> getAllFeedbacksByUser(@PathVariable("uid") int userId) {
        String result = service.getAllFeedbacksByUser(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getAllFeedbacksByEvent/{eid}")
    public ResponseEntity<String> getAllFeedbacksByEvent(@PathVariable("eid") int eventId) {
        String result = service.getAllFeedbacksByEvent(eventId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getAverageRatingByEvent/{eid}")
    public ResponseEntity<String> getAverageRatingByEvent(@PathVariable("eid") int eventId) {
        float averageRating = service.getAverageRatingByEvent(eventId);
        if (averageRating == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No feedback available for the event with ID: " + eventId);
        }
        return ResponseEntity.ok("The average rating for the event with ID " + eventId + " is: " + averageRating);
    }
}
