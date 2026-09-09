package com.formation.classservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fitness_classes")
public class FitnessClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String instructor;

    @Column(nullable = false)
    private String gymLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassLevel level;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Integer maxParticipants;

    @Column(nullable = false)
    private Integer currentParticipants = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassStatus status = ClassStatus.SCHEDULED;

    /*
     * Verrouillage optimiste. Hibernate ajoute une colonne "version" et, a chaque UPDATE,
     * genere : UPDATE fitness_classes SET ..., version = version + 1 WHERE id = ? AND version = ?
     * Si 0 ligne n'est affectee (quelqu'un d'autre a deja modifie et incremente la version),
     * Hibernate leve ObjectOptimisticLockingFailureException au moment du flush/commit.
     * Aucun verrou n'est pose en base : la detection se fait a posteriori, sans bloquer les lecteurs.
     */
    @Version
    private Long version;

    public FitnessClass() {
    }

    public FitnessClass(String name, String description, String instructor, String gymLocation,
                         ClassCategory category, ClassLevel level, Integer durationMinutes,
                         Integer maxParticipants, BigDecimal price, LocalDateTime dateTime) {
        this.name = name;
        this.description = description;
        this.instructor = instructor;
        this.gymLocation = gymLocation;
        this.category = category;
        this.level = level;
        this.durationMinutes = durationMinutes;
        this.maxParticipants = maxParticipants;
        this.price = price;
        this.dateTime = dateTime;
    }

    /**
     * Reserve {spots} places. La verification de capacite est faite ici, en memoire ;
     * c'est la sauvegarde (flush) dans une transaction @Transactional du service qui
     * declenche ensuite la verification de version par Hibernate.
     */
    public void reserveSpots(int spots) {
        if (this.currentParticipants + spots > this.maxParticipants) {
            throw new IllegalStateException("Plus de places disponibles");
        }
        this.currentParticipants += spots;
    }

    public void releaseSpots(int spots) {
        this.currentParticipants = Math.max(this.currentParticipants - spots, 0);
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
