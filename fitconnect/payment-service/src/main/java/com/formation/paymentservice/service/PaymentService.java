package com.formation.paymentservice.service;

import com.formation.paymentservice.dto.PaymentRequest;
import com.formation.paymentservice.dto.PaymentResponse;
import com.formation.paymentservice.exception.InvalidPaymentStateException;
import com.formation.paymentservice.exception.PaymentNotFoundException;
import com.formation.paymentservice.model.Payment;
import com.formation.paymentservice.model.PaymentStatus;
import com.formation.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // Simulation : tout paiement >= ce seuil est refuse. Externalise dans config-repo/payment-service.yml
    // (plutot qu'en dur dans le code) pour pouvoir l'ajuster sans recompiler - typiquement utile en test.
    @Value("${payment.rejection-threshold:100.00}")
    private BigDecimal rejectionThreshold;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setPaymentReference(generatePaymentReference());
        payment.setBookingId(request.getBookingId());
        payment.setBookingReference(request.getBookingReference());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setCardLastFour(request.getCardLastFour());
        payment.setTransactionId(request.getTransactionId());
        payment.setPaymentDate(LocalDateTime.now());

        // Simulation demandee par le sujet : montant < seuil -> accepte, sinon -> refuse.
        // On persiste le paiement dans les deux cas : un paiement FAILED reste une donnee utile
        // (historique, debug, "pourquoi mon paiement a-t-il ete refuse ?").
        payment.setStatus(request.getAmount().compareTo(rejectionThreshold) < 0
                ? PaymentStatus.SUCCESS
                : PaymentStatus.FAILED);

        Payment saved = paymentRepository.save(payment);
        return PaymentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getByBooking(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new PaymentNotFoundException("Aucun paiement trouve pour la reservation " + bookingId));
        return PaymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getByUser(Long userId) {
        return paymentRepository.findByUserId(userId).stream().map(PaymentMapper::toResponse).toList();
    }

    @Transactional
    public PaymentResponse refund(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Paiement introuvable avec l'id " + id));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStateException(
                    "Seul un paiement SUCCESS peut etre rembourse (statut actuel : " + payment.getStatus() + ")");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        return PaymentMapper.toResponse(paymentRepository.save(payment));
    }

    private String generatePaymentReference() {
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
