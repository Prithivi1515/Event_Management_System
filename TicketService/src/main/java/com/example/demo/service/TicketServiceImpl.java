package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

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
            throw new EventNotFoundException("Event not found with ID: " + ticket.getEventId());
        }

        // Check for duplicate ticket booking
        if (repository.existsByUserIdAndEventId(ticket.getUserId(), ticket.getEventId())) {
            throw new IllegalArgumentException("Ticket already booked for user ID: " + ticket.getUserId() + " and event ID: " + ticket.getEventId());
        }

        // Decrease ticket count and set ticket details
        eventClient.decreaseTicketCount(ticket.getEventId());
        ticket.setBookingDate(LocalDateTime.now());
        ticket.setStatus(Status.BOOKED);

        // Send notification
        NotificationRequest notificationRequest = new NotificationRequest(
            ticket.getUserId(),
            ticket.getEventId(),
            "Your ticket has been successfully booked"
        );
        notificationClient.sendNotification(notificationRequest);

        return repository.save(ticket);
    }

    @Override
    public Ticket getTicketById(int ticketId) {
        return repository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));
    }

    @Override
    public List<Ticket> getTicketsByUserId(int userId) {
        List<Ticket> tickets = repository.findByUserId(userId);
        if (tickets.isEmpty()) {
            throw new TicketNotFoundException("No tickets found for user ID: " + userId);
        }
        return tickets;
    }

    @Override
    public List<Ticket> getTicketsByEventId(int eventId) {
        List<Ticket> tickets = repository.findByEventId(eventId);
        if (tickets.isEmpty()) {
            throw new TicketNotFoundException("No tickets found for event ID: " + eventId);
        }
        return tickets;
    }

    @Override
    public void cancelTicket(int ticketId) {
        Ticket ticket = getTicketById(ticketId);

        // Check if the ticket is already canceled
        if (ticket.getStatus() == Status.CANCELLED) {
            throw new IllegalArgumentException("Ticket with ID " + ticketId + " is already canceled.");
        }

        // Cancel the ticket and increase ticket count
        ticket.setStatus(Status.CANCELLED);
        eventClient.increaseTicketCount(ticket.getEventId());
        repository.save(ticket);

        // Send notification
        NotificationRequest notificationRequest = new NotificationRequest(
            ticket.getUserId(),
            ticket.getEventId(),
            "Your ticket has been successfully canceled"
        );
        notificationClient.sendNotification(notificationRequest);
    }

    @Override
    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = repository.findAll();
        if (tickets.isEmpty()) {
            throw new TicketNotFoundException("No tickets found in the system.");
        }
        return tickets;
    }
}
