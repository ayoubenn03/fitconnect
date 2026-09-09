package com.formation.order.dto;

import com.formation.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderResponse {

    private Long id;
    private String customerName;
    private Instant orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;

    public OrderResponse() {
    }

    public OrderResponse(Long id, String customerName, Instant orderDate, OrderStatus status,
                          BigDecimal totalAmount, List<OrderItemResponse> items) {
        this.id = id;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Instant getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Instant orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }
}
