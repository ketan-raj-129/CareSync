package com.caresync.model;

public enum NotificationType {
    APPOINTMENT_BOOKED,
    APPOINTMENT_CONFIRMED,
    APPOINTMENT_CANCELLED,
    PRESCRIPTION_READY,
    SYSTEM_ALERT;

    public static NotificationType fromString(String value) {
        if (value == null) return null;
        for (NotificationType nt : values()) {
            if (nt.name().equalsIgnoreCase(value.trim())) {
                return nt;
            }
        }
        return SYSTEM_ALERT;
    }
}

