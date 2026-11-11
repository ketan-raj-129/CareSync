<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="DOCTOR" />
<c:set var="activeMenu" value="appointments" />
<c:set var="pageTitle" value="Clinical Appointment Management — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Patient Consultations</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Review upcoming clinical appointments, review booking requests, and document patient encounters
                    </p>
                </div>
                <div>
                    <a href="${ctx}/doctor/availability" class="btn btn-secondary btn-sm">
                        <i data-lucide="clock"></i>
                        <span>Office Hours &amp; Leaves</span>
                    </a>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Filter Tabs -->
            <div style="display: flex; gap: var(--space-2); margin-bottom: var(--space-6); overflow-x: auto; padding-bottom: var(--space-2);">
                <a href="${ctx}/doctor/appointments?filter=ALL" class="btn ${activeFilter == 'ALL' ? 'btn-primary' : 'btn-secondary'} btn-sm">All</a>
                <a href="${ctx}/doctor/appointments?filter=TODAY" class="btn ${activeFilter == 'TODAY' ? 'btn-primary' : 'btn-secondary'} btn-sm">Today</a>
                <a href="${ctx}/doctor/appointments?filter=UPCOMING" class="btn ${activeFilter == 'UPCOMING' ? 'btn-primary' : 'btn-secondary'} btn-sm">Upcoming</a>
                <a href="${ctx}/doctor/appointments?filter=PENDING" class="btn ${activeFilter == 'PENDING' ? 'btn-primary' : 'btn-secondary'} btn-sm">Pending Approval</a>
                <a href="${ctx}/doctor/appointments?filter=CONFIRMED" class="btn ${activeFilter == 'CONFIRMED' ? 'btn-primary' : 'btn-secondary'} btn-sm">Confirmed</a>
                <a href="${ctx}/doctor/appointments?filter=COMPLETED" class="btn ${activeFilter == 'COMPLETED' ? 'btn-primary' : 'btn-secondary'} btn-sm">Completed</a>
                <a href="${ctx}/doctor/appointments?filter=CANCELLED" class="btn ${activeFilter == 'CANCELLED' ? 'btn-primary' : 'btn-secondary'} btn-sm">Cancelled</a>
            </div>

            <!-- Appointments Table Card -->
            <div class="card">
                <div class="card-body" style="padding: 0;">
                    <c:choose>
                        <c:when test="${empty appointments}">
                            <div class="empty-state" style="padding: var(--space-12);">
                                <div class="empty-state-icon">
                                    <i data-lucide="calendar-x"></i>
                                </div>
                                <h3 class="empty-state-title">No appointments found</h3>
                                <p class="empty-state-desc">
                                    There are no patient appointments matching the "<c:out value="${activeFilter}" />" filter.
                                </p>
                                <a href="${ctx}/doctor/appointments?filter=ALL" class="btn btn-secondary btn-sm">View All Appointments</a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table" style="margin: 0;">
                                    <thead>
                                        <tr>
                                            <th>Date &amp; Time</th>
                                            <th>Patient Information</th>
                                            <th>Status</th>
                                            <th>Reason for Visit</th>
                                            <th style="text-align: right;">Clinical Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="apt" items="${appointments}">
                                            <tr>
                                                <td style="white-space: nowrap;">
                                                    <strong><c:out value="${apt.appointmentDate}" /></strong>
                                                    <div style="font-size: 0.8125rem; color: var(--text-secondary);">
                                                        <c:out value="${apt.startTime}" /> - <c:out value="${apt.endTime}" />
                                                    </div>
                                                </td>
                                                <td>
                                                    <div style="font-weight: 600;">
                                                        <c:out value="${apt.patient.firstName} ${apt.patient.lastName}" />
                                                    </div>
                                                    <div style="font-size: 0.8125rem; color: var(--text-secondary);">
                                                        Ref: <c:out value="${apt.referenceNumber}" /> &bull; Tel: <c:out value="${apt.patient.phoneNumber != null ? apt.patient.phoneNumber : '—'}" />
                                                    </div>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${apt.status == 'CONFIRMED'}"><span class="badge badge-success">Confirmed</span></c:when>
                                                        <c:when test="${apt.status == 'PENDING'}"><span class="badge badge-warning">Pending</span></c:when>
                                                        <c:when test="${apt.status == 'COMPLETED'}"><span class="badge badge-info">Completed</span></c:when>
                                                        <c:when test="${apt.status == 'CANCELLED'}"><span class="badge badge-danger">Cancelled</span></c:when>
                                                        <c:otherwise><span class="badge badge-secondary"><c:out value="${apt.status}" /></span></c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="max-width: 250px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                                    <c:out value="${apt.reasonForVisit != null ? apt.reasonForVisit : 'General Consultation'}" />
                                                    <c:if test="${apt.status == 'CANCELLED' && not empty apt.cancellationReason}">
                                                        <div style="font-size: 0.75rem; color: var(--danger);"><c:out value="${apt.cancellationReason}" /></div>
                                                    </c:if>
                                                </td>
                                                <td style="text-align: right; white-space: nowrap;">
                                                    <div style="display: inline-flex; gap: var(--space-2); align-items: center;">
                                                        <c:if test="${apt.status == 'PENDING'}">
                                                            <form action="${ctx}/doctor/appointments/action" method="POST" style="margin: 0; display: inline;">
                                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                                <input type="hidden" name="appointmentId" value="${apt.id}">
                                                                <input type="hidden" name="action" value="CONFIRM">
                                                                <input type="hidden" name="redirect" value="/doctor/appointments?filter=${activeFilter}">
                                                                <button type="submit" class="btn btn-primary btn-sm" title="Accept &amp; Confirm">
                                                                    <i data-lucide="check"></i> Confirm
                                                                </button>
                                                            </form>

                                                            <form action="${ctx}/doctor/appointments/action" method="POST" style="margin: 0; display: inline;">
                                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                                <input type="hidden" name="appointmentId" value="${apt.id}">
                                                                <input type="hidden" name="action" value="REJECT">
                                                                <input type="hidden" name="redirect" value="/doctor/appointments?filter=${activeFilter}">
                                                                <button type="submit" class="btn btn-secondary btn-sm" style="color: var(--danger);" title="Decline Request" onclick="return confirm('Decline this patient request?');">
                                                                    <i data-lucide="x"></i>
                                                                </button>
                                                            </form>
                                                        </c:if>

                                                        <c:if test="${apt.status == 'CONFIRMED'}">
                                                            <a href="${ctx}/doctor/consultation?appointmentId=${apt.id}" class="btn btn-primary btn-sm" title="Begin Consultation Encounter">
                                                                <i data-lucide="stethoscope"></i>
                                                                <span>Consult</span>
                                                            </a>
                                                        </c:if>

                                                        <a href="${ctx}/doctor/appointments/view?id=${apt.id}" class="btn btn-secondary btn-sm" title="View Full Encounter">
                                                            <i data-lucide="eye"></i>
                                                        </a>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

