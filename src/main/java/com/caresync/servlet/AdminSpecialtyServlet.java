package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.PaginatedResult;
import com.caresync.dto.SessionPrincipal;
import com.caresync.dto.SpecialtyFormDto;
import com.caresync.model.Specialty;
import com.caresync.service.AdminSpecialtyService;
import com.caresync.service.AdminSpecialtyServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AdminSpecialtyServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminSpecialtyServlet.class);

    private final AdminSpecialtyService specialtyService;

    public AdminSpecialtyServlet() {
        this(new AdminSpecialtyServiceImpl());
    }

    public AdminSpecialtyServlet(AdminSpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
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

        String path = request.getServletPath();

        if ("/admin/specialties/create".equals(path)) {
            request.setAttribute("formMode", "create");
            request.setAttribute("activeRole", "ADMIN");
            request.setAttribute("activeMenu", "specialties");
            request.setAttribute("pageTitle", "Add Medical Specialty — CareSync");
            request.getRequestDispatcher("/WEB-INF/views/admin/specialty-form.jsp").forward(request, response);
        } else if ("/admin/specialties/edit".equals(path)) {
            handleEditGet(request, response);
        } else {
            handleListGet(request, response);
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

        String path = request.getServletPath();

        if ("/admin/specialties/create".equals(path)) {
            handleCreatePost(request, response, principal, session);
        } else if ("/admin/specialties/edit".equals(path)) {
            handleEditPost(request, response, principal, session);
        } else if ("/admin/specialties/toggle".equals(path)) {
            handleTogglePost(request, response, principal, session);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/specialties");
        }
    }

    private void handleListGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String query = request.getParameter("q");
        String activeOnlyStr = request.getParameter("activeOnly");
        String pageStr = request.getParameter("page");
        String pageSizeStr = request.getParameter("pageSize");

        Boolean activeOnly = null;
        if (activeOnlyStr != null && !activeOnlyStr.trim().isEmpty()) {
            activeOnly = Boolean.parseBoolean(activeOnlyStr.trim());
        }

        int page = 1;
        int pageSize = 10;
        try {
            if (pageStr != null) page = Math.max(1, Integer.parseInt(pageStr.trim()));
            if (pageSizeStr != null) pageSize = Math.min(50, Math.max(5, Integer.parseInt(pageSizeStr.trim())));
        } catch (NumberFormatException ignored) {
        }

        PaginatedResult<Specialty> result = specialtyService.getSpecialties(query, activeOnly, page, pageSize);

        Map<Long, Long> doctorCounts = new HashMap<>();
        for (Specialty s : result.getItems()) {
            doctorCounts.put(s.getSpecialtyId(), specialtyService.getDoctorCountForSpecialty(s.getSpecialtyId()));
        }

        request.setAttribute("specialtiesResult", result);
        request.setAttribute("doctorCounts", doctorCounts);
        request.setAttribute("query", query);
        request.setAttribute("activeOnly", activeOnly);
        request.setAttribute("activeRole", "ADMIN");
        request.setAttribute("activeMenu", "specialties");
        request.setAttribute("pageTitle", "Specialty Management — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/admin/specialties.jsp").forward(request, response);
    }

    private void handleEditGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/specialties");
            return;
        }

        try {
            Long id = Long.parseLong(idStr.trim());
            Optional<Specialty> specialtyOpt = specialtyService.getSpecialtyById(id);

            if (specialtyOpt.isEmpty()) {
                request.getSession().setAttribute("flashError", "Specialty not found.");
                response.sendRedirect(request.getContextPath() + "/admin/specialties");
                return;
            }

            request.setAttribute("specialty", specialtyOpt.get());
            request.setAttribute("formMode", "edit");
            request.setAttribute("activeRole", "ADMIN");
            request.setAttribute("activeMenu", "specialties");
            request.setAttribute("pageTitle", "Edit Specialty — CareSync");

            request.getRequestDispatcher("/WEB-INF/views/admin/specialty-form.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/specialties");
        }
    }

    private void handleCreatePost(HttpServletRequest request, HttpServletResponse response,
                                  SessionPrincipal principal, HttpSession session) throws IOException {

        String name = request.getParameter("specialtyName");
        String iconName = request.getParameter("iconName");
        String description = request.getParameter("description");
        boolean isActive = "on".equalsIgnoreCase(request.getParameter("isActive")) ||
                           "true".equalsIgnoreCase(request.getParameter("isActive"));

        if (iconName == null || iconName.trim().isEmpty()) {
            iconName = "stethoscope";
        }

        SpecialtyFormDto dto = new SpecialtyFormDto(name, description, iconName.trim(), isActive);

        try {
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            specialtyService.createSpecialty(dto, principal.getUserId(), ipAddress, userAgent);
            session.setAttribute("flashSuccess", "Specialty '" + name + "' registered successfully.");
            response.sendRedirect(request.getContextPath() + "/admin/specialties");
        } catch (IllegalArgumentException e) {
            session.setAttribute("flashError", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/specialties/create");
        } catch (Exception e) {
            logger.error("Error creating specialty", e);
            session.setAttribute("flashError", "Failed to create specialty: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/specialties/create");
        }
    }

    private void handleEditPost(HttpServletRequest request, HttpServletResponse response,
                                SessionPrincipal principal, HttpSession session) throws IOException {

        String idStr = request.getParameter("specialtyId");
        String name = request.getParameter("specialtyName");
        String iconName = request.getParameter("iconName");
        String description = request.getParameter("description");
        boolean isActive = "on".equalsIgnoreCase(request.getParameter("isActive")) ||
                           "true".equalsIgnoreCase(request.getParameter("isActive"));

        if (idStr == null) {
            session.setAttribute("flashError", "Invalid specialty identifier.");
            response.sendRedirect(request.getContextPath() + "/admin/specialties");
            return;
        }

        if (iconName == null || iconName.trim().isEmpty()) {
            iconName = "stethoscope";
        }

        try {
            Long id = Long.parseLong(idStr.trim());
            SpecialtyFormDto dto = new SpecialtyFormDto(id, name, description, iconName.trim(), isActive);

            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            boolean updated = specialtyService.updateSpecialty(id, dto, principal.getUserId(), ipAddress, userAgent);
            if (updated) {
                session.setAttribute("flashSuccess", "Specialty details updated successfully.");
            } else {
                session.setAttribute("flashError", "Specialty not found or update failed.");
            }
            response.sendRedirect(request.getContextPath() + "/admin/specialties");
        } catch (IllegalArgumentException e) {
            session.setAttribute("flashError", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/specialties/edit?id=" + idStr);
        } catch (Exception e) {
            logger.error("Error updating specialty", e);
            session.setAttribute("flashError", "Failed to update specialty: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/specialties/edit?id=" + idStr);
        }
    }

    private void handleTogglePost(HttpServletRequest request, HttpServletResponse response,
                                  SessionPrincipal principal, HttpSession session) throws IOException {

        String idStr = request.getParameter("specialtyId");
        String activeStr = request.getParameter("active");

        if (idStr == null || activeStr == null) {
            session.setAttribute("flashError", "Missing toggle parameters.");
            response.sendRedirect(request.getContextPath() + "/admin/specialties");
            return;
        }

        try {
            Long id = Long.parseLong(idStr.trim());
            boolean active = Boolean.parseBoolean(activeStr.trim());

            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            boolean toggled = specialtyService.toggleSpecialtyActive(id, active, principal.getUserId(), ipAddress, userAgent);
            if (toggled) {
                session.setAttribute("flashSuccess", "Specialty active status set to " + active);
            } else {
                session.setAttribute("flashError", "Failed to toggle specialty status.");
            }
        } catch (Exception e) {
            logger.error("Error toggling specialty", e);
            session.setAttribute("flashError", "Error: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/specialties");
    }
}
