package com.formation.bookingservice.client;

import com.formation.bookingservice.client.dto.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications")
    void send(@RequestBody SendNotificationRequest request);
}
