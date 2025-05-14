package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.EventNotFoundException;
import com.example.demo.exception.TicketNotFoundException;
import com.example.demo.model.Ticket;
import com.example.demo.service.TicketService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/ticket")
@AllArgsConstructor
public class TicketController {

    TicketService service;

    @PostMapping("/book")
    public Ticket bookTicket(@RequestBody @Valid Ticket ticket) {
        return service.bookTicket(ticket);
    }

    @GetMapping("/getTicketById/{tid}")
    public Ticket getTicketById(@PathVariable("tid") @Min(value = 1, message = "Ticket ID must be greater than 0") int ticketId) {
        return service.getTicketById(ticketId);
    }

    @GetMapping("/getAllTickets")
    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = service.getAllTickets();
        if (tickets.isEmpty()) {
            throw new TicketNotFoundException("No tickets found in the system.");
        }
        return tickets;
    }

    @GetMapping("/getTicketByUserId/{uid}")
    public List<Ticket> getTicketsByUserId(@PathVariable("uid") @Min(value = 1, message = "User ID must be greater than 0") int userId) {
        List<Ticket> tickets = service.getTicketsByUserId(userId);
        if (tickets.isEmpty()) {
            throw new TicketNotFoundException("No tickets found for user ID: " + userId);
        }
        return tickets;
    }

    @GetMapping("/getTicketByEventId/{eid}")
    public List<Ticket> getTicketsByEventId(@PathVariable("eid") @Min(value = 1, message = "Event ID must be greater than 0") int eventId) {
        List<Ticket> tickets = service.getTicketsByEventId(eventId);
        if (tickets.isEmpty()) {
            throw new EventNotFoundException("No Event found for event ID: " + eventId);
        }
        return tickets;
    }

    @DeleteMapping("/cancel/{id}")
    public String cancelTicket(@PathVariable("id") @Min(value = 1, message = "Ticket ID must be greater than 0") int ticketId) {
        service.cancelTicket(ticketId);
        return "Ticket cancelled successfully";
    }
}
