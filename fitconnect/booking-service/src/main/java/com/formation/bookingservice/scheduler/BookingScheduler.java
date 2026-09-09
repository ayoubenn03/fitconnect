package com.formation.bookingservice.scheduler;

import com.formation.bookingservice.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Ne contient AUCUNE logique metier : juste le "quand" (les expressions cron viennent de
 * config-repo/booking-service.yml, pas d'une valeur figee dans le code). Le "quoi" vit dans
 * BookingService, qui reste testable unitairement sans avoir a attendre un declenchement cron.
 */
@Component
public class BookingScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingScheduler.class);

    private final BookingService bookingService;

    public BookingScheduler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Toutes les 5 minutes par defaut (booking.scheduler.expire-pending-payments-cron).
    // Format cron Spring : seconde minute heure jour-du-mois mois jour-de-semaine.
    @Scheduled(cron = "${booking.scheduler.expire-pending-payments-cron:0 */5 * * * *}")
    public void expirePendingPayments() {
        log.debug("Scheduler : verification des reservations en attente de paiement expirees");
        bookingService.expirePendingPayments();
    }

    // Toutes les heures par defaut (booking.scheduler.class-reminder-cron).
    @Scheduled(cron = "${booking.scheduler.class-reminder-cron:0 0 * * * *}")
    public void sendClassReminders() {
        log.debug("Scheduler : recherche des cours dans 24h pour envoi de rappel");
        bookingService.sendClassReminders();
    }
}
