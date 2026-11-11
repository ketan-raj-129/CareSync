<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="notifications" />
<c:set var="pageTitle" value="Notification Engine & Outbox — CareSync Administration" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: var(--space-4);">
                <div>
                    <h1 class="page-title">Notification Engine &amp; Dispatch Outbox</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Transactional email queue monitor, delivery audit trails, retry management, and in-app message streams
                    </p>
                </div>
            </div>

            <c:if test="${not empty sessionScope.flashSuccess}">
                <div class="alert alert-success" style="display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-5);">
                    <i data-lucide="check-circle" style="color: var(--success); flex-shrink: 0;"></i>
                    <span style="font-weight: 500;"><c:out value="${sessionScope.flashSuccess}" /></span>
                </div>
                <c:remove var="flashSuccess" scope="session" />
            </c:if>

            <c:if test="${not empty sessionScope.flashError}">
                <div class="alert alert-danger" style="display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-5);">
                    <i data-lucide="alert-triangle" style="color: var(--danger); flex-shrink: 0;"></i>
                    <span style="font-weight: 500;"><c:out value="${sessionScope.flashError}" /></span>
                </div>
                <c:remove var="flashError" scope="session" />
            </c:if>

            <!-- Navigation Tabs -->
            <div style="display: flex; gap: var(--space-2); margin-bottom: var(--space-5); border-bottom: 1px solid var(--border-color); padding-bottom: var(--space-3);">
                <a href="${ctx}/admin/notifications?tab=outbox"
                   class="btn btn-sm ${activeTab == 'outbox' ? 'btn-primary' : 'btn-secondary'}" style="text-decoration: none; display: flex; align-items: center; gap: var(--space-2);">
                    <i data-lucide="send"></i>
                    <span>Transactional Outbox Queue</span>
                </a>
                <a href="${ctx}/admin/notifications?tab=inapp"
                   class="btn btn-sm ${activeTab == 'inapp' ? 'btn-primary' : 'btn-secondary'}" style="text-decoration: none; display: flex; align-items: center; gap: var(--space-2);">
                    <i data-lucide="bell"></i>
                    <span>In-App Messages Audit Stream</span>
                </a>
            </div>

            <!-- TAB 1: Outbox Queue -->
            <c:if test="${activeTab == 'outbox'}">
                <!-- Filters -->
                <div class="card" style="margin-bottom: var(--space-5); padding: var(--space-4);">
                    <form method="GET" action="${ctx}/admin/notifications" style="display: flex; gap: var(--space-3); align-items: center; flex-wrap: wrap;">
                        <input type="hidden" name="tab" value="outbox" />
                        <label for="statusSelect" style="font-size: 0.875rem; font-weight: 600;">Delivery Status:</label>
                        <select id="statusSelect" name="status" class="form-select form-select-sm" style="width: auto;" onchange="this.form.submit()">
                            <option value="ALL" ${statusFilter == 'ALL' ? 'selected' : ''}>All Statuses</option>
                            <option value="PENDING" ${statusFilter == 'PENDING' ? 'selected' : ''}>Pending</option>
                            <option value="PROCESSING" ${statusFilter == 'PROCESSING' ? 'selected' : ''}>Processing</option>
                            <option value="SENT" ${statusFilter == 'SENT' ? 'selected' : ''}>Sent</option>
                            <option value="FAILED" ${statusFilter == 'FAILED' ? 'selected' : ''}>Failed</option>
                        </select>
                        <span style="font-size: 0.8125rem; color: var(--text-secondary); margin-left: auto;">
                            Total Tracked: <strong><c:out value="${totalCount}" /></strong>
                        </span>
                    </form>
                </div>

                <div class="card" style="padding: 0; overflow-x: auto;">
                    <table class="table" style="margin-bottom: 0;">
                        <thead>
                            <tr style="background-color: var(--color-gray-50, #F9FAFB);">
                                <th style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">ID / Key</th>
                                <th style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">Recipient</th>
                                <th style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">Channel / Event</th>
                                <th style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">Subject / Title</th>
                                <th style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">Status</th>
                                <th style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">Attempts</th>
                                <th style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">Sent / Next Retry</th>
                                <th style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem; text-align: right;">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty deliveries}">
                                    <tr>
                                        <td colspan="8" style="text-align: center; padding: var(--space-6); color: var(--text-secondary);">
                                            No outbox delivery records match the selected filter.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="item" items="${deliveries}">
                                        <tr style="border-top: 1px solid var(--border-color);">
                                            <td style="padding: var(--space-3) var(--space-4); font-family: monospace; font-size: 0.8125rem;">
                                                #<c:out value="${item.id}" /><br/>
                                                <small style="color: var(--text-secondary);"><c:out value="${item.idempotencyKey}" /></small>
                                            </td>
                                            <td style="padding: var(--space-3) var(--space-4); font-size: 0.875rem;">
                                                <strong><c:out value="${item.recipientName}" /></strong><br/>
                                                <small style="color: var(--text-secondary);"><c:out value="${item.recipientEmail}" /></small>
                                            </td>
                                            <td style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">
                                                <span class="badge bg-secondary rounded-pill"><c:out value="${item.channel}" /></span><br/>
                                                <small style="color: var(--text-secondary);"><c:out value="${item.eventType}" /></small>
                                            </td>
                                            <td style="padding: var(--space-3) var(--space-4); font-size: 0.875rem; max-width: 240px;">
                                                <div style="font-weight: 500; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                                    <c:out value="${item.title}" />
                                                </div>
                                                <c:if test="${not empty item.lastError}">
                                                    <small style="color: var(--danger); display: block; font-size: 0.75rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                                        Err: <c:out value="${item.lastError}" />
                                                    </small>
                                                </c:if>
                                            </td>
                                            <td style="padding: var(--space-3) var(--space-4);">
                                                <c:choose>
                                                    <c:when test="${item.status == 'SENT'}">
                                                        <span class="badge bg-success rounded-pill">SENT</span>
                                                    </c:when>
                                                    <c:when test="${item.status == 'PROCESSING'}">
                                                        <span class="badge bg-primary rounded-pill">PROCESSING</span>
                                                    </c:when>
                                                    <c:when test="${item.status == 'FAILED'}">
                                                        <span class="badge bg-danger rounded-pill">FAILED</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-warning text-dark rounded-pill">PENDING</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">
                                                <c:out value="${item.attemptCount}" /> / <c:out value="${item.maxAttempts}" />
                                            </td>
                                            <td style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem; color: var(--text-secondary);">
                                                <c:choose>
                                                    <c:when test="${not empty item.sentAt}">
                                                        <c:out value="${item.sentAt.toString().substring(0, 16).replace('T', ' ')}" />
                                                    </c:when>
                                                    <c:when test="${not empty item.nextRetryAt}">
                                                        Next: <c:out value="${item.nextRetryAt.toString().substring(0, 16).replace('T', ' ')}" />
                                                    </c:when>
                                                    <c:otherwise>&mdash;</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td style="padding: var(--space-3) var(--space-4); text-align: right;">
                                                <c:if test="${item.status == 'FAILED' || item.status == 'PENDING'}">
                                                    <form method="POST" action="${ctx}/admin/notifications/retry" style="margin: 0; display: inline;">
                                                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                                        <input type="hidden" name="outboxId" value="${item.id}" />
                                                        <button type="submit" class="btn btn-secondary btn-sm" title="Queue Immediate Retry">
                                                            <i data-lucide="rotate-cw" style="width: 12px; height: 12px;"></i>
                                                            <span>Retry</span>
                                                        </button>
                                                    </form>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </c:if>

            <!-- TAB 2: In-App Message Stream -->
            <c:if test="${activeTab == 'inapp'}">
                <div class="card" style="margin-bottom: var(--space-5); padding: var(--space-4);">
                    <form method="GET" action="${ctx}/admin/notifications" style="display: flex; gap: var(--space-3); align-items: center; flex-wrap: wrap;">
                        <input type="hidden" name="tab" value="inapp" />
                        <label for="typeSelect" style="font-size: 0.875rem; font-weight: 600;">Event Type:</label>
                        <select id="typeSelect" name="type" class="form-select form-select-sm" style="width: auto;" onchange="this.form.submit()">
                            <option value="ALL" ${typeFilter == 'ALL' ? 'selected' : ''}>All Events</option>
                            <option value="APPOINTMENT_BOOKED" ${typeFilter == 'APPOINTMENT_BOOKED' ? 'selected' : ''}>APPOINTMENT_BOOKED</option>
                            <option value="APPOINTMENT_CONFIRMED" ${typeFilter == 'APPOINTMENT_CONFIRMED' ? 'selected' : ''}>APPOINTMENT_CONFIRMED</option>
                            <option value="APPOINTMENT_REJECTED" ${typeFilter == 'APPOINTMENT_REJECTED' ? 'selected' : ''}>APPOINTMENT_REJECTED</option>
                            <option value="APPOINTMENT_CANCELLED" ${typeFilter == 'APPOINTMENT_CANCELLED' ? 'selected' : ''}>APPOINTMENT_CANCELLED</option>
                            <option value="APPOINTMENT_REMINDER" ${typeFilter == 'APPOINTMENT_REMINDER' ? 'selected' : ''}>APPOINTMENT_REMINDER</option>
                            <option value="SYSTEM_ALERT" ${typeFilter == 'SYSTEM_ALERT' ? 'selected' : ''}>SYSTEM_ALERT</option>
                        </select>
                        <span style="font-size: 0.8125rem; color: var(--text-secondary); margin-left: auto;">
                            Total Streamed: <strong><c:out value="${totalCount}" /></strong>
                        </span>
                    </form>
                </div>

                <div class="card" style="padding: 0; overflow-x: auto;">
                    <table class="table" style="margin-bottom: 0;">
                        <thead>
                            <tr style="background-color: var(--color-gray-50, #F9FAFB);">
                                <th style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">ID</th>
                                <th style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">User ID</th>
                                <th style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">Type</th>
                                <th style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">Title &amp; Message</th>
                                <th style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">Status</th>
                                <th style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">Timestamp</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty notifications}">
                                    <tr>
                                        <td colspan="6" style="text-align: center; padding: var(--space-6); color: var(--text-secondary);">
                                            No in-app notifications found matching the selected filter.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="item" items="${notifications}">
                                        <tr style="border-top: 1px solid var(--border-color);">
                                            <td style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">
                                                #<c:out value="${item.id}" />
                                            </td>
                                            <td style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">
                                                User #<c:out value="${item.userId}" />
                                            </td>
                                            <td style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem;">
                                                <span class="badge bg-secondary rounded-pill"><c:out value="${item.notificationType}" /></span>
                                            </td>
                                            <td style="padding: var(--space-3) var(--space-4); font-size: 0.875rem;">
                                                <strong><c:out value="${item.title}" /></strong><br/>
                                                <small style="color: var(--text-secondary);"><c:out value="${item.message}" /></small>
                                            </td>
                                            <td style="padding: var(--space-3) var(--space-4);">
                                                <c:choose>
                                                    <c:when test="${item.read}">
                                                        <span class="badge bg-secondary rounded-pill">Read</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-primary rounded-pill">Unread</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td style="padding: var(--space-3) var(--space-4); font-size: 0.8125rem; color: var(--text-secondary);">
                                                <c:out value="${item.createdAt != null ? item.createdAt.toString().substring(0, 16).replace('T', ' ') : ''}" />
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </c:if>

            <!-- Pagination -->
            <c:if test="${totalPages > 1}">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: var(--space-5);">
                    <span style="font-size: 0.875rem; color: var(--text-secondary);">
                        Page <c:out value="${currentPage}" /> of <c:out value="${totalPages}" />
                    </span>
                    <div style="display: flex; gap: var(--space-2);">
                        <c:if test="${currentPage > 1}">
                            <a href="${ctx}/admin/notifications?tab=${activeTab}&status=${statusFilter}&type=${typeFilter}&page=${currentPage - 1}" class="btn btn-secondary btn-sm" style="text-decoration: none;">
                                &larr; Previous
                            </a>
                        </c:if>
                        <c:if test="${currentPage < totalPages}">
                            <a href="${ctx}/admin/notifications?tab=${activeTab}&status=${statusFilter}&type=${typeFilter}&page=${currentPage + 1}" class="btn btn-secondary btn-sm" style="text-decoration: none;">
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

