package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Event;

public interface EventRepository extends JpaRepository<Event, Integer>{
	
	List<Event> findByCategory(String category);
	List<Event> findByLocation(String location);
	List<Event> findByDate(LocalDate date);


}
