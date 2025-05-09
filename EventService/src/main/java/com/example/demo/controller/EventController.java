package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.EventNotFoundException;
import com.example.demo.model.Event;
import com.example.demo.service.EventService;

@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    EventService service;

    @PostMapping("/create")
    public String createEvent(@RequestBody Event event) {
        String result = service.createEvent(event);
        if (!"Event created successfully.".equals(result)) {
            throw new IllegalArgumentException(result);
        }
        return result;
    }

    @GetMapping("/getEventById/{eid}")
    public Event getEventById(@PathVariable("eid") int eid) {
        Event event = service.getEventById(eid);
        if (event == null) {
            throw new EventNotFoundException("Event not found with ID: " + eid);
        }
        return event;
    }

    @GetMapping("/getAllEvents")
    public List<Event> getAllEvents() {
        List<Event> events = service.getAllEvents();
        if (events.isEmpty()) {
            throw new EventNotFoundException("No events found in the system.");
        }
        return events;
    }

    @DeleteMapping("/deleteEventById/{eid}")
    public String deleteEvent(@PathVariable("eid") int eid) {
        service.deleteEvent(eid);
        return "Event Deleted";
    }

    @GetMapping("/filterByCategory/{category}")
    public List<Event> filterByCategory(@PathVariable("category") String category) {
        List<Event> events = service.filterByCategory(category);
        if (events.isEmpty()) {
            throw new EventNotFoundException("No events found for category: " + category);
        }
        return events;
    }

    @GetMapping("/filterByLocation/{location}")
    public List<Event> filterByLocation(@PathVariable("location") String location) {
        List<Event> events = service.filterByLocation(location);
        if (events.isEmpty()) {
            throw new EventNotFoundException("No events found for location: " + location);
        }
        return events;
    }

    @PostMapping("/decreaseTicketCount/{id}")
    public String decreaseTicketCount(@PathVariable("id") int eventId) {
        service.decreaseTicketCount(eventId);
        return "Ticket count decreased successfully";
    }

    @PostMapping("/increaseTicketCount/{id}")
    public String increaseTicketCount(@PathVariable("id") int eventId) {
        service.increaseTicketCount(eventId);
        return "Ticket count increased successfully";
    }
}
