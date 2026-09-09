package com.formation.classservice.service;

import com.formation.classservice.dto.ClassRequest;
import com.formation.classservice.dto.ClassResponse;
import com.formation.classservice.model.FitnessClass;

public final class ClassMapper {

    private ClassMapper() {
    }

    public static FitnessClass toEntity(ClassRequest request) {
        return new FitnessClass(
                request.getName(),
                request.getDescription(),
                request.getInstructor(),
                request.getGymLocation(),
                request.getCategory(),
                request.getLevel(),
                request.getDurationMinutes(),
                request.getMaxParticipants(),
                request.getPrice(),
                request.getDateTime()
        );
    }

    public static ClassResponse toResponse(FitnessClass fitnessClass) {
        return new ClassResponse(
                fitnessClass.getId(),
                fitnessClass.getName(),
                fitnessClass.getDescription(),
                fitnessClass.getInstructor(),
                fitnessClass.getGymLocation(),
                fitnessClass.getCategory(),
                fitnessClass.getLevel(),
                fitnessClass.getDurationMinutes(),
                fitnessClass.getMaxParticipants(),
                fitnessClass.getCurrentParticipants(),
                fitnessClass.getPrice(),
                fitnessClass.getDateTime(),
                fitnessClass.getStatus(),
                fitnessClass.getVersion()
        );
    }
}
