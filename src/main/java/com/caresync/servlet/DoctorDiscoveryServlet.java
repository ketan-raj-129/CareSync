package com.caresync.servlet;

import com.caresync.model.Doctor;
import com.caresync.model.Specialty;
import com.caresync.service.DoctorDiscoveryService;
import com.caresync.service.DoctorDiscoveryServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller enabling patients to search, filter, and discover active medical specialists.
 */
public class DoctorDiscoveryServlet extends HttpServlet {

    private final DoctorDiscoveryService discoveryService;

    public DoctorDiscoveryServlet() {
        this(new DoctorDiscoveryServiceImpl());
    }

    public DoctorDiscoveryServlet(DoctorDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String specialtyParam = request.getParameter("specialtyId");
        String searchQuery = request.getParameter("q");

        Long specialtyId = null;
        if (specialtyParam != null && !specialtyParam.trim().isEmpty()) {
            try {
                specialtyId = Long.parseLong(specialtyParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        List<Specialty> specialties = discoveryService.getActiveSpecialties();
        List<Doctor> doctors = discoveryService.searchDoctors(specialtyId, searchQuery);

        request.setAttribute("specialties", specialties);
        request.setAttribute("doctors", doctors);
        request.setAttribute("selectedSpecialtyId", specialtyId);
        request.setAttribute("searchQuery", searchQuery != null ? searchQuery.trim() : "");
        request.setAttribute("activeRole", "PATIENT");
        request.setAttribute("pageTitle", "Find Doctors & Specialists — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/patient/doctor-list.jsp").forward(request, response);
    }
}

