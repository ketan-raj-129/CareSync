package com.caresync.exception;

import java.time.Instant;

/**
 * Root base runtime exception for the CareSync platform.
 */
public class CareSyncException extends RuntimeException {

    private final String errorCode;
    private final Instant timestamp;

    public CareSyncException(String message) {
        this("CARESYNC_ERROR", message, null);
    }

    public CareSyncException(String message, Throwable cause) {
        this("CARESYNC_ERROR", message, cause);
    }

    public CareSyncException(String errorCode, String message) {
        this(errorCode, message, null);
    }

    public CareSyncException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : "CARESYNC_ERROR";
        this.timestamp = Instant.now();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}

