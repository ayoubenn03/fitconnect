package com.formation.order.service;

import com.formation.order.client.ProductClient;
import com.formation.order.dto.OrderItemRequest;
import com.formation.order.dto.OrderRequest;
import com.formation.order.dto.OrderResponse;
import com.formation.order.dto.OrderStatusUpdateRequest;
import com.formation.order.dto.ProductDto;
import com.formation.order.exception.OrderNotFoundException;
import com.formation.order.exception.ProductNotFoundForOrderException;
import com.formation.order.model.Order;
import com.formation.order.model.OrderStatus;
import com.formation.order.repository.OrderRepository;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de OrderService : OrderRepository et ProductClient (Feign) sont mockes,
 * aucun appel HTTP reel ni base de donnees reelle n'est sollicite.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void create_appellePourChaqueArticleProductServiceEtCalculeLeTotal() {
        ProductDto product1 = productDto(1L, "Clavier", new BigDecimal("50.00"));
        ProductDto product2 = productDto(2L, "Souris", new BigDecimal("20.00"));
        when(productClient.getProductById(1L)).thenReturn(product1);
        when(productClient.getProductById(2L)).thenReturn(product2);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(10L);
            return order;
        });

        OrderRequest request = new OrderRequest("Alice", List.of(
                new OrderItemRequest(1L, 2),
                new OrderItemRequest(2L, 1)
        ));

        OrderResponse result = orderService.create(request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(result.getItems()).hasSize(2);
        // 2 * 50.00 + 1 * 20.00 = 120.00
        assertThat(result.getTotalAmount()).isEqualByComparingTo("120.00");
    }

    @Test
    void create_produitInexistant_leveProductNotFoundForOrderException() {
        when(productClient.getProductById(99L)).thenThrow(notFoundException());

        OrderRequest request = new OrderRequest("Bob", List.of(new OrderItemRequest(99L, 1)));

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(ProductNotFoundForOrderException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findById_commandeInexistante_leveOrderNotFoundException() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(404L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void updateStatus_commandeExistante_metAJourLeStatut() {
        Order existing = new Order("Alice", Instant.now(), OrderStatus.CREATED, BigDecimal.TEN);
        existing.setId(5L);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse result = orderService.updateStatus(5L, new OrderStatusUpdateRequest(OrderStatus.CONFIRMED));

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void delete_commandeInexistante_leveOrderNotFoundException() {
        when(orderRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.delete(404L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    private ProductDto productDto(Long id, String name, BigDecimal price) {
        ProductDto dto = new ProductDto();
        dto.setId(id);
        dto.setName(name);
        dto.setPrice(price);
        dto.setQuantity(100);
        return dto;
    }

    private FeignException.NotFound notFoundException() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/products/99",
                java.util.Map.of(), null, StandardCharsets.UTF_8, new RequestTemplate());
        return new FeignException.NotFound("not found", request, null, null);
    }
}
