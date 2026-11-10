package com.caresync.model;

public enum BloodGroup {
    A_POS("A+"),
    A_NEG("A-"),
    B_POS("B+"),
    B_NEG("B-"),
    AB_POS("AB+"),
    AB_NEG("AB-"),
    O_POS("O+"),
    O_NEG("O-");

    private final String displayValue;

    BloodGroup(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static BloodGroup fromString(String value) {
        if (value == null) return null;
        for (BloodGroup bg : values()) {
            if (bg.displayValue.equalsIgnoreCase(value.trim()) || bg.name().equalsIgnoreCase(value.trim())) {
                return bg;
            }
        }
        return null;
    }
}

