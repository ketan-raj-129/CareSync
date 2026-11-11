package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.SystemSetting;
import com.caresync.service.AdminSettingsService;
import com.caresync.service.AdminSettingsServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class AdminSettingsServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminSettingsServlet.class);

    private final AdminSettingsService settingsService;

    public AdminSettingsServlet() {
        this(new AdminSettingsServiceImpl());
    }

    public AdminSettingsServlet(AdminSettingsService settingsService) {
        this.settingsService = settingsService;
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

        List<SystemSetting> settings = settingsService.getAllSettings();

        request.setAttribute("settings", settings);
        request.setAttribute("activeRole", "ADMIN");
        request.setAttribute("activeMenu", "settings");
        request.setAttribute("pageTitle", "System Settings & Configuration — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/admin/settings.jsp").forward(request, response);
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

        String key = request.getParameter("settingKey");
        String value = request.getParameter("settingValue");

        if (key == null || value == null) {
            session.setAttribute("flashError", "Setting key and value are required.");
            response.sendRedirect(request.getContextPath() + "/admin/settings");
            return;
        }

        try {
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            boolean updated = settingsService.updateSetting(key.trim(), value.trim(), principal.getUserId(), ipAddress, userAgent);

            if (updated) {
                session.setAttribute("flashSuccess", "Configuration key '" + key + "' updated successfully.");
            } else {
                session.setAttribute("flashError", "Failed to update configuration key '" + key + "'.");
            }
        } catch (IllegalArgumentException e) {
            session.setAttribute("flashError", e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating system setting", e);
            session.setAttribute("flashError", "Error: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/settings");
    }
}

