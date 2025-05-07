package com.example.demo.service;

import java.util.List;

import com.example.demo.model.Ticket;

public interface TicketService {

	public abstract Ticket bookTicket(Ticket ticket);

	public abstract Ticket getTicketById(int ticketId);

	public abstract List<Ticket> getAllTickets();

	public abstract List<Ticket> getTicketsByUserId(int userId);

	public abstract List<Ticket> getTicketsByEventId(int eventId);

	public abstract String cancelTicket(int ticketId);

}
