package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.SessionPrincipal;
import com.caresync.exception.UnauthorizedException;
import com.caresync.model.Appointment;
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

import java.io.IOException;
import java.util.Optional;

/**
 * Controller presenting comprehensive appointment consultation details with doctor,
 * specialty, location, and status breakdown.
 */
public class AppointmentDetailsServlet extends HttpServlet {

    private final AppointmentService appointmentService;
    private final PatientService patientService;

    public AppointmentDetailsServlet() {
        this(new AppointmentServiceImpl(), new PatientServiceImpl());
    }

    public AppointmentDetailsServlet(AppointmentService appointmentService, PatientService patientService) {
        this.appointmentService = appointmentService;
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

        Optional<Patient> patientOpt = patientService.getPatientByUserId(principal.getUserId());
        if (patientOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Patient profile not found.");
            return;
        }

        String idParam = request.getParameter("id");
        Long appointmentId = null;
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                appointmentId = Long.parseLong(idParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        if (appointmentId == null) {
            response.sendRedirect(request.getContextPath() + "/patient/appointments");
            return;
        }

        try {
            Optional<Appointment> appointmentOpt = appointmentService.getAppointmentDetails(
                    appointmentId, patientOpt.get().getPatientProfileId());

            if (appointmentOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Appointment not found.");
                return;
            }

            if ("true".equals(request.getParameter("booked"))) {
                request.setAttribute("successMessage", "Consultation successfully reserved! Your booking reference is: " +
                        appointmentOpt.get().getReferenceNumber());
            }

            request.setAttribute("appointment", appointmentOpt.get());
            request.setAttribute("activeRole", "PATIENT");
            request.setAttribute("pageTitle", "Appointment " + appointmentOpt.get().getReferenceNumber() + " — CareSync");

            request.getRequestDispatcher("/WEB-INF/views/patient/appointment-details.jsp").forward(request, response);
        } catch (UnauthorizedException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
    }
}

