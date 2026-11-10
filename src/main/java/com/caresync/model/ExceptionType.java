package com.caresync.model;

public enum ExceptionType {
    LEAVE,
    SICK,
    CONFERENCE,
    HOLIDAY,
    EMERGENCY;

    public static ExceptionType fromString(String value) {
        if (value == null) return null;
        for (ExceptionType et : values()) {
            if (et.name().equalsIgnoreCase(value.trim())) {
                return et;
            }
        }
        return LEAVE;
    }
}

