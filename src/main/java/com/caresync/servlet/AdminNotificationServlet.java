package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dao.NotificationDaoImpl;
import com.caresync.dao.NotificationDeliveryDaoImpl;
import com.caresync.dto.NotificationDto;
import com.caresync.dto.SessionPrincipal;
import com.caresync.model.NotificationDelivery;
import com.caresync.model.NotificationDeliveryStatus;
import com.caresync.notification.EmailNotificationProvider;
import com.caresync.service.NotificationDispatcher;
import com.caresync.service.NotificationDispatcherImpl;
import com.caresync.service.NotificationService;
import com.caresync.service.NotificationServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class AdminNotificationServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminNotificationServlet.class);

    private final NotificationService notificationService;
    private final NotificationDispatcher notificationDispatcher;

    public AdminNotificationServlet() {
        this(new NotificationServiceImpl(new NotificationDaoImpl()),
             new NotificationDispatcherImpl(new NotificationDeliveryDaoImpl(), new EmailNotificationProvider()));
    }

    public AdminNotificationServlet(NotificationService notificationService, NotificationDispatcher notificationDispatcher) {
        this.notificationService = notificationService;
        this.notificationDispatcher = notificationDispatcher;
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

        String tab = request.getParameter("tab");
        if (tab == null || tab.isBlank()) tab = "outbox";

        String statusStr = request.getParameter("status");
        NotificationDeliveryStatus statusFilter = null;
        if (statusStr != null && !statusStr.isBlank() && !"ALL".equalsIgnoreCase(statusStr)) {
            try {
                statusFilter = NotificationDeliveryStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        String typeFilter = request.getParameter("type");
        if ("ALL".equalsIgnoreCase(typeFilter)) typeFilter = null;

        int page = 1;
        int pageSize = 15;
        try {
            String pageStr = request.getParameter("page");
            if (pageStr != null && !pageStr.isBlank()) {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            }
        } catch (NumberFormatException ignored) {}

        int offset = (page - 1) * pageSize;

        if ("inapp".equalsIgnoreCase(tab)) {
            long totalCount = notificationService.countAdminNotifications(typeFilter, null);
            int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
            List<NotificationDto> notifications = notificationService.getAdminNotifications(pageSize, offset, typeFilter, null);

            request.setAttribute("notifications", notifications);
            request.setAttribute("totalCount", totalCount);
            request.setAttribute("totalPages", totalPages);
        } else {
            long totalCount = notificationDispatcher.countOutboxDeliveries(statusFilter);
            int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
            List<NotificationDelivery> deliveries = notificationDispatcher.getOutboxDeliveries(pageSize, offset, statusFilter);

            request.setAttribute("deliveries", deliveries);
            request.setAttribute("totalCount", totalCount);
            request.setAttribute("totalPages", totalPages);
        }

        request.setAttribute("activeTab", tab);
        request.setAttribute("statusFilter", statusStr != null ? statusStr : "ALL");
        request.setAttribute("typeFilter", typeFilter != null ? typeFilter : "ALL");
        request.setAttribute("currentPage", page);

        request.getRequestDispatcher("/WEB-INF/views/admin/notifications.jsp").forward(request, response);
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
        if (path == null) path = "";

        if (path.endsWith("/retry")) {
            String outboxIdStr = request.getParameter("outboxId");
            if (outboxIdStr != null && !outboxIdStr.isBlank()) {
                try {
                    Long outboxId = Long.parseLong(outboxIdStr.trim());
                    boolean success = notificationDispatcher.retryDelivery(outboxId);
                    if (session != null) {
                        if (success) {
                            session.setAttribute("flashSuccess", "Delivery outbox item #" + outboxId + " queued for immediate retry.");
                        } else {
                            session.setAttribute("flashError", "Failed to find delivery outbox item #" + outboxId);
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Invalid outbox ID format for retry: {}", outboxIdStr);
                }
            }
        }

        response.sendRedirect(request.getContextPath() + "/admin/notifications");
    }
}

