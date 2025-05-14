package com.example.demo.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.dto.Event;

@FeignClient(name="EVENTSERVICE",path="/event")
public interface EventClient {

    @GetMapping("/getEventById/{id}")
    public Event getEventById(@PathVariable("id") int eventId);    
    
}
