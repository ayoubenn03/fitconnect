package com.formation.paymentservice.controller;

import com.formation.paymentservice.dto.PaymentRequest;
import com.formation.paymentservice.dto.PaymentResponse;
import com.formation.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Paiements", description = "Simulation de paiement (accepte si montant < seuil, refuse sinon)")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "[Interne, appele par booking-service] Traiter un paiement")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.created(URI.create("/api/payments/" + response.getId())).body(response);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Recuperer le paiement d'une reservation")
    public PaymentResponse getByBooking(@PathVariable Long bookingId) {
        return paymentService.getByBooking(bookingId);
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "[Interne, appele par booking-service] Rembourser un paiement SUCCESS")
    public PaymentResponse refund(@PathVariable Long id) {
        return paymentService.refund(id);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Historique des paiements d'un utilisateur")
    public List<PaymentResponse> getByUser(@PathVariable Long userId) {
        return paymentService.getByUser(userId);
    }
}
