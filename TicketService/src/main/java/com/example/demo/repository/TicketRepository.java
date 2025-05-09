package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Ticket;
import com.example.demo.model.Ticket.Status;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    // Find tickets by userId
    List<Ticket> findByUserId(int userId);

    // Find tickets by eventId
    List<Ticket> findByEventId(int eventId);

    // Check if a ticket exists for a specific user and event
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Ticket t WHERE t.userId = :userId AND t.eventId = :eventId")
    boolean existsByUserIdAndEventId(@Param("userId") int userId, @Param("eventId") int eventId);

    // Find tickets by userId and eventId
    List<Ticket> findByUserIdAndEventId(int userId, int eventId);
    
    // Added useful methods for finding tickets by status
    List<Ticket> findByStatus(Status status);
    
    // Find tickets by userId and status
    List<Ticket> findByUserIdAndStatus(int userId, Status status);
    
    // Find tickets by eventId and status
    List<Ticket> findByEventIdAndStatus(int eventId, Status status);
}
