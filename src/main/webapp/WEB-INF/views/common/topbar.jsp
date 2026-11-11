<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<header class="dashboard-topbar">
    <div class="topbar-left">
        <!-- Mobile Sidebar Toggle -->
        <button type="button" class="sidebar-toggle-btn" aria-label="Toggle navigation menu">
            <i data-lucide="menu"></i>
        </button>

        <!-- Breadcrumb Navigation -->
        <ul class="breadcrumb" style="margin-bottom: 0;">
            <li class="breadcrumb-item"><a href="${ctx}/">CareSync</a></li>
            <li class="breadcrumb-item"><c:out value="${activeRole != null ? activeRole : 'Portal'}" /></li>
            <li class="breadcrumb-item active">Overview</li>
        </ul>
    </div>

    <div class="topbar-right">
        <!-- Light / Dark Mode Toggle Switch -->
        <jsp:include page="/WEB-INF/views/common/theme-toggle.jsp" />

        <!-- Notification Indicator -->
        <a href="${ctx}/${activeRole == 'ADMIN' ? 'admin/notifications' : (activeRole == 'DOCTOR' ? 'doctor/notifications' : 'patient/notifications')}"
           class="btn btn-secondary btn-sm position-relative" title="Notifications" style="padding: 0.4rem; display: inline-flex; align-items: center; justify-content: center; text-decoration: none;">
            <i data-lucide="bell"></i>
            <c:if test="${not empty unreadCount && unreadCount > 0}">
                <span class="badge bg-danger rounded-pill position-absolute" style="top: -6px; right: -6px; font-size: 0.65rem; padding: 0.2rem 0.4rem;">
                    <c:out value="${unreadCount > 99 ? '99+' : unreadCount}" />
                </span>
            </c:if>
        </a>

        <!-- User Role Identity Pill -->
        <div class="user-menu-pill">
            <div class="user-avatar-circle">
                <c:choose>
                    <c:when test="${not empty sessionUser.displayName}">
                        <c:out value="${sessionUser.displayName.substring(0, 1).toUpperCase()}" />
                    </c:when>
                    <c:when test="${activeRole == 'ADMIN'}">A</c:when>
                    <c:when test="${activeRole == 'DOCTOR'}">D</c:when>
                    <c:otherwise>P</c:otherwise>
                </c:choose>
            </div>
            <span>
                <c:choose>
                    <c:when test="${not empty sessionUser.displayName}">
                        <c:out value="${sessionUser.displayName}" />
                    </c:when>
                    <c:when test="${activeRole == 'ADMIN'}">Administrator</c:when>
                    <c:when test="${activeRole == 'DOCTOR'}">Dr. Provider</c:when>
                    <c:otherwise>Patient Member</c:otherwise>
                </c:choose>
            </span>
        </div>

        <!-- Logout Form (Enforcing HTTP POST with CSRF verification) -->
        <form action="${ctx}/logout" method="POST" style="display: inline; margin: 0;">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <button type="submit" class="btn btn-secondary btn-sm" title="Sign Out" style="cursor: pointer; padding: 0.45rem 0.65rem;">
                <i data-lucide="log-out"></i>
            </button>
        </form>
    </div>
</header>
