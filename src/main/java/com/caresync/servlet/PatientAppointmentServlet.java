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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controller displaying the authenticated patient's chronological appointment consultations.
 */
public class PatientAppointmentServlet extends HttpServlet {

    private final AppointmentService appointmentService;
    private final PatientService patientService;

    public PatientAppointmentServlet() {
        this(new AppointmentServiceImpl(), new PatientServiceImpl());
    }

    public PatientAppointmentServlet(AppointmentService appointmentService, PatientService patientService) {
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

        Long patientId = patientOpt.get().getPatientProfileId();

        if ("true".equals(request.getParameter("cancelled"))) {
            request.setAttribute("successMessage", "Your appointment has been successfully cancelled.");
        }

        List<Appointment> upcoming = appointmentService.getUpcomingAppointmentsForPatient(patientId);
        List<Appointment> past = appointmentService.getPastAppointmentsForPatient(patientId);

        request.setAttribute("upcomingAppointments", upcoming);
        request.setAttribute("pastAppointments", past);
        request.setAttribute("activeRole", "PATIENT");
        request.setAttribute("pageTitle", "My Appointments — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/patient/appointment-list.jsp").forward(request, response);
    }
}

