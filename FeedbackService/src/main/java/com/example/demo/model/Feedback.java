package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Feedback {
    @Id
    @GeneratedValue
    private int feedbackId;
    private int userId; // ID of the user providing the feedback
    private int eventId; // ID of the event related to the feedback
    private String comments; // Feedback comments provided by the user
    private int rating; // Rating given by the user (e.g., 1 to 5 stars)
    private LocalDateTime timestamp; // Time when the feedback was submitted

   
    
}
