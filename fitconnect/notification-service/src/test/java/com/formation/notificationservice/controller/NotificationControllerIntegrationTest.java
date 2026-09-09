package com.formation.notificationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.notificationservice.dto.NotificationRequest;
import com.formation.notificationservice.model.NotificationType;
import com.formation.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void cleanDatabase() {
        notificationRepository.deleteAll();
    }

    @Test
    void envoyerPuisConsulterHistorique() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(9L);
        request.setEmail("eve@example.com");
        request.setType(NotificationType.BOOKING_CONFIRMATION);
        request.setSubject("Reservation en attente de paiement");
        request.setContent("Votre reservation BK-XXXX est en attente de paiement.");

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").exists());

        mockMvc.perform(get("/api/notifications/user/{userId}", 9L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("eve@example.com"));
    }

    @Test
    void create_avecEmailInvalide_retourne400() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(9L);
        request.setEmail("pas-un-email");
        request.setType(NotificationType.BOOKING_CONFIRMATION);
        request.setSubject("Sujet");
        request.setContent("Contenu");

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }
}
