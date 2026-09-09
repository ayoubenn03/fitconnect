package com.formation.paymentservice.dto;

import com.formation.paymentservice.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class PaymentRequest {

    @NotNull(message = "bookingId est obligatoire")
    private Long bookingId;

    @NotBlank(message = "bookingReference est obligatoire")
    private String bookingReference;

    @NotNull(message = "userId est obligatoire")
    private Long userId;

    @NotNull(message = "amount est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "amount doit etre strictement positif")
    private BigDecimal amount;

    @NotNull(message = "paymentMethod est obligatoire")
    private PaymentMethod paymentMethod;

    @Pattern(regexp = "\\d{4}", message = "cardLastFour doit contenir exactement 4 chiffres")
    private String cardLastFour;

    @NotBlank(message = "transactionId est obligatoire")
    private String transactionId;

    public PaymentRequest() {
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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
