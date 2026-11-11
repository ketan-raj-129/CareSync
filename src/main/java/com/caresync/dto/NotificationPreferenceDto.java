package com.caresync.dto;

import com.caresync.model.NotificationPreference;

import java.io.Serializable;

/**
 * Data transfer object for viewing and updating user notification preferences.
 */
public class NotificationPreferenceDto implements Serializable {

    private Long userId;
    private boolean emailConfirmations = true;
    private boolean emailCancellations = true;
    private boolean emailReminders = true;
    private boolean emailAccountAlerts = true;

    public NotificationPreferenceDto() {}

    public NotificationPreferenceDto(NotificationPreference p) {
        if (p != null) {
            this.userId = p.getUserId();
            this.emailConfirmations = p.isEmailConfirmations();
            this.emailCancellations = p.isEmailCancellations();
            this.emailReminders = p.isEmailReminders();
            this.emailAccountAlerts = p.isEmailAccountAlerts();
        }
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

