package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.SessionPrincipal;
import com.caresync.exception.UnauthorizedException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Patient;
import com.caresync.service.AppointmentService;
import com.caresync.service.AppointmentServiceImpl;
import com.caresync.service.PatientService;
import com.caresync.service.PatientServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Controller executing transactional appointment cancellations.
 * Strictly requires HTTP POST with CSRF verification and patient ownership enforcement.
 */
public class AppointmentCancellationServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentCancellationServlet.class);

    private final AppointmentService appointmentService;
    private final PatientService patientService;

    public AppointmentCancellationServlet() {
        this(new AppointmentServiceImpl(), new PatientServiceImpl());
    }

    public AppointmentCancellationServlet(AppointmentService appointmentService, PatientService patientService) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Appointment cancellation requires HTTP POST.");
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

        Optional<Patient> patientOpt = patientService.getPatientByUserId(principal.getUserId());
        if (patientOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Patient profile not found.");
            return;
        }

        Long patientId = patientOpt.get().getPatientProfileId();

        String appointmentIdParam = request.getParameter("appointmentId");
        String reason = request.getParameter("cancellationReason");

        Long appointmentId = null;
        if (appointmentIdParam != null && !appointmentIdParam.trim().isEmpty()) {
            try {
                appointmentId = Long.parseLong(appointmentIdParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        if (appointmentId == null) {
            response.sendRedirect(request.getContextPath() + "/patient/appointments");
            return;
        }

        try {
            appointmentService.cancelAppointment(
                    appointmentId,
                    patientId,
                    reason,
                    principal.getUserId(),
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent")
            );

            response.sendRedirect(request.getContextPath() + "/patient/appointments?cancelled=true");
        } catch (UnauthorizedException e) {
            logger.warn("Unauthorized cancellation attempt by user {}: {}", principal.getUserId(), e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (ValidationException e) {
            logger.warn("Validation error cancelling appointment {}: {}", appointmentId, e.getMessage());
            String encoded = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/patient/appointments/view?id=" + appointmentId + "&error=" + encoded);
        } catch (Exception e) {
            logger.error("Failed to cancel appointment {}: {}", appointmentId, e.getMessage(), e);
            String encoded = URLEncoder.encode("Cancellation could not be completed. Please try again.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/patient/appointments/view?id=" + appointmentId + "&error=" + encoded);
        }
    }
}

