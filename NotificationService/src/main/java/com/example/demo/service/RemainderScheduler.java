package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.dto.Ticket;
import com.example.demo.dto.Event;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.feignclient.EventClient;
import com.example.demo.feignclient.TicketClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RemainderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RemainderScheduler.class);
    private static final String REMINDER_MESSAGE = "Reminder: Your event is coming up soon!";

    @Autowired
    private TicketClient ticketClient;

    @Autowired
    private EventClient eventClient;

    @Autowired
    private NotificationRepository repository;

    // Scheduled to run every minute
    @Scheduled(fixedRate = 60000)
    public void sendRemainderForUpcomingEvents() {
        logger.info("Starting scheduled task to send reminders for upcoming events.");

        List<Ticket> allTickets;
        try {
            allTickets = ticketClient.getAllTickets();
        } catch (Exception e) {
            logger.error("Failed to fetch tickets from TicketClient: {}", e.getMessage(), e);
            return;
        }

        if (allTickets == null || allTickets.isEmpty()) {
            logger.info("No tickets found.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = now.plusHours(24);

        allTickets.forEach(ticket -> processTicket(ticket, now, reminderTime));

        logger.info("Scheduled task to send reminders completed.");
    }

    private void processTicket(Ticket ticket, LocalDateTime now, LocalDateTime reminderTime) {
        try {
            if (ticket.getEventId() <= 0 || ticket.getUserId() <= 0) {
                logger.warn("Invalid ticket data: {}", ticket);
                return;
            }

            Event event;
            try {
                event = eventClient.getEventById(ticket.getEventId());
            } catch (Exception e) {
                logger.error("Failed to fetch event details for event ID {}: {}", ticket.getEventId(), e.getMessage(), e);
                return;
            }

            if (event == null) {
                logger.warn("Event not found for ticket ID: {}", ticket.getTicketId());
                return;
            }

            LocalDateTime eventDateTime = event.getDate();
            if (eventDateTime == null) {
                logger.warn("Event date is null for event ID: {}", ticket.getEventId());
                return;
            }

            if (eventDateTime.isAfter(now) && eventDateTime.isBefore(reminderTime)) {
                sendNotificationIfNotExists(ticket, event);
            }
        } catch (Exception e) {
            logger.error("Error processing ticket ID {}: {}", ticket.getTicketId(), e.getMessage(), e);
        }
    }

    private void sendNotificationIfNotExists(Ticket ticket, Event event) {
        boolean exists = repository.existsByUserIdAndEventIdAndMessage(
                ticket.getUserId(),
                ticket.getEventId(),
                REMINDER_MESSAGE
        );

        if (!exists) {
            Notification notification = Notification.builder()
                    .userId(ticket.getUserId())
                    .eventId(ticket.getEventId())
                    .message(REMINDER_MESSAGE)
                    .timestamp(LocalDateTime.now())
                    .build();
            repository.save(notification);
            logger.info("Notification sent for ticket ID: {}, event ID: {}", ticket.getTicketId(), ticket.getEventId());
        } else {
            logger.info("Notification already exists for ticket ID: {}, event ID: {}", ticket.getTicketId(), ticket.getEventId());
        }
    }
}
