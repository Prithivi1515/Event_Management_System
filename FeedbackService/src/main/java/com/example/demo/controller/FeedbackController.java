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

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    
    @Autowired
    FeedbackService service;

    @PostMapping("/save")
     public String saveFeedback(@RequestBody Feedback feedback)
    {
        return service.saveFeedback(feedback);
    }
    
    @PutMapping("/update/{fid}")
    public String updateFeedback(@PathVariable("fid") int feedbackId,@RequestBody Feedback feedback)
    {
        return service.updateFeedback(feedbackId, feedback);
    }
    
    @DeleteMapping("/delete/{fid}")
    public String deleteFeedback(@PathVariable("fid") int feedbackId)
    {
        return service.deleteFeedback(feedbackId);
    }
    
    @GetMapping("/get/{fid}")
    public String getFeedbackById(@PathVariable("fid") int feedbackId){
        return service.getFeedbackById(feedbackId);
    }
    
    @GetMapping("/getAllFeedbacksByUser/{uid}")
    public String getAllFeedbacksByUser(@PathVariable("uid") int userId)
    {
        return service.getAllFeedbacksByUser(userId);
    }
    
    @GetMapping("/getAllFeedbacksByEvent/{eid}")
    public String getAllFeedbacksByEvent(@PathVariable("eid") int eventId)
    {
        return service.getAllFeedbacksByEvent(eventId);
    }


    

}
