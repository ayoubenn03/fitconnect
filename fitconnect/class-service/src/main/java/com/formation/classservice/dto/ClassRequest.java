package com.formation.classservice.dto;

import com.formation.classservice.model.ClassCategory;
import com.formation.classservice.model.ClassLevel;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO d'entree pour la creation/mise a jour d'un cours. Ne contient volontairement pas
 * currentParticipants, status ni version : ce sont des champs geres par le serveur
 * (currentParticipants evolue via /increment et /decrement, status via /cancel,
 * version par Hibernate) - les exposer en ecriture permettrait a un client de les falsifier.
 */
public class ClassRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 3, message = "Le nom doit contenir au moins 3 caracteres")
    private String name;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotBlank(message = "Le nom de l'instructeur est obligatoire")
    private String instructor;

    @NotBlank(message = "La localisation de la salle est obligatoire")
    private String gymLocation;

    @NotNull(message = "La categorie est obligatoire")
    private ClassCategory category;

    @NotNull(message = "Le niveau est obligatoire")
    private ClassLevel level;

    @NotNull(message = "La duree est obligatoire")
    private Integer durationMinutes;

    @NotNull(message = "Le nombre maximum de participants est obligatoire")
    @Min(value = 5, message = "Il faut au moins 5 places")
    @Max(value = 30, message = "Il ne peut pas y avoir plus de 30 places")
    private Integer maxParticipants;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "5.00", message = "Le prix doit etre d'au moins 5.00 euros")
    private BigDecimal price;

    @NotNull(message = "La date du cours est obligatoire")
    @Future(message = "La date du cours doit etre dans le futur")
    private LocalDateTime dateTime;

    public ClassRequest() {
    }

    public ClassRequest(String name, String description, String instructor, String gymLocation,
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

    private static final Set<Integer> ALLOWED_DURATIONS = Set.of(30, 45, 60, 90);

    // @AssertTrue permet d'ajouter une regle de validation "sur mesure" (ici : durationMinutes
    // doit valoir 30, 45, 60 ou 90) tout en restant dans le mecanisme Bean Validation standard :
    // Jakarta Validation appelle isDurationAllowed() comme n'importe quelle autre contrainte et
    // ajoute son message a la meme liste d'erreurs que @NotBlank/@Min/etc.
    @AssertTrue(message = "La duree doit valoir 30, 45, 60 ou 90 minutes")
    public boolean isDurationAllowed() {
        return durationMinutes == null || ALLOWED_DURATIONS.contains(durationMinutes);
    }
}
