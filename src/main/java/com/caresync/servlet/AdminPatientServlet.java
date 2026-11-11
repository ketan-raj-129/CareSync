package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.PaginatedResult;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.Patient;
import com.caresync.model.UserStatus;
import com.caresync.service.AdminPatientService;
import com.caresync.service.AdminPatientServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class AdminPatientServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminPatientServlet.class);

    private final AdminPatientService patientService;

    public AdminPatientServlet() {
        this(new AdminPatientServiceImpl());
    }

    public AdminPatientServlet(AdminPatientService patientService) {
        this.patientService = patientService;
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

        String path = request.getServletPath();

        if ("/admin/patients/view".equals(path)) {
            handlePatientView(request, response);
        } else {
            handlePatientList(request, response);
        }
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

        String path = request.getServletPath();
        if ("/admin/patients/status".equals(path)) {
            handleStatusUpdate(request, response, principal, session);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/patients");
        }
    }

    private void handlePatientList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String query = request.getParameter("q");
        String statusStr = request.getParameter("status");
        String pageStr = request.getParameter("page");
        String pageSizeStr = request.getParameter("pageSize");

        UserStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = UserStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        int page = 1;
        int pageSize = 10;
        try {
            if (pageStr != null) page = Math.max(1, Integer.parseInt(pageStr.trim()));
            if (pageSizeStr != null) pageSize = Math.min(50, Math.max(5, Integer.parseInt(pageSizeStr.trim())));
        } catch (NumberFormatException ignored) {
        }

        PaginatedResult<Patient> result = patientService.getPatients(query, status, page, pageSize);

        request.setAttribute("patientsResult", result);
        request.setAttribute("query", query);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("activeRole", "ADMIN");
        request.setAttribute("activeMenu", "patients");
        request.setAttribute("pageTitle", "Patient Management — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/admin/patients.jsp").forward(request, response);
    }

    private void handlePatientView(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/patients");
            return;
        }

        try {
            Long patientId = Long.parseLong(idStr.trim());
            Optional<Patient> patientOpt = patientService.getPatientDetails(patientId);

            if (patientOpt.isEmpty()) {
                request.getSession().setAttribute("flashError", "Patient not found with ID: " + patientId);
                response.sendRedirect(request.getContextPath() + "/admin/patients");
                return;
            }

            request.setAttribute("patient", patientOpt.get());
            request.setAttribute("activeRole", "ADMIN");
            request.setAttribute("activeMenu", "patients");
            request.setAttribute("pageTitle", "Patient Details — CareSync");

            request.getRequestDispatcher("/WEB-INF/views/admin/patient-details.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/patients");
        }
    }

    private void handleStatusUpdate(HttpServletRequest request, HttpServletResponse response,
                                    SessionPrincipal principal, HttpSession session) throws IOException {

        String idStr = request.getParameter("patientId");
        String statusStr = request.getParameter("status");

        if (idStr == null || statusStr == null) {
            session.setAttribute("flashError", "Invalid patient status update request.");
            response.sendRedirect(request.getContextPath() + "/admin/patients");
            return;
        }

        try {
            Long patientId = Long.parseLong(idStr.trim());
            UserStatus newStatus = UserStatus.valueOf(statusStr.trim().toUpperCase());

            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            boolean updated = patientService.updatePatientStatus(patientId, newStatus, principal.getUserId(), ipAddress, userAgent);

            if (updated) {
                session.setAttribute("flashSuccess", "Patient account status updated successfully to " + newStatus);
            } else {
                session.setAttribute("flashError", "Failed to update patient status.");
            }

            response.sendRedirect(request.getContextPath() + "/admin/patients/view?id=" + patientId);
        } catch (IllegalArgumentException e) {
            session.setAttribute("flashError", "Invalid status parameter: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/patients");
        } catch (Exception e) {
            logger.error("Error updating patient status", e);
            session.setAttribute("flashError", "An unexpected error occurred while updating patient status.");
            response.sendRedirect(request.getContextPath() + "/admin/patients");
        }
    }
}

