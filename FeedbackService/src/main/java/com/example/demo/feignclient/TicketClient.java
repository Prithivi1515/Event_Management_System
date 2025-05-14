package com.example.demo.feignclient;


import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.dto.Ticket;

@FeignClient(name="TICKETSERVICE", path ="/ticket") 
public interface TicketClient {

    @GetMapping("/getTicketByUserId/{uid}")
    public List<Ticket> getTicketsByUserId(@PathVariable("uid") int userId);
}
