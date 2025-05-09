package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.User;
import com.example.demo.exception.EventNotFoundException;
import com.example.demo.feignclient.UserClient;
import com.example.demo.model.Event;
import com.example.demo.repository.EventRepository;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository repository;

    @Autowired
    private UserClient userClient;

    @Override
    @Transactional
    public String createEvent(Event event) {
        int organizerId = event.getOrganizerId();

        // Fetch user details using Feign client with error handling
        User user;
        try {
            user = userClient.getUserById(organizerId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to fetch user details for organizer ID: " + organizerId, e);
        }

        // Validate user roles
        if (user == null || user.getRoles() == null || !user.getRoles().equalsIgnoreCase("organizer")) {
            throw new IllegalArgumentException("User is not an organizer or does not exist.");
        }

        // Save the event
        repository.save(event);
        return "Event created successfully.";
    }

    @Override
    public Event getEventById(int eventId) throws EventNotFoundException {
        return repository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));
    }

    @Override
    public List<Event> getAllEvents() throws EventNotFoundException {
        List<Event> events = repository.findAll();
        if (events.isEmpty()) {
            throw new EventNotFoundException("No events found in the system.");
        }
        return events;
    }

    @Override
    @Transactional
    public String updateEvent(int eventId, Event event) throws EventNotFoundException {
        if (!repository.existsById(eventId)) {
            throw new EventNotFoundException("Cannot update: Event not found with ID: " + eventId);
        }
        
        event.setEventId(eventId); // Ensure the ID matches
        repository.save(event);
        return "Event updated successfully.";
    }

    @Override
    @Transactional
    public String deleteEvent(int eventId) throws EventNotFoundException {
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));
        repository.delete(event);
        return "Event deleted successfully.";
    }

    @Override
    public List<Event> filterByCategory(String category) throws EventNotFoundException {
        // Fixed method call to match repository interface
        List<Event> events = repository.findByCategoryIgnoreCase(category);
        if (events.isEmpty()) {
            throw new EventNotFoundException("No events found for category: " + category);
        }
        return events;
    }

    @Override
    public List<Event> filterByLocation(String location) throws EventNotFoundException {
        // Fixed method call to match repository interface
        List<Event> events = repository.findByLocationIgnoreCase(location);
        if (events.isEmpty()) {
            throw new EventNotFoundException("No events found for location: " + location);
        }
        return events;
    }
    
    @Override
    public List<Event> searchEventsByName(String keyword) throws EventNotFoundException {
        List<Event> events = repository.findByNameContainingIgnoreCase(keyword);
        if (events.isEmpty()) {
            throw new EventNotFoundException("No events found matching keyword: " + keyword);
        }
        return events;
    }
    
    @Override
    public List<Event> getEventsByOrganizer(int organizerId) throws EventNotFoundException {
        List<Event> events = repository.findByOrganizerId(organizerId);
        if (events.isEmpty()) {
            throw new EventNotFoundException("No events found for organizer ID: " + organizerId);
        }
        return events;
    }

    @Override
    @Transactional
    public void decreaseTicketCount(int eventId) throws EventNotFoundException, IllegalArgumentException {
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));
        int currentCount = event.getTicketCount();
        if (currentCount > 0) {
            event.setTicketCount(currentCount - 1);
            repository.save(event);
        } else {
            throw new IllegalArgumentException("No tickets available for event with ID: " + eventId);
        }
    }

    @Override
    @Transactional
    public void increaseTicketCount(int eventId) throws EventNotFoundException {
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));
        int currentCount = event.getTicketCount();
        event.setTicketCount(currentCount + 1);
        repository.save(event);
    }
}
