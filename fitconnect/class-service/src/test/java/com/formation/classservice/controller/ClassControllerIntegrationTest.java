package com.formation.classservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.classservice.dto.ClassRequest;
import com.formation.classservice.model.ClassCategory;
import com.formation.classservice.model.ClassLevel;
import com.formation.classservice.repository.ClassRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClassControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClassRepository classRepository;

    @BeforeEach
    void cleanDatabase() {
        classRepository.deleteAll();
    }

    @Test
    void cycleDeVieComplet_creerRecupererMettreAJourAnnuler() throws Exception {
        ClassRequest createRequest = new ClassRequest("Yoga du soir", "Detente et etirements", "Sophie", "Paris 15e",
                ClassCategory.YOGA, ClassLevel.BEGINNER, 60, 12, new BigDecimal("20.00"), inDays(2));

        String response = mockMvc.perform(post("/api/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Yoga du soir"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.currentParticipants").value(0))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/classes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instructor").value("Sophie"));

        ClassRequest updateRequest = new ClassRequest("Yoga du soir avance", "Detente et etirements", "Sophie", "Paris 15e",
                ClassCategory.YOGA, ClassLevel.INTERMEDIATE, 60, 12, new BigDecimal("22.00"), inDays(2));
        mockMvc.perform(put("/api/classes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value("INTERMEDIATE"));

        mockMvc.perform(delete("/api/classes/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/classes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void incrementPuisDecrement_metAJourCurrentParticipants() throws Exception {
        Long id = createClass("Crossfit", ClassCategory.CROSSFIT, ClassLevel.ADVANCED, 5);

        mockMvc.perform(patch("/api/classes/{id}/increment", id).param("spots", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentParticipants").value(3));

        mockMvc.perform(patch("/api/classes/{id}/increment", id).param("spots", "3"))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/classes/{id}/decrement", id).param("spots", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentParticipants").value(2));
    }

    @Test
    void getAll_filtreParCategorieEtNiveau() throws Exception {
        createClass("Yoga debutant", ClassCategory.YOGA, ClassLevel.BEGINNER, 30);
        createClass("Yoga avance", ClassCategory.YOGA, ClassLevel.ADVANCED, 30);
        createClass("Boxe", ClassCategory.BOXING, ClassLevel.BEGINNER, 30);

        mockMvc.perform(get("/api/classes").param("category", "YOGA").param("level", "BEGINNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Yoga debutant"));

        mockMvc.perform(get("/api/classes").param("category", "YOGA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void create_avecDonneesInvalides_retourne400() throws Exception {
        ClassRequest invalid = new ClassRequest("AB", "", "", "",
                null, null, 40, 3, new BigDecimal("1.00"), LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.maxParticipants").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists())
                .andExpect(jsonPath("$.fieldErrors.dateTime").exists())
                .andExpect(jsonPath("$.fieldErrors.durationAllowed").exists());
    }

    private Long createClass(String name, ClassCategory category, ClassLevel level, int maxParticipants) throws Exception {
        ClassRequest request = new ClassRequest(name, "Description", "Instructeur", "Lyon",
                category, level, 45, maxParticipants, new BigDecimal("15.00"), inDays(1));
        String response = mockMvc.perform(post("/api/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private LocalDateTime inDays(int days) {
        return LocalDateTime.now().plusDays(days).withNano(0);
    }
}
