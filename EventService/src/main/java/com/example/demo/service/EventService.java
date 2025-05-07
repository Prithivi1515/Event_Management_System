package com.example.demo.service;

import java.util.List;

import com.example.demo.model.Event;

public interface EventService {

	public abstract String createEvent(Event event);

	public abstract Event getEventById(int eid);

	public abstract List<Event> getAllEvents();

	public abstract String deleteEvent(int eid);

	public abstract List<Event> filterByCategory(String category);

	public abstract List<Event> filterByLocation(String location);
	
	public abstract void decreaseTicketCount(int eventId);
	
	public abstract void increaseTicketCount(int eventId);

	
	

}
