package com.caresync.filter;

import com.caresync.config.AppConstants;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.Role;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Enforces Role-Based Access Control (RBAC) across protected route hierarchies.
 * Guarantees that users cannot access resources belonging to another role.
 */
public class AuthorizationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        if (!(req instanceof HttpServletRequest request) || !(res instanceof HttpServletResponse response)) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        SessionPrincipal principal = (session != null)
                ? (SessionPrincipal) session.getAttribute(AppConstants.SESSION_USER_PRINCIPAL)
                : null;

        if (principal == null) {
            // Unauthenticated requests are handled by AuthenticationFilter
            chain.doFilter(req, res);
            return;
        }

        String path = request.getServletPath();
        Role userRole = principal.getRole();

        if (path.startsWith("/admin/") && userRole != Role.ADMIN) {
            logger.warn("RBAC Violation: User '{}' ({}) attempted access to ADMIN path: {}",
                    principal.getEmail(), userRole, path);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Administrator role required.");
            return;
        }

        if (path.startsWith("/doctor/") && userRole != Role.DOCTOR) {
            logger.warn("RBAC Violation: User '{}' ({}) attempted access to DOCTOR path: {}",
                    principal.getEmail(), userRole, path);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Doctor role required.");
            return;
        }

        if (path.startsWith("/patient/") && userRole != Role.PATIENT) {
            logger.warn("RBAC Violation: User '{}' ({}) attempted access to PATIENT path: {}",
                    principal.getEmail(), userRole, path);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Patient role required.");
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {}
}

