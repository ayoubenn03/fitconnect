package com.formation.bookingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class PaymentConfirmationRequest {

    @NotNull(message = "paymentMethod est obligatoire")
    private PaymentMethod paymentMethod;

    @Pattern(regexp = "\\d{4}", message = "cardLastFour doit contenir exactement 4 chiffres")
    private String cardLastFour;

    @NotBlank(message = "transactionId est obligatoire")
    private String transactionId;

    public PaymentConfirmationRequest() {
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
