package com.formation.bookingservice.exception;

public class NoSpotsAvailableException extends RuntimeException {

    public NoSpotsAvailableException(String message) {
        super(message);
    }
}
