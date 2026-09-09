package com.formation.classservice.dto;

import com.formation.classservice.model.ClassCategory;
import com.formation.classservice.model.ClassLevel;
import com.formation.classservice.model.ClassStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ClassResponse {

    private Long id;
    private String name;
    private String description;
    private String instructor;
    private String gymLocation;
    private ClassCategory category;
    private ClassLevel level;
    private Integer durationMinutes;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private BigDecimal price;
    private LocalDateTime dateTime;
    private ClassStatus status;
    private Long version;

    public ClassResponse() {
    }

    public ClassResponse(Long id, String name, String description, String instructor, String gymLocation,
                          ClassCategory category, ClassLevel level, Integer durationMinutes, Integer maxParticipants,
                          Integer currentParticipants, BigDecimal price, LocalDateTime dateTime, ClassStatus status,
                          Long version) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.instructor = instructor;
        this.gymLocation = gymLocation;
        this.category = category;
        this.level = level;
        this.durationMinutes = durationMinutes;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = currentParticipants;
        this.price = price;
        this.dateTime = dateTime;
        this.status = status;
        this.version = version;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public ClassCategory getCategory() {
        return category;
    }

    public void setCategory(ClassCategory category) {
        this.category = category;
    }

    public ClassLevel getLevel() {
        return level;
    }

    public void setLevel(ClassLevel level) {
        this.level = level;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public ClassStatus getStatus() {
        return status;
    }

    public void setStatus(ClassStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
