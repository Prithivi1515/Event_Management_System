package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.dto.Ticket;
import com.example.demo.dto.Event;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;

import lombok.AllArgsConstructor;

import com.example.demo.feignclient.EventClient;
import com.example.demo.feignclient.TicketClient;

@Service
@AllArgsConstructor
public class RemainderScheduler { 

    private static final String REMINDER_MESSAGE = "Reminder: Your event is coming up soon!";
    private static final Logger logger = Logger.getLogger(RemainderScheduler.class.getName());

    private TicketClient ticketClient;
    private EventClient eventClient;
    private NotificationRepository repository;

    // Scheduled to run every minute
    @Scheduled(fixedRate = 60000)
    public void sendReminderForUpcomingEvents() {
        try {
            List<Ticket> allTickets = ticketClient.getAllTickets();

            if (allTickets == null || allTickets.isEmpty()) {
                logger.info("No tickets found for sending reminders.");
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reminderTime = now.plusHours(24);

            allTickets.forEach(ticket -> {
                try {
                    processTicket(ticket, now, reminderTime);
                } catch (Exception e) {
                    logger.log(Level.WARNING, 
                        "Error processing ticket ID: " + ticket.getTicketId(), e);
                }
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in reminder scheduler job", e);
        }
    }

    private void processTicket(Ticket ticket, LocalDateTime now, LocalDateTime reminderTime) {
        // Validate ticket
        if (ticket.getEventId() <= 0 || ticket.getUserId() <= 0) {
            logger.warning(() -> "Invalid ticket data for ticket ID: " + ticket.getTicketId());
            return;  
        }

        try {
            Event event = eventClient.getEventById(ticket.getEventId());
            if (event == null) {
                logger.warning(() -> "Event not found for ticket ID: " + ticket.getTicketId());
                return;  
            }

            LocalDateTime eventDateTime = event.getDate();
            if (eventDateTime == null) {
                logger.warning(() -> "Event date is null for event ID: " + ticket.getEventId());
                return;
            }

            if (eventDateTime.isAfter(now) && eventDateTime.isBefore(reminderTime)) {
                sendNotificationIfNotExists(ticket);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, 
                "Error retrieving event for ticket ID: " + ticket.getTicketId(), e);
        }
    }

    private void sendNotificationIfNotExists(Ticket ticket) {
        try {
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
                logger.info(() -> "Reminder notification sent for user ID: " + ticket.getUserId() + 
                           " and event ID: " + ticket.getEventId());
            } else {
                logger.fine(() -> "Notification already exists for user ID: " + ticket.getUserId() +
                           " and event ID: " + ticket.getEventId());
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, 
                "Error sending notification for ticket ID: " + ticket.getTicketId(), e);
        }
    }
}
