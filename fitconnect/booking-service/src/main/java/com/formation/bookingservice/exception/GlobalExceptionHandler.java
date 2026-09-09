package com.formation.bookingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiError> handleBookingNotFound(BookingNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ClassNotFoundForBookingException.class)
    public ResponseEntity<ApiError> handleClassNotFound(ClassNotFoundForBookingException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NoSpotsAvailableException.class)
    public ResponseEntity<ApiError> handleNoSpotsAvailable(NoSpotsAvailableException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(PaymentExpiredException.class)
    public ResponseEntity<ApiError> handlePaymentExpired(PaymentExpiredException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ApiError> handlePaymentFailed(PaymentFailedException ex) {
        return build(HttpStatus.PAYMENT_REQUIRED, ex.getMessage());
    }

    @ExceptionHandler(CancellationNotAllowedException.class)
    public ResponseEntity<ApiError> handleCancellationNotAllowed(CancellationNotAllowedException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidBookingStateException.class)
    public ResponseEntity<ApiError> handleInvalidBookingState(InvalidBookingStateException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Le circuit est ouvert, ou class/payment/notification-service ne repond pas :
    // 503 signale au client que le probleme est cote infra, pas cote donnees envoyees.
    @ExceptionHandler(DownstreamServiceUnavailableException.class)
    public ResponseEntity<ApiError> handleDownstreamUnavailable(DownstreamServiceUnavailableException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        ApiError body = new ApiError(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Erreur de validation", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(status.value(), status.getReasonPhrase(), message));
    }
}
