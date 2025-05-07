package com.example.demo.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="NOTIFICATIONSERVICE",path="/notification")
public interface NotificationClient {


    @PostMapping("/sendNotification")
    public void sendNotification(@RequestParam("userId") int userId,@RequestParam("eventId") int eventId,@RequestParam("message") String message);

}