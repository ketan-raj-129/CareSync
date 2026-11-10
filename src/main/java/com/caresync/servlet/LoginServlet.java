package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.AuthenticationResult;
import com.caresync.dto.SessionPrincipal;
import com.caresync.service.AuthService;
import com.caresync.service.AuthServiceImpl;
import com.caresync.util.SecurityUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controller managing user authentication, credential submission, session fixation defense,
 * and authenticated session initialization.
 */
public class LoginServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);

    private final AuthService authService;

    public LoginServlet() {
        this(new AuthServiceImpl());
    }

    public LoginServlet(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // If user is already authenticated, redirect to their role dashboard
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AppConstants.SESSION_USER_PRINCIPAL) instanceof SessionPrincipal sp) {
            response.sendRedirect(request.getContextPath() + sp.getDashboardUrl());
            return;
        }

        if ("true".equals(request.getParameter("loggedOut"))) {
            request.setAttribute("infoMessage", "You have been successfully signed out.");
        } else if ("true".equals(request.getParameter("registered"))) {
            request.setAttribute("successMessage", "Account created successfully! Please sign in with your credentials.");
        }

        request.setAttribute("pageTitle", "Sign In — CareSync");
        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String passwordStr = request.getParameter("password");
        char[] password = (passwordStr != null) ? passwordStr.toCharArray() : new char[0];

        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        AuthenticationResult result = authService.authenticate(email, password, ipAddress, userAgent);

        if (result.isSuccess()) {
            // Defend against session fixation: rotate session ID upon authentication
            request.changeSessionId();

            HttpSession session = request.getSession(true);
            session.setAttribute(AppConstants.SESSION_USER_PRINCIPAL, result.getPrincipal());

            // Rotate CSRF token for the authenticated session
            SecurityUtils.rotateCsrfToken(session);

            String redirectUrl = request.getParameter("redirect");
            if (isValidRedirect(redirectUrl, request.getContextPath())) {
                response.sendRedirect(redirectUrl);
            } else {
                response.sendRedirect(request.getContextPath() + result.getPrincipal().getDashboardUrl());
            }
        } else {
            request.setAttribute("errorMessage", result.getErrorMessage());
            request.setAttribute("preservedEmail", email != null ? email.trim() : "");
            request.setAttribute("pageTitle", "Sign In — CareSync");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
        }
    }

    private boolean isValidRedirect(String redirectUrl, String contextPath) {
        if (redirectUrl == null || redirectUrl.trim().isEmpty()) {
            return false;
        }
        // Prevent open redirect vulnerabilities: only allow local relative paths within the same context
        return redirectUrl.startsWith(contextPath + "/") && !redirectUrl.contains("//") && !redirectUrl.contains("\\");
    }
}

