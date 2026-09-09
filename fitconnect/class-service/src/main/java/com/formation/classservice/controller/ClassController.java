package com.formation.classservice.controller;

import com.formation.classservice.dto.ClassRequest;
import com.formation.classservice.dto.ClassResponse;
import com.formation.classservice.dto.ClassSearchCriteria;
import com.formation.classservice.service.ClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/classes")
@Tag(name = "Cours", description = "Gestion des cours de sport")
public class ClassController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    @Operation(summary = "Lister les cours (filtres optionnels + pagination)",
            description = "Filtres : category, level, location, instructor, dateFrom, dateTo. "
                    + "Pagination Spring Data standard : page, size, sort (ex: sort=dateTime,asc)")
    public Page<ClassResponse> getAll(@ModelAttribute ClassSearchCriteria criteria, Pageable pageable) {
        return classService.search(criteria, pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des cours par date, categorie, niveau, localisation")
    public Page<ClassResponse> search(@ModelAttribute ClassSearchCriteria criteria, Pageable pageable) {
        return classService.search(criteria, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer un cours par son id")
    public ClassResponse getById(@PathVariable Long id) {
        return classService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Creer un nouveau cours")
    public ResponseEntity<ClassResponse> create(@Valid @RequestBody ClassRequest request) {
        ClassResponse created = classService.create(request);
        return ResponseEntity.created(URI.create("/api/classes/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre a jour un cours existant")
    public ClassResponse update(@PathVariable Long id, @Valid @RequestBody ClassRequest request) {
        return classService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler un cours (passage au statut CANCELLED)")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        classService.cancel(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/increment")
    @Operation(summary = "[Interne, appele par booking-service] Reserve des places sur le cours",
            description = "Verrouillage optimiste : renvoie 409 si le cours a ete modifie entre-temps ou si la capacite est depassee")
    public ClassResponse increment(@PathVariable Long id, @RequestParam(defaultValue = "1") Integer spots) {
        return classService.incrementParticipants(id, spots);
    }

    @PatchMapping("/{id}/decrement")
    @Operation(summary = "[Interne, appele par booking-service] Libere des places sur le cours")
    public ClassResponse decrement(@PathVariable Long id, @RequestParam(defaultValue = "1") Integer spots) {
        return classService.decrementParticipants(id, spots);
    }
}
