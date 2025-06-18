package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TicketServiceImpl implements TicketService {
    
    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);
    
    // Log message constants
    private static final String LOG_VERIFY_USER_EXISTS = "Verifying user exists with ID: {}";
    private static final String LOG_VERIFY_EVENT_EXISTS = "Verifying event exists with ID: {}";
    private static final String LOG_INVALID_USER_ID = "Invalid user ID: {}";
    private static final String LOG_INVALID_EVENT_ID = "Invalid event ID: {}";
    private static final String LOG_INVALID_STATUS = "Invalid status: null";
    private static final String LOG_BOOKING_ATTEMPT = "Attempting to book ticket for user ID: {} and event ID: {}";
    private static final String LOG_TICKET_SAVED = "Ticket saved successfully with ID: {}";
    private static final String LOG_USER_NOT_FOUND = "User not found with ID: {}";
    private static final String LOG_EVENT_NOT_FOUND = "Event not found with ID: {}";
    
    // Error message constants
    private static final String ERR_USER_ID_INVALID = "User ID must be greater than 0";
    private static final String ERR_EVENT_ID_INVALID = "Event ID must be greater than 0";
    private static final String ERR_STATUS_NULL = "Status cannot be null";
    private static final String ERR_USER_NOT_FOUND = "User not found with ID: %d";
    private static final String ERR_EVENT_NOT_FOUND = "Event not found with ID: %d";
    private static final String ERR_NO_TICKETS = "No tickets available for event ID: %d";
    private static final String ERR_DUPLICATE_TICKET = "Ticket already booked for user ID: %d and event ID: %d";
    private static final String ERR_TICKET_ALREADY_CANCELLED = "Ticket with ID %d is already canceled";
    private static final String ERR_TICKET_ID_INVALID = "Ticket ID must be greater than 0";
    
    // Dependencies
    private final TicketRepository repository;
    private final EventClient eventClient;
    private final UserClient userClient;
    private final NotificationClient notificationClient;


    @Override
    @Transactional
    public Ticket bookTicket(Ticket ticket) throws UserNotFoundException, EventNotFoundException {
        logger.info(LOG_BOOKING_ATTEMPT, ticket.getUserId(), ticket.getEventId());
        
        if (ticket.getUserId() <= 0) {
            logger.warn(LOG_INVALID_USER_ID, ticket.getUserId());
            throw new IllegalArgumentException(ERR_USER_ID_INVALID);
        }
        
        if (ticket.getEventId() <= 0) {
            logger.warn(LOG_INVALID_EVENT_ID, ticket.getEventId());
            throw new IllegalArgumentException(ERR_EVENT_ID_INVALID);
        }
        
        // Check if the user exists
        logger.debug(LOG_VERIFY_USER_EXISTS, ticket.getUserId());
        User user = userClient.getUserById(ticket.getUserId());
        if (user == null) {
            logger.error(LOG_USER_NOT_FOUND, ticket.getUserId());
            throw new UserNotFoundException(String.format(ERR_USER_NOT_FOUND, ticket.getUserId()));
        }
        
        // Check if the event exists 
        logger.debug(LOG_VERIFY_EVENT_EXISTS, ticket.getEventId());
        Event event = eventClient.getEventById(ticket.getEventId());
        if (event == null) {
            logger.error(LOG_EVENT_NOT_FOUND, ticket.getEventId());
            throw new EventNotFoundException(String.format(ERR_EVENT_NOT_FOUND, ticket.getEventId()));
        }
        
        // Check if event has available tickets
        logger.debug("Checking ticket availability for event ID: {}, available: {}", 
                ticket.getEventId(), event.getTicketCount());
        if (event.getTicketCount() <= 0) {
            logger.warn("No tickets available for event ID: {}", ticket.getEventId());
            throw new IllegalArgumentException(String.format(ERR_NO_TICKETS, ticket.getEventId()));
        }

        // Set ticket details
        ticket.setBookingDate(LocalDateTime.now());
        ticket.setStatus(Status.BOOKED);
        int quantity =ticket.getQuantity();
        
        // Save the ticket first - if this fails, we won't decrease the ticket count
        logger.debug("Saving ticket to database");
        Ticket savedTicket = repository.save(ticket);
        logger.info(LOG_TICKET_SAVED, savedTicket.getTicketId());
        
        try {
            // Now decrease ticket count
            logger.debug("Decreasing ticket count for event ID: {}", ticket.getEventId());
            eventClient.decreaseTicketCount(ticket.getEventId(),quantity);
            logger.info("Ticket count decreased successfully for event ID: {}", ticket.getEventId());
        } catch (Exception e) {
            // If decreasing ticket count fails, delete the saved ticket to maintain consistency
            logger.error("Failed to decrease ticket count for event ID: {}: {}", 
                    ticket.getEventId(), e.getMessage(), e);
            logger.debug("Deleting ticket ID: {} to maintain consistency", savedTicket.getTicketId());
            repository.delete(savedTicket);
            throw new RuntimeException(
                    String.format("Failed to decrease ticket count for event ID: %d", ticket.getEventId()), e);
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
                    ticket.getUserId(), e.getMessage(), e);
            // Non-critical operation, don't rethrow
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
            throw new IllegalArgumentException(ERR_TICKET_ID_INVALID);
        }
        
        return repository.findById(ticketId)
                .orElseThrow(() -> {
                    logger.error("Ticket not found with ID: {}", ticketId);
                    return new TicketNotFoundException(
                            String.format("Ticket not found with ID: %d", ticketId));
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
            logger.warn(LOG_INVALID_USER_ID, userId);
            throw new IllegalArgumentException(ERR_USER_ID_INVALID);
        }
        
        // Verify user exists
        logger.debug(LOG_VERIFY_USER_EXISTS, userId);
        User user = userClient.getUserById(userId);
        if (user == null) {
            logger.error(LOG_USER_NOT_FOUND, userId);
            throw new UserNotFoundException(String.format(ERR_USER_NOT_FOUND, userId));
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
            logger.warn(LOG_INVALID_EVENT_ID, eventId);
            throw new IllegalArgumentException(ERR_EVENT_ID_INVALID);
        }
        
        // Verify event exists
        logger.debug(LOG_VERIFY_EVENT_EXISTS, eventId);
        Event event = eventClient.getEventById(eventId);
        if (event == null) {
            logger.error(LOG_EVENT_NOT_FOUND, eventId);
            throw new EventNotFoundException(String.format(ERR_EVENT_NOT_FOUND, eventId));
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
            throw new IllegalArgumentException(String.format(ERR_TICKET_ALREADY_CANCELLED, ticketId));
        }
        int quantity =ticket.getQuantity();


        // Try to increase ticket count BEFORE marking ticket as cancelled
        try {
            logger.debug("Increasing ticket count for event ID: {}", ticket.getEventId());
            // Increase ticket count in event service first
            eventClient.increaseTicketCount(ticket.getEventId(),quantity);
            logger.info("Ticket count increased successfully for event ID: {}", ticket.getEventId());
        } catch (Exception e) {
            // Log the error with contextual information and rethrow
            String errorMessage = String.format("Failed to increase event ticket count for event ID: %d", 
                    ticket.getEventId());
            logger.error("{}: {}", errorMessage, e.getMessage(), e);
            throw new RuntimeException(errorMessage, e);
        }

        // Now that ticket count is increased, mark ticket as cancelled
        logger.debug("Setting ticket ID: {} status to CANCELLED", ticket.getTicketId());
        ticket.setStatus(Status.CANCELLED);
        Ticket savedTicket = repository.save(ticket);
        logger.info("Ticket ID: {} canceled successfully", ticket.getTicketId());

        // Send notification 
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
                    ticket.getUserId(), e.getMessage(), e);
        }
        
        return savedTicket;
    }
    
    @Override
    public List<Ticket> getTicketsByStatus(Status status) {
        logger.debug("Retrieving tickets with status: {}", status);
        
        if (status == null) {
            logger.warn(LOG_INVALID_STATUS);
            throw new IllegalArgumentException(ERR_STATUS_NULL);
        }
        
        List<Ticket> tickets = repository.findByStatus(status);
        logger.info("Found {} tickets with status: {}", tickets.size(), status);
        return tickets;
    }
    
    @Override
    public List<Ticket> getTicketsByUserIdAndStatus(int userId, Status status) throws UserNotFoundException {
        logger.debug("Retrieving tickets for user ID: {} with status: {}", userId, status);
        
        if (userId <= 0) {
            logger.warn(LOG_INVALID_USER_ID, userId);
            throw new IllegalArgumentException(ERR_USER_ID_INVALID);
        }
        
        if (status == null) {
            logger.warn(LOG_INVALID_STATUS);
            throw new IllegalArgumentException(ERR_STATUS_NULL);
        }
        
        // Verify user exists
        logger.debug(LOG_VERIFY_USER_EXISTS, userId);
        User user = userClient.getUserById(userId);
        if (user == null) {
            logger.error(LOG_USER_NOT_FOUND, userId);
            throw new UserNotFoundException(String.format(ERR_USER_NOT_FOUND, userId));
        }
        
        List<Ticket> tickets = repository.findByUserIdAndStatus(userId, status);
        logger.info("Found {} tickets for user ID: {} with status: {}", tickets.size(), userId, status);
        return tickets;
    }
    
    @Override
    public List<Ticket> getTicketsByEventIdAndStatus(int eventId, Status status) throws EventNotFoundException {
        logger.debug("Retrieving tickets for event ID: {} with status: {}", eventId, status);
        
        if (eventId <= 0) {
            logger.warn(LOG_INVALID_EVENT_ID, eventId);
            throw new IllegalArgumentException(ERR_EVENT_ID_INVALID);
        }
        
        if (status == null) {
            logger.warn(LOG_INVALID_STATUS);
            throw new IllegalArgumentException(ERR_STATUS_NULL);
        }
        
        // Verify event exists
        logger.debug(LOG_VERIFY_EVENT_EXISTS, eventId);
        Event event = eventClient.getEventById(eventId);
        if (event == null) {
            logger.error(LOG_EVENT_NOT_FOUND, eventId);
            throw new EventNotFoundException(String.format(ERR_EVENT_NOT_FOUND, eventId));
        }
        
        List<Ticket> tickets = repository.findByEventIdAndStatus(eventId, status);
        logger.info("Found {} tickets for event ID: {} with status: {}", tickets.size(), eventId, status);
        return tickets;
    }
    
    @Override
    public boolean hasUserBookedEvent(int userId, int eventId) {
        logger.debug("Checking if user ID: {} has booked event ID: {}", userId, eventId);
        
        if (userId <= 0) {
            logger.warn(LOG_INVALID_USER_ID, userId);
            throw new IllegalArgumentException(ERR_USER_ID_INVALID);
        }
        
        if (eventId <= 0) {
            logger.warn(LOG_INVALID_EVENT_ID, eventId);
            throw new IllegalArgumentException(ERR_EVENT_ID_INVALID);
        }
        
        boolean hasBooked = repository.existsByUserIdAndEventId(userId, eventId);
        logger.info("User ID: {} has {} event ID: {}", userId, hasBooked ? "booked" : "not booked", eventId);
        return hasBooked;
    }
}
