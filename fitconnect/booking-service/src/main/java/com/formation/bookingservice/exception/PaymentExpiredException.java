package com.formation.bookingservice.exception;

public class PaymentExpiredException extends RuntimeException {

    public PaymentExpiredException(String message) {
        super(message);
    }
}
