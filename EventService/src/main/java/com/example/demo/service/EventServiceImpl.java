package com.example.demo.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.User;
import com.example.demo.exception.EventNotFoundException;
import com.example.demo.feignclient.UserClient;
import com.example.demo.model.Event;
import com.example.demo.repository.EventRepository;

@Service
public class EventServiceImpl implements EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);
    
    // Log message constants
    private static final String LOG_EVENT_NOT_FOUND = "Event not found with ID: {}";
    private static final String LOG_FETCH_EVENT = "Fetching event with ID: {}";
    private static final String LOG_UPDATE_ATTEMPT = "Attempting to update event with ID: {}";
    private static final String LOG_DELETE_ATTEMPT = "Attempting to delete event with ID: {}";
    private static final String LOG_TICKET_COUNT_DECREASE = "Attempting to decrease ticket count for event ID: {}";
    private static final String LOG_TICKET_COUNT_INCREASE = "Attempting to increase ticket count for event ID: {}";
    
    // Error message constants
    private static final String ERR_EVENT_NOT_FOUND = "Event not found with ID: ";
    private static final String ERR_CANNOT_UPDATE = "Cannot update: Event not found with ID: ";
    private static final String ERR_NO_TICKETS = "No tickets available for event with ID: ";
    private static final String ERR_DB_DECREASE = "Failed to decrease ticket count due to database error";
    private static final String ERR_DB_INCREASE = "Failed to increase ticket count due to database error";

    private final EventRepository repository;
    private final UserClient userClient;

    public EventServiceImpl(EventRepository repository, UserClient userClient) {
        this.repository = repository;
        this.userClient = userClient;
    }

    @Override
    public String createEvent(Event event) {
        logger.info("Attempting to create event by organizer ID: {}", event.getOrganizerId());
        int organizerId = event.getOrganizerId();

        // Fetch user details using Feign client with error handling
        User user;
        try {
            logger.debug("Validating organizer with ID: {}", organizerId);
            user = userClient.getUserById(organizerId);
        } catch (Exception e) {
            logger.error("Failed to fetch user details for organizer ID: {}: {}", organizerId, e.getMessage());
            throw new IllegalArgumentException("Failed to fetch user details for organizer ID: " + organizerId, e);
        }

        // Validate user roles
        if (user == null || user.getRoles() == null || !user.getRoles().equalsIgnoreCase("organizer")) {
            logger.warn("Invalid organizer attempt: user ID: {} has role: {}", 
                    organizerId, user != null ? user.getRoles() : "null");
            throw new IllegalArgumentException("User is not an organizer or does not exist.");
        }

        // Save the event
        logger.debug("Saving event to database: {}", event.getName());
        repository.save(event);
        logger.info("Event created successfully: ID={}, name={}", event.getEventId(), event.getName());
        return "Event created successfully.";
    }

    @Override
    public Event getEventById(int eventId) throws EventNotFoundException {
        logger.debug(LOG_FETCH_EVENT, eventId);
        
        return repository.findById(eventId)
                .orElseThrow(() -> {
                    logger.error(LOG_EVENT_NOT_FOUND, eventId);
                    return new EventNotFoundException(ERR_EVENT_NOT_FOUND + eventId);
                });
    }

    @Override
    public List<Event> getAllEvents() throws EventNotFoundException {
        logger.debug("Retrieving all events");
        
        List<Event> events = repository.findAll();
        if (events.isEmpty()) {
            logger.warn("No events found in the system");
            throw new EventNotFoundException("No events found in the system.");
        }
        
        logger.info("Retrieved {} events", events.size());
        return events;
    }

    @Override
    public String updateEvent(int eventId, Event event) throws EventNotFoundException {
        logger.info(LOG_UPDATE_ATTEMPT, eventId);
        
        if (!repository.existsById(eventId)) {
            logger.error(LOG_EVENT_NOT_FOUND, eventId);
            throw new EventNotFoundException(ERR_CANNOT_UPDATE + eventId);
        }
        
        event.setEventId(eventId); // Ensure the ID matches
        repository.save(event);
        logger.info("Event updated successfully: ID={}, name={}", eventId, event.getName());
        return "Event updated successfully.";
    }

    @Override
    public String deleteEvent(int eventId) throws EventNotFoundException {
        logger.info(LOG_DELETE_ATTEMPT, eventId);
        
        Event event = repository.findById(eventId)
                .orElseThrow(() -> {
                    logger.error(LOG_EVENT_NOT_FOUND, eventId);
                    return new EventNotFoundException(ERR_EVENT_NOT_FOUND + eventId);
                });
                
        repository.delete(event);
        logger.info("Event deleted successfully: ID={}, name={}", eventId, event.getName());
        return "Event deleted successfully.";
    }

    @Override
    public List<Event> filterByCategory(String category) throws EventNotFoundException {
        logger.debug("Filtering events by category: {}", category);
        
        List<Event> events = repository.findByCategoryIgnoreCase(category);
        if (events.isEmpty()) {
            logger.warn("No events found for category: {}", category);
            throw new EventNotFoundException("No events found for category: " + category);
        }
        
        logger.info("Found {} events for category: {}", events.size(), category);
        return events;
    }

    @Override
    public List<Event> filterByLocation(String location) throws EventNotFoundException {
        logger.debug("Filtering events by location: {}", location);
        
        List<Event> events = repository.findByLocationIgnoreCase(location);
        if (events.isEmpty()) {
            logger.warn("No events found for location: {}", location);
            throw new EventNotFoundException("No events found for location: " + location);
        }
        
        logger.info("Found {} events for location: {}", events.size(), location);
        return events;
    }
    
    @Override
    public List<Event> searchEventsByName(String keyword) throws EventNotFoundException {
        logger.debug("Searching events by keyword: {}", keyword);
        
        List<Event> events = repository.findByNameContainingIgnoreCase(keyword);
        if (events.isEmpty()) {
            logger.warn("No events found matching keyword: {}", keyword);
            throw new EventNotFoundException("No events found matching keyword: " + keyword);
        }
        
        logger.info("Found {} events matching keyword: {}", events.size(), keyword);
        return events;
    }
    
    @Override
    public List<Event> getEventsByOrganizer(int organizerId) throws EventNotFoundException {
        logger.debug("Retrieving events for organizer ID: {}", organizerId);
        
        List<Event> events = repository.findByOrganizerId(organizerId);
        if (events.isEmpty()) {
            logger.warn("No events found for organizer ID: {}", organizerId);
            throw new EventNotFoundException("No events found for organizer ID: " + organizerId);
        }
        
        logger.info("Found {} events for organizer ID: {}", events.size(), organizerId);
        return events;
    }

    @Override
    @Transactional
    public void decreaseTicketCount(int eventId) throws EventNotFoundException, IllegalArgumentException {
        logger.info(LOG_TICKET_COUNT_DECREASE, eventId);
        
        // Use pessimistic locking to prevent race conditions
        Event event = repository.findById(eventId)
                .orElseThrow(() -> {
                    logger.error(LOG_EVENT_NOT_FOUND, eventId);
                    return new EventNotFoundException(ERR_EVENT_NOT_FOUND + eventId);
                });
                
        // Validate ticket availability
        int currentCount = event.getTicketCount();
        logger.debug("Current ticket count for event ID {}: {}", eventId, currentCount);
        
        if (currentCount <= 0) {
            logger.warn("No tickets available for event ID: {}", eventId);
            throw new IllegalArgumentException(ERR_NO_TICKETS + eventId);
        }
        
        // Update ticket count
        event.setTicketCount(currentCount - 1);
        logger.debug("Decreasing ticket count from {} to {} for event ID: {}", 
                currentCount, currentCount - 1, eventId);
        
        try {
            repository.save(event);
            logger.info("Ticket count decreased successfully for event ID: {}, new count: {}", 
                    eventId, currentCount - 1);
        } catch (Exception e) {
            logger.error("Database error decreasing ticket count for event ID {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException(ERR_DB_DECREASE, e);
        }
    }

    @Override
    @Transactional
    public void increaseTicketCount(int eventId) throws EventNotFoundException {
        logger.info(LOG_TICKET_COUNT_INCREASE, eventId);
        
        // Use pessimistic locking to prevent race conditions
        Event event = repository.findById(eventId)
                .orElseThrow(() -> {
                    logger.error(LOG_EVENT_NOT_FOUND, eventId);
                    return new EventNotFoundException(ERR_EVENT_NOT_FOUND + eventId);
                });
                
        int currentCount = event.getTicketCount();
        logger.debug("Current ticket count for event ID {}: {}", eventId, currentCount);
        
        event.setTicketCount(currentCount + 1);
        logger.debug("Increasing ticket count from {} to {} for event ID: {}", 
                currentCount, currentCount + 1, eventId);
        
        try {
            repository.save(event);
            logger.info("Ticket count increased successfully for event ID: {}, new count: {}", 
                    eventId, currentCount + 1);
        } catch (Exception e) {
            logger.error("Database error increasing ticket count for event ID {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException(ERR_DB_INCREASE, e);
        }
    }
}
