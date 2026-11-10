package com.caresync.dto;

import java.io.Serializable;

/**
 * Result container for user authentication attempts.
 */
public class AuthenticationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final SessionPrincipal principal;
    private final String errorMessage;
    private final boolean rehashNeeded;

    private AuthenticationResult(boolean success, SessionPrincipal principal, String errorMessage, boolean rehashNeeded) {
        this.success = success;
        this.principal = principal;
        this.errorMessage = errorMessage;
        this.rehashNeeded = rehashNeeded;
    }

    public static AuthenticationResult success(SessionPrincipal principal, boolean rehashNeeded) {
        return new AuthenticationResult(true, principal, null, rehashNeeded);
    }

    public static AuthenticationResult failure(String errorMessage) {
        return new AuthenticationResult(false, null, errorMessage, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public SessionPrincipal getPrincipal() {
        return principal;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isRehashNeeded() {
        return rehashNeeded;
    }
}

