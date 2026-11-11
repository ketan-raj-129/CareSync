package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.PaginatedResult;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.Specialty;
import com.caresync.service.AdminAppointmentService;
import com.caresync.service.AdminAppointmentServiceImpl;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class AdminAppointmentServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminAppointmentServlet.class);

    private final AdminAppointmentService appointmentService;
    private final AdminSpecialtyService specialtyService;

    public AdminAppointmentServlet() {
        this(new AdminAppointmentServiceImpl(), new AdminSpecialtyServiceImpl());
    }

    public AdminAppointmentServlet(AdminAppointmentService appointmentService, AdminSpecialtyService specialtyService) {
        this.appointmentService = appointmentService;
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

        if ("/admin/appointments/view".equals(path)) {
            handleAppointmentView(request, response);
        } else {
            handleAppointmentList(request, response);
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
        if ("/admin/appointments/action".equals(path)) {
            handleAppointmentAction(request, response, principal, session);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/appointments");
        }
    }

    private void handleAppointmentList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String reference = request.getParameter("reference");
        String doctorIdStr = request.getParameter("doctorId");
        String patientIdStr = request.getParameter("patientId");
        String specialtyIdStr = request.getParameter("specialtyId");
        String statusStr = request.getParameter("status");
        String dateStr = request.getParameter("date");
        String pageStr = request.getParameter("page");
        String pageSizeStr = request.getParameter("pageSize");

        Long doctorId = parseLongOrNull(doctorIdStr);
        Long patientId = parseLongOrNull(patientIdStr);
        Long specialtyId = parseLongOrNull(specialtyIdStr);

        AppointmentStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = AppointmentStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        LocalDate date = null;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                date = LocalDate.parse(dateStr.trim());
            } catch (DateTimeParseException ignored) {
            }
        }

        int page = 1;
        int pageSize = 10;
        try {
            if (pageStr != null) page = Math.max(1, Integer.parseInt(pageStr.trim()));
            if (pageSizeStr != null) pageSize = Math.min(50, Math.max(5, Integer.parseInt(pageSizeStr.trim())));
        } catch (NumberFormatException ignored) {
        }

        PaginatedResult<Appointment> result = appointmentService.getAppointments(
                reference, doctorId, patientId, specialtyId, status, date, page, pageSize);

        List<Specialty> specialties = specialtyService.getAllSpecialties();

        request.setAttribute("appointmentsResult", result);
        request.setAttribute("specialties", specialties);
        request.setAttribute("reference", reference);
        request.setAttribute("selectedDoctorId", doctorId);
        request.setAttribute("selectedPatientId", patientId);
        request.setAttribute("selectedSpecialtyId", specialtyId);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("selectedDate", dateStr);
        request.setAttribute("activeRole", "ADMIN");
        request.setAttribute("activeMenu", "appointments");
        request.setAttribute("pageTitle", "Appointment Administration — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/admin/appointments.jsp").forward(request, response);
    }

    private void handleAppointmentView(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/appointments");
            return;
        }

        try {
            Long appointmentId = Long.parseLong(idStr.trim());
            Optional<Appointment> appointmentOpt = appointmentService.getAppointmentDetails(appointmentId);

            if (appointmentOpt.isEmpty()) {
                request.getSession().setAttribute("flashError", "Appointment not found with ID: " + appointmentId);
                response.sendRedirect(request.getContextPath() + "/admin/appointments");
                return;
            }

            request.setAttribute("appointment", appointmentOpt.get());
            request.setAttribute("activeRole", "ADMIN");
            request.setAttribute("activeMenu", "appointments");
            request.setAttribute("pageTitle", "Appointment Audit Details — CareSync");

            request.getRequestDispatcher("/WEB-INF/views/admin/appointment-details.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/appointments");
        }
    }

    private void handleAppointmentAction(HttpServletRequest request, HttpServletResponse response,
                                         SessionPrincipal principal, HttpSession session) throws IOException {

        String idStr = request.getParameter("appointmentId");
        String action = request.getParameter("action");
        String reason = request.getParameter("reason");

        if (idStr == null || action == null) {
            session.setAttribute("flashError", "Invalid appointment action request.");
            response.sendRedirect(request.getContextPath() + "/admin/appointments");
            return;
        }

        try {
            Long appointmentId = Long.parseLong(idStr.trim());
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            if ("CANCEL".equalsIgnoreCase(action.trim())) {
                if (reason == null || reason.trim().isEmpty()) {
                    session.setAttribute("flashError", "Administrative cancellation requires a non-empty reason.");
                    response.sendRedirect(request.getContextPath() + "/admin/appointments/view?id=" + appointmentId);
                    return;
                }

                boolean cancelled = appointmentService.cancelAppointment(
                        appointmentId, reason.trim(), principal.getUserId(), ipAddress, userAgent);

                if (cancelled) {
                    session.setAttribute("flashSuccess", "Appointment #" + appointmentId + " cancelled by Administrator.");
                } else {
                    session.setAttribute("flashError", "Could not cancel appointment. Check current status.");
                }
            } else {
                session.setAttribute("flashError", "Unsupported administrative action: " + action);
            }

            response.sendRedirect(request.getContextPath() + "/admin/appointments/view?id=" + appointmentId);
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Invalid appointment ID.");
            response.sendRedirect(request.getContextPath() + "/admin/appointments");
        } catch (Exception e) {
            logger.error("Error performing appointment action", e);
            session.setAttribute("flashError", "Error: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/appointments");
        }
    }

    private Long parseLongOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
