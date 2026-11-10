package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dto.SessionPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controller handling user sign-out.
 * Strictly requires HTTP POST with CSRF verification to prevent unauthorized logout attacks.
 */
public class LogoutServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LogoutServlet.class);

    private final AuditLogDao auditLogDao;

    public LogoutServlet() {
        this(new AuditLogDaoImpl());
    }

    public LogoutServlet(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Enforce POST method for logout per security requirements
        logger.warn("GET request rejected for /logout from IP: {}. POST is strictly required.", request.getRemoteAddr());
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Logout must be performed via POST with CSRF token.");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object principalObj = session.getAttribute(AppConstants.SESSION_USER_PRINCIPAL);
            if (principalObj instanceof SessionPrincipal sp) {
                logger.info("Logging out user '{}' (ID: {})", sp.getEmail(), sp.getUserId());
                try {
                    auditLogDao.log(sp.getUserId(), "LOGOUT", "users", sp.getUserId(),
                            request.getRemoteAddr(), request.getHeader("User-Agent"), "User signed out");
                } catch (Exception e) {
                    logger.warn("Failed to audit log logout event: {}", e.getMessage());
                }
            }

            // Invalidate the session and clear authentication context
            session.invalidate();
        }

        response.sendRedirect(request.getContextPath() + "/login?loggedOut=true");
    }
}

