package com.formation.bookingservice.repository;

import com.formation.bookingservice.model.Booking;
import com.formation.bookingservice.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    // Utilise par le scheduler d'expiration : reservations encore en attente de paiement
    // dont la deadline est deja depassee.
    List<Booking> findByStatusAndPaymentDeadlineBefore(BookingStatus status, LocalDateTime now);

    // Utilise par le scheduler de rappel : reservations confirmees dont le cours a lieu
    // dans la fenetre [from, to) (typiquement "dans ~24h").
    List<Booking> findByStatusAndClassDateBetween(BookingStatus status, LocalDateTime from, LocalDateTime to);
}
