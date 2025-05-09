package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.dto.Ticket;
import com.example.demo.dto.Event;
import com.example.demo.exception.EventNotFoundException;
import com.example.demo.exception.NotificationNotFoundException;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.feignclient.EventClient;
import com.example.demo.feignclient.TicketClient;

@Service
public class RemainderScheduler {

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
        List<Ticket> allTickets = ticketClient.getAllTickets();

        if (allTickets == null || allTickets.isEmpty()) {
            throw new NotificationNotFoundException("No tickets found for sending reminders.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = now.plusHours(24);

        allTickets.forEach(ticket -> processTicket(ticket, now, reminderTime));
    }

    private void processTicket(Ticket ticket, LocalDateTime now, LocalDateTime reminderTime) {
        if (ticket.getEventId() <= 0 || ticket.getUserId() <= 0) {
            throw new IllegalArgumentException("Invalid ticket data for ticket ID: " + ticket.getTicketId());
        }

        Event event = eventClient.getEventById(ticket.getEventId());
        if (event == null) {
            throw new EventNotFoundException("Event not found for ticket ID: " + ticket.getTicketId());
        }

        LocalDateTime eventDateTime = event.getDate();
        if (eventDateTime == null) {
            throw new IllegalArgumentException("Event date is null for event ID: " + ticket.getEventId());
        }

        if (eventDateTime.isAfter(now) && eventDateTime.isBefore(reminderTime)) {
            sendNotificationIfNotExists(ticket, event);
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
        } else {
            throw new IllegalArgumentException("Duplicate notification already exists for ticket ID: " 
                    + ticket.getTicketId() + " and event ID: " + ticket.getEventId());
        }
    }
}
