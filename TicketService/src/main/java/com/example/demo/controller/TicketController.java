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

import com.example.demo.exception.TicketNotFoundException;
import com.example.demo.model.Ticket;
import com.example.demo.service.TicketService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    @Autowired
    TicketService service;

    @PostMapping("/book")
    public ResponseEntity<Ticket> bookTicket(@RequestBody @Valid Ticket ticket) {
        Ticket bookedTicket = service.bookTicket(ticket);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookedTicket);
    }

    @GetMapping("/getTicketById/{tid}")
    public ResponseEntity<?> getTicketById(@PathVariable("tid") @Min(value = 1, message = "Ticket ID must be greater than 0") int ticketId) {
        try {
            Ticket ticket = service.getTicketById(ticketId);
            return ResponseEntity.ok(ticket);
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ticket not found: " + e.getMessage());
        }
    }

    @GetMapping("/getAllTickets")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = service.getAllTickets();
        if (tickets.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/getTicketByUserId/{uid}")
    public ResponseEntity<?> getTicketsByUserId(@PathVariable("uid") @Min(value = 1, message = "User ID must be greater than 0") int userId) {
        List<Ticket> tickets = service.getTicketsByUserId(userId);
        if (tickets.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No tickets found for user ID: " + userId);
        }
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/getTicketByEventId/{eid}")
    public ResponseEntity<?> getTicketsByEventId(@PathVariable("eid") @Min(value = 1, message = "Event ID must be greater than 0") int eventId) {
        List<Ticket> tickets = service.getTicketsByEventId(eventId);
        if (tickets.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No tickets found for event ID: " + eventId);
        }
        return ResponseEntity.ok(tickets);
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<?> cancelTicket(@PathVariable("id") @Min(value = 1, message = "Ticket ID must be greater than 0") int ticketId) {
        try {
            service.cancelTicket(ticketId);
            return ResponseEntity.ok("Ticket cancelled successfully");
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ticket not found: " + e.getMessage());
        }
    }
}
