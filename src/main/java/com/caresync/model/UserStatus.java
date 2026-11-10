package com.caresync.model;

public enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED;

    public static UserStatus fromString(String value) {
        if (value == null) return null;
        for (UserStatus s : values()) {
            if (s.name().equalsIgnoreCase(value.trim())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown UserStatus: " + value);
    }
}

