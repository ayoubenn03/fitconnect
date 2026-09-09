package com.formation.bookingservice.client;

import com.formation.bookingservice.client.dto.PaymentDto;
import com.formation.bookingservice.client.dto.ProcessPaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/api/payments")
    PaymentDto processPayment(@RequestBody ProcessPaymentRequest request);

    @GetMapping("/api/payments/booking/{bookingId}")
    PaymentDto getPaymentByBooking(@PathVariable("bookingId") Long bookingId);

    @PostMapping("/api/payments/{id}/refund")
    PaymentDto refund(@PathVariable("id") Long paymentId);
}
