package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    // Find tickets by userId
    List<Ticket> findByUserId(int userId);

    // Find tickets by eventId
    List<Ticket> findByEventId(int eventId);

    // Check if a ticket exists for a specific user and event
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.userId = :userId AND t.eventId = :eventId")
    boolean existsByUserIdAndEventId(@Param("userId") int userId, @Param("eventId") int eventId);

    // Find tickets by userId and eventId
    @Query("SELECT t FROM Ticket t WHERE t.userId = :userId AND t.eventId = :eventId")
    List<Ticket> findByUserIdAndEventId(@Param("userId") int userId, @Param("eventId") int eventId);
}
