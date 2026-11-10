package com.caresync.model;

public enum Gender {
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_TO_SAY;

    public static Gender fromString(String value) {
        if (value == null) return null;
        for (Gender g : values()) {
            if (g.name().equalsIgnoreCase(value.trim())) {
                return g;
            }
        }
        return PREFER_NOT_TO_SAY;
    }
}

