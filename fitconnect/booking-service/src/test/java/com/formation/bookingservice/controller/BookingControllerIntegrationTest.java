package com.formation.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.bookingservice.client.ClassClient;
import com.formation.bookingservice.client.NotificationClient;
import com.formation.bookingservice.client.PaymentClient;
import com.formation.bookingservice.client.dto.ClassDto;
import com.formation.bookingservice.client.dto.PaymentDto;
import com.formation.bookingservice.client.dto.ProcessPaymentRequest;
import com.formation.bookingservice.dto.BookingRequest;
import com.formation.bookingservice.dto.PaymentConfirmationRequest;
import com.formation.bookingservice.dto.PaymentMethod;
import com.formation.bookingservice.model.Booking;
import com.formation.bookingservice.model.BookingStatus;
import com.formation.bookingservice.repository.BookingRepository;
import com.formation.bookingservice.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'integration bout en bout : controller -> service -> repository -> H2 en memoire.
 * class-service, payment-service, notification-service ne sont PAS reellement lances : leurs
 * clients Feign sont mockes (@MockBean) pour isoler booking-service, exactement comme
 * OrderControllerIntegrationTest le fait pour ProductClient.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingService bookingService;

    @MockBean
    private ClassClient classClient;

    @MockBean
    private PaymentClient paymentClient;

    @MockBean
    private NotificationClient notificationClient;

    @BeforeEach
    void cleanDatabase() {
        bookingRepository.deleteAll();
    }

    @Test
    void shouldCompleteFullBookingFlow() throws Exception {
        // 1. Create class (simule : class-service repond avec un cours qui a de la place)
        ClassDto fitnessClass = new ClassDto();
        fitnessClass.setId(101L);
        fitnessClass.setName("Pilates matinal");
        fitnessClass.setInstructor("Nadia");
        fitnessClass.setGymLocation("Paris 10e");
        fitnessClass.setDateTime(LocalDateTime.now().plusDays(5));
        fitnessClass.setPrice(new BigDecimal("18.00"));
        fitnessClass.setMaxParticipants(12);
        fitnessClass.setCurrentParticipants(3);
        fitnessClass.setStatus("SCHEDULED");
        when(classClient.getClassById(101L)).thenReturn(fitnessClass);
        when(classClient.incrementParticipants(eq(101L), any())).thenReturn(fitnessClass);

        PaymentDto successPayment = new PaymentDto();
        successPayment.setId(500L);
        successPayment.setStatus("SUCCESS");
        when(paymentClient.processPayment(any(ProcessPaymentRequest.class))).thenReturn(successPayment);

        // 2. Create booking
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setUserId(7L);
        bookingRequest.setUserEmail("chris@example.com");
        bookingRequest.setUserName("Chris");
        bookingRequest.setClassId(101L);
        bookingRequest.setNumberOfSpots(1);

        String createResponse = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.className").value("Pilates matinal"))
                .andReturn().getResponse().getContentAsString();

        Long bookingId = objectMapper.readTree(createResponse).get("id").asLong();

        // 3. Confirm payment
        PaymentConfirmationRequest confirmRequest = new PaymentConfirmationRequest();
        confirmRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        confirmRequest.setCardLastFour("4242");
        confirmRequest.setTransactionId("txn_success_1");

        mockMvc.perform(patch("/api/bookings/{id}/confirm", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // 4. Verify booking status = CONFIRMED (relecture)
        Booking stored = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        // 5. Verify spots decreased -> incrementParticipants a bien ete appele sur class-service
        verify(classClient).incrementParticipants(101L, 1);

        // 6. Verify notification sent (BOOKING_CONFIRMATION a la creation + PAYMENT_CONFIRMATION a la confirmation)
        verify(notificationClient, org.mockito.Mockito.times(2)).send(any());
    }

    @Test
    void shouldCancelExpiredBookings() {
        // 1. Create booking with paymentDeadline in past (on ecrit directement via le repository
        // pour simuler une reservation creee il y a plus d'1h, sans attendre le temps reel)
        Booking expired = new Booking();
        expired.setBookingReference("BK-EXPIRED1");
        expired.setUserId(3L);
        expired.setUserEmail("dana@example.com");
        expired.setUserName("Dana");
        expired.setClassId(202L);
        expired.setClassName("Zumba");
        expired.setClassDate(LocalDateTime.now().plusDays(10));
        expired.setInstructor("Leo");
        expired.setPrice(new BigDecimal("16.00"));
        expired.setNumberOfSpots(2);
        expired.setTotalAmount(new BigDecimal("32.00"));
        expired.setBookingDate(LocalDateTime.now().minusHours(3));
        expired.setStatus(BookingStatus.PENDING_PAYMENT);
        expired.setPaymentDeadline(LocalDateTime.now().minusHours(2)); // deja depassee
        expired.setCancellationDeadline(LocalDateTime.now().plusDays(9));
        Long id = bookingRepository.save(expired).getId();

        // 2. Run scheduler (on appelle directement la logique metier, cf. carte 07 de la page
        // de suivi : c'est exactement ce que le @Scheduled declenche, sans attendre un vrai cron)
        int cancelled = bookingService.expirePendingPayments();

        // 3. Verify status = CANCELLED
        assertThat(cancelled).isEqualTo(1);
        Booking reloaded = bookingRepository.findById(id).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(BookingStatus.CANCELLED);

        // 4. Verify spots restored -> decrementParticipants appele sur class-service
        verify(classClient).decrementParticipants(202L, 2);
        // Une reservation PENDING_PAYMENT n'a jamais ete payee : pas de remboursement declenche.
        verify(paymentClient, org.mockito.Mockito.never()).getPaymentByBooking(anyLong());
    }
}
