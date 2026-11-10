package com.caresync.servlet;

import com.caresync.model.Doctor;
import com.caresync.service.DoctorDiscoveryService;
import com.caresync.service.DoctorDiscoveryServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller displaying an individual physician's credentials, bio, specialty, and consultation fee.
 */
public class DoctorDetailsServlet extends HttpServlet {

    private final DoctorDiscoveryService discoveryService;

    public DoctorDetailsServlet() {
        this(new DoctorDiscoveryServiceImpl());
    }

    public DoctorDetailsServlet(DoctorDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        Long doctorId = null;
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                doctorId = Long.parseLong(idParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        if (doctorId == null) {
            response.sendRedirect(request.getContextPath() + "/patient/doctors");
            return;
        }

        Optional<Doctor> doctorOpt = discoveryService.getDoctorById(doctorId);
        if (doctorOpt.isEmpty() || !doctorOpt.get().isActive()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Doctor profile not found or inactive.");
            return;
        }

        request.setAttribute("doctor", doctorOpt.get());
        request.setAttribute("activeRole", "PATIENT");
        request.setAttribute("pageTitle", "Dr. " + doctorOpt.get().getFullName() + " — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/patient/doctor-details.jsp").forward(request, response);
    }
}

