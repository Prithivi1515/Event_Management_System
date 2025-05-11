package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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

    @NotNull(message = "Event ID cannot be null")
    @Min(value = 1, message = "Event ID must be greater than 0")
    private int eventId;

    @NotNull(message = "User ID cannot be null")
    @Min(value = 1, message = "User ID must be greater than 0")
    private int userId;

  //  @NotNull(message = "Booking date cannot be null")
    private LocalDateTime bookingDate;

//    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        BOOKED, CANCELLED
    }
    
    @PrePersist
    protected void onCreate() {
        if (bookingDate == null) {
            bookingDate = LocalDateTime.now();
        }
        if (status == null) {
            status = Status.BOOKED;
        }
    }
}
