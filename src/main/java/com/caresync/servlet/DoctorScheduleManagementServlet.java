package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.Doctor;
import com.caresync.model.DoctorAvailability;
import com.caresync.model.DoctorAvailabilityException;
import com.caresync.model.ExceptionType;
import com.caresync.service.DoctorPortalService;
import com.caresync.service.DoctorPortalServiceImpl;
import com.caresync.service.DoctorScheduleService;
import com.caresync.service.DoctorScheduleServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller allowing authenticated doctors to manage recurring weekly office hours
 * and time-off / leave exceptions.
 */
public class DoctorScheduleManagementServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DoctorScheduleManagementServlet.class);

    private final DoctorScheduleService doctorScheduleService;
    private final DoctorPortalService doctorPortalService;

    public DoctorScheduleManagementServlet() {
        this(new DoctorScheduleServiceImpl(), new DoctorPortalServiceImpl());
    }

    public DoctorScheduleManagementServlet(DoctorScheduleService doctorScheduleService, DoctorPortalService doctorPortalService) {
        this.doctorScheduleService = doctorScheduleService;
        this.doctorPortalService = doctorPortalService;
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

        Optional<Doctor> doctorOpt = doctorPortalService.getDoctorByUserId(principal.getUserId());
        if (doctorOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Doctor profile not found.");
            return;
        }

        Doctor doctor = doctorOpt.get();
        List<DoctorAvailability> weeklySchedule = doctorScheduleService.getWeeklySchedule(doctor.getDoctorProfileId());
        List<DoctorAvailabilityException> exceptions = doctorScheduleService.getExceptions(doctor.getDoctorProfileId());

        request.setAttribute("doctor", doctor);
        request.setAttribute("weeklySchedule", weeklySchedule);
        request.setAttribute("exceptions", exceptions);
        request.setAttribute("activeRole", "DOCTOR");
        request.setAttribute("pageTitle", "Manage Practice Availability — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/doctor/availability.jsp").forward(request, response);
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

        Optional<Doctor> doctorOpt = doctorPortalService.getDoctorByUserId(principal.getUserId());
        if (doctorOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Doctor profile not found.");
            return;
        }

        Doctor doctor = doctorOpt.get();
        String action = request.getParameter("action");
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        try {
            if ("UPDATE_SCHEDULE".equalsIgnoreCase(action)) {
                List<DoctorAvailability> updatedList = new ArrayList<>();
                for (int day = 1; day <= 7; day++) {
                    String activeParam = request.getParameter("day_" + day + "_active");
                    boolean isActive = "true".equalsIgnoreCase(activeParam) || "on".equalsIgnoreCase(activeParam);

                    String startParam = request.getParameter("day_" + day + "_start");
                    String endParam = request.getParameter("day_" + day + "_end");
                    String durParam = request.getParameter("day_" + day + "_duration");

                    if (isActive && startParam != null && endParam != null &&
                            !startParam.trim().isEmpty() && !endParam.trim().isEmpty()) {
                        DoctorAvailability da = new DoctorAvailability();
                        da.setDoctorId(doctor.getDoctorProfileId());
                        da.setDayOfWeek(day);
                        da.setStartTime(LocalTime.parse(startParam.trim()));
                        da.setEndTime(LocalTime.parse(endParam.trim()));
                        int duration = 30;
                        if (durParam != null && !durParam.trim().isEmpty()) {
                            duration = Integer.parseInt(durParam.trim());
                        }
                        da.setSlotDurationMinutes(duration);
                        da.setActive(true);
                        updatedList.add(da);
                    } else {
                        // Inactive day
                        DoctorAvailability da = new DoctorAvailability();
                        da.setDoctorId(doctor.getDoctorProfileId());
                        da.setDayOfWeek(day);
                        da.setStartTime(LocalTime.of(9, 0));
                        da.setEndTime(LocalTime.of(17, 0));
                        da.setSlotDurationMinutes(30);
                        da.setActive(false);
                        updatedList.add(da);
                    }
                }

                doctorScheduleService.updateWeeklySchedule(doctor.getDoctorProfileId(), updatedList,
                        principal.getUserId(), ipAddress, userAgent);
                session.setAttribute("flashSuccess", "Weekly practice hours successfully updated.");

            } else if ("ADD_EXCEPTION".equalsIgnoreCase(action)) {
                String dateParam = request.getParameter("exceptionDate");
                String typeParam = request.getParameter("exceptionType");
                String fullDayParam = request.getParameter("isFullDay");
                String reason = request.getParameter("reason");

                boolean isFullDay = !"false".equalsIgnoreCase(fullDayParam) && !"0".equalsIgnoreCase(fullDayParam);

                DoctorAvailabilityException ex = new DoctorAvailabilityException();
                ex.setDoctorId(doctor.getDoctorProfileId());
                ex.setExceptionDate(LocalDate.parse(dateParam.trim()));
                ex.setFullDay(isFullDay);
                ex.setExceptionType(ExceptionType.valueOf(typeParam.trim().toUpperCase()));
                ex.setReason(reason != null ? reason.trim() : null);

                if (!isFullDay) {
                    String startParam = request.getParameter("startTime");
                    String endParam = request.getParameter("endTime");
                    if (startParam != null && endParam != null && !startParam.trim().isEmpty() && !endParam.trim().isEmpty()) {
                        ex.setStartTime(LocalTime.parse(startParam.trim()));
                        ex.setEndTime(LocalTime.parse(endParam.trim()));
                    }
                }

                doctorScheduleService.addException(doctor.getDoctorProfileId(), ex,
                        principal.getUserId(), ipAddress, userAgent);
                session.setAttribute("flashSuccess", "Time-off / leave exception added successfully.");

            } else if ("DELETE_EXCEPTION".equalsIgnoreCase(action)) {
                String idParam = request.getParameter("exceptionId");
                if (idParam != null && !idParam.trim().isEmpty()) {
                    Long exceptionId = Long.parseLong(idParam.trim());
                    doctorScheduleService.deleteException(exceptionId, doctor.getDoctorProfileId(),
                            principal.getUserId(), ipAddress, userAgent);
                    session.setAttribute("flashSuccess", "Availability exception removed.");
                }
            } else {
                session.setAttribute("flashError", "Unrecognized schedule management action.");
            }
        } catch (Exception e) {
            logger.warn("Schedule operation failed for doctor ID {}: {}", doctor.getDoctorProfileId(), e.getMessage());
            session.setAttribute("flashError", e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/doctor/availability");
    }
}

