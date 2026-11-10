package com.caresync.exception;

/**
 * Thrown when credential verification or session authentication fails.
 */
public class AuthenticationException extends CareSyncException {

    private final String attemptedEmail;

    public AuthenticationException(String message) {
        this(null, message);
    }

    public AuthenticationException(String attemptedEmail, String message) {
        super("AUTHENTICATION_FAILED", message);
        this.attemptedEmail = attemptedEmail;
    }

    public String getAttemptedEmail() {
        return attemptedEmail;
    }
}

