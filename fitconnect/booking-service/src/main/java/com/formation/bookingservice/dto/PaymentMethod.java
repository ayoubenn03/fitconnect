package com.formation.bookingservice.dto;

/**
 * Duplique volontairement l'enum PaymentMethod de payment-service. En microservices,
 * on NE PARTAGE PAS de module Java commun contenant les classes de domaine entre services :
 * cela recreerait un couplage fort (un changement dans payment-service forcerait un redeploiement
 * de booking-service). Chaque service definit son propre contrat ; la coherence est assuree par
 * convention/tests, pas par le compilateur.
 */
public enum PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    PAYPAL,
    STRIPE
}
