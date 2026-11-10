package com.caresync.model;

public enum AppointmentStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    NO_SHOW;

    public boolean isActive() {
        return this == PENDING || this == CONFIRMED;
    }

    public static AppointmentStatus fromString(String value) {
        if (value == null) return null;
        for (AppointmentStatus s : values()) {
            if (s.name().equalsIgnoreCase(value.trim())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown AppointmentStatus: " + value);
    }
}

