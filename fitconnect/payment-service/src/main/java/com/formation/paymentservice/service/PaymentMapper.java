package com.formation.paymentservice.service;

import com.formation.paymentservice.dto.PaymentResponse;
import com.formation.paymentservice.model.Payment;

public final class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentReference(),
                payment.getBookingId(),
                payment.getBookingReference(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getCardLastFour(),
                payment.getTransactionId(),
                payment.getPaymentDate(),
                payment.getStatus()
        );
    }
}
