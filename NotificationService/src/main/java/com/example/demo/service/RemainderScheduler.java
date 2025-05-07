package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.dto.Ticket;
import com.example.demo.feignclient.EventClient;
import com.example.demo.feignclient.TicketClient;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;

import com.example.demo.dto.Event;

@Service
public class RemainderScheduler {

    @Autowired
    TicketClient ticketClient;

    @Autowired
    EventClient eventClient;

    @Autowired
    NotificationRepository repository;

    // Every minute
    @Scheduled(fixedRate = 60000)
    public void sendRemainderForUpcomingEvents() {
        List<Ticket> allTickets = ticketClient.getAllTickets();
        if (allTickets == null || allTickets.isEmpty()) {
            System.out.println("No tickets found.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime remainderTime = now.plusHours(24);

        for (Ticket ticket : allTickets) {
            try {
                Event event = eventClient.getEventById(ticket.getEventId());
                if (event == null) {
                    System.out.println("Event not found for ticket ID: " + ticket.getEventId());
                    continue;
                }

                LocalDateTime eventDateTime = event.getEventDate(); 
                if (eventDateTime == null) {
                    System.out.println("Event date is null for event ID: " + ticket.getEventId());
                    continue;
                }

                if (eventDateTime.isAfter(now) && eventDateTime.isBefore(remainderTime)) {
                    boolean exists = repository.existsByUserIdAndEventIdAndMessage(
                            ticket.getUserId(),
                            ticket.getEventId(),
                            "Reminder: Your event is coming up soon!"
                    );

                    if (!exists) {
                        Notification notification = Notification.builder()
                                .userId(ticket.getUserId())
                                .eventId(ticket.getEventId())
                                .message("Reminder: Your event is coming up soon!")
                                .timestamp(LocalDateTime.now())
                                .build();
                        repository.save(notification);
                        System.out.println("Notification sent for ticket ID: " + ticket.getTicketId());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing ticket ID: " + ticket.getTicketId() + " - " + e.getMessage());
                e.printStackTrace(); // Log the full stack trace for debugging
            }
        }
    }
}
