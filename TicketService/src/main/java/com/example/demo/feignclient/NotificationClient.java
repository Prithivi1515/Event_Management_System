package com.example.demo.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.dto.NotificationRequest;

@FeignClient(name = "NOTIFICATIONSERVICE", path = "/notification")
public interface NotificationClient {

    @PostMapping("/sendNotification")
    public void sendNotification(@RequestBody NotificationRequest notificationRequest);
}