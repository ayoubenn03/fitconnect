package com.formation.notificationservice.service;

import com.formation.notificationservice.dto.NotificationRequest;
import com.formation.notificationservice.dto.NotificationResponse;
import com.formation.notificationservice.exception.NotificationNotFoundException;
import com.formation.notificationservice.model.Notification;
import com.formation.notificationservice.model.NotificationStatus;
import com.formation.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    // Simulation d'un canal d'envoi (email/SMS) instable : ~10% d'echec, pour donner un sens
    // reel a /pending et /retry plutot que d'avoir un flux qui reussit toujours a 100%.
    private static final double SIMULATED_FAILURE_RATE = 0.10;

    private final NotificationRepository notificationRepository;
    private final Random random;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this(notificationRepository, new Random());
    }

    // Constructeur secondaire : permet aux tests d'injecter un Random deterministe
    // (ex: new Random(seed fixe), ou un stub) pour rendre le comportement previsible.
    NotificationService(NotificationRepository notificationRepository, Random random) {
        this.notificationRepository = notificationRepository;
        this.random = random;
    }

    @Transactional
    public NotificationResponse send(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setEmail(request.getEmail());
        notification.setType(request.getType());
        notification.setSubject(request.getSubject());
        notification.setContent(request.getContent());
        notification.setSentDate(LocalDateTime.now());
        notification.setStatus(attemptSend());

        Notification saved = notificationRepository.save(notification);
        return NotificationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getByUser(Long userId) {
        return notificationRepository.findByUserId(userId).stream().map(NotificationMapper::toResponse).toList();
    }

    // "Pending" = notifications FAILED en attente de retry, pas un statut PENDING litteral (jamais atteint).
    @Transactional(readOnly = true)
    public List<NotificationResponse> getPending() {
        return notificationRepository.findByStatus(NotificationStatus.FAILED).stream()
                .map(NotificationMapper::toResponse).toList();
    }

    @Transactional
    public NotificationResponse retry(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));

        notification.setStatus(attemptSend());
        notification.setSentDate(LocalDateTime.now());
        Notification updated = notificationRepository.save(notification);
        return NotificationMapper.toResponse(updated);
    }

    private NotificationStatus attemptSend() {
        boolean success = random.nextDouble() >= SIMULATED_FAILURE_RATE;
        if (!success) {
            log.warn("Simulation : echec d'envoi de notification (canal indisponible)");
        }
        return success ? NotificationStatus.SENT : NotificationStatus.FAILED;
    }
}
