package com.formation.notificationservice.controller;

import com.formation.notificationservice.dto.NotificationRequest;
import com.formation.notificationservice.dto.NotificationResponse;
import com.formation.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Simulation d'envoi email/SMS")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @Operation(summary = "[Interne, appele par les autres services] Envoyer une notification")
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.send(request);
        return ResponseEntity.created(URI.create("/api/notifications/" + response.getId())).body(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Historique des notifications d'un utilisateur")
    public List<NotificationResponse> getByUser(@PathVariable Long userId) {
        return notificationService.getByUser(userId);
    }

    @GetMapping("/pending")
    @Operation(summary = "Notifications non envoyees avec succes (candidates au retry)",
            description = "Utilise par le scheduler de relance")
    public List<NotificationResponse> getPending() {
        return notificationService.getPending();
    }

    @PatchMapping("/{id}/retry")
    @Operation(summary = "Reessayer d'envoyer une notification en echec")
    public NotificationResponse retry(@PathVariable Long id) {
        return notificationService.retry(id);
    }
}
