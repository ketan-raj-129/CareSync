package com.caresync.filter;

import com.caresync.config.AppConstants;
import com.caresync.dto.SessionPrincipal;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Intercepts requests to protected resources (/admin/*, /doctor/*, /patient/*)
 * and redirects unauthenticated requests to the login page.
 */
public class AuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

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
            String targetUri = request.getRequestURI();
            String query = request.getQueryString();
            if (query != null) {
                targetUri += "?" + query;
            }
            logger.info("Unauthenticated access attempt to protected path: {}. Redirecting to /login", targetUri);
            String encodedTarget = URLEncoder.encode(targetUri, StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/login?redirect=" + encodedTarget);
            return;
        }

        // Expose principal to request scope for JSP layout components
        request.setAttribute("sessionUser", principal);
        request.setAttribute("activeRole", principal.getRole().name());

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {}
}

