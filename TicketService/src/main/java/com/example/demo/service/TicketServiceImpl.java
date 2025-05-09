package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.Event;
import com.example.demo.dto.NotificationRequest;
import com.example.demo.dto.User;
import com.example.demo.exception.EventNotFoundException;
import com.example.demo.exception.TicketNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.feignclient.EventClient;
import com.example.demo.feignclient.NotificationClient;
import com.example.demo.feignclient.UserClient;
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
    private UserClient userClient;

    @Autowired
    private NotificationClient notificationClient;

    @Override
    @Transactional
    public Ticket bookTicket(Ticket ticket) throws UserNotFoundException, EventNotFoundException {
        if (ticket.getUserId() <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0");
        }
        
        if (ticket.getEventId() <= 0) {
            throw new IllegalArgumentException("Event ID must be greater than 0");
        }
        
        // Check if the user exists
        User user = userClient.getUserById(ticket.getUserId());
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + ticket.getUserId());
        }
        
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
    public Ticket getTicketById(int ticketId) throws TicketNotFoundException {
        if (ticketId <= 0) {
            throw new IllegalArgumentException("Ticket ID must be greater than 0");
        }
        
        return repository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));
    }

    @Override
    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = repository.findAll();
        if (tickets.isEmpty()) {
            throw new TicketNotFoundException("No tickets found in the system.");
        }
        return tickets;
    }

    @Override
    public List<Ticket> getTicketsByUserId(int userId) throws UserNotFoundException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0");
        }
        
        // Verify user exists
        User user = userClient.getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        
        List<Ticket> tickets = repository.findByUserId(userId);
        if (tickets.isEmpty()) {
            // Just return empty list instead of throwing exception
            return tickets;
        }
        return tickets;
    }

    @Override
    public List<Ticket> getTicketsByEventId(int eventId) throws EventNotFoundException {
        if (eventId <= 0) {
            throw new IllegalArgumentException("Event ID must be greater than 0");
        }
        
        // Verify event exists
        Event event = eventClient.getEventById(eventId);
        if (event == null) {
            throw new EventNotFoundException("Event not found with ID: " + eventId);
        }
        
        List<Ticket> tickets = repository.findByEventId(eventId);
        if (tickets.isEmpty()) {
            // Just return empty list instead of throwing exception
            return tickets;
        }
        return tickets;
    }

    @Override
    @Transactional
    public Ticket cancelTicket(int ticketId) throws TicketNotFoundException {
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
        
        return ticket; // Return the updated ticket
    }
    
    @Override
    public List<Ticket> getTicketsByStatus(Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        return repository.findByStatus(status);
    }
    
    @Override
    public List<Ticket> getTicketsByUserIdAndStatus(int userId, Status status) throws UserNotFoundException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0");
        }
        
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        // Verify user exists
        User user = userClient.getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        
        return repository.findByUserIdAndStatus(userId, status);
    }
    
    @Override
    public List<Ticket> getTicketsByEventIdAndStatus(int eventId, Status status) throws EventNotFoundException {
        if (eventId <= 0) {
            throw new IllegalArgumentException("Event ID must be greater than 0");
        }
        
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        // Verify event exists
        Event event = eventClient.getEventById(eventId);
        if (event == null) {
            throw new EventNotFoundException("Event not found with ID: " + eventId);
        }
        
        return repository.findByEventIdAndStatus(eventId, status);
    }
    
    @Override
    public boolean hasUserBookedEvent(int userId, int eventId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0");
        }
        
        if (eventId <= 0) {
            throw new IllegalArgumentException("Event ID must be greater than 0");
        }
        
        return repository.existsByUserIdAndEventId(userId, eventId);
    }
}
