package com.formation.notificationservice.service;

import com.formation.notificationservice.dto.NotificationResponse;
import com.formation.notificationservice.model.Notification;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getEmail(),
                notification.getType(),
                notification.getSubject(),
                notification.getContent(),
                notification.getSentDate(),
                notification.getStatus()
        );
    }
}
