package com.formation.order.exception;

public class ProductNotFoundForOrderException extends RuntimeException {

    public ProductNotFoundForOrderException(Long productId) {
        super("Le produit " + productId + " demande dans la commande est introuvable dans product-service");
    }
}
