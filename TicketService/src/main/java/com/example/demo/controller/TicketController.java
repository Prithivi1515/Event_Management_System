package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/ticket")
public class TicketController {

    @Autowired
    TicketService service;

    @PostMapping("/book")
    public ResponseEntity<Ticket> bookTicket(@RequestBody @Valid Ticket ticket) {
        Ticket bookedTicket = service.bookTicket(ticket);
        return ResponseEntity.ok(bookedTicket);
    }

    @GetMapping("/getTicketById/{tid}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable("tid") int ticketId) {
        try {
            Ticket ticket = service.getTicketById(ticketId);
            return ResponseEntity.ok(ticket);
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @GetMapping("/getAllTickets")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = service.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/getTicketByUserId/{uid}")
    public ResponseEntity<List<Ticket>> getTicketsByUserId(@PathVariable("uid") int userId) {
        List<Ticket> tickets = service.getTicketsByUserId(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/getTicketByEventId/{eid}")
    public ResponseEntity<List<Ticket>> getTicketsByEventId(@PathVariable("eid") int eventId) {
        List<Ticket> tickets = service.getTicketsByEventId(eventId);
        return ResponseEntity.ok(tickets);
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<String> cancelTicket(@PathVariable("id") int ticketId) {
        try {
            service.cancelTicket(ticketId);
            return ResponseEntity.ok("Ticket cancelled successfully");
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(404).body("Ticket not found: " + e.getMessage());
        }
    }
}
