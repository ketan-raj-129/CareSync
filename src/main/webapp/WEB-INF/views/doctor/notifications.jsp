<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="DOCTOR" />
<c:set var="activeMenu" value="notifications" />
<c:set var="pageTitle" value="Doctor Notifications — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: var(--space-4);">
                <div>
                    <h1 class="page-title">Practice Notifications</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Clinical alerts, new booking requests from patients, and schedule changes
                    </p>
                </div>
                <div style="display: flex; gap: var(--space-3); align-items: center;">
                    <a href="${ctx}/doctor/preferences" class="btn btn-secondary">
                        <i data-lucide="settings"></i>
                        <span>Alert Preferences</span>
                    </a>
                    <c:if test="${not empty unreadCount && unreadCount > 0}">
                        <form action="${ctx}/doctor/notifications/read-all" method="POST" style="margin: 0;">
                            <input type="hidden" name="csrfToken" value="${csrfToken}" />
                            <input type="hidden" name="filter" value="${filter}" />
                            <button type="submit" class="btn btn-primary">
                                <i data-lucide="check-check"></i>
                                <span>Mark All Read</span>
                            </button>
                        </form>
                    </c:if>
                </div>
            </div>

            <c:if test="${not empty sessionScope.flashSuccess}">
                <div class="alert alert-success" style="display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-5);">
                    <i data-lucide="check-circle" style="color: var(--success); flex-shrink: 0;"></i>
                    <span style="font-weight: 500;"><c:out value="${sessionScope.flashSuccess}" /></span>
                </div>
                <c:remove var="flashSuccess" scope="session" />
            </c:if>

            <!-- Filter Tabs -->
            <div style="display: flex; gap: var(--space-2); margin-bottom: var(--space-5); border-bottom: 1px solid var(--border-color); padding-bottom: var(--space-3);">
                <a href="${ctx}/doctor/notifications"
                   class="btn btn-sm ${filter == 'all' ? 'btn-primary' : 'btn-secondary'}" style="text-decoration: none;">
                    All Notifications
                </a>
                <a href="${ctx}/doctor/notifications?filter=unread"
                   class="btn btn-sm ${filter == 'unread' ? 'btn-primary' : 'btn-secondary'}" style="text-decoration: none; display: flex; align-items: center; gap: var(--space-2);">
                    Unread Only
                    <c:if test="${not empty unreadCount && unreadCount > 0}">
                        <span class="badge bg-danger text-white rounded-pill" style="font-size: 0.75rem; padding: 0.15rem 0.5rem;">
                            <c:out value="${unreadCount}" />
                        </span>
                    </c:if>
                </a>
            </div>

            <!-- Notification List -->
            <div class="card" style="padding: 0; overflow: hidden;">
                <c:choose>
                    <c:when test="${empty notifications}">
                        <div style="text-align: center; padding: var(--space-8) var(--space-4);">
                            <div style="width: 56px; height: 56px; border-radius: 50%; background-color: var(--color-teal-50, #E9F3F2); color: var(--primary, #176E65); display: flex; align-items: center; justify-content: center; margin: 0 auto var(--space-4);">
                                <i data-lucide="bell-off" style="width: 28px; height: 28px;"></i>
                            </div>
                            <h3 style="margin: 0 0 var(--space-2); font-size: 1.125rem;">No notifications found</h3>
                            <p style="color: var(--text-secondary); margin: 0;">
                                <c:choose>
                                    <c:when test="${filter == 'unread'}">All practitioner notifications have been reviewed.</c:when>
                                    <c:otherwise>No clinical notifications recorded in your inbox yet.</c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <ul style="list-style: none; margin: 0; padding: 0;">
                            <c:forEach var="item" items="${notifications}">
                                <li style="padding: var(--space-4) var(--space-5); border-bottom: 1px solid var(--border-color); display: flex; gap: var(--space-4); align-items: flex-start; background-color: ${item.read ? 'transparent' : 'rgba(23, 110, 101, 0.04)'}; transition: background-color 0.2s;">
                                    <div style="width: 40px; height: 40px; border-radius: 8px; flex-shrink: 0; display: flex; align-items: center; justify-content: center;
                                         background-color: ${item.notificationType == 'APPOINTMENT_CONFIRMED' ? 'rgba(16, 185, 129, 0.12)' : (item.notificationType == 'APPOINTMENT_REJECTED' || item.notificationType == 'APPOINTMENT_CANCELLED' ? 'rgba(239, 68, 68, 0.12)' : 'rgba(23, 110, 101, 0.12)'))};
                                         color: ${item.notificationType == 'APPOINTMENT_CONFIRMED' ? '#10B981' : (item.notificationType == 'APPOINTMENT_REJECTED' || item.notificationType == 'APPOINTMENT_CANCELLED' ? '#EF4444' : '#176E65')};">
                                        <c:choose>
                                            <c:when test="${item.notificationType == 'APPOINTMENT_CONFIRMED'}"><i data-lucide="check-circle-2"></i></c:when>
                                            <c:when test="${item.notificationType == 'APPOINTMENT_REJECTED' || item.notificationType == 'APPOINTMENT_CANCELLED'}"><i data-lucide="x-circle"></i></c:when>
                                            <c:otherwise><i data-lucide="calendar"></i></c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div style="flex-grow: 1;">
                                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                                            <h4 style="margin: 0; font-size: 0.95rem; font-weight: ${item.read ? '600' : '700'}; color: var(--text-primary);">
                                                <c:out value="${item.title}" />
                                                <c:if test="${!item.read}">
                                                    <span class="badge bg-primary rounded-pill" style="font-size: 0.65rem; margin-left: 6px;">New</span>
                                                </c:if>
                                            </h4>
                                            <span style="font-size: 0.8125rem; color: var(--text-secondary);">
                                                <c:out value="${item.createdAt != null ? item.createdAt.toString().substring(0, 16).replace('T', ' ') : ''}" />
                                            </span>
                                        </div>
                                        <p style="margin: 0 0 var(--space-2); font-size: 0.875rem; color: var(--text-secondary); line-height: 1.45;">
                                            <c:out value="${item.message}" />
                                        </p>
                                        <div style="display: flex; gap: var(--space-3); align-items: center;">
                                            <c:if test="${item.relatedEntityType == 'APPOINTMENT'}">
                                                <a href="${ctx}/doctor/appointments" class="btn btn-secondary btn-sm" style="padding: 0.2rem 0.6rem; font-size: 0.75rem; text-decoration: none;">
                                                    <i data-lucide="external-link" style="width: 12px; height: 12px;"></i>
                                                    <span>View In Schedule</span>
                                                </a>
                                            </c:if>
                                            <c:if test="${!item.read}">
                                                <form action="${ctx}/doctor/notifications/read" method="POST" style="margin: 0; display: inline;">
                                                    <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                                    <input type="hidden" name="id" value="${item.id}" />
                                                    <input type="hidden" name="filter" value="${filter}" />
                                                    <button type="submit" class="btn btn-secondary btn-sm" style="padding: 0.2rem 0.6rem; font-size: 0.75rem;">
                                                        <i data-lucide="check" style="width: 12px; height: 12px;"></i>
                                                        <span>Mark as read</span>
                                                    </button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </div>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- Pagination -->
            <c:if test="${totalPages > 1}">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: var(--space-5);">
                    <span style="font-size: 0.875rem; color: var(--text-secondary);">
                        Page <c:out value="${currentPage}" /> of <c:out value="${totalPages}" /> (<c:out value="${totalCount}" /> total)
                    </span>
                    <div style="display: flex; gap: var(--space-2);">
                        <c:if test="${currentPage > 1}">
                            <a href="${ctx}/doctor/notifications?filter=${filter}&page=${currentPage - 1}" class="btn btn-secondary btn-sm" style="text-decoration: none;">
                                &larr; Previous
                            </a>
                        </c:if>
                        <c:if test="${currentPage < totalPages}">
                            <a href="${ctx}/doctor/notifications?filter=${filter}&page=${currentPage + 1}" class="btn btn-secondary btn-sm" style="text-decoration: none;">
                                Next &rarr;
                            </a>
                        </c:if>
                    </div>
                </div>
            </c:if>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

