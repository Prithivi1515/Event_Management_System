package com.example.demo.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.example.demo.dto.Event;

@FeignClient(name = "EVENTSERVICE", path = "/event")
public interface EventClient {

    @GetMapping("/getEventById/{id}")
    public Event getEventById(@PathVariable("id") int eventId);

    @PutMapping("/decreaseTicketCount/{eventId}")
    public void decreaseTicketCount(@PathVariable("eventId") int eventId);

    @PutMapping("/increaseTicketCount/{eventId}")
    public void increaseTicketCount(@PathVariable("eventId") int eventId);
}
