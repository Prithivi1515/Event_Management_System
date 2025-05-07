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

import com.example.demo.model.Event;
import com.example.demo.service.EventService;

@RestController
@RequestMapping("/event")
public class EventController {
	
	@Autowired
	EventService service;
	
	@PostMapping("/create")
	public String createEvent(@RequestBody Event event)
	{
		service.createEvent(event);
		return "Event Created";
	}

	@GetMapping("/getEventById/{eid}")
	public Event getEventById(@PathVariable("eid") int eid)
	{
		return service.getEventById(eid);
	}

	@GetMapping("/getAllEvents")
	public List<Event> getAllEvents()
	{
		return service.getAllEvents();
	}

	@DeleteMapping("/deleteEventById/{eid}")
	public String deleteEvent(@PathVariable("eid") int eid)
	{
		service.deleteEvent(eid);
		return "Event Deleted";
	}

	@PostMapping("/filterByCategory/{category}")
	public List<Event> filterByCategory(@PathVariable("category") String category)
	{
		return service.filterByCategory(category);
	}

	@PostMapping("/filterByLocation/{location}")
	public List<Event> filterByLocation(@PathVariable("location") String location)
	{
		return service.filterByLocation(location);
	}
	
	@PostMapping("/decreaseTicketCount/{id}")
	public void decreaseTicketCount(@PathVariable("id") int eventId)
	{
		service.decreaseTicketCount(eventId);
	}
	
	
	@PostMapping("/increaseTicketCount/{id}")
	public void increaseTicketCount(@PathVariable("id") int eventId)
	{
		service.increaseTicketCount(eventId);
	}
	

}
