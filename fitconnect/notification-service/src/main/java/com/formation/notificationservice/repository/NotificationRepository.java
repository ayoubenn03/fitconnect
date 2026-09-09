package com.formation.notificationservice.repository;

import com.formation.notificationservice.model.Notification;
import com.formation.notificationservice.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findByStatus(NotificationStatus status);
}
