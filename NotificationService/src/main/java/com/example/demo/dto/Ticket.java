package com.example.demo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {

    private int ticketId;
    private int userId;
    private int eventId;
    private String ticketStatus;
    private LocalDateTime ticketDate; 
    private int ticketPrice;
    
}
