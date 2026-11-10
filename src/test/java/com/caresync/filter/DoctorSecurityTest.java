package com.caresync.filter;

import com.caresync.config.AppConstants;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.Role;
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

@DisplayName("CareSync Phase 5 Doctor Security & RBAC Tests")
class DoctorSecurityTest {

    @Test
    @DisplayName("Verify AuthenticationFilter redirects unauthenticated user accessing /doctor/dashboard to /login")
    void testUnauthenticatedDoctorAccessRedirects() throws ServletException, IOException {
        AuthenticationFilter filter = new AuthenticationFilter();

        AtomicReference<String> redirectUrl = new AtomicReference<>();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest request = createMockRequest("GET", "/doctor/dashboard", null, null);
        HttpServletResponse response = createMockResponse(new AtomicInteger(0), redirectUrl);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertFalse(chainCalled.get(), "FilterChain must not proceed for unauthenticated doctor route");
        assertNotNull(redirectUrl.get());
        assertTrue(redirectUrl.get().contains("/login"));
    }

    @Test
    @DisplayName("Verify AuthorizationFilter forbids PATIENT from accessing /doctor/dashboard (HTTP 403)")
    void testPatientBlockedFromDoctorDashboard() throws ServletException, IOException {
        AuthorizationFilter filter = new AuthorizationFilter();

        SessionPrincipal patientPrincipal = new SessionPrincipal(
                10L, "patient@example.com", Role.PATIENT, "Patient", "User");

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(AppConstants.SESSION_USER_PRINCIPAL, patientPrincipal);

        AtomicInteger statusSent = new AtomicInteger(0);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest request = createMockRequest("GET", "/doctor/dashboard", sessionAttrs, null);
        HttpServletResponse response = createMockResponse(statusSent, new AtomicReference<>());
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertEquals(403, statusSent.get(), "Patient accessing /doctor/dashboard must return 403 Forbidden");
        assertFalse(chainCalled.get());
    }

    @Test
    @DisplayName("Verify AuthorizationFilter permits DOCTOR role to access /doctor/*")
    void testDoctorPermittedToDoctorRoutes() throws ServletException, IOException {
        AuthorizationFilter filter = new AuthorizationFilter();

        SessionPrincipal doctorPrincipal = new SessionPrincipal(
                20L, "doctor@caresync.com", Role.DOCTOR, "Doctor", "Who");

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(AppConstants.SESSION_USER_PRINCIPAL, doctorPrincipal);

        AtomicInteger statusSent = new AtomicInteger(0);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest request = createMockRequest("GET", "/doctor/dashboard", sessionAttrs, null);
        HttpServletResponse response = createMockResponse(statusSent, new AtomicReference<>());
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertEquals(0, statusSent.get());
        assertTrue(chainCalled.get(), "Doctor must be permitted to access doctor routes");
    }

    @Test
    @DisplayName("Verify CsrfFilter rejects POST to /doctor/consultation without valid token (HTTP 403)")
    void testCsrfEnforcedOnDoctorConsultation() throws ServletException, IOException {
        CsrfFilter filter = new CsrfFilter();

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(AppConstants.SESSION_CSRF_TOKEN, "valid_secret_token");

        AtomicInteger statusSent = new AtomicInteger(0);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        // Missing csrfToken param
        HttpServletRequest request = createMockRequest("POST", "/doctor/consultation", sessionAttrs, Map.of("appointmentId", "1"));
        HttpServletResponse response = createMockResponse(statusSent, new AtomicReference<>());
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertEquals(403, statusSent.get(), "Missing CSRF token must trigger 403 Forbidden");
        assertFalse(chainCalled.get());
    }

    // =========================================================================
    // Mock Helpers
    // =========================================================================

    private HttpServletRequest createMockRequest(String method, String path,
                                                 Map<String, Object> sessionAttrs,
                                                 Map<String, String> params) {
        HttpSession session = null;
        if (sessionAttrs != null) {
            session = (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class<?>[]{HttpSession.class},
                    (proxy, m, args) -> {
                        if ("getAttribute".equals(m.getName())) return sessionAttrs.get(args[0]);
                        if ("setAttribute".equals(m.getName())) {
                            sessionAttrs.put((String) args[0], args[1]);
                            return null;
                        }
                        return null;
                    }
            );
        }

        final HttpSession finalSession = session;
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, m, args) -> {
                    if ("getMethod".equals(m.getName())) return method;
                    if ("getServletPath".equals(m.getName())) return path;
                    if ("getRequestURI".equals(m.getName())) return "/caresync" + path;
                    if ("getContextPath".equals(m.getName())) return "/caresync";
                    if ("getParameter".equals(m.getName())) return params != null ? params.get(args[0]) : null;
                    if ("getHeader".equals(m.getName())) return null;
                    if ("getSession".equals(m.getName())) return finalSession;
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
                    if ("sendRedirect".equals(m.getName())) {
                        redirectCollector.set((String) args[0]);
                        return null;
                    }
                    return null;
                }
        );
    }
}
