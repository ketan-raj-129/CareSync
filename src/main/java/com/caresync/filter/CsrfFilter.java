package com.caresync.filter;

import com.caresync.config.AppConstants;
import com.caresync.util.SecurityUtils;
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
import java.util.Set;

/**
 * Enforces Synchronizer Token Pattern to defend against Cross-Site Request Forgery (CSRF).
 * Automatically provisions CSRF tokens in sessions and validates state-changing HTTP methods.
 */
public class CsrfFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CsrfFilter.class);
    private static final Set<String> STATE_CHANGING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        if (!(req instanceof HttpServletRequest request) || !(res instanceof HttpServletResponse response)) {
            chain.doFilter(req, res);
            return;
        }

        String path = request.getServletPath();
        if (path != null && (path.startsWith("/assets/") || path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".ico"))) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(true);
        String sessionToken = (String) session.getAttribute(AppConstants.SESSION_CSRF_TOKEN);
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            sessionToken = SecurityUtils.rotateCsrfToken(session);
        }

        // Expose token to request attributes so JSP views can easily embed it in forms
        request.setAttribute("csrfToken", sessionToken);

        String method = request.getMethod().toUpperCase();
        if (STATE_CHANGING_METHODS.contains(method)) {
            String submittedToken = request.getParameter("csrfToken");
            if (submittedToken == null || submittedToken.trim().isEmpty()) {
                submittedToken = request.getHeader("X-CSRF-TOKEN");
            }

            if (!SecurityUtils.constantTimeEquals(sessionToken, submittedToken)) {
                logger.warn("CSRF validation rejected for {} {} from IP {}", method, path, request.getRemoteAddr());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {}
}

