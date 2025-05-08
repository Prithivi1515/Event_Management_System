package com.example.demo.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Min(value = 1, message = "Event ID must be greater than 0")
    private int eventId;

    @NotBlank(message = "Event name cannot be blank")
    private String name;

    @NotBlank(message = "Category cannot be blank")
    private String category;

    @NotBlank(message = "Location cannot be blank")
    private String location;

    @NotNull(message = "Event date cannot be null")
    @Future(message = "Event date must be in the future")
    private LocalDateTime date;

    @Min(value = 1, message = "Organizer ID must be greater than 0")
    private int organizerId;

    @Min(value = 0, message = "Ticket count cannot be negative")
    private int ticketCount;
}
