package com.formation.bookingservice.exception;

/**
 * La reservation existe mais n'est pas dans le statut attendu pour l'operation demandee
 * (ex : tenter de confirmer le paiement d'une reservation deja CONFIRMED, ou de completer
 * une reservation qui n'est pas CONFIRMED).
 */
public class InvalidBookingStateException extends RuntimeException {

    public InvalidBookingStateException(String message) {
        super(message);
    }
}
