package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.PaginatedResult;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.Doctor;
import com.caresync.model.Specialty;
import com.caresync.model.UserStatus;
import com.caresync.service.AdminDoctorService;
import com.caresync.service.AdminDoctorServiceImpl;
import com.caresync.service.AdminSpecialtyService;
import com.caresync.service.AdminSpecialtyServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AdminDoctorServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminDoctorServlet.class);

    private final AdminDoctorService doctorService;
    private final AdminSpecialtyService specialtyService;

    public AdminDoctorServlet() {
        this(new AdminDoctorServiceImpl(), new AdminSpecialtyServiceImpl());
    }

    public AdminDoctorServlet(AdminDoctorService doctorService, AdminSpecialtyService specialtyService) {
        this.doctorService = doctorService;
        this.specialtyService = specialtyService;
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

        if ("/admin/doctors/view".equals(path)) {
            handleDoctorView(request, response, false);
        } else if ("/admin/doctors/verification".equals(path)) {
            handleDoctorView(request, response, true);
        } else {
            handleDoctorList(request, response);
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
        if ("/admin/doctors/status".equals(path)) {
            handleDoctorStatusOrAvailability(request, response, principal, session);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/doctors");
        }
    }

    private void handleDoctorList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String query = request.getParameter("q");
        String specialtyStr = request.getParameter("specialtyId");
        String statusStr = request.getParameter("status");
        String availableStr = request.getParameter("available");
        String pageStr = request.getParameter("page");
        String pageSizeStr = request.getParameter("pageSize");

        Long specialtyId = null;
        if (specialtyStr != null && !specialtyStr.trim().isEmpty()) {
            try {
                specialtyId = Long.parseLong(specialtyStr.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        UserStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = UserStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Boolean available = null;
        if (availableStr != null && !availableStr.trim().isEmpty()) {
            available = Boolean.parseBoolean(availableStr.trim());
        }

        int page = 1;
        int pageSize = 10;
        try {
            if (pageStr != null) page = Math.max(1, Integer.parseInt(pageStr.trim()));
            if (pageSizeStr != null) pageSize = Math.min(50, Math.max(5, Integer.parseInt(pageSizeStr.trim())));
        } catch (NumberFormatException ignored) {
        }

        PaginatedResult<Doctor> result = doctorService.getDoctors(query, specialtyId, status, available, page, pageSize);
        List<Specialty> specialties = specialtyService.getAllSpecialties();

        request.setAttribute("doctorsResult", result);
        request.setAttribute("specialties", specialties);
        request.setAttribute("query", query);
        request.setAttribute("selectedSpecialtyId", specialtyId);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("selectedAvailable", available);
        request.setAttribute("activeRole", "ADMIN");
        request.setAttribute("activeMenu", "doctors");
        request.setAttribute("pageTitle", "Doctor Management & Verification — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/admin/doctors.jsp").forward(request, response);
    }

    private void handleDoctorView(HttpServletRequest request, HttpServletResponse response, boolean isVerificationView)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/doctors");
            return;
        }

        try {
            Long doctorId = Long.parseLong(idStr.trim());
            Optional<Doctor> doctorOpt = doctorService.getDoctorDetails(doctorId);

            if (doctorOpt.isEmpty()) {
                request.getSession().setAttribute("flashError", "Doctor not found with ID: " + doctorId);
                response.sendRedirect(request.getContextPath() + "/admin/doctors");
                return;
            }

            request.setAttribute("doctor", doctorOpt.get());
            request.setAttribute("activeRole", "ADMIN");
            request.setAttribute("activeMenu", "doctors");

            if (isVerificationView) {
                request.setAttribute("pageTitle", "Doctor Credential Verification — CareSync");
                request.getRequestDispatcher("/WEB-INF/views/admin/doctor-verification.jsp").forward(request, response);
            } else {
                request.setAttribute("pageTitle", "Doctor Profile Details — CareSync");
                request.getRequestDispatcher("/WEB-INF/views/admin/doctor-details.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/doctors");
        }
    }

    private void handleDoctorStatusOrAvailability(HttpServletRequest request, HttpServletResponse response,
                                                  SessionPrincipal principal, HttpSession session) throws IOException {

        String idStr = request.getParameter("doctorId");
        String statusStr = request.getParameter("status");
        String toggleAvailabilityStr = request.getParameter("toggleAvailability");
        String redirectTarget = request.getParameter("redirect");

        if (idStr == null) {
            session.setAttribute("flashError", "Invalid doctor identifier.");
            response.sendRedirect(request.getContextPath() + "/admin/doctors");
            return;
        }

        try {
            Long doctorId = Long.parseLong(idStr.trim());
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            if (toggleAvailabilityStr != null && !toggleAvailabilityStr.trim().isEmpty()) {
                boolean newAvailable = Boolean.parseBoolean(toggleAvailabilityStr.trim());
                boolean updated = doctorService.toggleDoctorAvailability(doctorId, newAvailable, principal.getUserId(), ipAddress, userAgent);
                if (updated) {
                    session.setAttribute("flashSuccess", "Doctor clinical availability set to " + (newAvailable ? "Available" : "Unavailable"));
                } else {
                    session.setAttribute("flashError", "Failed to update doctor availability.");
                }
            } else if (statusStr != null && !statusStr.trim().isEmpty()) {
                UserStatus newStatus = UserStatus.valueOf(statusStr.trim().toUpperCase());
                boolean updated = doctorService.updateDoctorStatus(doctorId, newStatus, principal.getUserId(), ipAddress, userAgent);
                if (updated) {
                    session.setAttribute("flashSuccess", "Doctor account status updated to " + newStatus);
                } else {
                    session.setAttribute("flashError", "Failed to update doctor status.");
                }
            }

            if ("verification".equals(redirectTarget)) {
                response.sendRedirect(request.getContextPath() + "/admin/doctors/verification?id=" + doctorId);
            } else if (redirectTarget != null && redirectTarget.startsWith("/")) {
                response.sendRedirect(request.getContextPath() + redirectTarget);
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/doctors/view?id=" + doctorId);
            }
        } catch (Exception e) {
            logger.error("Error updating doctor status/availability", e);
            session.setAttribute("flashError", "Error processing request: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/doctors");
        }
    }
}
