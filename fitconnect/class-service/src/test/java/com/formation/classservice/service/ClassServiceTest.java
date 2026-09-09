package com.formation.classservice.service;

import com.formation.classservice.dto.ClassRequest;
import com.formation.classservice.dto.ClassResponse;
import com.formation.classservice.exception.ClassNotFoundException;
import com.formation.classservice.exception.NoSpotsAvailableException;
import com.formation.classservice.model.ClassCategory;
import com.formation.classservice.model.ClassLevel;
import com.formation.classservice.model.ClassStatus;
import com.formation.classservice.model.FitnessClass;
import com.formation.classservice.repository.ClassRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de ClassService : ClassRepository est mocke, aucune base reelle sollicitee.
 * Le verrouillage optimiste reel (ObjectOptimisticLockingFailureException leve par Hibernate)
 * est teste a part, avec un vrai contexte JPA : voir ClassOptimisticLockingIT.
 */
@ExtendWith(MockitoExtension.class)
class ClassServiceTest {

    @Mock
    private ClassRepository classRepository;

    @InjectMocks
    private ClassService classService;

    @Test
    void incrementParticipants_whenSpotsAvailable_reserveLesPlaces() {
        FitnessClass fitnessClass = existingClass(9, 10);
        when(classRepository.findById(1L)).thenReturn(Optional.of(fitnessClass));
        when(classRepository.save(any(FitnessClass.class))).thenAnswer(inv -> inv.getArgument(0));

        ClassResponse result = classService.incrementParticipants(1L, 1);

        assertThat(result.getCurrentParticipants()).isEqualTo(10);
    }

    @Test
    void incrementParticipants_whenCapaciteDepassee_leveNoSpotsAvailableExceptionSansSauvegarder() {
        FitnessClass fitnessClass = existingClass(10, 10);
        when(classRepository.findById(1L)).thenReturn(Optional.of(fitnessClass));

        assertThatThrownBy(() -> classService.incrementParticipants(1L, 1))
                .isInstanceOf(NoSpotsAvailableException.class);

        verify(classRepository, never()).save(any());
    }

    @Test
    void findById_coursInexistant_leveClassNotFoundException() {
        when(classRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> classService.findById(404L))
                .isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    void create_sauvegardeEtRetourneLeCoursCree() {
        when(classRepository.save(any(FitnessClass.class))).thenAnswer(inv -> {
            FitnessClass fc = inv.getArgument(0);
            fc.setId(1L);
            return fc;
        });

        ClassRequest request = new ClassRequest("Yoga matinal", "Reveil en douceur", "Marie", "Paris 11e",
                ClassCategory.YOGA, ClassLevel.BEGINNER, 45, 15, new BigDecimal("18.00"),
                LocalDateTime.now().plusDays(2));

        ClassResponse result = classService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(ClassStatus.SCHEDULED);
        assertThat(result.getCurrentParticipants()).isZero();
    }

    @Test
    void cancel_passeLeCoursAuStatutCancelled() {
        FitnessClass fitnessClass = existingClass(3, 10);
        when(classRepository.findById(1L)).thenReturn(Optional.of(fitnessClass));
        when(classRepository.save(any(FitnessClass.class))).thenAnswer(inv -> inv.getArgument(0));

        classService.cancel(1L);

        assertThat(fitnessClass.getStatus()).isEqualTo(ClassStatus.CANCELLED);
    }

    private FitnessClass existingClass(int currentParticipants, int maxParticipants) {
        FitnessClass fc = new FitnessClass("Crossfit intense", "Seance intense", "Karim", "Lyon",
                ClassCategory.CROSSFIT, ClassLevel.ADVANCED, 60, maxParticipants,
                new BigDecimal("22.00"), LocalDateTime.now().plusDays(1));
        fc.setId(1L);
        fc.setCurrentParticipants(currentParticipants);
        return fc;
    }
}
