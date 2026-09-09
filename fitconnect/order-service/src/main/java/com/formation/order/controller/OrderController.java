package com.formation.order.controller;

import com.formation.order.dto.OrderRequest;
import com.formation.order.dto.OrderResponse;
import com.formation.order.dto.OrderStatusUpdateRequest;
import com.formation.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Commandes", description = "Gestion des commandes, avec enrichissement des articles via product-service")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "Lister toutes les commandes")
    public List<OrderResponse> getAll() {
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une commande par son id")
    public OrderResponse getById(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Creer une commande (chaque article est verifie et enrichi via product-service)")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        OrderResponse created = orderService.create(request);
        return ResponseEntity.created(URI.create("/api/orders/" + created.getId())).body(created);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Changer le statut d'une commande (CREATED, CONFIRMED, CANCELLED)")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orderService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une commande")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
