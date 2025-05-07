package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
	@Id
	@GeneratedValue
	private int ticketId;
	private int eventId;
	private int userId;
	private LocalDateTime bookingDate;
	private Status status;
	public enum Status {
		BOOKED, CANCELLED
	}


}
