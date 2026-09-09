package com.formation.classservice.service;

import com.formation.classservice.dto.ClassRequest;
import com.formation.classservice.dto.ClassResponse;
import com.formation.classservice.dto.ClassSearchCriteria;
import com.formation.classservice.exception.ClassNotFoundException;
import com.formation.classservice.exception.NoSpotsAvailableException;
import com.formation.classservice.model.ClassStatus;
import com.formation.classservice.model.FitnessClass;
import com.formation.classservice.repository.ClassRepository;
import com.formation.classservice.repository.ClassSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassService {

    private final ClassRepository classRepository;

    public ClassService(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    @Transactional(readOnly = true)
    public Page<ClassResponse> search(ClassSearchCriteria criteria, Pageable pageable) {
        return classRepository.findAll(ClassSpecifications.fromCriteria(criteria), pageable)
                .map(ClassMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ClassResponse findById(Long id) {
        return ClassMapper.toResponse(getClassOrThrow(id));
    }

    @Transactional
    public ClassResponse create(ClassRequest request) {
        FitnessClass saved = classRepository.save(ClassMapper.toEntity(request));
        return ClassMapper.toResponse(saved);
    }

    @Transactional
    public ClassResponse update(Long id, ClassRequest request) {
        FitnessClass fitnessClass = getClassOrThrow(id);
        fitnessClass.setName(request.getName());
        fitnessClass.setDescription(request.getDescription());
        fitnessClass.setInstructor(request.getInstructor());
        fitnessClass.setGymLocation(request.getGymLocation());
        fitnessClass.setCategory(request.getCategory());
        fitnessClass.setLevel(request.getLevel());
        fitnessClass.setDurationMinutes(request.getDurationMinutes());
        fitnessClass.setMaxParticipants(request.getMaxParticipants());
        fitnessClass.setPrice(request.getPrice());
        fitnessClass.setDateTime(request.getDateTime());
        return ClassMapper.toResponse(classRepository.save(fitnessClass));
    }

    /**
     * "Supprimer/annuler un cours" : on ne fait jamais de DELETE physique une fois que des
     * reservations existent potentiellement ailleurs (booking-service garde une reference
     * classId) -> on passe le cours a CANCELLED, ce qui est reversible et tracable.
     */
    @Transactional
    public void cancel(Long id) {
        FitnessClass fitnessClass = getClassOrThrow(id);
        fitnessClass.setStatus(ClassStatus.CANCELLED);
        classRepository.save(fitnessClass);
    }

    /**
     * Appelee par booking-service (etape 2 du workflow de reservation).
     * @Transactional est ce qui delimite le moment ou Hibernate flush l'UPDATE et verifie
     * la version : c'est donc CETTE frontiere, et non une annotation sur l'entite, qui
     * declenche l'ObjectOptimisticLockingFailureException en cas de conflit.
     */
    @Transactional
    public ClassResponse incrementParticipants(Long id, int spots) {
        FitnessClass fitnessClass = getClassOrThrow(id);
        try {
            fitnessClass.reserveSpots(spots);
        } catch (IllegalStateException ex) {
            throw new NoSpotsAvailableException("Plus de places disponibles pour le cours " + id);
        }
        return ClassMapper.toResponse(classRepository.save(fitnessClass));
    }

    /**
     * Appelee par booking-service pour liberer des places (compensation d'annulation,
     * ou expiration d'une reservation en attente de paiement).
     */
    @Transactional
    public ClassResponse decrementParticipants(Long id, int spots) {
        FitnessClass fitnessClass = getClassOrThrow(id);
        fitnessClass.releaseSpots(spots);
        return ClassMapper.toResponse(classRepository.save(fitnessClass));
    }

    private FitnessClass getClassOrThrow(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new ClassNotFoundException(id));
    }
}
