package com.caresync.exception;

/**
 * Thrown when an authenticated user attempts an action outside their granted role or care relationship.
 */
public class UnauthorizedException extends CareSyncException {

    private final String requiredRole;
    private final String attemptedAction;

    public UnauthorizedException(String message) {
        this(null, null, message);
    }

    public UnauthorizedException(String requiredRole, String attemptedAction, String message) {
        super("UNAUTHORIZED_ACCESS", message);
        this.requiredRole = requiredRole;
        this.attemptedAction = attemptedAction;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public String getAttemptedAction() {
        return attemptedAction;
    }
}

