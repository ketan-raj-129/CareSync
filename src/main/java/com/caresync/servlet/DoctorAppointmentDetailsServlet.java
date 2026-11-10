package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.Appointment;
import com.caresync.model.Doctor;
import com.caresync.model.MedicalRecord;
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
 * Controller displaying consultation encounter details, patient medical history,
 * clinical findings, and issued prescriptions.
 */
public class DoctorAppointmentDetailsServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DoctorAppointmentDetailsServlet.class);

    private final DoctorAppointmentService doctorAppointmentService;
    private final DoctorPortalService doctorPortalService;
    private final ClinicalService clinicalService;

    public DoctorAppointmentDetailsServlet() {
        this(new DoctorAppointmentServiceImpl(), new DoctorPortalServiceImpl(), new ClinicalServiceImpl());
    }

    public DoctorAppointmentDetailsServlet(DoctorAppointmentService doctorAppointmentService,
                                          DoctorPortalService doctorPortalService,
                                          ClinicalService clinicalService) {
        this.doctorAppointmentService = doctorAppointmentService;
        this.doctorPortalService = doctorPortalService;
        this.clinicalService = clinicalService;
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

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/doctor/appointments");
            return;
        }

        Long appointmentId;
        try {
            appointmentId = Long.parseLong(idParam.trim());
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid appointment ID.");
            return;
        }

        Optional<Doctor> doctorOpt = doctorPortalService.getDoctorByUserId(principal.getUserId());
        if (doctorOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Doctor profile not found.");
            return;
        }

        Doctor doctor = doctorOpt.get();

        try {
            Optional<Appointment> aptOpt = doctorAppointmentService.getAppointmentDetails(
                    appointmentId, doctor.getDoctorProfileId());

            if (aptOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Appointment record not found.");
                return;
            }

            Appointment appointment = aptOpt.get();

            Optional<MedicalRecord> medicalRecord = clinicalService.getMedicalRecordByAppointment(
                    appointmentId, doctor.getDoctorProfileId());

            Optional<Prescription> prescription = clinicalService.getPrescriptionByAppointment(
                    appointmentId, doctor.getDoctorProfileId());

            List<MedicalRecord> patientHistory = clinicalService.getPatientMedicalHistory(
                    appointment.getPatientId(), doctor.getDoctorProfileId());

            request.setAttribute("doctor", doctor);
            request.setAttribute("appointment", appointment);
            request.setAttribute("medicalRecord", medicalRecord.orElse(null));
            request.setAttribute("prescription", prescription.orElse(null));
            request.setAttribute("patientHistory", patientHistory);
            request.setAttribute("activeRole", "DOCTOR");
            request.setAttribute("pageTitle", "Encounter Details — " + appointment.getReferenceNumber());

            request.getRequestDispatcher("/WEB-INF/views/doctor/appointment-details.jsp").forward(request, response);

        } catch (Exception e) {
            logger.warn("Access violation or error loading appointment ID {}: {}", appointmentId, e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
    }
}
