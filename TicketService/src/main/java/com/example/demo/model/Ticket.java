package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ticketId;

    @Min(value = 1, message = "Event ID must be greater than 0")
    private int eventId;

    @Min(value = 1, message = "User ID must be greater than 0")
    private int userId;

    @NotNull(message = "Booking date cannot be null")
    private LocalDateTime bookingDate;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status cannot be null")
    private Status status;

    public enum Status {
        BOOKED, CANCELLED
    }
}
