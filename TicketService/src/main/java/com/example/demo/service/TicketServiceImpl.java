package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.Event;
import com.example.demo.dto.NotificationRequest;
import com.example.demo.feignclient.EventClient;
import com.example.demo.feignclient.NotificationClient;
import com.example.demo.model.Ticket;
import com.example.demo.model.Ticket.Status;
import com.example.demo.repository.TicketRepository;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    TicketRepository repository;

    @Autowired
    EventClient eventClient;

    @Autowired
    NotificationClient notificationClient;

    @Override
    public Ticket bookTicket(Ticket ticket) {
        Event event = eventClient.getEventById(ticket.getEventId());
        if (event == null) {
            throw new RuntimeException("Event not found with Id: " + ticket.getEventId());
        }
        eventClient.decreaseTicketCount(ticket.getEventId());
        ticket.setBookingDate(LocalDateTime.now());
        ticket.setStatus(Status.BOOKED);

        try {
            NotificationRequest notificationRequest = new NotificationRequest(
                ticket.getUserId(),
                ticket.getEventId(),
                "Your ticket has been successfully booked"
            );
            notificationClient.sendNotification(notificationRequest);
        } catch (Exception e) {
            System.out.println("Failed to send Notification: " + e.getMessage());
        }

        return repository.save(ticket);
    }

    @Override
    public Ticket getTicketById(int ticketId) {
        return repository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with Id: " + ticketId));
    }

    @Override
    public List<Ticket> getTicketsByUserId(int userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public List<Ticket> getTicketsByEventId(int eventId) {
        return repository.findByEventId(eventId);
    }

    @Override
    public void cancelTicket(int ticketId) { // Changed return type to void
        Ticket ticket = getTicketById(ticketId);
        ticket.setStatus(Status.CANCELLED);
        eventClient.increaseTicketCount(ticket.getEventId());
        repository.save(ticket);

        try {
            NotificationRequest notificationRequest = new NotificationRequest(
                ticket.getUserId(),
                ticket.getEventId(),
                "Your ticket has been successfully cancelled"
            );
            notificationClient.sendNotification(notificationRequest);
        } catch (Exception e) {
            System.out.println("Failed to send Notification: " + e.getMessage());
        }
    }

    @Override
    public List<Ticket> getAllTickets() {
        return repository.findAll();
    }
}
