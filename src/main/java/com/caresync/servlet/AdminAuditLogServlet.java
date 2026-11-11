package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dto.PaginatedResult;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.AuditLog;
import com.caresync.service.AdminAuditLogService;
import com.caresync.service.AdminAuditLogServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controller servlet for the central Audit Log viewer.
 * Strictly read-only to ensure tamper-evidence and audit integrity.
 */
public class AdminAuditLogServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuditLogServlet.class);

    private final AdminAuditLogService auditLogService;

    public AdminAuditLogServlet() {
        this(new AdminAuditLogServiceImpl());
    }

    public AdminAuditLogServlet(AdminAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
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

        String action = request.getParameter("action");
        String userIdStr = request.getParameter("userId");
        String targetEntity = request.getParameter("targetEntity");
        String pageStr = request.getParameter("page");
        String pageSizeStr = request.getParameter("pageSize");

        Long userId = null;
        if (userIdStr != null && !userIdStr.trim().isEmpty()) {
            try {
                userId = Long.parseLong(userIdStr.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        int page = 1;
        int pageSize = 15;
        try {
            if (pageStr != null) page = Math.max(1, Integer.parseInt(pageStr.trim()));
            if (pageSizeStr != null) pageSize = Math.min(100, Math.max(5, Integer.parseInt(pageSizeStr.trim())));
        } catch (NumberFormatException ignored) {
        }

        PaginatedResult<AuditLog> result = auditLogService.getAuditLogs(action, userId, targetEntity, page, pageSize);

        request.setAttribute("auditLogsResult", result);
        request.setAttribute("selectedAction", action);
        request.setAttribute("selectedUserId", userId);
        request.setAttribute("selectedTargetEntity", targetEntity);
        request.setAttribute("activeRole", "ADMIN");
        request.setAttribute("activeMenu", "audit-logs");
        request.setAttribute("pageTitle", "System Audit Logs & Compliance — CareSync");

        request.getRequestDispatcher("/WEB-INF/views/admin/audit-logs.jsp").forward(request, response);
    }
}

