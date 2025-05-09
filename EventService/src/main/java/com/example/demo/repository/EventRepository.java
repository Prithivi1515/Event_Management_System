package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Event;

public interface EventRepository extends JpaRepository<Event, Integer> {
    
    // Case-insensitive search by category
    List<Event> findByCategoryIgnoreCase(String category);
    
    // Case-insensitive search by location
    List<Event> findByLocationIgnoreCase(String location);
    
    // Find events between two dates (inclusive)
    List<Event> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find upcoming events (where date is after the current time)
    @Query("SELECT e FROM Event e WHERE e.date > :currentDate ORDER BY e.date ASC")
    List<Event> findUpcomingEvents(@Param("currentDate") LocalDateTime currentDate);
    
    // Find events by organizer ID
    List<Event> findByOrganizerId(int organizerId);
    
    // Search events by name containing a keyword (case-insensitive)
    List<Event> findByNameContainingIgnoreCase(String keyword);
    
    // Find events with available tickets
    @Query("SELECT e FROM Event e WHERE e.ticketCount > 0")
    List<Event> findEventsWithAvailableTickets();
}
