package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.PaginatedResult;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.Role;
import com.caresync.model.User;
import com.caresync.model.UserStatus;
import com.caresync.service.AdminUserService;
import com.caresync.service.AdminUserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class AdminUserServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserServlet.class);

    private final AdminUserService userService;

    public AdminUserServlet() {
        this(new AdminUserServiceImpl());
    }

    public AdminUserServlet(AdminUserService userService) {
        this.userService = userService;
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

        if ("/admin/users/view".equals(path)) {
            handleUserView(request, response);
        } else {
            handleUserList(request, response);
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

        if ("/admin/users/status".equals(path)) {
            handleStatusPost(request, response, principal, session);
        } else if ("/admin/users/role".equals(path)) {
            handleRolePost(request, response, principal, session);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/users");
        }
    }

    private void handleUserList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String query = request.getParameter("q");
        String roleStr = request.getParameter("role");
        String statusStr = request.getParameter("status");
        String pageStr = request.getParameter("page");
        String pageSizeStr = request.getParameter("pageSize");

        Role role = null;
        if (roleStr != null && !roleStr.trim().isEmpty()) {
            try {
                role = Role.valueOf(roleStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        UserStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = UserStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        int page = 1;
        int pageSize = 10;
        try {
            if (pageStr != null) page = Math.max(1, Integer.parseInt(pageStr.trim()));
            if (pageSizeStr != null) pageSize = Math.min(50, Math.max(5, Integer.parseInt(pageSizeStr.trim())));
        } catch (NumberFormatException ignored) {
        }

        PaginatedResult<User> result = userService.getUsers(query, role, status, page, pageSize);

        request.setAttribute("usersResult", result);
        request.setAttribute("query", query);
        request.setAttribute("selectedRole", role);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("activeRole", "ADMIN");
        request.setAttribute("activeMenu", "users");
        request.setAttribute("pageTitle", "User Account Governance — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(request, response);
    }

    private void handleUserView(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        try {
            Long userId = Long.parseLong(idStr.trim());
            Optional<User> userOpt = userService.getUserById(userId);

            if (userOpt.isEmpty()) {
                request.getSession().setAttribute("flashError", "User not found with ID: " + userId);
                response.sendRedirect(request.getContextPath() + "/admin/users");
                return;
            }

            request.setAttribute("userAccount", userOpt.get());
            request.setAttribute("activeRole", "ADMIN");
            request.setAttribute("activeMenu", "users");
            request.setAttribute("pageTitle", "User Account Details — CareSync");

            request.getRequestDispatcher("/WEB-INF/views/admin/user-details.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/users");
        }
    }

    private void handleStatusPost(HttpServletRequest request, HttpServletResponse response,
                                  SessionPrincipal principal, HttpSession session) throws IOException {

        String idStr = request.getParameter("userId");
        String statusStr = request.getParameter("status");

        if (idStr == null || statusStr == null) {
            session.setAttribute("flashError", "Invalid user status update request.");
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        try {
            Long userId = Long.parseLong(idStr.trim());
            UserStatus newStatus = UserStatus.valueOf(statusStr.trim().toUpperCase());

            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            boolean updated = userService.updateUserStatus(userId, newStatus, principal.getUserId(), ipAddress, userAgent);

            if (updated) {
                session.setAttribute("flashSuccess", "User account status updated to " + newStatus);
            } else {
                session.setAttribute("flashError", "Failed to update user status.");
            }

            response.sendRedirect(request.getContextPath() + "/admin/users/view?id=" + userId);
        } catch (IllegalArgumentException e) {
            session.setAttribute("flashError", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users/view?id=" + idStr);
        } catch (Exception e) {
            logger.error("Error updating user status", e);
            session.setAttribute("flashError", "Error: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users");
        }
    }

    private void handleRolePost(HttpServletRequest request, HttpServletResponse response,
                                SessionPrincipal principal, HttpSession session) throws IOException {

        String idStr = request.getParameter("userId");
        String roleStr = request.getParameter("role");

        if (idStr == null || roleStr == null) {
            session.setAttribute("flashError", "Invalid user role update request.");
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        try {
            Long userId = Long.parseLong(idStr.trim());
            Role newRole = Role.valueOf(roleStr.trim().toUpperCase());

            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            boolean updated = userService.updateUserRole(userId, newRole, principal.getUserId(), ipAddress, userAgent);

            if (updated) {
                session.setAttribute("flashSuccess", "User role changed successfully to " + newRole);
            } else {
                session.setAttribute("flashError", "Failed to update user role.");
            }

            response.sendRedirect(request.getContextPath() + "/admin/users/view?id=" + userId);
        } catch (IllegalArgumentException e) {
            session.setAttribute("flashError", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users/view?id=" + idStr);
        } catch (Exception e) {
            logger.error("Error updating user role", e);
            session.setAttribute("flashError", "Error: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users");
        }
    }
}
