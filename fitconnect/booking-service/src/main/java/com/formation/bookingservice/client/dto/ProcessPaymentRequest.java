package com.formation.bookingservice.client.dto;

import com.formation.bookingservice.dto.PaymentMethod;

import java.math.BigDecimal;

public class ProcessPaymentRequest {

    private Long bookingId;
    private String bookingReference;
    private Long userId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String cardLastFour;
    private String transactionId;

    public ProcessPaymentRequest() {
    }

    public ProcessPaymentRequest(Long bookingId, String bookingReference, Long userId, BigDecimal amount,
                                  PaymentMethod paymentMethod, String cardLastFour, String transactionId) {
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.cardLastFour = cardLastFour;
        this.transactionId = transactionId;
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
