package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.SessionPrincipal;
import com.caresync.dto.TimeSlotDto;
import com.caresync.model.Patient;
import com.caresync.service.AvailabilityService;
import com.caresync.service.AvailabilityServiceImpl;
import com.caresync.service.PatientService;
import com.caresync.service.PatientServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * RESTful endpoint providing computed discrete time slots for a specified physician and date.
 * Serializes slot metadata into JSON format.
 */
public class DoctorAvailabilityServlet extends HttpServlet {

    private final AvailabilityService availabilityService;
    private final PatientService patientService;

    public DoctorAvailabilityServlet() {
        this(new AvailabilityServiceImpl(), new PatientServiceImpl());
    }

    public DoctorAvailabilityServlet(AvailabilityService availabilityService, PatientService patientService) {
        this.availabilityService = availabilityService;
        this.patientService = patientService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String doctorIdParam = request.getParameter("doctorId");
        String dateParam = request.getParameter("date");

        Long doctorId = null;
        LocalDate date = null;

        try {
            if (doctorIdParam != null) doctorId = Long.parseLong(doctorIdParam.trim());
            if (dateParam != null) date = LocalDate.parse(dateParam.trim());
        } catch (NumberFormatException | DateTimeParseException ignored) {}

        if (doctorId == null || date == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"doctorId and valid date (YYYY-MM-DD) are required\"}");
            return;
        }

        // Identify current patient if authenticated to validate against patient's concurrent schedule
        Long patientId = null;
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AppConstants.SESSION_USER_PRINCIPAL) instanceof SessionPrincipal sp) {
            Optional<Patient> pOpt = patientService.getPatientByUserId(sp.getUserId());
            if (pOpt.isPresent()) {
                patientId = pOpt.get().getPatientProfileId();
            }
        }

        List<TimeSlotDto> slots = availabilityService.getAvailableSlots(doctorId, date, patientId);

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < slots.size(); i++) {
            TimeSlotDto slot = slots.get(i);
            json.append("{")
                .append("\"startTime\":\"").append(slot.getStartTime()).append("\",")
                .append("\"endTime\":\"").append(slot.getEndTime()).append("\",")
                .append("\"formattedRange\":\"").append(escapeJson(slot.getFormattedRange())).append("\",")
                .append("\"available\":").append(slot.isAvailable()).append(",")
                .append("\"reason\":").append(slot.getReason() != null ? "\"" + escapeJson(slot.getReason()) + "\"" : "null")
                .append("}");
            if (i < slots.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        PrintWriter out = response.getWriter();
        out.print(json.toString());
        out.flush();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

