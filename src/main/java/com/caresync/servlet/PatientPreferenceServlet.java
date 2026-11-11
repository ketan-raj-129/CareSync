package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dao.NotificationPreferenceDaoImpl;
import com.caresync.dto.NotificationPreferenceDto;
import com.caresync.dto.SessionPrincipal;
import com.caresync.service.NotificationPreferenceService;
import com.caresync.service.NotificationPreferenceServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PatientPreferenceServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(PatientPreferenceServlet.class);

    private final NotificationPreferenceService preferenceService;

    public PatientPreferenceServlet() {
        this(new NotificationPreferenceServiceImpl(new NotificationPreferenceDaoImpl()));
    }

    public PatientPreferenceServlet(NotificationPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        SessionPrincipal principal = (session != null)
                ? (SessionPrincipal) session.getAttribute(AppConstants.SESSION_USER_PRINCIPAL)
                : null;

        if (principal == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        NotificationPreferenceDto prefs = preferenceService.getPreferences(principal.getUserId());
        request.setAttribute("preferences", prefs);

        request.getRequestDispatcher("/WEB-INF/views/patient/preferences.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        SessionPrincipal principal = (session != null)
                ? (SessionPrincipal) session.getAttribute(AppConstants.SESSION_USER_PRINCIPAL)
                : null;

        if (principal == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        boolean confirmations = "on".equalsIgnoreCase(request.getParameter("emailConfirmations")) ||
                                 "true".equalsIgnoreCase(request.getParameter("emailConfirmations"));
        boolean cancellations = "on".equalsIgnoreCase(request.getParameter("emailCancellations")) ||
                                 "true".equalsIgnoreCase(request.getParameter("emailCancellations"));
        boolean reminders = "on".equalsIgnoreCase(request.getParameter("emailReminders")) ||
                             "true".equalsIgnoreCase(request.getParameter("emailReminders"));
        boolean alerts = "on".equalsIgnoreCase(request.getParameter("emailAccountAlerts")) ||
                         "true".equalsIgnoreCase(request.getParameter("emailAccountAlerts"));

        NotificationPreferenceDto dto = new NotificationPreferenceDto();
        dto.setUserId(principal.getUserId());
        dto.setEmailConfirmations(confirmations);
        dto.setEmailCancellations(cancellations);
        dto.setEmailReminders(reminders);
        dto.setEmailAccountAlerts(alerts);

        preferenceService.updatePreferences(principal.getUserId(), dto);
        if (session != null) {
            session.setAttribute("flashSuccess", "Your communication preferences have been updated successfully.");
        }
        logger.info("Patient ID {} updated email notification preferences", principal.getUserId());

        response.sendRedirect(request.getContextPath() + "/patient/preferences");
    }
}

