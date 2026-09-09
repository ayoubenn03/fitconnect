package com.formation.order.service;

import com.formation.order.dto.OrderItemResponse;
import com.formation.order.dto.OrderResponse;
import com.formation.order.model.Order;
import com.formation.order.model.OrderItem;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getItems().stream().map(OrderMapper::toItemResponse).toList()
        );
    }

    private static OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProductId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }
}
