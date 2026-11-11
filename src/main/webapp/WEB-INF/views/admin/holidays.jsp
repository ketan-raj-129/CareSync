<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="holidays" />
<c:set var="pageTitle" value="Clinic-Wide Holiday Management — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Clinic-Wide Holiday &amp; Closure Management</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Schedule facility closures, institutional holidays, and inspect existing booking conflicts
                    </p>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <div class="grid-2" style="margin-bottom: var(--space-6);">
                <!-- Declare Holiday Form -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title" style="font-size: 1.125rem;">Declare Clinic Closure Date</h2>
                    </div>
                    <div class="card-body">
                        <p style="font-size: 0.875rem; color: var(--text-secondary); margin-bottom: var(--space-4);">
                            Setting a clinic holiday blocks patient appointment booking for all practitioners across the institution.
                        </p>

                        <form method="post" action="${ctx}/admin/holidays/manage">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                            <input type="hidden" name="action" value="ADD" />

                            <div style="margin-bottom: var(--space-4);">
                                <label class="form-label" for="exceptionDate">Closure Date *</label>
                                <input type="date" class="form-control" id="exceptionDate" name="exceptionDate" required>
                            </div>

                            <div style="margin-bottom: var(--space-4);">
                                <label class="form-label" for="reason">Holiday / Closure Reason *</label>
                                <input type="text" class="form-control" id="reason" name="reason" required maxlength="255"
                                       placeholder="e.g. Republic Day, Annual Facility Maintenance">
                            </div>

                            <button type="submit" class="btn btn-primary">
                                <i data-lucide="calendar-plus"></i>
                                <span>Declare Clinic Holiday</span>
                            </button>
                        </form>
                    </div>
                </div>

                <!-- Booking Conflict Inspector -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title" style="font-size: 1.125rem;">Closure Conflict Inspector</h2>
                    </div>
                    <div class="card-body">
                        <p style="font-size: 0.875rem; color: var(--text-secondary); margin-bottom: var(--space-4);">
                            Check if any patient consultations are already scheduled on a planned closure date before declaring the holiday.
                        </p>

                        <form method="get" action="${ctx}/admin/holidays" style="display: flex; gap: var(--space-3); margin-bottom: var(--space-4);">
                            <input type="date" class="form-control" name="checkDate" value="<c:out value='${checkDate}' />" required>
                            <button type="submit" class="btn btn-secondary" style="white-space: nowrap;">
                                <i data-lucide="search"></i>
                                <span>Inspect Date</span>
                            </button>
                        </form>

                        <c:if test="${checkDate != null}">
                            <div style="padding: var(--space-3); border-radius: var(--radius-md); background-color: var(--background);">
                                <div style="font-weight: 600; font-size: 0.875rem; margin-bottom: 4px;">
                                    Inspection for <c:out value="${checkDate}" />:
                                </div>
                                <c:choose>
                                    <c:when test="${not empty affectedAppointments}">
                                        <div style="color: var(--danger); font-size: 0.875rem; font-weight: 600; margin-bottom: 6px;">
                                            ⚠ <c:out value="${affectedAppointments.size()}" /> existing appointment(s) found!
                                        </div>
                                        <ul style="padding-left: var(--space-4); margin: 0; font-size: 0.8125rem;">
                                            <c:forEach var="aff" items="${affectedAppointments}">
                                                <li>
                                                    <strong>#<c:out value="${aff.referenceNumber}" /></strong> (<c:out value="${aff.status}" />):
                                                    Dr. <c:out value="${aff.doctor.lastName}" /> with <c:out value="${aff.patient.firstName}" /> <c:out value="${aff.patient.lastName}" /> at <c:out value="${aff.startTime}" />
                                                </li>
                                            </c:forEach>
                                        </ul>
                                    </c:when>
                                    <c:otherwise>
                                        <div style="color: var(--success); font-size: 0.875rem;">
                                            ✓ No conflicting appointments scheduled on this date.
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:if>
                    </div>
                </div>
            </div>

            <!-- Existing Holidays Table -->
            <div class="card">
                <div class="card-header">
                    <h2 class="card-title" style="font-size: 1.125rem;">Scheduled Clinic Closures &amp; Holidays</h2>
                </div>
                <div class="card-body" style="padding: 0;">
                    <c:choose>
                        <c:when test="${not empty holidays}">
                            <div class="table-responsive">
                                <table class="table" style="margin-bottom: 0;">
                                    <thead>
                                        <tr>
                                            <th>Date</th>
                                            <th>Reason / Occasion</th>
                                            <th>Scope</th>
                                            <th>Status</th>
                                            <th style="text-align: right;">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="h" items="${holidays}">
                                            <tr>
                                                <td style="font-weight: 600;"><c:out value="${h.exceptionDate}" /></td>
                                                <td><c:out value="${h.reason != null ? h.reason : 'Clinic Holiday'}" /></td>
                                                <td><span class="badge badge-info">All Practitioners</span></td>
                                                <td><span class="badge badge-danger">Closed</span></td>
                                                <td style="text-align: right;">
                                                    <form method="post" action="${ctx}/admin/holidays/manage" style="display: inline; margin: 0;">
                                                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                                        <input type="hidden" name="action" value="DELETE" />
                                                        <input type="hidden" name="exceptionId" value="<c:out value='${h.exceptionId}' />" />
                                                        <button type="submit" class="btn btn-secondary btn-sm" style="color: var(--danger); padding: 4px 8px; font-size: 0.75rem;"
                                                                onclick="return confirm('Remove clinic holiday for ${h.exceptionDate}?');">
                                                            Delete
                                                        </button>
                                                    </form>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state" style="padding: var(--space-8) var(--space-4);">
                                <div class="empty-state-icon">
                                    <i data-lucide="calendar"></i>
                                </div>
                                <h3 class="empty-state-title">No clinic holidays declared</h3>
                                <p class="empty-state-desc">No facility-wide closures are currently registered in the schedule.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
