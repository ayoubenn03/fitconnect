package com.formation.order.client;

import com.formation.order.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client Feign vers product-service. Le nom logique "product-service" est resolu
 * via Eureka (load-balance automatiquement si plusieurs instances sont enregistrees).
 */
@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductDto getProductById(@PathVariable("id") Long id);
}
