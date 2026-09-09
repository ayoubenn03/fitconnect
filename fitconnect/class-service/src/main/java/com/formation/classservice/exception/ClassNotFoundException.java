package com.formation.classservice.exception;

public class ClassNotFoundException extends RuntimeException {

    public ClassNotFoundException(Long id) {
        super("Cours introuvable avec l'id " + id);
    }
}
