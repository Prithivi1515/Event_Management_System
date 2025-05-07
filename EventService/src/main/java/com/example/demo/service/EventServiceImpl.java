package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Event;
import com.example.demo.repository.EventRepository;

@Service
public class EventServiceImpl implements EventService {

	@Autowired
	EventRepository repository;

	@Override
	public String createEvent(Event event) {
		event.setDate(LocalDateTime.now());
		repository.save(event);
		return "Event created";
	}

	@Override
	public Event getEventById(int eid) {
		return repository.findById(eid)
				.orElseThrow(() -> new RuntimeException("Event not found with Id: "+eid));
	}

	@Override
	public List<Event> getAllEvents() {
		return repository.findAll();
	}

	@Override
	public String deleteEvent(int eid) {
		repository.deleteById(eid);
		return "Event Deleted";
	}

	@Override
	public List<Event> filterByCategory(String category) {
		return repository.findByCategory(category);
	}

	@Override
	public List<Event> filterByLocation(String location) {
		return repository.findByLocation(location);
	}

	@Override
	public void decreaseTicketCount(int eventId) {
		Event event = repository.findById(eventId)
				.orElseThrow(() -> new RuntimeException("Event not found with Id: "+eventId));
		int currentCount = event.getTicketCount();
		if (currentCount > 0) {
			event.setTicketCount(currentCount - 1);
			repository.save(event);
		} else {
			throw new RuntimeException("No tickets available for event with Id: "+eventId);
		}
	}

	@Override
	public void increaseTicketCount(int eventId) {
		Event event = repository.findById(eventId)
				.orElseThrow(() -> new RuntimeException("Event not found with Id: "+eventId));
		int currentCount = event.getTicketCount();
		event.setTicketCount(currentCount + 1);
		repository.save(event);
		
	}

}
