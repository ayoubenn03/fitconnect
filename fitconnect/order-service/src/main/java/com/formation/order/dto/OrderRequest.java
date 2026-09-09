package com.formation.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class OrderRequest {

    @NotBlank(message = "Le nom du client est obligatoire")
    private String customerName;

    @NotEmpty(message = "La commande doit contenir au moins un article")
    @Valid
    private List<OrderItemRequest> items;

    public OrderRequest() {
    }

    public OrderRequest(String customerName, List<OrderItemRequest> items) {
        this.customerName = customerName;
        this.items = items;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
