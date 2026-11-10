package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dao.SpecialtyDao;
import com.caresync.dao.SpecialtyDaoImpl;
import com.caresync.dto.DoctorProfileUpdateDto;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.Doctor;
import com.caresync.model.Specialty;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller allowing authenticated doctors to review and update permitted professional profile fields.
 */
public class DoctorProfileServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DoctorProfileServlet.class);

    private final DoctorPortalService doctorPortalService;
    private final SpecialtyDao specialtyDao;

    public DoctorProfileServlet() {
        this(new DoctorPortalServiceImpl(), new SpecialtyDaoImpl());
    }

    public DoctorProfileServlet(DoctorPortalService doctorPortalService, SpecialtyDao specialtyDao) {
        this.doctorPortalService = doctorPortalService;
        this.specialtyDao = specialtyDao;
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

        List<Specialty> specialties = specialtyDao.findAll();

        request.setAttribute("doctor", doctorOpt.get());
        request.setAttribute("specialties", specialties);
        request.setAttribute("activeRole", "DOCTOR");
        request.setAttribute("pageTitle", "My Practice Profile — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/doctor/profile.jsp").forward(request, response);
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

        try {
            DoctorProfileUpdateDto dto = new DoctorProfileUpdateDto();
            dto.setQualification(request.getParameter("qualification"));

            String expParam = request.getParameter("experienceYears");
            if (expParam != null && !expParam.trim().isEmpty()) {
                dto.setExperienceYears(Integer.parseInt(expParam.trim()));
            } else {
                dto.setExperienceYears(doctor.getExperienceYears());
            }

            String feeParam = request.getParameter("consultationFee");
            if (feeParam != null && !feeParam.trim().isEmpty()) {
                dto.setConsultationFee(new BigDecimal(feeParam.trim()));
            } else {
                dto.setConsultationFee(doctor.getConsultationFee());
            }

            dto.setBio(request.getParameter("bio"));
            dto.setRoomNumber(request.getParameter("roomNumber"));
            dto.setPhoneNumber(request.getParameter("phoneNumber"));

            String availParam = request.getParameter("available");
            dto.setAvailable("true".equalsIgnoreCase(availParam) || "on".equalsIgnoreCase(availParam));

            String specParam = request.getParameter("specialtyId");
            if (specParam != null && !specParam.trim().isEmpty()) {
                dto.setSpecialtyId(Long.parseLong(specParam.trim()));
            } else {
                dto.setSpecialtyId(doctor.getSpecialtyId());
            }

            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            doctorPortalService.updateDoctorProfile(doctor.getDoctorProfileId(), principal.getUserId(),
                    dto, principal.getUserId(), ipAddress, userAgent);

            session.setAttribute("flashSuccess", "Your professional profile has been successfully updated.");

        } catch (Exception e) {
            logger.warn("Profile update failed for doctor ID {}: {}", doctor.getDoctorProfileId(), e.getMessage());
            session.setAttribute("flashError", e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/doctor/profile");
    }
}
