package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Event;
import com.example.demo.service.EventService;
import com.example.demo.exception.EventNotFoundException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/event")
@AllArgsConstructor
public class EventController {

    private EventService service;

    @PostMapping("/create")
    public ResponseEntity<String> createEvent(@RequestBody @Valid Event event) {
        if (event.getEventId() != 0) {
            return ResponseEntity.badRequest().body("Event ID should not be provided for creation");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createEvent(event));
    }

    @GetMapping("/getEventById/{eventId}")
    public ResponseEntity<Event> getEventById(@PathVariable("eventId") int eventId) {
        if (eventId <= 0) {
            throw new IllegalArgumentException("Event ID must be greater than 0");
        }
        return ResponseEntity.ok(service.getEventById(eventId));
    }

    @GetMapping("/getAllEvents")
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(service.getAllEvents());
    }

    @PutMapping("/update/{eventId}")
    public ResponseEntity<String> updateEvent(@PathVariable("eventId") int eventId, @RequestBody @Valid Event event) {
        if (eventId <= 0) {
            throw new IllegalArgumentException("Event ID must be greater than 0");
        }
        return ResponseEntity.ok(service.updateEvent(eventId, event));
    }

    @DeleteMapping("/deleteEventById/{eventId}")
    public ResponseEntity<String> deleteEvent(@PathVariable("eventId") int eventId) {
        if (eventId <= 0) {
            throw new IllegalArgumentException("Event ID must be greater than 0");
        }
        return ResponseEntity.ok(service.deleteEvent(eventId));
    }

    @GetMapping("/filterByCategory/{category}")
    public ResponseEntity<List<Event>> filterByCategory(@PathVariable("category") String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        return ResponseEntity.ok(service.filterByCategory(category));
    }

    @GetMapping("/filterByLocation/{location}")
    public ResponseEntity<List<Event>> filterByLocation(@PathVariable("location") String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be empty");
        }
        return ResponseEntity.ok(service.filterByLocation(location));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Event>> searchEventsByName(@RequestParam("keyword") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be empty");
        }
        return ResponseEntity.ok(service.searchEventsByName(keyword));
    }
    
    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<List<Event>> getEventsByOrganizer(@PathVariable("organizerId") int organizerId) {
        if (organizerId <= 0) {
            throw new IllegalArgumentException("Organizer ID must be greater than 0");
        }
        return ResponseEntity.ok(service.getEventsByOrganizer(organizerId));
    }

    @PutMapping("/decreaseTicketCount/{eventId}")
    public ResponseEntity<String> decreaseTicketCount(
            @PathVariable("eventId") @Min(value = 1, message = "Event ID must be greater than 0") int eventId,
            @RequestParam(value = "quantity", defaultValue = "1") @Min(value = 1, message = "Quantity must be greater than 0") int quantity) {
        
        try {
            service.decreaseTicketCount(eventId, quantity);
            return ResponseEntity.ok("Ticket count decreased successfully");
        } catch (EventNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/increaseTicketCount/{eventId}")
    public ResponseEntity<String> increaseTicketCount(
            @PathVariable("eventId") @Min(value = 1, message = "Event ID must be greater than 0") int eventId,
            @RequestParam(value = "quantity", defaultValue = "1") @Min(value = 1, message = "Quantity must be greater than 0") int quantity) {
        
        try {
            service.increaseTicketCount(eventId, quantity);
            return ResponseEntity.ok("Ticket count increased successfully");
        } catch (EventNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
