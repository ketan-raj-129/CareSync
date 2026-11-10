package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.PatientRegistrationDto;
import com.caresync.dto.SessionPrincipal;
import com.caresync.exception.ValidationException;
import com.caresync.model.BloodGroup;
import com.caresync.model.Gender;
import com.caresync.service.AuthService;
import com.caresync.service.AuthServiceImpl;
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

/**
 * Controller managing public patient account registration.
 * Explicitly rejects any attempts to register administrator or doctor credentials.
 */
public class RegisterServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(RegisterServlet.class);

    private final AuthService authService;

    public RegisterServlet() {
        this(new AuthServiceImpl());
    }

    public RegisterServlet(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // If user is already authenticated, redirect to their role dashboard
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AppConstants.SESSION_USER_PRINCIPAL) instanceof SessionPrincipal sp) {
            response.sendRedirect(request.getContextPath() + sp.getDashboardUrl());
            return;
        }

        request.setAttribute("pageTitle", "Create Patient Account — CareSync");
        request.getRequestDispatcher("/WEB-INF/views/auth/register-patient.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String phone = request.getParameter("phone");
        String dobStr = request.getParameter("dateOfBirth");
        String genderStr = request.getParameter("gender");
        String bloodGroupStr = request.getParameter("bloodGroup");
        String address = request.getParameter("address");
        String emergencyName = request.getParameter("emergencyContactName");
        String emergencyPhone = request.getParameter("emergencyContactPhone");
        String termsConsent = request.getParameter("termsConsent");

        // Preserve form fields for repopulation on error
        request.setAttribute("preservedFirstName", firstName);
        request.setAttribute("preservedLastName", lastName);
        request.setAttribute("preservedEmail", email);
        request.setAttribute("preservedPhone", phone);
        request.setAttribute("preservedDob", dobStr);
        request.setAttribute("preservedAddress", address);
        request.setAttribute("preservedEmergencyName", emergencyName);
        request.setAttribute("preservedEmergencyPhone", emergencyPhone);

        if (password == null || confirmPassword == null || !password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Passwords do not match. Please re-enter your password.");
            request.setAttribute("pageTitle", "Create Patient Account — CareSync");
            request.getRequestDispatcher("/WEB-INF/views/auth/register-patient.jsp").forward(request, response);
            return;
        }

        PatientRegistrationDto dto = new PatientRegistrationDto();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setPassword(password.toCharArray());
        dto.setPhoneNumber(phone);
        dto.setAddress(address);
        dto.setEmergencyContactName(emergencyName);
        dto.setEmergencyContactPhone(emergencyPhone);
        dto.setTermsAccepted("on".equalsIgnoreCase(termsConsent) || "true".equalsIgnoreCase(termsConsent));

        if (dobStr != null && !dobStr.trim().isEmpty()) {
            try {
                dto.setDateOfBirth(LocalDate.parse(dobStr.trim()));
            } catch (DateTimeParseException e) {
                request.setAttribute("errorMessage", "Invalid date format for Date of Birth. Please use YYYY-MM-DD.");
                request.setAttribute("pageTitle", "Create Patient Account — CareSync");
                request.getRequestDispatcher("/WEB-INF/views/auth/register-patient.jsp").forward(request, response);
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

        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        try {
            authService.registerPatient(dto, ipAddress, userAgent);
            response.sendRedirect(request.getContextPath() + "/login?registered=true");
        } catch (ValidationException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("pageTitle", "Create Patient Account — CareSync");
            request.getRequestDispatcher("/WEB-INF/views/auth/register-patient.jsp").forward(request, response);
        } catch (Exception e) {
            logger.error("Error registering patient: {}", email, e);
            request.setAttribute("errorMessage", "Registration could not be completed at this time. Please try again.");
            request.setAttribute("pageTitle", "Create Patient Account — CareSync");
            request.getRequestDispatcher("/WEB-INF/views/auth/register-patient.jsp").forward(request, response);
        }
    }
}

