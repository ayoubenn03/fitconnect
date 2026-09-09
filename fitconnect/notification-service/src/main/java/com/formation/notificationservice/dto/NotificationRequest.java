package com.formation.notificationservice.dto;

import com.formation.notificationservice.model.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NotificationRequest {

    @NotNull(message = "userId est obligatoire")
    private Long userId;

    @NotBlank(message = "email est obligatoire")
    @Email(message = "email doit etre une adresse valide")
    private String email;

    @NotNull(message = "type est obligatoire")
    private NotificationType type;

    @NotBlank(message = "subject est obligatoire")
    private String subject;

    @NotBlank(message = "content est obligatoire")
    private String content;

    public NotificationRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
