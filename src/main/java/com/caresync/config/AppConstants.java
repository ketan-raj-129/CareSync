package com.caresync.config;

/**
 * Global application constants.
 */
public final class AppConstants {

    private AppConstants() {}

    // Security & Cryptography
    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    public static final int DEFAULT_PBKDF2_ITERATIONS = 600_000;
    public static final int SALT_BYTE_LENGTH = 16;
    public static final int HASH_KEY_LENGTH_BITS = 256;

    // Session Management
    public static final String SESSION_COOKIE_NAME = "CARESYNC_SESSIONID";
    public static final String SESSION_USER_PRINCIPAL = "USER_PRINCIPAL";
    public static final String SESSION_CSRF_TOKEN = "CSRF_TOKEN";
    public static final int DEFAULT_SESSION_TIMEOUT_MINUTES = 30;

    // Slot Durations
    public static final int DEFAULT_SLOT_DURATION_MINUTES = 30;
    public static final int ADVANCE_BOOKING_MAX_DAYS = 60;
    public static final int MINIMUM_CANCELLATION_HOURS = 2;
}

