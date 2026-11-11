package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.Doctor;
import com.caresync.service.DoctorAppointmentService;
import com.caresync.service.DoctorAppointmentServiceImpl;
import com.caresync.service.DoctorPortalService;
import com.caresync.service.DoctorPortalServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller handling state-changing lifecycle actions on appointments by the authenticated doctor.
 * Strictly accepts POST requests protected with CSRF synchronizer tokens.
 */
public class DoctorAppointmentActionServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DoctorAppointmentActionServlet.class);

    private final DoctorAppointmentService doctorAppointmentService;
    private final DoctorPortalService doctorPortalService;

    public DoctorAppointmentActionServlet() {
        this(new DoctorAppointmentServiceImpl(), new DoctorPortalServiceImpl());
    }

    public DoctorAppointmentActionServlet(DoctorAppointmentService doctorAppointmentService,
                                         DoctorPortalService doctorPortalService) {
        this.doctorAppointmentService = doctorAppointmentService;
        this.doctorPortalService = doctorPortalService;
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
        String idParam = request.getParameter("appointmentId");
        String action = request.getParameter("action");
        String reason = request.getParameter("reason");

        if (idParam == null || action == null) {
            response.sendRedirect(request.getContextPath() + "/doctor/appointments");
            return;
        }

        Long appointmentId;
        try {
            appointmentId = Long.parseLong(idParam.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Invalid appointment ID specified.");
            response.sendRedirect(request.getContextPath() + "/doctor/appointments");
            return;
        }

        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        try {
            switch (action.trim().toUpperCase()) {
                case "CONFIRM":
                    doctorAppointmentService.confirmAppointment(appointmentId, doctor.getDoctorProfileId(),
                            principal.getUserId(), ipAddress, userAgent);
                    session.setAttribute("flashSuccess", "Appointment has been confirmed.");
                    break;

                case "REJECT":
                    doctorAppointmentService.rejectAppointment(appointmentId, doctor.getDoctorProfileId(),
                            reason, principal.getUserId(), ipAddress, userAgent);
                    session.setAttribute("flashSuccess", "Appointment request has been declined.");
                    break;

                case "COMPLETE":
                    doctorAppointmentService.completeAppointment(appointmentId, doctor.getDoctorProfileId(),
                            principal.getUserId(), ipAddress, userAgent);
                    session.setAttribute("flashSuccess", "Consultation encounter marked as completed.");
                    break;

                case "CANCEL":
                    doctorAppointmentService.cancelAppointmentByDoctor(appointmentId, doctor.getDoctorProfileId(),
                            reason, principal.getUserId(), ipAddress, userAgent);
                    session.setAttribute("flashSuccess", "Appointment has been cancelled.");
                    break;

                default:
                    session.setAttribute("flashError", "Unsupported appointment action: " + action);
                    break;
            }
        } catch (Exception e) {
            logger.warn("Action '{}' failed on appointment ID {}: {}", action, appointmentId, e.getMessage());
            session.setAttribute("flashError", e.getMessage());
        }

        String redirect = request.getParameter("redirect");
        if (redirect != null && !redirect.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + redirect.trim());
        } else {
            response.sendRedirect(request.getContextPath() + "/doctor/appointments/view?id=" + appointmentId);
        }
    }
}

