package com.formation.bookingservice.client.dto;

public class SendNotificationRequest {

    private Long userId;
    private String email;
    private NotificationType type;
    private String subject;
    private String content;

    public SendNotificationRequest() {
    }

    public SendNotificationRequest(Long userId, String email, NotificationType type, String subject, String content) {
        this.userId = userId;
        this.email = email;
        this.type = type;
        this.subject = subject;
        this.content = content;
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
