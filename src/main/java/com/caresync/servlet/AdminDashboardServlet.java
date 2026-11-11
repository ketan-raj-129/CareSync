package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.AdminDashboardDto;
import com.caresync.dto.SessionPrincipal;
import com.caresync.service.AdminDashboardService;
import com.caresync.service.AdminDashboardServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class AdminDashboardServlet extends HttpServlet {

    private final AdminDashboardService dashboardService;

    public AdminDashboardServlet() {
        this(new AdminDashboardServiceImpl());
    }

    public AdminDashboardServlet(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
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

        AdminDashboardDto metrics = dashboardService.getDashboardMetrics();

        request.setAttribute("metrics", metrics);
        request.setAttribute("activeRole", "ADMIN");
        request.setAttribute("activeMenu", "dashboard");
        request.setAttribute("pageTitle", "Administration Dashboard — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
    }
}

