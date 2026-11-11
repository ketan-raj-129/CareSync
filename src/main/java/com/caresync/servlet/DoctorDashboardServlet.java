package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.DoctorDashboardDto;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.Doctor;
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
 * Controller rendering the authenticated doctor dashboard with practice metrics,
 * today's patient queue, and upcoming consultations.
 */
public class DoctorDashboardServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DoctorDashboardServlet.class);

    private final DoctorPortalService doctorPortalService;

    public DoctorDashboardServlet() {
        this(new DoctorPortalServiceImpl());
    }

    public DoctorDashboardServlet(DoctorPortalService doctorPortalService) {
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
            logger.warn("Doctor record not found for authenticated user ID: {}", principal.getUserId());
            request.setAttribute("errorMessage", "Practitioner profile not found. Please contact administration.");
            request.getRequestDispatcher("/WEB-INF/views/common/500.jsp").forward(request, response);
            return;
        }

        Doctor doctor = doctorOpt.get();
        Long doctorId = doctor.getDoctorProfileId();

        DoctorDashboardDto metrics = doctorPortalService.getDashboardMetrics(doctorId);

        request.setAttribute("doctor", doctor);
        request.setAttribute("metrics", metrics);
        request.setAttribute("activeRole", "DOCTOR");
        request.setAttribute("pageTitle", "Doctor Dashboard — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/doctor/dashboard.jsp").forward(request, response);
    }
}

