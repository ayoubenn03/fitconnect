package com.formation.bookingservice.service;

import com.formation.bookingservice.client.ClassClient;
import com.formation.bookingservice.client.NotificationClient;
import com.formation.bookingservice.client.PaymentClient;
import com.formation.bookingservice.client.dto.ClassDto;
import com.formation.bookingservice.client.dto.NotificationType;
import com.formation.bookingservice.client.dto.PaymentDto;
import com.formation.bookingservice.client.dto.ProcessPaymentRequest;
import com.formation.bookingservice.client.dto.SendNotificationRequest;
import com.formation.bookingservice.dto.BookingRequest;
import com.formation.bookingservice.dto.BookingResponse;
import com.formation.bookingservice.dto.PaymentConfirmationRequest;
import com.formation.bookingservice.exception.BookingNotFoundException;
import com.formation.bookingservice.exception.CancellationNotAllowedException;
import com.formation.bookingservice.exception.ClassNotFoundForBookingException;
import com.formation.bookingservice.exception.DownstreamServiceUnavailableException;
import com.formation.bookingservice.exception.InvalidBookingStateException;
import com.formation.bookingservice.exception.NoSpotsAvailableException;
import com.formation.bookingservice.exception.PaymentExpiredException;
import com.formation.bookingservice.exception.PaymentFailedException;
import com.formation.bookingservice.model.Booking;
import com.formation.bookingservice.model.BookingStatus;
import com.formation.bookingservice.repository.BookingRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final ClassClient classClient;
    private final PaymentClient paymentClient;
    private final NotificationClient notificationClient;

    public BookingService(BookingRepository bookingRepository, ClassClient classClient,
                           PaymentClient paymentClient, NotificationClient notificationClient) {
        this.bookingRepository = bookingRepository;
        this.classClient = classClient;
        this.paymentClient = paymentClient;
        this.notificationClient = notificationClient;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findAll() {
        return bookingRepository.findAll().stream().map(BookingMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse findById(Long id) {
        return BookingMapper.toResponse(getBookingOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findByUser(Long userId) {
        return bookingRepository.findByUserId(userId).stream().map(BookingMapper::toResponse).toList();
    }

    // ------------------------------------------------------------------
    // CAS 1 (succes) / CAS 2 (plus de places) du workflow de reservation
    // ------------------------------------------------------------------
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // Etape 1 : verifier le cours + capturer le snapshot (nom, date, instructeur, prix)
        ClassDto fitnessClass = fetchClass(request.getClassId());

        // Etape 2 : reserver les places. C'est CE SEUL appel qui peut echouer par surreservation
        // (409) : deux booking-service pourraient avoir lu le meme "places restantes" a l'etape 1,
        // mais increment() est atomique et proteges par le verrouillage optimiste de class-service.
        // Si ca echoue ici, AUCUNE ligne Booking n'a encore ete creee -> rien a compenser (Cas 2).
        reserveSpots(request.getClassId(), request.getNumberOfSpots());

        // Etape 3 : creer la reservation. Si CETTE etape echoue apres que les places ont ete
        // prises (ex: erreur de validation JPA imprevue), il FAUT compenser en relachant les
        // places reservees a l'etape 2 - sinon elles restent bloquees pour rien.
        Booking saved;
        try {
            saved = persistNewBooking(request, fitnessClass);
        } catch (RuntimeException ex) {
            log.warn("Echec de creation de la reservation apres reservation des places, compensation (decrement) sur classId={}",
                    request.getClassId(), ex);
            safeReleaseSpots(request.getClassId(), request.getNumberOfSpots());
            throw ex;
        }

        // Etape 4 : notifier (side-effect non-bloquant : un echec de notification ne doit pas
        // annuler une reservation par ailleurs valide et payee-a-venir).
        notifyBestEffort(saved.getUserId(), saved.getUserEmail(), NotificationType.BOOKING_CONFIRMATION,
                "Reservation en attente de paiement",
                "Votre reservation " + saved.getBookingReference() + " pour " + saved.getClassName()
                        + " est en attente de paiement. Payez avant " + saved.getPaymentDeadline() + ".");

        // Etape 5 : retour au client (201, statut PENDING_PAYMENT)
        return BookingMapper.toResponse(saved);
    }

    // ------------------------------------------------------------------
    // CAS 3 : confirmation du paiement
    // ------------------------------------------------------------------
    @Transactional
    public BookingResponse confirmPayment(Long id, PaymentConfirmationRequest request) {
        Booking booking = getBookingOrThrow(id);

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new InvalidBookingStateException(
                    "La reservation " + booking.getBookingReference() + " n'est pas en attente de paiement (statut actuel : "
                            + booking.getStatus() + ")");
        }
        if (LocalDateTime.now().isAfter(booking.getPaymentDeadline())) {
            throw new PaymentExpiredException(
                    "Le delai de paiement pour la reservation " + booking.getBookingReference() + " est depasse");
        }

        PaymentDto payment = processPayment(booking, request);

        if (!"SUCCESS".equals(payment.getStatus())) {
            // Choix assume : on NE PASSE PAS la reservation a CANCELLED sur un echec de paiement,
            // pour laisser l'utilisateur retenter tant que paymentDeadline n'est pas depassee
            // (le scheduler se chargera de l'expirer sinon, voir booking.scheduler).
            throw new PaymentFailedException("Le paiement a ete refuse pour la reservation " + booking.getBookingReference());
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking updated = bookingRepository.save(booking);

        notifyBestEffort(updated.getUserId(), updated.getUserEmail(), NotificationType.PAYMENT_CONFIRMATION,
                "Paiement confirme",
                "Votre paiement pour la reservation " + updated.getBookingReference() + " a bien ete recu. A bientot au cours !");

        return BookingMapper.toResponse(updated);
    }

    // ------------------------------------------------------------------
    // CAS 4 : annulation (dans les delais)
    // ------------------------------------------------------------------
    @Transactional
    public BookingResponse cancelBooking(Long id) {
        Booking booking = getBookingOrThrow(id);

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new CancellationNotAllowedException(
                    "La reservation " + booking.getBookingReference() + " ne peut plus etre annulee (statut : " + booking.getStatus() + ")");
        }
        if (LocalDateTime.now().isAfter(booking.getCancellationDeadline())) {
            throw new CancellationNotAllowedException(
                    "Le delai d'annulation gratuite pour la reservation " + booking.getBookingReference() + " est depasse (24h avant le cours)");
        }

        // Remboursement UNIQUEMENT si un paiement a reellement ete effectue (statut CONFIRMED).
        // Une reservation encore PENDING_PAYMENT n'a jamais ete payee : rien a rembourser.
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            refundBestEffort(booking);
        }

        // Dans TOUS les cas (CONFIRMED ou PENDING_PAYMENT), les places avaient ete reservees
        // des la creation (etape 2 du workflow de reservation) -> il faut toujours les liberer.
        safeReleaseSpots(booking.getClassId(), booking.getNumberOfSpots());

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updated = bookingRepository.save(booking);

        notifyBestEffort(updated.getUserId(), updated.getUserEmail(), NotificationType.BOOKING_CANCELLED,
                "Reservation annulee",
                "Votre reservation " + updated.getBookingReference() + " a ete annulee.");

        return BookingMapper.toResponse(updated);
    }

    @Transactional
    public BookingResponse completeBooking(Long id) {
        Booking booking = getBookingOrThrow(id);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException(
                    "Seule une reservation CONFIRMED peut passer a COMPLETED (statut actuel : " + booking.getStatus() + ")");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        return BookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findExpiredPendingPayments() {
        return bookingRepository.findByStatusAndPaymentDeadlineBefore(BookingStatus.PENDING_PAYMENT, LocalDateTime.now())
                .stream().map(BookingMapper::toResponse).toList();
    }

    // ------------------------------------------------------------------
    // Appelee par BookingScheduler toutes les 5 minutes (booking.scheduler.expire-pending-payments-cron).
    // Meme logique de liberation des places que cancelBooking (jamais de remboursement ici : une
    // reservation PENDING_PAYMENT n'a par definition jamais ete payee), mais SANS les verifications
    // de delai/statut d'une annulation utilisateur : le simple fait d'etre dans cette liste EST la
    // condition (paymentDeadline deja depassee, cf. la requete du repository).
    // ------------------------------------------------------------------
    @Transactional
    public int expirePendingPayments() {
        List<Booking> expired = bookingRepository.findByStatusAndPaymentDeadlineBefore(
                BookingStatus.PENDING_PAYMENT, LocalDateTime.now());

        for (Booking booking : expired) {
            safeReleaseSpots(booking.getClassId(), booking.getNumberOfSpots());
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            notifyBestEffort(booking.getUserId(), booking.getUserEmail(), NotificationType.BOOKING_CANCELLED,
                    "Reservation expiree",
                    "Votre reservation " + booking.getBookingReference()
                            + " a ete annulee automatiquement : le paiement n'a pas ete recu dans le delai imparti (1h).");
        }
        if (!expired.isEmpty()) {
            log.info("Scheduler : {} reservation(s) en attente de paiement expiree(s) et annulee(s)", expired.size());
        }
        return expired.size();
    }

    // ------------------------------------------------------------------
    // Appelee par BookingScheduler toutes les heures (booking.scheduler.class-reminder-cron).
    // Fenetre [now+24h, now+25h) : avec un job qui tourne une fois par heure, chaque reservation
    // confirmee ne peut traverser cette fenetre qu'une seule fois -> un seul rappel envoye,
    // jamais de doublon ni d'oubli (contrairement a une simple comparaison "classDate - 24h <= now").
    // ------------------------------------------------------------------
    @Transactional(readOnly = true)
    public int sendClassReminders() {
        LocalDateTime windowStart = LocalDateTime.now().plusHours(24);
        LocalDateTime windowEnd = LocalDateTime.now().plusHours(25);

        List<Booking> upcoming = bookingRepository.findByStatusAndClassDateBetween(
                BookingStatus.CONFIRMED, windowStart, windowEnd);

        for (Booking booking : upcoming) {
            notifyBestEffort(booking.getUserId(), booking.getUserEmail(), NotificationType.BOOKING_REMINDER,
                    "Rappel : cours demain",
                    "Rappel : votre cours " + booking.getClassName() + " a lieu le " + booking.getClassDate()
                            + " avec " + booking.getInstructor() + ". A demain !");
        }
        if (!upcoming.isEmpty()) {
            log.info("Scheduler : {} rappel(s) de cours envoye(s)", upcoming.size());
        }
        return upcoming.size();
    }

    // ==================== Helpers d'appel aux autres services ====================

    private ClassDto fetchClass(Long classId) {
        try {
            return classClient.getClassById(classId);
        } catch (FeignException.NotFound ex) {
            throw new ClassNotFoundForBookingException(classId);
        } catch (CallNotPermittedException ex) {
            throw new DownstreamServiceUnavailableException("class-service indisponible (circuit ouvert)", ex);
        } catch (FeignException ex) {
            throw new DownstreamServiceUnavailableException("class-service indisponible", ex);
        }
    }

    private void reserveSpots(Long classId, int spots) {
        try {
            classClient.incrementParticipants(classId, spots);
        } catch (FeignException.Conflict ex) {
            throw new NoSpotsAvailableException("Plus de places disponibles pour le cours " + classId);
        } catch (FeignException.NotFound ex) {
            throw new ClassNotFoundForBookingException(classId);
        } catch (CallNotPermittedException ex) {
            throw new DownstreamServiceUnavailableException("class-service indisponible (circuit ouvert)", ex);
        } catch (FeignException ex) {
            throw new DownstreamServiceUnavailableException("class-service indisponible", ex);
        }
    }

    /**
     * Compensation : on essaie de liberer les places, mais si class-service est injoignable
     * a CE moment-la, on ne fait pas echouer l'operation appelante pour autant (elle a deja son
     * propre statut a renvoyer) - on logge fort pour un rattrapage manuel/monitore.
     * (Une vraie Saga irait plus loin avec une table d'evenements a rejouer ; hors perimetre du TP.)
     */
    private void safeReleaseSpots(Long classId, int spots) {
        try {
            classClient.decrementParticipants(classId, spots);
        } catch (RuntimeException ex) {
            log.error("ECHEC DE COMPENSATION : impossible de liberer {} place(s) sur le cours {} - intervention manuelle necessaire",
                    spots, classId, ex);
        }
    }

    private PaymentDto processPayment(Booking booking, PaymentConfirmationRequest request) {
        try {
            ProcessPaymentRequest paymentRequest = new ProcessPaymentRequest(
                    booking.getId(), booking.getBookingReference(), booking.getUserId(), booking.getTotalAmount(),
                    request.getPaymentMethod(), request.getCardLastFour(), request.getTransactionId());
            return paymentClient.processPayment(paymentRequest);
        } catch (CallNotPermittedException ex) {
            throw new DownstreamServiceUnavailableException("payment-service indisponible (circuit ouvert)", ex);
        } catch (FeignException ex) {
            throw new DownstreamServiceUnavailableException("payment-service indisponible", ex);
        }
    }

    private void refundBestEffort(Booking booking) {
        try {
            PaymentDto payment = paymentClient.getPaymentByBooking(booking.getId());
            paymentClient.refund(payment.getId());
        } catch (RuntimeException ex) {
            log.error("ECHEC DE REMBOURSEMENT pour la reservation {} - intervention manuelle necessaire",
                    booking.getBookingReference(), ex);
        }
    }

    private void notifyBestEffort(Long userId, String email, NotificationType type, String subject, String content) {
        try {
            notificationClient.send(new SendNotificationRequest(userId, email, type, subject, content));
        } catch (RuntimeException ex) {
            log.warn("Echec d'envoi de notification ({}) a l'utilisateur {} - la reservation reste valide", type, userId, ex);
        }
    }

    // ==================== Construction interne ====================

    private Booking persistNewBooking(BookingRequest request, ClassDto fitnessClass) {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal total = fitnessClass.getPrice().multiply(BigDecimal.valueOf(request.getNumberOfSpots()));

        Booking booking = new Booking();
        booking.setBookingReference(generateBookingReference());
        booking.setUserId(request.getUserId());
        booking.setUserEmail(request.getUserEmail());
        booking.setUserName(request.getUserName());
        booking.setClassId(fitnessClass.getId());
        booking.setClassName(fitnessClass.getName());
        booking.setClassDate(fitnessClass.getDateTime());
        booking.setInstructor(fitnessClass.getInstructor());
        booking.setPrice(fitnessClass.getPrice());
        booking.setNumberOfSpots(request.getNumberOfSpots());
        booking.setTotalAmount(total);
        booking.setBookingDate(now);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setPaymentDeadline(now.plusHours(1));
        booking.setCancellationDeadline(fitnessClass.getDateTime().minusHours(24));

        return bookingRepository.save(booking);
    }

    private String generateBookingReference() {
        return "BK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private Booking getBookingOrThrow(Long id) {
        return bookingRepository.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
    }
}
