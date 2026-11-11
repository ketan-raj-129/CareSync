package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.Appointment;
import com.caresync.model.Doctor;
import com.caresync.model.Prescription;
import com.caresync.service.ClinicalService;
import com.caresync.service.ClinicalServiceImpl;
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
 * Controller displaying issued prescription registries and structured medical prescription sheets.
 */
public class DoctorPrescriptionServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DoctorPrescriptionServlet.class);

    private final ClinicalService clinicalService;
    private final DoctorPortalService doctorPortalService;
    private final DoctorAppointmentService doctorAppointmentService;

    public DoctorPrescriptionServlet() {
        this(new ClinicalServiceImpl(), new DoctorPortalServiceImpl(), new DoctorAppointmentServiceImpl());
    }

    public DoctorPrescriptionServlet(ClinicalService clinicalService,
                                    DoctorPortalService doctorPortalService,
                                    DoctorAppointmentService doctorAppointmentService) {
        this.clinicalService = clinicalService;
        this.doctorPortalService = doctorPortalService;
        this.doctorAppointmentService = doctorAppointmentService;
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
        String path = request.getServletPath();

        if (path.endsWith("/view")) {
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/doctor/prescriptions");
                return;
            }

            Long prescriptionId;
            try {
                prescriptionId = Long.parseLong(idParam.trim());
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid prescription ID.");
                return;
            }

            Optional<Prescription> rxOpt = clinicalService.getPrescriptionById(prescriptionId, doctor.getDoctorProfileId());
            if (rxOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Prescription not found.");
                return;
            }

            Prescription prescription = rxOpt.get();
            Optional<Appointment> aptOpt = doctorAppointmentService.getAppointmentDetails(
                    prescription.getAppointmentId(), doctor.getDoctorProfileId());

            request.setAttribute("doctor", doctor);
            request.setAttribute("prescription", prescription);
            request.setAttribute("appointment", aptOpt.orElse(null));
            request.setAttribute("activeRole", "DOCTOR");
            request.setAttribute("pageTitle", "Prescription #" + prescription.getId() + " — CareSync");

            request.getRequestDispatcher("/WEB-INF/views/doctor/prescription-details.jsp").forward(request, response);

        } else {
            List<Prescription> prescriptions = clinicalService.getPrescriptionsByDoctor(doctor.getDoctorProfileId());

            request.setAttribute("doctor", doctor);
            request.setAttribute("prescriptions", prescriptions);
            request.setAttribute("activeRole", "DOCTOR");
            request.setAttribute("pageTitle", "Issued Prescriptions — CareSync");

            request.getRequestDispatcher("/WEB-INF/views/doctor/prescriptions.jsp").forward(request, response);
        }
    }
}

