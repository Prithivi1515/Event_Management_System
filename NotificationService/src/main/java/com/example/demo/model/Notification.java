package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", nullable = false)
    private int notificationId;

    @NotNull(message = "User ID cannot be null")
    private int userId;

    @NotNull(message = "Event ID cannot be null")
    private int eventId;

    @NotBlank(message = "Message cannot be blank")
    private String message;

    @NotNull(message = "Timestamp cannot be null")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
