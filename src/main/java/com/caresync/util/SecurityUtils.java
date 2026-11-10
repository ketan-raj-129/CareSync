package com.caresync.util;

import com.caresync.config.AppConstants;
import com.caresync.dto.SessionPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Common security and session helper utilities.
 */
public final class SecurityUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SecurityUtils() {}

    /**
     * Retrieves the authenticated SessionPrincipal from the current HTTP session if present.
     */
    public static Optional<SessionPrincipal> getAuthenticatedPrincipal(HttpServletRequest request) {
        if (request == null) return Optional.empty();
        HttpSession session = request.getSession(false);
        if (session == null) return Optional.empty();
        Object principal = session.getAttribute(AppConstants.SESSION_USER_PRINCIPAL);
        if (principal instanceof SessionPrincipal sp) {
            return Optional.of(sp);
        }
        return Optional.empty();
    }

    /**
     * Generates a new 32-byte cryptographically secure random token in hexadecimal format.
     */
    public static String generateRandomToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /**
     * Compares two tokens using constant-time byte comparison.
     */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(a.getBytes(), b.getBytes());
    }

    /**
     * Rotates and stores a new CSRF token in the session, returning the fresh token.
     */
    public static String rotateCsrfToken(HttpSession session) {
        if (session == null) return null;
        String token = generateRandomToken();
        session.setAttribute(AppConstants.SESSION_CSRF_TOKEN, token);
        return token;
    }
}

