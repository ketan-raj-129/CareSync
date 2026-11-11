package com.caresync.model;

import java.io.Serializable;
import java.time.Instant;

/**
 * User-managed multi-channel communication preferences.
 */
public class NotificationPreference extends BaseModel implements Serializable {

    private Long userId;
    private boolean emailConfirmations = true;
    private boolean emailCancellations = true;
    private boolean emailReminders = true;
    private boolean emailAccountAlerts = true;

    public NotificationPreference() {
        super();
    }

    public NotificationPreference(Long userId) {
        super();
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isEmailConfirmations() {
        return emailConfirmations;
    }

    public void setEmailConfirmations(boolean emailConfirmations) {
        this.emailConfirmations = emailConfirmations;
    }

    public boolean isEmailCancellations() {
        return emailCancellations;
    }

    public void setEmailCancellations(boolean emailCancellations) {
        this.emailCancellations = emailCancellations;
    }

    public boolean isEmailReminders() {
        return emailReminders;
    }

    public void setEmailReminders(boolean emailReminders) {
        this.emailReminders = emailReminders;
    }

    public boolean isEmailAccountAlerts() {
        return emailAccountAlerts;
    }

    public void setEmailAccountAlerts(boolean emailAccountAlerts) {
        this.emailAccountAlerts = emailAccountAlerts;
    }
}

