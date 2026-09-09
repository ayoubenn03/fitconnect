package com.formation.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.product.dto.ProductRequest;
import com.formation.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'integration bout en bout : controller -> service -> repository -> H2 en memoire.
 * Le config-server et eureka-server sont desactives (voir src/test/resources/application.yml).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
    }

    @Test
    void cycleDeVieComplet_creerListerRecupererModifierSupprimer() throws Exception {
        ProductRequest createRequest = new ProductRequest("Webcam", "Webcam HD 1080p", new BigDecimal("39.90"), 15);

        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Webcam"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Webcam"));

        ProductRequest updateRequest = new ProductRequest("Webcam Pro", "Webcam HD 1080p avec micro", new BigDecimal("49.90"), 10);
        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Webcam Pro"))
                .andExpect(jsonPath("$.quantity").value(10));

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Produit introuvable avec l'id " + id));
    }

    @Test
    void create_avecDonneesInvalides_retourne400AvecErreursDeChamps() throws Exception {
        ProductRequest invalidRequest = new ProductRequest("", null, new BigDecimal("-5"), -1);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists())
                .andExpect(jsonPath("$.fieldErrors.quantity").exists());
    }

    @Test
    void getById_produitInexistant_retourne404() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
