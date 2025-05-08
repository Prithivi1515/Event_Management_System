package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {

    @Min(value = 1, message = "User ID must be greater than 0")
    private int userId;

    @Min(value = 1, message = "Event ID must be greater than 0")
    private int eventId;

    @NotBlank(message = "Message cannot be blank")
    private String message;
}
