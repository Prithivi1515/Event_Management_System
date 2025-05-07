package com.example.demo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private int eventId;
    private String name;
    private String category;
    private String location;
    private LocalDateTime date;
    private int organizerId;
    private int ticketCount;
}
