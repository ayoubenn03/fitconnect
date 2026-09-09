package com.formation.bookingservice.client;

import com.formation.bookingservice.client.dto.ClassDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Client Feign vers class-service. Le nom logique "class-service" est resolu via Eureka
 * (cote booking-service.yml, feign.circuitbreaker.enabled=true enveloppe automatiquement
 * chaque appel de ce client dans un CircuitBreaker Resilience4j nomme "class-service").
 */
@FeignClient(name = "class-service")
public interface ClassClient {

    @GetMapping("/api/classes/{id}")
    ClassDto getClassById(@PathVariable("id") Long id);

    @PatchMapping("/api/classes/{id}/increment")
    ClassDto incrementParticipants(@PathVariable("id") Long id, @RequestParam("spots") Integer spots);

    @PatchMapping("/api/classes/{id}/decrement")
    ClassDto decrementParticipants(@PathVariable("id") Long id, @RequestParam("spots") Integer spots);
}
