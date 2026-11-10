package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.ConsultationRequestDto;
import com.caresync.dto.PrescriptionItemDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller providing the interactive clinical workspace for recording diagnoses,
 * symptoms, progress notes, and medication prescriptions.
 */
public class DoctorConsultationServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DoctorConsultationServlet.class);

    private final ClinicalService clinicalService;
    private final DoctorAppointmentService doctorAppointmentService;
    private final DoctorPortalService doctorPortalService;

    public DoctorConsultationServlet() {
        this(new ClinicalServiceImpl(), new DoctorAppointmentServiceImpl(), new DoctorPortalServiceImpl());
    }

    public DoctorConsultationServlet(ClinicalService clinicalService,
                                    DoctorAppointmentService doctorAppointmentService,
                                    DoctorPortalService doctorPortalService) {
        this.clinicalService = clinicalService;
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

        String idParam = request.getParameter("appointmentId");
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

            Optional<MedicalRecord> existingRecord = clinicalService.getMedicalRecordByAppointment(
                    appointmentId, doctor.getDoctorProfileId());

            Optional<Prescription> existingRx = clinicalService.getPrescriptionByAppointment(
                    appointmentId, doctor.getDoctorProfileId());

            List<MedicalRecord> patientHistory = clinicalService.getPatientMedicalHistory(
                    appointment.getPatientId(), doctor.getDoctorProfileId());

            request.setAttribute("doctor", doctor);
            request.setAttribute("appointment", appointment);
            request.setAttribute("medicalRecord", existingRecord.orElse(null));
            request.setAttribute("prescription", existingRx.orElse(null));
            request.setAttribute("patientHistory", patientHistory);
            request.setAttribute("activeRole", "DOCTOR");
            request.setAttribute("pageTitle", "Clinical Consultation — " + appointment.getReferenceNumber());

            request.getRequestDispatcher("/WEB-INF/views/doctor/consultation.jsp").forward(request, response);

        } catch (Exception e) {
            logger.warn("Error accessing consultation for appointment ID {}: {}", appointmentId, e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
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

        if (idParam == null || idParam.trim().isEmpty()) {
            session.setAttribute("flashError", "Appointment ID is required.");
            response.sendRedirect(request.getContextPath() + "/doctor/appointments");
            return;
        }

        Long appointmentId;
        try {
            appointmentId = Long.parseLong(idParam.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Invalid appointment ID format.");
            response.sendRedirect(request.getContextPath() + "/doctor/appointments");
            return;
        }

        try {
            ConsultationRequestDto dto = new ConsultationRequestDto();
            dto.setAppointmentId(appointmentId);
            dto.setSymptoms(request.getParameter("symptoms"));
            dto.setDiagnosis(request.getParameter("diagnosis"));
            dto.setTreatmentPlan(request.getParameter("treatmentPlan"));
            dto.setClinicalNotes(request.getParameter("clinicalNotes"));
            dto.setGeneralAdvice(request.getParameter("generalAdvice"));

            // Parse dynamic array parameters for prescription medications
            String[] medNames = request.getParameterValues("medicineName[]");
            String[] dosages = request.getParameterValues("dosage[]");
            String[] frequencies = request.getParameterValues("frequency[]");
            String[] durations = request.getParameterValues("duration[]");
            String[] instructions = request.getParameterValues("instructions[]");

            if (medNames != null && medNames.length > 0) {
                List<PrescriptionItemDto> items = new ArrayList<>();
                for (int i = 0; i < medNames.length; i++) {
                    String name = medNames[i];
                    if (name != null && !name.trim().isEmpty()) {
                        String dosage = (dosages != null && dosages.length > i) ? dosages[i] : "";
                        String freq = (frequencies != null && frequencies.length > i) ? frequencies[i] : "";
                        String dur = (durations != null && durations.length > i) ? durations[i] : "";
                        String inst = (instructions != null && instructions.length > i) ? instructions[i] : "";
                        items.add(new PrescriptionItemDto(name.trim(), dosage.trim(), freq.trim(), dur.trim(), inst.trim()));
                    }
                }
                dto.setPrescriptionItems(items);
            }

            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            clinicalService.recordConsultation(dto, doctor.getDoctorProfileId(),
                    principal.getUserId(), ipAddress, userAgent);

            session.setAttribute("flashSuccess", "Clinical consultation documentation and prescription successfully saved.");
            response.sendRedirect(request.getContextPath() + "/doctor/appointments/view?id=" + appointmentId);

        } catch (Exception e) {
            logger.warn("Consultation recording failed for appointment ID {}: {}", appointmentId, e.getMessage());
            session.setAttribute("flashError", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/doctor/consultation?appointmentId=" + appointmentId);
        }
    }
}
