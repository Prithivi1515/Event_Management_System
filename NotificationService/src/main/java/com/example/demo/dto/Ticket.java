package com.example.demo.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {

    @NotNull(message = "Ticket ID cannot be null")
    private int ticketId;

    @NotNull(message = "User ID cannot be null")
    private int userId;

    @NotNull(message = "Event ID cannot be null")
    private int eventId;

    @NotBlank(message = "Ticket status cannot be blank")
    private String ticketStatus;

    @NotNull(message = "Ticket date cannot be null")
    private LocalDateTime ticketDate;

    @Min(value = 0, message = "Ticket price cannot be negative")
    private int ticketPrice;
}
