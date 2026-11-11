package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.Appointment;
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
import java.util.List;
import java.util.Optional;

/**
 * Controller listing and filtering patient consultations for the authenticated doctor.
 */
public class DoctorAppointmentServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DoctorAppointmentServlet.class);

    private final DoctorAppointmentService doctorAppointmentService;
    private final DoctorPortalService doctorPortalService;

    public DoctorAppointmentServlet() {
        this(new DoctorAppointmentServiceImpl(), new DoctorPortalServiceImpl());
    }

    public DoctorAppointmentServlet(DoctorAppointmentService doctorAppointmentService, DoctorPortalService doctorPortalService) {
        this.doctorAppointmentService = doctorAppointmentService;
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
        String filter = request.getParameter("filter");
        if (filter == null || filter.trim().isEmpty()) {
            filter = "ALL";
        }

        List<Appointment> appointments = doctorAppointmentService.getAppointmentsForDoctor(
                doctor.getDoctorProfileId(), filter);

        request.setAttribute("doctor", doctor);
        request.setAttribute("appointments", appointments);
        request.setAttribute("activeFilter", filter.toUpperCase());
        request.setAttribute("activeRole", "DOCTOR");
        request.setAttribute("pageTitle", "Appointments Management — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/doctor/appointments.jsp").forward(request, response);
    }
}

