package com.example.demo.service;

import java.util.List;

import com.example.demo.exception.EventNotFoundException;
import com.example.demo.model.Event;

public interface EventService {

    String createEvent(Event event);

    Event getEventById(int eventId) throws EventNotFoundException;

    List<Event> getAllEvents() throws EventNotFoundException;

    String updateEvent(int eventId, Event event) throws EventNotFoundException;

    String deleteEvent(int eventId) throws EventNotFoundException;

    List<Event> filterByCategory(String category) throws EventNotFoundException;

    List<Event> filterByLocation(String location) throws EventNotFoundException;
    
    List<Event> searchEventsByName(String keyword) throws EventNotFoundException;
    
    List<Event> getEventsByOrganizer(int organizerId) throws EventNotFoundException;

    void decreaseTicketCount(int eventId) throws EventNotFoundException, IllegalArgumentException;

    void increaseTicketCount(int eventId) throws EventNotFoundException, IllegalArgumentException;
}
