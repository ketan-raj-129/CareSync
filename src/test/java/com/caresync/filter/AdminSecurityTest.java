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

@DisplayName("CareSync Phase 6 Admin Security & RBAC Tests")
class AdminSecurityTest {

    @Test
    @DisplayName("Verify AuthenticationFilter redirects unauthenticated user accessing /admin/dashboard to /login")
    void testUnauthenticatedAdminAccessRedirects() throws ServletException, IOException {
        AuthenticationFilter filter = new AuthenticationFilter();

        AtomicReference<String> redirectUrl = new AtomicReference<>();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest request = createMockRequest("GET", "/admin/dashboard", null, null);
        HttpServletResponse response = createMockResponse(new AtomicInteger(0), redirectUrl);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertFalse(chainCalled.get(), "FilterChain must not proceed for unauthenticated admin route");
        assertNotNull(redirectUrl.get());
        assertTrue(redirectUrl.get().contains("/login"));
    }

    @Test
    @DisplayName("Verify AuthorizationFilter blocks PATIENT role from accessing /admin/dashboard (HTTP 403)")
    void testPatientBlockedFromAdminDashboard() throws ServletException, IOException {
        AuthorizationFilter filter = new AuthorizationFilter();

        SessionPrincipal patientPrincipal = new SessionPrincipal(
                10L, "patient@example.com", Role.PATIENT, "Patient", "User");

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(AppConstants.SESSION_USER_PRINCIPAL, patientPrincipal);

        AtomicInteger statusSent = new AtomicInteger(0);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest request = createMockRequest("GET", "/admin/dashboard", sessionAttrs, null);
        HttpServletResponse response = createMockResponse(statusSent, new AtomicReference<>());
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertFalse(chainCalled.get(), "FilterChain must not proceed for PATIENT accessing admin routes");
        assertEquals(HttpServletResponse.SC_FORBIDDEN, statusSent.get(), "Expected HTTP 403 Forbidden");
    }

    @Test
    @DisplayName("Verify AuthorizationFilter blocks DOCTOR role from accessing /admin/users (HTTP 403)")
    void testDoctorBlockedFromAdminUsers() throws ServletException, IOException {
        AuthorizationFilter filter = new AuthorizationFilter();

        SessionPrincipal doctorPrincipal = new SessionPrincipal(
                20L, "doctor@example.com", Role.DOCTOR, "Doctor", "Practitioner");

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(AppConstants.SESSION_USER_PRINCIPAL, doctorPrincipal);

        AtomicInteger statusSent = new AtomicInteger(0);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest request = createMockRequest("GET", "/admin/users", sessionAttrs, null);
        HttpServletResponse response = createMockResponse(statusSent, new AtomicReference<>());
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertFalse(chainCalled.get(), "FilterChain must not proceed for DOCTOR accessing admin routes");
        assertEquals(HttpServletResponse.SC_FORBIDDEN, statusSent.get(), "Expected HTTP 403 Forbidden");
    }

    @Test
    @DisplayName("Verify AuthorizationFilter permits ADMIN role to access /admin/dashboard")
    void testAdminPermittedToAccessAdminDashboard() throws ServletException, IOException {
        AuthorizationFilter filter = new AuthorizationFilter();

        SessionPrincipal adminPrincipal = new SessionPrincipal(
                1L, "admin@caresync.com", Role.ADMIN, "System", "Admin");

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(AppConstants.SESSION_USER_PRINCIPAL, adminPrincipal);

        AtomicInteger statusSent = new AtomicInteger(0);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest request = createMockRequest("GET", "/admin/dashboard", sessionAttrs, null);
        HttpServletResponse response = createMockResponse(statusSent, new AtomicReference<>());
        FilterChain chain = (req, res) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertTrue(chainCalled.get(), "FilterChain must proceed for authorized ADMIN");
        assertEquals(0, statusSent.get(), "No error status should be sent");
    }

    private HttpServletRequest createMockRequest(String method, String servletPath,
                                                 Map<String, Object> sessionAttrs,
                                                 Map<String, String> params) {
        HttpSession mockSession = (sessionAttrs != null) ? (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("getAttribute".equals(name) && args.length == 1) {
                        return sessionAttrs.get((String) args[0]);
                    }
                    if ("setAttribute".equals(name) && args.length == 2) {
                        sessionAttrs.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("removeAttribute".equals(name) && args.length == 1) {
                        sessionAttrs.remove((String) args[0]);
                        return null;
                    }
                    return null;
                }
        ) : null;

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("getMethod".equals(name)) return method;
                    if ("getServletPath".equals(name)) return servletPath;
                    if ("getContextPath".equals(name)) return "";
                    if ("getRequestURI".equals(name)) return servletPath;
                    if ("getSession".equals(name)) {
                        boolean create = args.length == 0 || (boolean) args[0];
                        return (mockSession != null || create) ? mockSession : null;
                    }
                    if ("getParameter".equals(name) && args.length == 1 && params != null) {
                        return params.get((String) args[0]);
                    }
                    if ("getHeader".equals(name)) return "TestAgent";
                    if ("getRemoteAddr".equals(name)) return "127.0.0.1";
                    return null;
                }
        );
    }

    private HttpServletResponse createMockResponse(AtomicInteger statusSent, AtomicReference<String> redirectUrl) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("sendRedirect".equals(name) && args.length == 1) {
                        redirectUrl.set((String) args[0]);
                        return null;
                    }
                    if ("sendError".equals(name) && args.length >= 1) {
                        statusSent.set((Integer) args[0]);
                        return null;
                    }
                    if ("setStatus".equals(name) && args.length >= 1) {
                        statusSent.set((Integer) args[0]);
                        return null;
                    }
                    return null;
                }
        );
    }
}

