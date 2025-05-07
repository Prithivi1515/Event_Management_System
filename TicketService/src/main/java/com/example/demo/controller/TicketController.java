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

import com.example.demo.model.Ticket;
import com.example.demo.service.TicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/ticket")
public class TicketController {

	@Autowired
	TicketService service;
	
	@PostMapping("/book")
	public Ticket bookTicket(@RequestBody @Valid Ticket ticket)
	{
		return service.bookTicket(ticket);
	}

	@GetMapping("/getTicketById/{tid}")
	public Ticket getTicketById(@PathVariable("tid") int ticketId)
	{
		return service.getTicketById(ticketId);
	}

	@GetMapping("/getAllTickets")
	public List<Ticket> getAllTickets()
	{
		return service.getAllTickets();
	}

	@GetMapping("/getTicketByUserId/{uid}")
	public List<Ticket> getTicketsByUserId(@PathVariable("uid") int userId)
	{
		return service.getTicketsByUserId(userId);
	}

	@GetMapping("/getTicketByEventId/{eid}")
	public List<Ticket> getTicketsByEventId(@PathVariable("eid") int eventId)
	{
		return service.getTicketsByEventId(eventId);
	}

	@DeleteMapping("/cancel/{id}")
	public String cancelTicket(@PathVariable("id") int ticketId)
	{
		service.cancelTicket(ticketId);
		return "Ticket cancelled successfully";
	}

}
