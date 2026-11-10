package com.caresync.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Filter applying centralized HTTP security headers:
 * - nosniff, DENY framing, strict-origin referrer policy
 * - Content-Security-Policy tuned for Bootstrap, Lucide, and local styles
 * - no-store cache directives for sensitive/authenticated views
 */
public class SecurityHeadersFilter implements Filter {

    private static final String CSP_POLICY =
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' https://unpkg.com; " +
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
            "font-src 'self' https://fonts.gstatic.com data:; " +
            "img-src 'self' data: https:; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none';";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        if (res instanceof HttpServletResponse response) {
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            response.setHeader("Content-Security-Policy", CSP_POLICY);

            if (req instanceof HttpServletRequest request) {
                String path = request.getServletPath();
                if (isSensitivePath(path)) {
                    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    response.setHeader("Pragma", "no-cache");
                    response.setDateHeader("Expires", 0);
                }
            }
        }

        chain.doFilter(req, res);
    }

    private boolean isSensitivePath(String path) {
        if (path == null) return false;
        return path.startsWith("/admin/") ||
               path.startsWith("/doctor/") ||
               path.startsWith("/patient/") ||
               path.equals("/login") ||
               path.startsWith("/register") ||
               path.equals("/logout");
    }

    @Override
    public void destroy() {}
}

