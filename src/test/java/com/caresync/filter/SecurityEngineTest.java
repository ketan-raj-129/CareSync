package com.caresync.filter;

import com.caresync.config.AppConstants;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.Role;
import com.caresync.util.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CareSync Security Engine, Filters, CSRF, and RBAC Tests")
class SecurityEngineTest {

    @Test
    @DisplayName("Verify CsrfFilter rejects POST requests with missing token (HTTP 403)")
    void testCsrfRejectionWhenMissingToken() throws ServletException, IOException {
        CsrfFilter filter = new CsrfFilter();

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(AppConstants.SESSION_CSRF_TOKEN, "valid_token_12345");

        AtomicInteger statusSent = new AtomicInteger(0);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest request = createMockRequest("POST", "/patient/book", sessionAttrs, null, null);
        HttpServletResponse response = createMockResponse(statusSent, null);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertEquals(403, statusSent.get(), "POST without CSRF token must respond with 403 Forbidden");
        assertFalse(chainCalled.get(), "Filter chain must not proceed when CSRF validation fails");
    }

    @Test
    @DisplayName("Verify CsrfFilter rejects POST requests with invalid token (HTTP 403)")
    void testCsrfRejectionWhenInvalidToken() throws ServletException, IOException {
        CsrfFilter filter = new CsrfFilter();

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(AppConstants.SESSION_CSRF_TOKEN, "valid_token_12345");

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "attacker_tampered_token");

        AtomicInteger statusSent = new AtomicInteger(0);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest request = createMockRequest("POST", "/patient/book", sessionAttrs, params, null);
        HttpServletResponse response = createMockResponse(statusSent, null);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertEquals(403, statusSent.get(), "POST with mismatched CSRF token must respond with 403 Forbidden");
        assertFalse(chainCalled.get());
    }

    @Test
    @DisplayName("Verify CsrfFilter allows POST requests with valid matching token")
    void testCsrfAcceptanceWhenValidToken() throws ServletException, IOException {
        CsrfFilter filter = new CsrfFilter();

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(AppConstants.SESSION_CSRF_TOKEN, "correct_secret_token");

        Map<String, String> params = new HashMap<>();
        params.put("csrfToken", "correct_secret_token");

        AtomicInteger statusSent = new AtomicInteger(0);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest request = createMockRequest("POST", "/patient/book", sessionAttrs, params, null);
        HttpServletResponse response = createMockResponse(statusSent, null);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertEquals(0, statusSent.get(), "No error should be sent");
        assertTrue(chainCalled.get(), "Filter chain must proceed when CSRF token matches");
    }

    @Test
    @DisplayName("Verify AuthenticationFilter redirects unauthenticated requests to /login")
    void testAuthenticationFilterRedirectsUnauthenticated() throws ServletException, IOException {
        AuthenticationFilter filter = new AuthenticationFilter();

        Map<String, Object> sessionAttrs = new HashMap<>(); // Empty session, no principal
        AtomicReference<String> redirectedUrl = new AtomicReference<>();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest request = createMockRequest("GET", "/doctor/dashboard", sessionAttrs, null, null);
        HttpServletResponse response = createMockResponse(new AtomicInteger(0), redirectedUrl);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertNotNull(redirectedUrl.get(), "Should redirect unauthenticated request");
        assertTrue(redirectedUrl.get().contains("/login"), "Redirect destination must be login page");
        assertFalse(chainCalled.get(), "Chain must not proceed for unauthenticated user");
    }

    @Test
    @DisplayName("Verify AuthorizationFilter enforces RBAC and rejects cross-role access (HTTP 403)")
    void testRbacRejection() throws ServletException, IOException {
        AuthorizationFilter filter = new AuthorizationFilter();

        // Logged-in user has Role.PATIENT
        SessionPrincipal patientPrincipal = new SessionPrincipal(1L, "patient@test.com", Role.PATIENT, "Pat", "User");
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(AppConstants.SESSION_USER_PRINCIPAL, patientPrincipal);

        AtomicInteger statusSent = new AtomicInteger(0);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        // Patient attempting to access /admin/dashboard
        HttpServletRequest request = createMockRequest("GET", "/admin/dashboard", sessionAttrs, null, null);
        HttpServletResponse response = createMockResponse(statusSent, null);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertEquals(403, statusSent.get(), "Patient accessing admin path must receive 403 Forbidden");
        assertFalse(chainCalled.get(), "Filter chain must not proceed on RBAC failure");
    }

    @Test
    @DisplayName("Verify AuthorizationFilter permits authorized role access")
    void testRbacPermitsAuthorizedRole() throws ServletException, IOException {
        AuthorizationFilter filter = new AuthorizationFilter();

        // Logged-in user has Role.ADMIN
        SessionPrincipal adminPrincipal = new SessionPrincipal(1L, "admin@caresync.com", Role.ADMIN, "Super", "Admin");
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(AppConstants.SESSION_USER_PRINCIPAL, adminPrincipal);

        AtomicInteger statusSent = new AtomicInteger(0);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest request = createMockRequest("GET", "/admin/dashboard", sessionAttrs, null, null);
        HttpServletResponse response = createMockResponse(statusSent, null);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertEquals(0, statusSent.get());
        assertTrue(chainCalled.get(), "Admin accessing /admin/dashboard must be allowed through");
    }

    @Test
    @DisplayName("Verify SecurityHeadersFilter adds nosniff, DENY, and CSP headers")
    void testSecurityHeaders() throws ServletException, IOException {
        SecurityHeadersFilter filter = new SecurityHeadersFilter();

        Map<String, String> responseHeaders = new HashMap<>();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest request = createMockRequest("GET", "/home", new HashMap<>(), null, null);
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setHeader".equals(method.getName()) && args.length >= 2) {
                        responseHeaders.put((String) args[0], (String) args[1]);
                    }
                    return null;
                }
        );
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertEquals("nosniff", responseHeaders.get("X-Content-Type-Options"));
        assertEquals("DENY", responseHeaders.get("X-Frame-Options"));
        assertEquals("strict-origin-when-cross-origin", responseHeaders.get("Referrer-Policy"));
        assertNotNull(responseHeaders.get("Content-Security-Policy"));
        assertTrue(chainCalled.get());
    }

    // =========================================================================
    // Mock Helpers
    // =========================================================================

    private HttpServletRequest createMockRequest(String method, String path,
                                                 Map<String, Object> sessionAttrs,
                                                 Map<String, String> params,
                                                 Map<String, String> headers) {

        Map<String, Object> requestAttrs = new HashMap<>();

        HttpSession session = (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, m, args) -> {
                    if ("getAttribute".equals(m.getName())) return sessionAttrs.get(args[0]);
                    if ("setAttribute".equals(m.getName())) {
                        sessionAttrs.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("getId".equals(m.getName())) return "session_id_xyz";
                    return null;
                }
        );

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, m, args) -> {
                    if ("getMethod".equals(m.getName())) return method;
                    if ("getServletPath".equals(m.getName())) return path;
                    if ("getRequestURI".equals(m.getName())) return "/caresync" + path;
                    if ("getContextPath".equals(m.getName())) return "/caresync";
                    if ("getQueryString".equals(m.getName())) return null;
                    if ("getParameter".equals(m.getName())) {
                        return params != null ? params.get(args[0]) : null;
                    }
                    if ("getHeader".equals(m.getName())) {
                        return headers != null ? headers.get(args[0]) : null;
                    }
                    if ("getSession".equals(m.getName())) return session;
                    if ("getAttribute".equals(m.getName())) return requestAttrs.get(args[0]);
                    if ("setAttribute".equals(m.getName())) {
                        requestAttrs.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("getRemoteAddr".equals(m.getName())) return "127.0.0.1";
                    return null;
                }
        );
    }

    private HttpServletResponse createMockResponse(AtomicInteger statusCollector, AtomicReference<String> redirectCollector) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, m, args) -> {
                    if ("sendError".equals(m.getName())) {
                        statusCollector.set((int) args[0]);
                        return null;
                    }
                    if ("setStatus".equals(m.getName())) {
                        statusCollector.set((int) args[0]);
                        return null;
                    }
                    if ("sendRedirect".equals(m.getName())) {
                        if (redirectCollector != null) {
                            redirectCollector.set((String) args[0]);
                        }
                        return null;
                    }
                    return null;
                }
        );
    }
}

