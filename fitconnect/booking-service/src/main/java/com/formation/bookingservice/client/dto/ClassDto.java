package com.formation.bookingservice.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Vue partielle de class-service.ClassResponse : booking-service ne recopie que les champs
 * dont il a besoin pour construire le snapshot d'une reservation (voir Booking.java).
 * Encore une fois : pas de module partage entre services, chacun definit son propre contrat.
 */
public class ClassDto {

    private Long id;
    private String name;
    private String instructor;
    private String gymLocation;
    private LocalDateTime dateTime;
    private BigDecimal price;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String status;

    public ClassDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getGymLocation() {
        return gymLocation;
    }

    public void setGymLocation(String gymLocation) {
        this.gymLocation = gymLocation;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
