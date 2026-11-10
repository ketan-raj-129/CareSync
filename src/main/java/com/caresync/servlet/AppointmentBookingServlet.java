package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.BookingRequestDto;
import com.caresync.dto.SessionPrincipal;
import com.caresync.dto.TimeSlotDto;
import com.caresync.exception.SlotConflictException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Appointment;
import com.caresync.model.Doctor;
import com.caresync.model.Patient;
import com.caresync.service.AppointmentService;
import com.caresync.service.AppointmentServiceImpl;
import com.caresync.service.AvailabilityService;
import com.caresync.service.AvailabilityServiceImpl;
import com.caresync.service.DoctorDiscoveryService;
import com.caresync.service.DoctorDiscoveryServiceImpl;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controller managing the appointment reservation journey, slot selection,
 * double-booking prevention, and atomic database persistence.
 */
public class AppointmentBookingServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentBookingServlet.class);

    private final AppointmentService appointmentService;
    private final DoctorDiscoveryService doctorDiscoveryService;
    private final AvailabilityService availabilityService;
    private final PatientService patientService;

    public AppointmentBookingServlet() {
        this(new AppointmentServiceImpl(), new DoctorDiscoveryServiceImpl(),
             new AvailabilityServiceImpl(), new PatientServiceImpl());
    }

    public AppointmentBookingServlet(AppointmentService appointmentService,
                                     DoctorDiscoveryService doctorDiscoveryService,
                                     AvailabilityService availabilityService,
                                     PatientService patientService) {
        this.appointmentService = appointmentService;
        this.doctorDiscoveryService = doctorDiscoveryService;
        this.availabilityService = availabilityService;
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

        String doctorIdParam = request.getParameter("doctorId");
        String dateParam = request.getParameter("date");
        String timeParam = request.getParameter("time");

        Long doctorId = null;
        if (doctorIdParam != null && !doctorIdParam.trim().isEmpty()) {
            try {
                doctorId = Long.parseLong(doctorIdParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        if (doctorId == null) {
            response.sendRedirect(request.getContextPath() + "/patient/doctors");
            return;
        }

        Optional<Doctor> doctorOpt = doctorDiscoveryService.getDoctorById(doctorId);
        if (doctorOpt.isEmpty() || !doctorOpt.get().isActive()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Doctor profile not found.");
            return;
        }

        LocalDate selectedDate = null;
        if (dateParam != null && !dateParam.trim().isEmpty()) {
            try {
                selectedDate = LocalDate.parse(dateParam.trim());
            } catch (DateTimeParseException ignored) {}
        }
        if (selectedDate == null) {
            selectedDate = LocalDate.now(AvailabilityServiceImpl.CLINIC_ZONE);
        }

        Optional<Patient> patientOpt = patientService.getPatientByUserId(principal.getUserId());
        Long patientId = patientOpt.map(Patient::getPatientProfileId).orElse(null);

        List<TimeSlotDto> slots = availabilityService.getAvailableSlots(doctorId, selectedDate, patientId);

        request.setAttribute("doctor", doctorOpt.get());
        request.setAttribute("selectedDate", selectedDate);
        request.setAttribute("selectedTime", timeParam != null ? timeParam.trim() : "");
        request.setAttribute("slots", slots);
        request.setAttribute("activeRole", "PATIENT");
        request.setAttribute("pageTitle", "Book Consultation — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/patient/book-appointment.jsp").forward(request, response);
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

        // Derive patient identity strictly from authenticated session
        Optional<Patient> patientOpt = patientService.getPatientByUserId(principal.getUserId());
        if (patientOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Patient profile not found.");
            return;
        }

        Long patientId = patientOpt.get().getPatientProfileId();

        String doctorIdParam = request.getParameter("doctorId");
        String dateParam = request.getParameter("appointmentDate");
        String timeParam = request.getParameter("startTime");
        String reason = request.getParameter("reasonForVisit");

        Long doctorId = null;
        LocalDate appointmentDate = null;
        LocalTime startTime = null;

        try {
            if (doctorIdParam != null) doctorId = Long.parseLong(doctorIdParam.trim());
            if (dateParam != null) appointmentDate = LocalDate.parse(dateParam.trim());
            if (timeParam != null) startTime = LocalTime.parse(timeParam.trim());
        } catch (NumberFormatException | DateTimeParseException ignored) {}

        BookingRequestDto bookingDto = new BookingRequestDto(doctorId, appointmentDate, startTime, reason);

        try {
            Appointment appointment = appointmentService.bookAppointment(
                    patientId,
                    bookingDto,
                    principal.getUserId(),
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent")
            );

            // Post/Redirect/Get to view booking details
            response.sendRedirect(request.getContextPath() + "/patient/appointments/view?id=" +
                    appointment.getId() + "&booked=true");
        } catch (SlotConflictException e) {
            logger.warn("Slot conflict reported during booking: {}", e.getMessage());
            populateFormAndReturn(request, response, doctorId, appointmentDate, timeParam, reason,
                    "Sorry, this appointment slot has just been booked. Please select another available time.");
        } catch (ValidationException e) {
            populateFormAndReturn(request, response, doctorId, appointmentDate, timeParam, reason, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected booking error for patient {}: {}", patientId, e.getMessage(), e);
            populateFormAndReturn(request, response, doctorId, appointmentDate, timeParam, reason,
                    "Our booking service is temporarily unavailable. Please try again in a few moments.");
        }
    }

    private void populateFormAndReturn(HttpServletRequest request, HttpServletResponse response,
                                       Long doctorId, LocalDate selectedDate, String selectedTime,
                                       String reason, String errorMessage) throws ServletException, IOException {

        Optional<Doctor> doctorOpt = (doctorId != null) ? doctorDiscoveryService.getDoctorById(doctorId) : Optional.empty();
        doctorOpt.ifPresent(doctor -> request.setAttribute("doctor", doctor));

        if (selectedDate == null) {
            selectedDate = LocalDate.now(AvailabilityServiceImpl.CLINIC_ZONE);
        }

        List<TimeSlotDto> slots = (doctorId != null)
                ? availabilityService.getAvailableSlots(doctorId, selectedDate)
                : Collections.emptyList();

        request.setAttribute("selectedDate", selectedDate);
        request.setAttribute("selectedTime", selectedTime);
        request.setAttribute("preservedReason", reason);
        request.setAttribute("slots", slots);
        request.setAttribute("errorMessage", errorMessage);
        request.setAttribute("activeRole", "PATIENT");
        request.setAttribute("pageTitle", "Book Consultation — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/patient/book-appointment.jsp").forward(request, response);
    }
}

