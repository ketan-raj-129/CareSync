package com.caresync.dto;

import com.caresync.model.Notification;
import com.caresync.model.NotificationType;

import java.io.Serializable;
import java.time.Instant;

/**
 * Data transfer object representing an in-app notification in patient and doctor inboxes.
 */
public class NotificationDto implements Serializable {

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType notificationType;
    private String relatedEntityType;
    private Long relatedEntityId;
    private boolean read;
    private Instant createdAt;
    private String targetUrl;

    public NotificationDto() {}

    public NotificationDto(Notification n) {
        if (n != null) {
            this.id = n.getId();
            this.userId = n.getUserId();
            this.title = n.getTitle();
            this.message = n.getMessage();
            this.notificationType = n.getNotificationType();
            this.relatedEntityType = n.getRelatedEntityType();
            this.relatedEntityId = n.getRelatedEntityId();
            this.read = n.isRead();
            this.createdAt = n.getCreatedAt();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}

