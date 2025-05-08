package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Event;
import com.example.demo.service.EventService;

@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    EventService service;

    @PostMapping("/create")
    public ResponseEntity<String> createEvent(@RequestBody Event event) {
        String result = service.createEvent(event);
        if ("Event created successfully.".equals(result)) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }

    @GetMapping("/getEventById/{eid}")
    public ResponseEntity<Event> getEventById(@PathVariable("eid") int eid) {
        Event event = service.getEventById(eid);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/getAllEvents")
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = service.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @DeleteMapping("/deleteEventById/{eid}")
    public ResponseEntity<String> deleteEvent(@PathVariable("eid") int eid) {
        service.deleteEvent(eid);
        return ResponseEntity.ok("Event Deleted");
    }

    @GetMapping("/filterByCategory/{category}")
    public ResponseEntity<List<Event>> filterByCategory(@PathVariable("category") String category) {
        List<Event> events = service.filterByCategory(category);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/filterByLocation/{location}")
    public ResponseEntity<List<Event>> filterByLocation(@PathVariable("location") String location) {
        List<Event> events = service.filterByLocation(location);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/decreaseTicketCount/{id}")
    public ResponseEntity<String> decreaseTicketCount(@PathVariable("id") int eventId) {
        service.decreaseTicketCount(eventId);
        return ResponseEntity.ok("Ticket count decreased successfully");
    }

    @PostMapping("/increaseTicketCount/{id}")
    public ResponseEntity<String> increaseTicketCount(@PathVariable("id") int eventId) {
        service.increaseTicketCount(eventId);
        return ResponseEntity.ok("Ticket count increased successfully");
    }
}
