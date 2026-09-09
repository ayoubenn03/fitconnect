package com.formation.bookingservice.controller;

import com.formation.bookingservice.dto.BookingRequest;
import com.formation.bookingservice.dto.BookingResponse;
import com.formation.bookingservice.dto.PaymentConfirmationRequest;
import com.formation.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Reservations", description = "Orchestration Saga : class-service -> booking-service -> payment-service -> notification-service")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    @Operation(summary = "Lister toutes les reservations")
    public List<BookingResponse> getAll() {
        return bookingService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une reservation par son id")
    public BookingResponse getById(@PathVariable Long id) {
        return bookingService.findById(id);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Lister les reservations d'un utilisateur")
    public List<BookingResponse> getByUser(@PathVariable Long userId) {
        return bookingService.findByUser(userId);
    }

    @GetMapping("/expired")
    @Operation(summary = "Lister les reservations en attente de paiement dont la deadline est depassee",
            description = "Utilise par le scheduler d'expiration ; expose aussi ici pour inspection manuelle")
    public List<BookingResponse> getExpired() {
        return bookingService.findExpiredPendingPayments();
    }

    @PostMapping
    @Operation(summary = "Creer une reservation (Saga : verification cours -> reservation places -> creation -> notification)")
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request) {
        BookingResponse created = bookingService.createBooking(request);
        return ResponseEntity.created(URI.create("/api/bookings/" + created.getId())).body(created);
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirmer le paiement d'une reservation en attente")
    public BookingResponse confirm(@PathVariable Long id, @Valid @RequestBody PaymentConfirmationRequest request) {
        return bookingService.confirmPayment(id, request);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler une reservation (remboursement si deja payee, liberation des places)")
    public BookingResponse cancel(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Marquer une reservation confirmee comme terminee (cours passe)")
    public BookingResponse complete(@PathVariable Long id) {
        return bookingService.completeBooking(id);
    }
}
