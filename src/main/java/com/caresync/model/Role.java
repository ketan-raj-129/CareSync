package com.caresync.model;

public enum Role {
    ADMIN,
    DOCTOR,
    PATIENT;

    public static Role fromString(String value) {
        if (value == null) return null;
        for (Role r : values()) {
            if (r.name().equalsIgnoreCase(value.trim())) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown Role: " + value);
    }
}

