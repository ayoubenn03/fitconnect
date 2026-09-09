package com.formation.bookingservice.service;

import com.formation.bookingservice.client.ClassClient;
import com.formation.bookingservice.client.NotificationClient;
import com.formation.bookingservice.client.PaymentClient;
import com.formation.bookingservice.client.dto.ClassDto;
import com.formation.bookingservice.client.dto.PaymentDto;
import com.formation.bookingservice.dto.BookingRequest;
import com.formation.bookingservice.dto.BookingResponse;
import com.formation.bookingservice.dto.PaymentConfirmationRequest;
import com.formation.bookingservice.dto.PaymentMethod;
import com.formation.bookingservice.exception.NoSpotsAvailableException;
import com.formation.bookingservice.model.Booking;
import com.formation.bookingservice.model.BookingStatus;
import com.formation.bookingservice.repository.BookingRepository;
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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de BookingService : BookingRepository ET les 3 clients Feign (Class/Payment/
 * Notification) sont mockes. Aucun appel HTTP, aucune base reelle : on teste UNIQUEMENT
 * l'orchestration de la Saga (etapes, exceptions, compensation).
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ClassClient classClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void shouldCreateBooking_whenSpotsAvailable() {
        ClassDto fitnessClass = classDto(101L, "Crossfit du midi", "Karim", new BigDecimal("20.00"), 10, 5);
        when(classClient.getClassById(101L)).thenReturn(fitnessClass);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setUserEmail("alice@example.com");
        request.setUserName("Alice");
        request.setClassId(101L);
        request.setNumberOfSpots(2);

        BookingResponse result = bookingService.createBooking(request);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("40.00"); // 20.00 * 2
        assertThat(result.getBookingReference()).startsWith("BK-");
        verify(classClient).incrementParticipants(101L, 2);
    }

    @Test
    void shouldThrowException_whenNoSpotsAvailable() {
        ClassDto fitnessClass = classDto(101L, "Crossfit du midi", "Karim", new BigDecimal("20.00"), 10, 9);
        when(classClient.getClassById(101L)).thenReturn(fitnessClass);
        when(classClient.incrementParticipants(101L, 2)).thenThrow(conflictException());

        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setUserEmail("bob@example.com");
        request.setUserName("Bob");
        request.setClassId(101L);
        request.setNumberOfSpots(2);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(NoSpotsAvailableException.class);

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldCancelBookingAndRefund_whenWithinDeadline() {
        Booking confirmedBooking = confirmedBooking();
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(confirmedBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentDto existingPayment = new PaymentDto();
        existingPayment.setId(55L);
        existingPayment.setStatus("SUCCESS");
        when(paymentClient.getPaymentByBooking(10L)).thenReturn(existingPayment);

        BookingResponse result = bookingService.cancelBooking(10L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(paymentClient).refund(55L);
        verify(classClient).decrementParticipants(eq(confirmedBooking.getClassId()), eq(confirmedBooking.getNumberOfSpots()));
    }

    @Test
    void confirmPayment_quandDelaiDePaiementDepasse_leveException() {
        Booking expiredPending = confirmedBooking();
        expiredPending.setStatus(BookingStatus.PENDING_PAYMENT);
        expiredPending.setPaymentDeadline(LocalDateTime.now().minusMinutes(5));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(expiredPending));

        PaymentConfirmationRequest confirmRequest = new PaymentConfirmationRequest();
        confirmRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        confirmRequest.setTransactionId("txn_1");

        assertThatThrownBy(() -> bookingService.confirmPayment(10L, confirmRequest))
                .hasMessageContaining("depasse");

        verify(paymentClient, never()).processPayment(any());
    }

    private ClassDto classDto(Long id, String name, String instructor, BigDecimal price, int max, int current) {
        ClassDto dto = new ClassDto();
        dto.setId(id);
        dto.setName(name);
        dto.setInstructor(instructor);
        dto.setGymLocation("Paris");
        dto.setDateTime(LocalDateTime.now().plusDays(5));
        dto.setPrice(price);
        dto.setMaxParticipants(max);
        dto.setCurrentParticipants(current);
        dto.setStatus("SCHEDULED");
        return dto;
    }

    private Booking confirmedBooking() {
        Booking booking = new Booking();
        booking.setId(10L);
        booking.setBookingReference("BK-TESTONE");
        booking.setUserId(1L);
        booking.setUserEmail("alice@example.com");
        booking.setUserName("Alice");
        booking.setClassId(101L);
        booking.setClassName("Crossfit du midi");
        booking.setClassDate(LocalDateTime.now().plusDays(5));
        booking.setInstructor("Karim");
        booking.setPrice(new BigDecimal("20.00"));
        booking.setNumberOfSpots(2);
        booking.setTotalAmount(new BigDecimal("40.00"));
        booking.setBookingDate(LocalDateTime.now().minusHours(2));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentDeadline(LocalDateTime.now().minusHours(1));
        booking.setCancellationDeadline(LocalDateTime.now().plusDays(4)); // classDate - 24h, dans le futur
        return booking;
    }

    private FeignException.Conflict conflictException() {
        Request request = Request.create(Request.HttpMethod.PATCH, "/api/classes/101/increment",
                Map.of(), null, StandardCharsets.UTF_8, new RequestTemplate());
        return new FeignException.Conflict("conflict", request, null, null);
    }
}
