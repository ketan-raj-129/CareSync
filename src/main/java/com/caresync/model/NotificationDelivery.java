package com.caresync.model;

import java.io.Serializable;
import java.time.Instant;

/**
 * Represents a discrete notification delivery item tracked in the outbox.
 */
public class NotificationDelivery extends BaseModel implements Serializable {

    private Long userId;
    private String recipientEmail;
    private String recipientName;
    private NotificationChannel channel = NotificationChannel.IN_APP;
    private NotificationType eventType = NotificationType.SYSTEM_ALERT;
    private String title;
    private String contentHtml;
    private String contentText;
    private String relatedEntityType;
    private Long relatedEntityId;
    private String idempotencyKey;
    private NotificationDeliveryStatus status = NotificationDeliveryStatus.PENDING;
    private int attemptCount = 0;
    private int maxAttempts = 3;
    private Instant nextRetryAt = Instant.now();
    private Instant lockedUntil;
    private String lastError;
    private Instant sentAt;

    public NotificationDelivery() {
        super();
    }

    public NotificationDelivery(Long id) {
        super(id);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel != null ? channel : NotificationChannel.IN_APP;
    }

    public NotificationType getEventType() {
        return eventType;
    }

    public void setEventType(NotificationType eventType) {
        this.eventType = eventType != null ? eventType : NotificationType.SYSTEM_ALERT;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public NotificationDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationDeliveryStatus status) {
        this.status = status != null ? status : NotificationDeliveryStatus.PENDING;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Instant getNextRetryAt() {
        return nextRetryAt;
    }

    public void setNextRetryAt(Instant nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }
}

