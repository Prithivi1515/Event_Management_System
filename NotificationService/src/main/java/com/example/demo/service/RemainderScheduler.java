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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RemainderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RemainderScheduler.class);

    @Autowired
    TicketClient ticketClient;

    @Autowired
    EventClient eventClient;

    @Autowired
    NotificationRepository repository;

    // Every minute
    @Scheduled(fixedRate = 60000)
    public void sendRemainderForUpcomingEvents() {
        logger.info("Starting scheduled task to send reminders for upcoming events.");

        List<Ticket> allTickets;
        try {
            allTickets = ticketClient.getAllTickets();
        } catch (Exception e) {
            logger.error("Failed to fetch tickets from TicketClient: {}", e.getMessage());
            return;
        }

        if (allTickets == null || allTickets.isEmpty()) {
            logger.info("No tickets found.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime remainderTime = now.plusHours(24);

        for (Ticket ticket : allTickets) {
            try {
                if (ticket.getEventId() <= 0 || ticket.getUserId() <= 0) {
                    logger.warn("Invalid ticket data: {}", ticket);
                    continue;
                }

                Event event;
                try {
                    event = eventClient.getEventById(ticket.getEventId());
                } catch (Exception e) {
                    logger.error("Failed to fetch event details for event ID {}: {}", ticket.getEventId(), e.getMessage());
                    continue;
                }

                if (event == null) {
                    logger.warn("Event not found for ticket ID: {}", ticket.getTicketId());
                    continue;
                }

                LocalDateTime eventDateTime = event.getDate();
                if (eventDateTime == null) {
                    logger.warn("Event date is null for event ID: {}", ticket.getEventId());
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
                        logger.info("Notification sent for ticket ID: {}", ticket.getTicketId());
                    } else {
                        logger.info("Notification already exists for ticket ID: {}", ticket.getTicketId());
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing ticket ID {}: {}", ticket.getTicketId(), e.getMessage());
                logger.debug("Stack trace: ", e);
            }
        }

        logger.info("Scheduled task to send reminders completed.");
    }
}
