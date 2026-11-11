package com.caresync.model;

/**
 * Lifecycle states of an outbound notification delivery item.
 */
public enum NotificationDeliveryStatus {
    PENDING,
    PROCESSING,
    SENT,
    FAILED,
    CANCELLED;

    public static NotificationDeliveryStatus fromString(String value) {
        if (value == null) return PENDING;
        for (NotificationDeliveryStatus st : values()) {
            if (st.name().equalsIgnoreCase(value.trim())) {
                return st;
            }
        }
        return PENDING;
    }
}

