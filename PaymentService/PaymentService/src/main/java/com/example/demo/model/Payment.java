package com.example.demo.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class Payment {

	@Id
    @GeneratedValue
    private Long id;
 
    private String razorpayOrderId;
    private String paymentId;
    private String status; // created, success, failed, etc.
 
    private int amount;
 
    private LocalDateTime createdAt = LocalDateTime.now();
}
