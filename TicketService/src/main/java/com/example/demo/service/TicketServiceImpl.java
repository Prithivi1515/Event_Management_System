package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

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
        logger.info("Attempting to book ticket for user ID: {} and event ID: {}", ticket.getUserId(), ticket.getEventId());
        
        if (ticket.getUserId() <= 0) {
            logger.warn("Invalid user ID: {}", ticket.getUserId());
            throw new IllegalArgumentException("User ID must be greater than 0");
        }
        
        if (ticket.getEventId() <= 0) {
            logger.warn("Invalid event ID: {}", ticket.getEventId());
            throw new IllegalArgumentException("Event ID must be greater than 0");
        }
        
        // Check if the user exists
        logger.debug("Verifying user exists with ID: {}", ticket.getUserId());
        User user = userClient.getUserById(ticket.getUserId());
        if (user == null) {
            logger.error("User not found with ID: {}", ticket.getUserId());
            throw new UserNotFoundException("User not found with ID: " + ticket.getUserId());
        }
        
        // Check if the event exists and has available tickets
        logger.debug("Verifying event exists with ID: {}", ticket.getEventId());
        Event event = eventClient.getEventById(ticket.getEventId());
        if (event == null) {
            logger.error("Event not found with ID: {}", ticket.getEventId());
            throw new EventNotFoundException("Event not found with ID: " + ticket.getEventId());
        }
        
        // Check if event has available tickets
        logger.debug("Checking ticket availability for event ID: {}, available: {}", 
                ticket.getEventId(), event.getTicketCount());
        if (event.getTicketCount() <= 0) {
            logger.warn("No tickets available for event ID: {}", ticket.getEventId());
            throw new IllegalArgumentException("No tickets available for event ID: " + ticket.getEventId());
        }

        // Check for duplicate ticket booking
        logger.debug("Checking for existing bookings for user ID: {} and event ID: {}", 
                ticket.getUserId(), ticket.getEventId());
        if (repository.existsByUserIdAndEventId(ticket.getUserId(), ticket.getEventId())) {
            logger.warn("Duplicate booking attempt - user ID: {} already has a ticket for event ID: {}", 
                    ticket.getUserId(), ticket.getEventId());
            throw new IllegalArgumentException("Ticket already booked for user ID: " + ticket.getUserId() + 
                                              " and event ID: " + ticket.getEventId());
        }

        // Set ticket details
        ticket.setBookingDate(LocalDateTime.now());
        ticket.setStatus(Status.BOOKED);
        
        // Save the ticket first - if this fails, we won't decrease the ticket count
        logger.debug("Saving ticket to database");
        Ticket savedTicket = repository.save(ticket);
        logger.info("Ticket saved successfully with ID: {}", savedTicket.getTicketId());
        
        try {
            // Now decrease ticket count
            logger.debug("Decreasing ticket count for event ID: {}", ticket.getEventId());
            eventClient.decreaseTicketCount(ticket.getEventId());
            logger.info("Ticket count decreased successfully for event ID: {}", ticket.getEventId());
        } catch (Exception e) {
            // If decreasing ticket count fails, delete the saved ticket to maintain consistency
            logger.error("Failed to decrease ticket count for event ID: {}: {}", ticket.getEventId(), e.getMessage());
            logger.debug("Deleting ticket ID: {} to maintain consistency", savedTicket.getTicketId());
            repository.delete(savedTicket);
            throw new RuntimeException("Failed to decrease ticket count: " + e.getMessage(), e);
        }

        // Only send notification if everything else succeeded
        try {
            logger.debug("Sending booking notification to user ID: {}", ticket.getUserId());
            NotificationRequest notificationRequest = new NotificationRequest(
                ticket.getUserId(),
                ticket.getEventId(),
                "Your ticket has been successfully booked"
            );
            notificationClient.sendNotification(notificationRequest);
            logger.info("Booking notification sent to user ID: {}", ticket.getUserId());
        } catch (Exception e) {
            // Log the notification failure but don't roll back the transaction
            logger.warn("Failed to send booking notification to user ID: {}: {}", 
                    ticket.getUserId(), e.getMessage());
        }

        logger.info("Ticket booking completed successfully - ticket ID: {}, user ID: {}, event ID: {}", 
                savedTicket.getTicketId(), ticket.getUserId(), ticket.getEventId());
        return savedTicket;
    }

    @Override
    public Ticket getTicketById(int ticketId) throws TicketNotFoundException {
        logger.debug("Fetching ticket with ID: {}", ticketId);
        
        if (ticketId <= 0) {
            logger.warn("Invalid ticket ID: {}", ticketId);
            throw new IllegalArgumentException("Ticket ID must be greater than 0");
        }
        
        return repository.findById(ticketId)
                .orElseThrow(() -> {
                    logger.error("Ticket not found with ID: {}", ticketId);
                    return new TicketNotFoundException("Ticket not found with ID: " + ticketId);
                });
    }

    @Override
    public List<Ticket> getAllTickets() {
        logger.debug("Retrieving all tickets");
        
        List<Ticket> tickets = repository.findAll();
        if (tickets.isEmpty()) {
            logger.warn("No tickets found in the system");
            throw new TicketNotFoundException("No tickets found in the system.");
        }
        
        logger.info("Retrieved {} tickets from database", tickets.size());
        return tickets;
    }

    @Override
    public List<Ticket> getTicketsByUserId(int userId) throws UserNotFoundException {
        logger.debug("Retrieving tickets for user ID: {}", userId);
        
        if (userId <= 0) {
            logger.warn("Invalid user ID: {}", userId);
            throw new IllegalArgumentException("User ID must be greater than 0");
        }
        
        // Verify user exists
        logger.debug("Verifying user exists with ID: {}", userId);
        User user = userClient.getUserById(userId);
        if (user == null) {
            logger.error("User not found with ID: {}", userId);
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        
        List<Ticket> tickets = repository.findByUserId(userId);
        if (tickets.isEmpty()) {
            logger.info("No tickets found for user ID: {}", userId);
        } else {
            logger.info("Found {} tickets for user ID: {}", tickets.size(), userId);
        }
        return tickets;
    }

    @Override
    public List<Ticket> getTicketsByEventId(int eventId) throws EventNotFoundException {
        logger.debug("Retrieving tickets for event ID: {}", eventId);
        
        if (eventId <= 0) {
            logger.warn("Invalid event ID: {}", eventId);
            throw new IllegalArgumentException("Event ID must be greater than 0");
        }
        
        // Verify event exists
        logger.debug("Verifying event exists with ID: {}", eventId);
        Event event = eventClient.getEventById(eventId);
        if (event == null) {
            logger.error("Event not found with ID: {}", eventId);
            throw new EventNotFoundException("Event not found with ID: " + eventId);
        }
        
        List<Ticket> tickets = repository.findByEventId(eventId);
        if (tickets.isEmpty()) {
            logger.info("No tickets found for event ID: {}", eventId);
        } else {
            logger.info("Found {} tickets for event ID: {}", tickets.size(), eventId);
        }
        return tickets;
    }

    @Override
    @Transactional
    public Ticket cancelTicket(int ticketId) throws TicketNotFoundException {
        logger.info("Attempting to cancel ticket with ID: {}", ticketId);
        
        // First, get and validate the ticket
        Ticket ticket = getTicketById(ticketId);
        logger.debug("Retrieved ticket ID: {} for user ID: {} and event ID: {}", 
                ticket.getTicketId(), ticket.getUserId(), ticket.getEventId());

        // Check if the ticket is already canceled
        if (ticket.getStatus() == Status.CANCELLED) {
            logger.warn("Ticket ID: {} is already canceled", ticketId);
            throw new IllegalArgumentException("Ticket with ID " + ticketId + " is already canceled.");
        }

        // Try to increase ticket count BEFORE marking ticket as cancelled
        try {
            logger.debug("Increasing ticket count for event ID: {}", ticket.getEventId());
            // Increase ticket count in event service first
            eventClient.increaseTicketCount(ticket.getEventId());
            logger.info("Ticket count increased successfully for event ID: {}", ticket.getEventId());
        } catch (Exception e) {
            // Log the error and throw a more specific exception
            String errorMessage = "Failed to increase event ticket count: " + e.getMessage();
            logger.error("Failed to increase ticket count for event ID: {}: {}", ticket.getEventId(), e.getMessage());
            throw new RuntimeException(errorMessage, e); 
        }

        // Now that ticket count is increased, mark ticket as cancelled
        logger.debug("Setting ticket ID: {} status to CANCELLED", ticket.getTicketId());
        ticket.setStatus(Status.CANCELLED);
        Ticket savedTicket = repository.save(ticket);
        logger.info("Ticket ID: {} canceled successfully", ticket.getTicketId());

        // Send notification (non-critical operation)
        try {
            logger.debug("Sending cancellation notification to user ID: {}", ticket.getUserId());
            NotificationRequest notificationRequest = new NotificationRequest(
                ticket.getUserId(),
                ticket.getEventId(),
                "Your ticket has been successfully canceled"
            );
            notificationClient.sendNotification(notificationRequest);
            logger.info("Cancellation notification sent to user ID: {}", ticket.getUserId());
        } catch (Exception e) {
            // Log notification failure but don't fail the cancellation
            logger.warn("Failed to send cancellation notification to user ID: {}: {}", 
                    ticket.getUserId(), e.getMessage());
        }
        
        return savedTicket;
    }
    
    @Override
    public List<Ticket> getTicketsByStatus(Status status) {
        logger.debug("Retrieving tickets with status: {}", status);
        
        if (status == null) {
            logger.warn("Invalid status: null");
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        List<Ticket> tickets = repository.findByStatus(status);
        logger.info("Found {} tickets with status: {}", tickets.size(), status);
        return tickets;
    }
    
    @Override
    public List<Ticket> getTicketsByUserIdAndStatus(int userId, Status status) throws UserNotFoundException {
        logger.debug("Retrieving tickets for user ID: {} with status: {}", userId, status);
        
        if (userId <= 0) {
            logger.warn("Invalid user ID: {}", userId);
            throw new IllegalArgumentException("User ID must be greater than 0");
        }
        
        if (status == null) {
            logger.warn("Invalid status: null");
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        // Verify user exists
        logger.debug("Verifying user exists with ID: {}", userId);
        User user = userClient.getUserById(userId);
        if (user == null) {
            logger.error("User not found with ID: {}", userId);
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        
        List<Ticket> tickets = repository.findByUserIdAndStatus(userId, status);
        logger.info("Found {} tickets for user ID: {} with status: {}", tickets.size(), userId, status);
        return tickets;
    }
    
    @Override
    public List<Ticket> getTicketsByEventIdAndStatus(int eventId, Status status) throws EventNotFoundException {
        logger.debug("Retrieving tickets for event ID: {} with status: {}", eventId, status);
        
        if (eventId <= 0) {
            logger.warn("Invalid event ID: {}", eventId);
            throw new IllegalArgumentException("Event ID must be greater than 0");
        }
        
        if (status == null) {
            logger.warn("Invalid status: null");
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        // Verify event exists
        logger.debug("Verifying event exists with ID: {}", eventId);
        Event event = eventClient.getEventById(eventId);
        if (event == null) {
            logger.error("Event not found with ID: {}", eventId);
            throw new EventNotFoundException("Event not found with ID: " + eventId);
        }
        
        List<Ticket> tickets = repository.findByEventIdAndStatus(eventId, status);
        logger.info("Found {} tickets for event ID: {} with status: {}", tickets.size(), eventId, status);
        return tickets;
    }
    
    @Override
    public boolean hasUserBookedEvent(int userId, int eventId) {
        logger.debug("Checking if user ID: {} has booked event ID: {}", userId, eventId);
        
        if (userId <= 0) {
            logger.warn("Invalid user ID: {}", userId);
            throw new IllegalArgumentException("User ID must be greater than 0");
        }
        
        if (eventId <= 0) {
            logger.warn("Invalid event ID: {}", eventId);
            throw new IllegalArgumentException("Event ID must be greater than 0");
        }
        
        boolean hasBooked = repository.existsByUserIdAndEventId(userId, eventId);
        logger.info("User ID: {} has {} event ID: {}", userId, hasBooked ? "booked" : "not booked", eventId);
        return hasBooked;
    }
}
