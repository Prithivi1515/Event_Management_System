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
    private int userId; 
    private int eventId;
    private String comments; 
    private int rating; 
    private LocalDateTime timestamp; 

   
    
}
