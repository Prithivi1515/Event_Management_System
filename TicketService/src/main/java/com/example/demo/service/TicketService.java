package com.example.demo.service;

import java.util.List;

import com.example.demo.exception.EventNotFoundException;
import com.example.demo.exception.TicketNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.Ticket;
import com.example.demo.model.Ticket.Status;

public interface TicketService {

    Ticket bookTicket(Ticket ticket) throws UserNotFoundException, EventNotFoundException;

    Ticket getTicketById(int ticketId) throws TicketNotFoundException;

    List<Ticket> getAllTickets();

    List<Ticket> getTicketsByUserId(int userId) throws UserNotFoundException;

    List<Ticket> getTicketsByEventId(int eventId) throws EventNotFoundException;

    Ticket cancelTicket(int ticketId) throws TicketNotFoundException;
    
    List<Ticket> getTicketsByStatus(Status status);
    
    List<Ticket> getTicketsByUserIdAndStatus(int userId, Status status) throws UserNotFoundException;
    
    List<Ticket> getTicketsByEventIdAndStatus(int eventId, Status status) throws EventNotFoundException;
    
    boolean hasUserBookedEvent(int userId, int eventId);
}
