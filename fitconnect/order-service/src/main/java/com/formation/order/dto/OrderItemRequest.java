package com.formation.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderItemRequest {

    @NotNull(message = "L'identifiant du produit est obligatoire")
    private Long productId;

    @NotNull(message = "La quantite est obligatoire")
    @Min(value = 1, message = "La quantite doit etre au moins 1")
    private Integer quantity;

    public OrderItemRequest() {
    }

    public OrderItemRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
