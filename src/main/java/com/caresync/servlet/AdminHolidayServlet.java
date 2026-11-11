package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.ClinicHolidayRequestDto;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.Appointment;
import com.caresync.model.DoctorAvailabilityException;
import com.caresync.service.AdminHolidayService;
import com.caresync.service.AdminHolidayServiceImpl;
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

public class AdminHolidayServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminHolidayServlet.class);

    private final AdminHolidayService holidayService;

    public AdminHolidayServlet() {
        this(new AdminHolidayServiceImpl());
    }

    public AdminHolidayServlet(AdminHolidayService holidayService) {
        this.holidayService = holidayService;
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

        List<DoctorAvailabilityException> holidays = holidayService.getClinicHolidays();

        String checkDateStr = request.getParameter("checkDate");
        List<Appointment> affectedAppointments = null;
        if (checkDateStr != null && !checkDateStr.trim().isEmpty()) {
            try {
                LocalDate checkDate = LocalDate.parse(checkDateStr.trim());
                affectedAppointments = holidayService.getAffectedAppointments(checkDate);
                request.setAttribute("checkDate", checkDate);
                request.setAttribute("affectedAppointments", affectedAppointments);
            } catch (DateTimeParseException ignored) {
            }
        }

        request.setAttribute("holidays", holidays);
        request.setAttribute("activeRole", "ADMIN");
        request.setAttribute("activeMenu", "holidays");
        request.setAttribute("pageTitle", "Clinic-Wide Holiday Management — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/admin/holidays.jsp").forward(request, response);
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

        String action = request.getParameter("action");
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        if ("ADD".equalsIgnoreCase(action)) {
            handleAddHoliday(request, response, principal, session, ipAddress, userAgent);
        } else if ("DELETE".equalsIgnoreCase(action)) {
            handleDeleteHoliday(request, response, principal, session, ipAddress, userAgent);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/holidays");
        }
    }

    private void handleAddHoliday(HttpServletRequest request, HttpServletResponse response,
                                  SessionPrincipal principal, HttpSession session,
                                  String ipAddress, String userAgent) throws IOException {

        String dateStr = request.getParameter("exceptionDate");
        String reason = request.getParameter("reason");

        if (dateStr == null || dateStr.trim().isEmpty()) {
            session.setAttribute("flashError", "Holiday date is required.");
            response.sendRedirect(request.getContextPath() + "/admin/holidays");
            return;
        }

        try {
            LocalDate exceptionDate = LocalDate.parse(dateStr.trim());
            ClinicHolidayRequestDto dto = new ClinicHolidayRequestDto(exceptionDate, reason, false);

            holidayService.addClinicHoliday(dto, principal.getUserId(), ipAddress, userAgent);
            session.setAttribute("flashSuccess", "Clinic holiday for " + exceptionDate + " registered successfully.");
        } catch (DateTimeParseException e) {
            session.setAttribute("flashError", "Invalid date format. Expected YYYY-MM-DD.");
        } catch (IllegalArgumentException e) {
            session.setAttribute("flashError", e.getMessage());
        } catch (Exception e) {
            logger.error("Error declaring clinic holiday", e);
            session.setAttribute("flashError", "Failed to add holiday: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/holidays");
    }

    private void handleDeleteHoliday(HttpServletRequest request, HttpServletResponse response,
                                     SessionPrincipal principal, HttpSession session,
                                     String ipAddress, String userAgent) throws IOException {

        String idStr = request.getParameter("exceptionId");
        if (idStr == null || idStr.trim().isEmpty()) {
            session.setAttribute("flashError", "Invalid holiday identifier.");
            response.sendRedirect(request.getContextPath() + "/admin/holidays");
            return;
        }

        try {
            Long exceptionId = Long.parseLong(idStr.trim());
            boolean deleted = holidayService.deleteClinicHoliday(exceptionId, principal.getUserId(), ipAddress, userAgent);

            if (deleted) {
                session.setAttribute("flashSuccess", "Clinic holiday removed successfully.");
            } else {
                session.setAttribute("flashError", "Holiday not found or already deleted.");
            }
        } catch (Exception e) {
            logger.error("Error removing clinic holiday", e);
            session.setAttribute("flashError", "Error: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/holidays");
    }
}

