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
    EventRepository repository;

    @Autowired
    UserClient userClient;

    @Override
    @Transactional
    public String createEvent(Event event) {
        int organizerId = event.getOrganizerId();

        // Fetch user details using Feign client with error handling
        User user;
        try {
            user = userClient.getUserById(organizerId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user details for organizer ID: " + organizerId, e);
        }

        // Validate user roles
        if (user == null || user.getRoles() == null || !user.getRoles().equalsIgnoreCase("organizer")) {
            throw new RuntimeException("User is not an organizer or does not exist.");
        }

        // Save the event
        repository.save(event);
        return "Event created successfully.";
    }

    @Override
    public Event getEventById(int eid) {
        return repository.findById(eid)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eid));
    }

    @Override
    public List<Event> getAllEvents() {
        List<Event> events = repository.findAll();
        if (events.isEmpty()) {
            throw new EventNotFoundException("No events found in the system.");
        }
        return events;
    }

    @Override
    public String deleteEvent(int eid) {
        Event event = repository.findById(eid)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eid));
        repository.delete(event);
        return "Event deleted successfully.";
    }

    @Override
    public List<Event> filterByCategory(String category) {
        List<Event> events = repository.findByCategory(category);
        if (events.isEmpty()) {
            throw new EventNotFoundException("No events found for category: " + category);
        }
        return events;
    }

    @Override
    public List<Event> filterByLocation(String location) {
        List<Event> events = repository.findByLocation(location);
        if (events.isEmpty()) {
            throw new EventNotFoundException("No events found for location: " + location);
        }
        return events;
    }

    @Override
    public void decreaseTicketCount(int eventId) {
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));
        int currentCount = event.getTicketCount();
        if (currentCount > 0) {
            event.setTicketCount(currentCount - 1);
            repository.save(event);
        } else {
            throw new RuntimeException("No tickets available for event with ID: " + eventId);
        }
    }

    @Override
    public void increaseTicketCount(int eventId) {
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));
        int currentCount = event.getTicketCount();
        event.setTicketCount(currentCount + 1);
        repository.save(event);
    }
}
