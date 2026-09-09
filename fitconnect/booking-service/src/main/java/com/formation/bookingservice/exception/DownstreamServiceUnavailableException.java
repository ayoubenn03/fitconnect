package com.formation.bookingservice.exception;

/**
 * Levee directement dans BookingService (catch FeignException / CallNotPermittedException
 * autour des appels a class-service et payment-service) quand un service en aval ne repond
 * pas (timeout, 5xx) ou que son circuit Resilience4j est ouvert.
 */
public class DownstreamServiceUnavailableException extends RuntimeException {

    public DownstreamServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
