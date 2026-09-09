package com.formation.notificationservice.service;

import com.formation.notificationservice.dto.NotificationRequest;
import com.formation.notificationservice.dto.NotificationResponse;
import com.formation.notificationservice.exception.NotificationNotFoundException;
import com.formation.notificationservice.model.Notification;
import com.formation.notificationservice.model.NotificationStatus;
import com.formation.notificationservice.model.NotificationType;
import com.formation.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Le canal d'envoi simule utilise un java.util.Random interne pour decider SENT/FAILED
 * (~10% d'echec). Plutot que de rendre le test flaky en le laissant vraiment aleatoire, on
 * injecte un Random mocke dont on controle nextDouble() -> comportement 100% deterministe.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private Random random;

    @Test
    void send_quandLeTirageDepasseLeTauxDechec_estMarqueSent() {
        when(random.nextDouble()).thenReturn(0.99); // >= 0.10 -> succes
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });
        NotificationService service = new NotificationService(notificationRepository, random);

        NotificationResponse result = service.send(request());

        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void send_quandLeTirageEstSousLeTauxDechec_estMarqueFailed() {
        when(random.nextDouble()).thenReturn(0.01); // < 0.10 -> echec simule
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(2L);
            return n;
        });
        NotificationService service = new NotificationService(notificationRepository, random);

        NotificationResponse result = service.send(request());

        assertThat(result.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    void retry_notificationInexistante_leveNotificationNotFoundException() {
        when(notificationRepository.findById(404L)).thenReturn(Optional.empty());
        NotificationService service = new NotificationService(notificationRepository, random);

        assertThatThrownBy(() -> service.retry(404L))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void retry_notificationEnEchec_peutRepasserSent() {
        Notification failed = new Notification();
        failed.setId(5L);
        failed.setUserId(1L);
        failed.setEmail("test@example.com");
        failed.setType(NotificationType.BOOKING_CONFIRMATION);
        failed.setSubject("Sujet");
        failed.setContent("Contenu");
        failed.setStatus(NotificationStatus.FAILED);
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(failed));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(random.nextDouble()).thenReturn(0.99);
        NotificationService service = new NotificationService(notificationRepository, random);

        NotificationResponse result = service.retry(5L);

        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    private NotificationRequest request() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setEmail("test@example.com");
        request.setType(NotificationType.BOOKING_CONFIRMATION);
        request.setSubject("Sujet");
        request.setContent("Contenu");
        return request;
    }
}
