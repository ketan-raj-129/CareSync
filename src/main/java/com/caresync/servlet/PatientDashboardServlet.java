package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.SessionPrincipal;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controller rendering the authenticated patient dashboard with profile overview,
 * active appointments, and quick clinical actions.
 */
public class PatientDashboardServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(PatientDashboardServlet.class);

    private final PatientService patientService;
    private final AppointmentService appointmentService;

    public PatientDashboardServlet() {
        this(new PatientServiceImpl(), new AppointmentServiceImpl());
    }

    public PatientDashboardServlet(PatientService patientService, AppointmentService appointmentService) {
        this.patientService = patientService;
        this.appointmentService = appointmentService;
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
            logger.warn("Patient record not found for authenticated user ID: {}", principal.getUserId());
            request.setAttribute("errorMessage", "Patient profile not found. Please contact hospital support.");
            request.getRequestDispatcher("/WEB-INF/views/common/500.jsp").forward(request, response);
            return;
        }

        Patient patient = patientOpt.get();
        Long patientId = patient.getPatientProfileId();

        List<Appointment> upcomingAppointments = appointmentService.getUpcomingAppointmentsForPatient(patientId);
        List<Appointment> pastAppointments = appointmentService.getPastAppointmentsForPatient(patientId);

        request.setAttribute("patient", patient);
        request.setAttribute("upcomingAppointments", upcomingAppointments);
        request.setAttribute("pastAppointments", pastAppointments);
        request.setAttribute("totalAppointments", upcomingAppointments.size() + pastAppointments.size());
        request.setAttribute("activeRole", "PATIENT");
        request.setAttribute("pageTitle", "Patient Dashboard — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/patient/dashboard.jsp").forward(request, response);
    }
}

