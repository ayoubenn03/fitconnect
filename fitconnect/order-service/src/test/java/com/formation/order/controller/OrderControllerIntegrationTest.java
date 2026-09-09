package com.formation.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.order.client.ProductClient;
import com.formation.order.dto.OrderItemRequest;
import com.formation.order.dto.OrderRequest;
import com.formation.order.dto.OrderStatusUpdateRequest;
import com.formation.order.dto.ProductDto;
import com.formation.order.model.OrderStatus;
import com.formation.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'integration bout en bout : controller -> service -> repository -> H2 en memoire.
 * ProductClient (Feign) est mocke afin de ne pas dependre d'un product-service reellement lance.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private ProductClient productClient;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll();
    }

    @Test
    void cycleDeVieComplet_creerListerRecupererChangerStatutSupprimer() throws Exception {
        ProductDto product = new ProductDto();
        product.setId(1L);
        product.setName("Clavier mecanique");
        product.setPrice(new BigDecimal("79.90"));
        product.setQuantity(50);
        when(productClient.getProductById(eq(1L))).thenReturn(product);

        OrderRequest createRequest = new OrderRequest("Alice", List.of(new OrderItemRequest(1L, 2)));

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("Alice"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(159.80))
                .andExpect(jsonPath("$.items[0].productName").value("Clavier mecanique"))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Alice"));

        mockMvc.perform(patch("/api/orders/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderStatusUpdateRequest(OrderStatus.CONFIRMED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(delete("/api/orders/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_produitInexistantChezProductService_retourne400() throws Exception {
        when(productClient.getProductById(eq(999L))).thenThrow(notFoundException());

        OrderRequest createRequest = new OrderRequest("Bob", List.of(new OrderItemRequest(999L, 1)));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void create_avecDonneesInvalides_retourne400AvecErreursDeChamps() throws Exception {
        OrderRequest invalidRequest = new OrderRequest("", List.of());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.customerName").exists())
                .andExpect(jsonPath("$.fieldErrors.items").exists());
    }

    private feign.FeignException.NotFound notFoundException() {
        feign.Request request = feign.Request.create(feign.Request.HttpMethod.GET, "/api/products/999",
                java.util.Map.of(), null, java.nio.charset.StandardCharsets.UTF_8, new feign.RequestTemplate());
        return new feign.FeignException.NotFound("not found", request, null, null);
    }
}
