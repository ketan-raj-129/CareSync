package com.caresync.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controller servlet handling public navigation, authentication prototype views,
 * and dashboard preview shells.
 */
public class HomeServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(HomeServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String fullPath = path + (pathInfo != null ? pathInfo : "");

        logger.debug("Routing request for path: {}", fullPath);

        switch (fullPath) {
            case "":
            case "/":
            case "/home":
                request.setAttribute("pageTitle", "Healthcare, connected.");
                request.getRequestDispatcher("/WEB-INF/views/public/home.jsp").forward(request, response);
                break;

            case "/register-doctor":
                request.setAttribute("pageTitle", "Doctor Application - CareSync");
                request.getRequestDispatcher("/WEB-INF/views/auth/register-doctor.jsp").forward(request, response);
                break;

            case "/admin/dashboard":
                request.setAttribute("pageTitle", "Admin Dashboard - CareSync");
                request.setAttribute("activeRole", "ADMIN");
                request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
                break;

            default:
                logger.warn("Unmapped path requested: {}", fullPath);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                request.setAttribute("pageTitle", "Page Not Found - CareSync");
                request.getRequestDispatcher("/WEB-INF/views/common/404.jsp").forward(request, response);
                break;
        }
    }
}

