package com.formation.product.controller;

import com.formation.product.dto.ProductRequest;
import com.formation.product.dto.ProductResponse;
import com.formation.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Produits", description = "Gestion du catalogue de produits")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Lister tous les produits")
    public List<ProductResponse> getAll() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer un produit par son id")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Creer un nouveau produit")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.create(request);
        return ResponseEntity.created(URI.create("/api/products/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre a jour un produit existant")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un produit")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
