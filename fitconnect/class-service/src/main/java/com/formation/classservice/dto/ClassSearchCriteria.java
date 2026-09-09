package com.formation.classservice.dto;

import com.formation.classservice.model.ClassCategory;
import com.formation.classservice.model.ClassLevel;

import java.time.LocalDate;

/**
 * Regroupe tous les filtres optionnels acceptes par GET /api/classes et GET /api/classes/search.
 * Un champ a null = filtre non applique.
 */
public class ClassSearchCriteria {

    private ClassCategory category;
    private ClassLevel level;
    private String location;
    private String instructor;
    private LocalDate dateFrom;
    private LocalDate dateTo;

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }
}
