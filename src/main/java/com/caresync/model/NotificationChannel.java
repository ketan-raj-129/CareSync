package com.caresync.model;

/**
 * Supported communication channels for notification delivery.
 */
public enum NotificationChannel {
    IN_APP,
    EMAIL,
    SMS;

    public static NotificationChannel fromString(String value) {
        if (value == null) return IN_APP;
        for (NotificationChannel ch : values()) {
            if (ch.name().equalsIgnoreCase(value.trim())) {
                return ch;
            }
        }
        return IN_APP;
    }
}

