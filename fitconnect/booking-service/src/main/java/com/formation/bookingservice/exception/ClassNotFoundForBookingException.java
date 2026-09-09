package com.formation.bookingservice.exception;

public class ClassNotFoundForBookingException extends RuntimeException {

    public ClassNotFoundForBookingException(Long classId) {
        super("Cours introuvable avec l'id " + classId);
    }
}
