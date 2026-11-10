package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.PatientProfileUpdateDto;
import com.caresync.dto.SessionPrincipal;
import com.caresync.exception.ValidationException;
import com.caresync.model.BloodGroup;
import com.caresync.model.Gender;
import com.caresync.model.Patient;
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
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Controller allowing patients to view and update permitted demographic information.
 */
public class PatientProfileServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(PatientProfileServlet.class);

    private final PatientService patientService;

    public PatientProfileServlet() {
        this(new PatientServiceImpl());
    }

    public PatientProfileServlet(PatientService patientService) {
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
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Patient profile not found");
            return;
        }

        if ("true".equals(request.getParameter("updated"))) {
            request.setAttribute("successMessage", "Your personal profile details have been successfully updated.");
        }

        request.setAttribute("patient", patientOpt.get());
        request.setAttribute("activeRole", "PATIENT");
        request.setAttribute("pageTitle", "My Profile — CareSync");
        request.getRequestDispatcher("/WEB-INF/views/patient/profile.jsp").forward(request, response);
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

        Optional<Patient> patientOpt = patientService.getPatientByUserId(principal.getUserId());
        if (patientOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Patient profile not found");
            return;
        }

        Patient patient = patientOpt.get();

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String phone = request.getParameter("phone");
        String dobStr = request.getParameter("dateOfBirth");
        String genderStr = request.getParameter("gender");
        String bloodGroupStr = request.getParameter("bloodGroup");
        String address = request.getParameter("address");
        String emergencyName = request.getParameter("emergencyContactName");
        String emergencyPhone = request.getParameter("emergencyContactPhone");

        PatientProfileUpdateDto dto = new PatientProfileUpdateDto();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setPhoneNumber(phone);
        dto.setAddress(address);
        dto.setEmergencyContactName(emergencyName);
        dto.setEmergencyContactPhone(emergencyPhone);

        if (dobStr != null && !dobStr.trim().isEmpty()) {
            try {
                dto.setDateOfBirth(LocalDate.parse(dobStr.trim()));
            } catch (DateTimeParseException e) {
                request.setAttribute("errorMessage", "Invalid date format for Date of Birth. Use YYYY-MM-DD.");
                request.setAttribute("patient", patient);
                request.setAttribute("activeRole", "PATIENT");
                request.setAttribute("pageTitle", "My Profile — CareSync");
                request.getRequestDispatcher("/WEB-INF/views/patient/profile.jsp").forward(request, response);
                return;
            }
        }

        if (genderStr != null && !genderStr.trim().isEmpty()) {
            try {
                dto.setGender(Gender.valueOf(genderStr.trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
                dto.setGender(Gender.PREFER_NOT_TO_SAY);
            }
        }

        if (bloodGroupStr != null && !bloodGroupStr.trim().isEmpty()) {
            dto.setBloodGroup(BloodGroup.fromString(bloodGroupStr));
        }

        try {
            patientService.updatePatientProfile(
                    principal.getUserId(),
                    patient.getPatientProfileId(),
                    dto,
                    principal.getUserId(),
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent")
            );

            // Update session principal display name if first/last name changed
            SessionPrincipal updatedPrincipal = new SessionPrincipal(
                    principal.getUserId(),
                    principal.getEmail(),
                    principal.getRole(),
                    firstName,
                    lastName
            );
            session.setAttribute(AppConstants.SESSION_USER_PRINCIPAL, updatedPrincipal);

            response.sendRedirect(request.getContextPath() + "/patient/profile?updated=true");
        } catch (ValidationException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("patient", patient);
            request.setAttribute("activeRole", "PATIENT");
            request.setAttribute("pageTitle", "My Profile — CareSync");
            request.getRequestDispatcher("/WEB-INF/views/patient/profile.jsp").forward(request, response);
        } catch (Exception e) {
            logger.error("Failed to update patient profile: {}", principal.getEmail(), e);
            request.setAttribute("errorMessage", "Profile could not be updated. Please try again.");
            request.setAttribute("patient", patient);
            request.setAttribute("activeRole", "PATIENT");
            request.setAttribute("pageTitle", "My Profile — CareSync");
            request.getRequestDispatcher("/WEB-INF/views/patient/profile.jsp").forward(request, response);
        }
    }
}

