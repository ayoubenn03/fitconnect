package com.formation.paymentservice.service;

import com.formation.paymentservice.dto.PaymentRequest;
import com.formation.paymentservice.dto.PaymentResponse;
import com.formation.paymentservice.exception.InvalidPaymentStateException;
import com.formation.paymentservice.exception.PaymentNotFoundException;
import com.formation.paymentservice.model.Payment;
import com.formation.paymentservice.model.PaymentMethod;
import com.formation.paymentservice.model.PaymentStatus;
import com.formation.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * PaymentRepository est mocke. rejectionThreshold est normalement injecte par Spring via
 * @Value depuis config-repo/payment-service.yml : en test unitaire pur (sans ApplicationContext),
 * on le fixe nous-memes avec ReflectionTestUtils pour reproduire la meme valeur qu'en production.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setRejectionThreshold() {
        ReflectionTestUtils.setField(paymentService, "rejectionThreshold", new BigDecimal("100.00"));
    }

    @Test
    void processPayment_montantSousLeSeuil_estAccepte() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        PaymentResponse result = paymentService.processPayment(request(new BigDecimal("49.90")));

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(result.getPaymentReference()).startsWith("PAY-");
    }

    @Test
    void processPayment_montantAuDessusDuSeuil_estRefuseMaisSauvegarde() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });

        PaymentResponse result = paymentService.processPayment(request(new BigDecimal("150.00")));

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void refund_paiementSuccess_passeAuStatutRefunded() {
        Payment success = new Payment();
        success.setId(3L);
        success.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findById(3L)).thenReturn(Optional.of(success));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse result = paymentService.refund(3L);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void refund_paiementDejaEchoue_leveInvalidPaymentStateException() {
        Payment failed = new Payment();
        failed.setId(4L);
        failed.setStatus(PaymentStatus.FAILED);
        when(paymentRepository.findById(4L)).thenReturn(Optional.of(failed));

        assertThatThrownBy(() -> paymentService.refund(4L))
                .isInstanceOf(InvalidPaymentStateException.class);
    }

    @Test
    void refund_paiementInexistant_levePaymentNotFoundException() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.refund(999L))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    private PaymentRequest request(BigDecimal amount) {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setBookingReference("BK-ABC12345");
        request.setUserId(1L);
        request.setAmount(amount);
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        request.setCardLastFour("4242");
        request.setTransactionId("txn_1");
        return request;
    }
}
