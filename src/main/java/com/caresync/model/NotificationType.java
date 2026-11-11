package com.caresync.model;

public enum NotificationType {
    APPOINTMENT_BOOKED,
    APPOINTMENT_CONFIRMED,
    APPOINTMENT_REJECTED,
    APPOINTMENT_CANCELLED,
    APPOINTMENT_REMINDER,
    PRESCRIPTION_READY,
    SYSTEM_ALERT;

    public String toDbString() {
        // Map to existing schema CHECK constraint values when storing in legacy notifications table
        if (this == APPOINTMENT_REJECTED) return APPOINTMENT_CANCELLED.name();
        if (this == APPOINTMENT_REMINDER) return SYSTEM_ALERT.name();
        return this.name();
    }

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

