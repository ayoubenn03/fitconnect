package com.formation.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.paymentservice.dto.PaymentRequest;
import com.formation.paymentservice.model.PaymentMethod;
import com.formation.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void cleanDatabase() {
        paymentRepository.deleteAll();
    }

    @Test
    void payerPuisRembourser_cycleComplet() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(42L);
        request.setBookingReference("BK-ABCDEF12");
        request.setUserId(1L);
        request.setAmount(new BigDecimal("35.00"));
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        request.setCardLastFour("4242");
        request.setTransactionId("txn_ok");

        String response = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/payments/booking/{bookingId}", 42L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentReference").exists());

        mockMvc.perform(post("/api/payments/{id}/refund", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    void payer_montantAuDessusDuSeuil_estRefuse() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(43L);
        request.setBookingReference("BK-GHIJKL34");
        request.setUserId(2L);
        request.setAmount(new BigDecimal("250.00"));
        request.setPaymentMethod(PaymentMethod.PAYPAL);
        request.setTransactionId("txn_ko");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }
}
