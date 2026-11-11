package com.caresync.servlet;

import com.caresync.config.AppConstants;
import com.caresync.dao.NotificationDaoImpl;
import com.caresync.dto.NotificationDto;
import com.caresync.dto.SessionPrincipal;
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

public class DoctorNotificationServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DoctorNotificationServlet.class);

    private final NotificationService notificationService;

    public DoctorNotificationServlet() {
        this(new NotificationServiceImpl(new NotificationDaoImpl()));
    }

    public DoctorNotificationServlet(NotificationService notificationService) {
        this.notificationService = notificationService;
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

        Long userId = principal.getUserId();
        String filterParam = request.getParameter("filter");
        boolean unreadOnly = "unread".equalsIgnoreCase(filterParam);

        int page = 1;
        int pageSize = 10;
        try {
            String pageStr = request.getParameter("page");
            if (pageStr != null && !pageStr.isBlank()) {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            }
        } catch (NumberFormatException ignored) {}

        int offset = (page - 1) * pageSize;

        long totalCount = notificationService.countUserNotifications(userId, unreadOnly);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
        if (page > totalPages && totalCount > 0) {
            page = totalPages;
            offset = (page - 1) * pageSize;
        }

        List<NotificationDto> notifications = notificationService.getUserNotifications(userId, pageSize, offset, unreadOnly);
        long unreadCount = notificationService.getUnreadCount(userId);

        request.setAttribute("notifications", notifications);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("unreadCount", unreadCount);
        request.setAttribute("filter", unreadOnly ? "unread" : "all");

        request.getRequestDispatcher("/WEB-INF/views/doctor/notifications.jsp").forward(request, response);
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

        if (path.endsWith("/read-all")) {
            notificationService.markAllAsRead(principal.getUserId());
            if (session != null) {
                session.setAttribute("flashSuccess", "All notifications have been marked as read.");
            }
        } else if (path.endsWith("/read")) {
            String idStr = request.getParameter("id");
            if (idStr != null && !idStr.isBlank()) {
                try {
                    Long notifId = Long.parseLong(idStr.trim());
                    notificationService.markAsRead(notifId, principal.getUserId());
                } catch (NumberFormatException e) {
                    logger.warn("Invalid notification ID format: {}", idStr);
                }
            }
        }

        String redirectUrl = request.getContextPath() + "/doctor/notifications";
        String filter = request.getParameter("filter");
        if (filter != null && !filter.isBlank()) {
            redirectUrl += "?filter=" + filter;
        }
        response.sendRedirect(redirectUrl);
    }
}

