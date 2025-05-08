package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.Event;
import com.example.demo.dto.NotificationRequest;
import com.example.demo.exception.EventNotFoundException;
import com.example.demo.exception.TicketNotFoundException;
import com.example.demo.feignclient.EventClient;
import com.example.demo.feignclient.NotificationClient;
import com.example.demo.model.Ticket;
import com.example.demo.model.Ticket.Status;
import com.example.demo.repository.TicketRepository;

@Service
public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    @Autowired
    private TicketRepository repository;

    @Autowired
    private EventClient eventClient;

    @Autowired
    private NotificationClient notificationClient;

    @Override
    public Ticket bookTicket(Ticket ticket) {
        // Check if the event exists
        Event event = eventClient.getEventById(ticket.getEventId());
        if (event == null) {
            logger.error("Event not found with ID: {}", ticket.getEventId());
            throw new EventNotFoundException("Event not found with ID: " + ticket.getEventId());
        }

        // Check for duplicate ticket booking
        if (repository.existsByUserIdAndEventId(ticket.getUserId(), ticket.getEventId())) {
            logger.error("Duplicate ticket booking attempt for userId: {}, eventId: {}", ticket.getUserId(), ticket.getEventId());
            throw new IllegalArgumentException("Ticket already booked for userId: " + ticket.getUserId() + " and eventId: " + ticket.getEventId());
        }

        // Decrease ticket count and set ticket details
        eventClient.decreaseTicketCount(ticket.getEventId());
        ticket.setBookingDate(LocalDateTime.now());
        ticket.setStatus(Status.BOOKED);

        // Send notification
        try {
            NotificationRequest notificationRequest = new NotificationRequest(
                ticket.getUserId(),
                ticket.getEventId(),
                "Your ticket has been successfully booked"
            );
            notificationClient.sendNotification(notificationRequest);
        } catch (Exception e) {
            logger.error("Failed to send notification for ticket booking: {}", e.getMessage(), e);
        }

        return repository.save(ticket);
    }

    @Override
    public Ticket getTicketById(int ticketId) {
        return repository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));
    }

    @Override
    public List<Ticket> getTicketsByUserId(int userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public List<Ticket> getTicketsByEventId(int eventId) {
        return repository.findByEventId(eventId);
    }

    @Override
    public void cancelTicket(int ticketId) {
        Ticket ticket = getTicketById(ticketId);

        // Check if the ticket is already canceled
        if (ticket.getStatus() == Status.CANCELLED) {
            logger.warn("Ticket with ID {} is already canceled", ticketId);
            throw new IllegalArgumentException("Ticket with ID " + ticketId + " is already canceled.");
        }

        // Cancel the ticket and increase ticket count
        ticket.setStatus(Status.CANCELLED);
        eventClient.increaseTicketCount(ticket.getEventId());
        repository.save(ticket);

        // Send notification
        try {
            NotificationRequest notificationRequest = new NotificationRequest(
                ticket.getUserId(),
                ticket.getEventId(),
                "Your ticket has been successfully canceled"
            );
            notificationClient.sendNotification(notificationRequest);
        } catch (Exception e) {
            logger.error("Failed to send notification for ticket cancellation: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<Ticket> getAllTickets() {
        return repository.findAll();
    }
}
