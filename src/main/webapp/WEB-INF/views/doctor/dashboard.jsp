<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="DOCTOR" />
<c:set var="activeMenu" value="dashboard" />
<c:set var="pageTitle" value="Doctor Practice Dashboard — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <!-- Sidebar Navigation -->
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <!-- Main Workspace -->
    <div class="dashboard-main">
        <!-- Topbar Navigation -->
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <!-- Dashboard Content -->
        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Welcome, Dr. <c:out value="${doctor.lastName != null ? doctor.lastName : doctor.firstName}" /></h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        <c:out value="${doctor.specialty.name != null ? doctor.specialty.name : 'Clinical Practice'}" /> &bull; 
                        Room <c:out value="${doctor.roomNumber != null ? doctor.roomNumber : 'TBD'}" /> &bull; 
                        Lic. #<c:out value="${doctor.licenseNumber}" />
                    </p>
                </div>
                <div style="display: flex; gap: var(--space-3); flex-wrap: wrap;">
                    <a href="${ctx}/doctor/availability" class="btn btn-secondary btn-sm">
                        <i data-lucide="clock"></i>
                        <span>Manage Hours</span>
                    </a>
                    <a href="${ctx}/doctor/appointments?filter=PENDING" class="btn btn-secondary btn-sm">
                        <i data-lucide="bell"></i>
                        <span>Pending Requests (<c:out value="${metrics.pendingAppointmentsCount}" />)</span>
                    </a>
                    <a href="${ctx}/doctor/appointments" class="btn btn-primary btn-sm">
                        <i data-lucide="calendar"></i>
                        <span>All Appointments</span>
                    </a>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Metrics Row (5 Cards) -->
            <div class="grid-5" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: var(--space-4); margin-bottom: var(--space-8);">
                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--primary-soft); color: var(--primary);">
                        <i data-lucide="calendar-check"></i>
                    </div>
                    <div>
                        <div class="stat-value"><c:out value="${metrics.todayAppointmentsCount}" /></div>
                        <div class="stat-label">Today's Queue</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--warning-soft); color: var(--warning);">
                        <i data-lucide="clock"></i>
                    </div>
                    <div>
                        <div class="stat-value"><c:out value="${metrics.pendingAppointmentsCount}" /></div>
                        <div class="stat-label">Pending Requests</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--info-soft); color: var(--info);">
                        <i data-lucide="check-circle"></i>
                    </div>
                    <div>
                        <div class="stat-value"><c:out value="${metrics.confirmedAppointmentsCount}" /></div>
                        <div class="stat-label">Confirmed Upcoming</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--success-soft); color: var(--success);">
                        <i data-lucide="stethoscope"></i>
                    </div>
                    <div>
                        <div class="stat-value"><c:out value="${metrics.completedAppointmentsCount}" /></div>
                        <div class="stat-label">Completed Consults</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: rgba(29, 168, 155, 0.12); color: var(--primary);">
                        <i data-lucide="users"></i>
                    </div>
                    <div>
                        <div class="stat-value"><c:out value="${metrics.totalAppointments}" /></div>
                        <div class="stat-label">Total Encounters</div>
                    </div>
                </div>
            </div>

            <!-- Two-Column Layout: Today's Schedule & Actionable Queues -->
            <div style="display: grid; grid-template-columns: 2fr 1.2fr; gap: var(--space-6); margin-bottom: var(--space-8);">
                <!-- Left: Today's Consultations -->
                <div class="card">
                    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                        <h2 class="card-title" style="display: flex; align-items: center; gap: var(--space-2); margin: 0; font-size: 1.125rem;">
                            <i data-lucide="calendar" style="color: var(--primary);"></i>
                            <span>Today's Consultation Schedule</span>
                        </h2>
                        <span class="badge badge-primary"><c:out value="${metrics.todayAppointments.size()}" /> Patients</span>
                    </div>

                    <div class="card-body" style="padding: 0;">
                        <c:choose>
                            <c:when test="${empty metrics.todayAppointments}">
                                <div class="empty-state" style="padding: var(--space-8);">
                                    <div class="empty-state-icon">
                                        <i data-lucide="calendar-check"></i>
                                    </div>
                                    <h3 class="empty-state-title">No appointments scheduled for today</h3>
                                    <p class="empty-state-desc">You currently have no patient consultations scheduled for today's clinical hours.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table" style="margin: 0;">
                                        <thead>
                                            <tr>
                                                <th>Time</th>
                                                <th>Patient</th>
                                                <th>Status</th>
                                                <th>Reason for Visit</th>
                                                <th style="text-align: right;">Action</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="apt" items="${metrics.todayAppointments}">
                                                <tr>
                                                    <td>
                                                        <strong><c:out value="${apt.startTime}" /> - <c:out value="${apt.endTime}" /></strong>
                                                    </td>
                                                    <td>
                                                        <div style="font-weight: 600;"><c:out value="${apt.patient.firstName} ${apt.patient.lastName}" /></div>
                                                        <div style="font-size: 0.8125rem; color: var(--text-secondary);">Ref: <c:out value="${apt.referenceNumber}" /></div>
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
                                                    <td style="max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                                        <c:out value="${apt.reasonForVisit != null ? apt.reasonForVisit : 'General Consultation'}" />
                                                    </td>
                                                    <td style="text-align: right; white-space: nowrap;">
                                                        <c:if test="${apt.status == 'CONFIRMED'}">
                                                            <a href="${ctx}/doctor/consultation?appointmentId=${apt.id}" class="btn btn-primary btn-sm" title="Launch Consultation Encounter">
                                                                <i data-lucide="stethoscope"></i>
                                                                <span>Start Consult</span>
                                                            </a>
                                                        </c:if>
                                                        <a href="${ctx}/doctor/appointments/view?id=${apt.id}" class="btn btn-secondary btn-sm" title="View Patient Details">
                                                            <i data-lucide="eye"></i>
                                                        </a>
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

                <!-- Right: Pending Approval Queue -->
                <div class="card">
                    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                        <h2 class="card-title" style="display: flex; align-items: center; gap: var(--space-2); margin: 0; font-size: 1.125rem;">
                            <i data-lucide="inbox" style="color: var(--warning);"></i>
                            <span>Pending Approvals</span>
                        </h2>
                        <a href="${ctx}/doctor/appointments?filter=PENDING" style="font-size: 0.8125rem;">View All</a>
                    </div>

                    <div class="card-body" style="padding: 0;">
                        <c:choose>
                            <c:when test="${empty metrics.pendingAppointments}">
                                <div class="empty-state" style="padding: var(--space-8);">
                                    <div class="empty-state-icon">
                                        <i data-lucide="check-check"></i>
                                    </div>
                                    <h3 class="empty-state-title">No pending requests</h3>
                                    <p class="empty-state-desc">All appointment bookings have been reviewed and processed.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <ul style="list-style: none; margin: 0; padding: 0;">
                                    <c:forEach var="pendingApt" items="${metrics.pendingAppointments}">
                                        <li style="padding: var(--space-4); border-bottom: 1px solid var(--border-color); display: flex; flex-direction: column; gap: var(--space-2);">
                                            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                                                <div>
                                                    <div style="font-weight: 600;"><c:out value="${pendingApt.patient.firstName} ${pendingApt.patient.lastName}" /></div>
                                                    <div style="font-size: 0.8125rem; color: var(--text-secondary);">
                                                        <c:out value="${pendingApt.appointmentDate}" /> &bull; <c:out value="${pendingApt.startTime}" />
                                                    </div>
                                                </div>
                                                <span class="badge badge-warning">Pending</span>
                                            </div>

                                            <c:if test="${not empty pendingApt.reasonForVisit}">
                                                <div style="font-size: 0.8125rem; color: var(--text-muted); background: var(--background); padding: var(--space-2); border-radius: var(--radius-sm);">
                                                    <c:out value="${pendingApt.reasonForVisit}" />
                                                </div>
                                            </c:if>

                                            <div style="display: flex; gap: var(--space-2); justify-content: flex-end; margin-top: var(--space-1);">
                                                <form action="${ctx}/doctor/appointments/action" method="POST" style="margin: 0; display: inline;">
                                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                    <input type="hidden" name="appointmentId" value="${pendingApt.id}">
                                                    <input type="hidden" name="action" value="CONFIRM">
                                                    <input type="hidden" name="redirect" value="/doctor/dashboard">
                                                    <button type="submit" class="btn btn-primary btn-sm" style="padding: 0.25rem 0.65rem; font-size: 0.8125rem;">
                                                        <i data-lucide="check"></i> Confirm
                                                    </button>
                                                </form>

                                                <form action="${ctx}/doctor/appointments/action" method="POST" style="margin: 0; display: inline;">
                                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                    <input type="hidden" name="appointmentId" value="${pendingApt.id}">
                                                    <input type="hidden" name="action" value="REJECT">
                                                    <input type="hidden" name="redirect" value="/doctor/dashboard">
                                                    <button type="submit" class="btn btn-secondary btn-sm" style="padding: 0.25rem 0.65rem; font-size: 0.8125rem; color: var(--danger);" onclick="return confirm('Decline this appointment request?');">
                                                        <i data-lucide="x"></i> Decline
                                                    </button>
                                                </form>
                                            </div>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- Upcoming Encounters List -->
            <div class="card">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <h2 class="card-title" style="display: flex; align-items: center; gap: var(--space-2); margin: 0; font-size: 1.125rem;">
                        <i data-lucide="clock" style="color: var(--primary);"></i>
                        <span>Upcoming Practice Schedule (Next Days)</span>
                    </h2>
                    <a href="${ctx}/doctor/appointments?filter=UPCOMING" class="btn btn-secondary btn-sm">View Schedule</a>
                </div>

                <div class="card-body" style="padding: 0;">
                    <c:choose>
                        <c:when test="${empty metrics.upcomingAppointments}">
                            <div class="empty-state" style="padding: var(--space-8);">
                                <div class="empty-state-icon">
                                    <i data-lucide="calendar"></i>
                                </div>
                                <h3 class="empty-state-title">No upcoming appointments booked</h3>
                                <p class="empty-state-desc">Your future calendar currently has no reserved appointments.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table" style="margin: 0;">
                                    <thead>
                                        <tr>
                                            <th>Date &amp; Time</th>
                                            <th>Patient Name</th>
                                            <th>Contact</th>
                                            <th>Status</th>
                                            <th>Reason</th>
                                            <th style="text-align: right;">Encounter</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="upApt" items="${metrics.upcomingAppointments}">
                                            <tr>
                                                <td>
                                                    <strong><c:out value="${upApt.appointmentDate}" /></strong>
                                                    <div style="font-size: 0.8125rem; color: var(--text-secondary);"><c:out value="${upApt.startTime}" /> - <c:out value="${upApt.endTime}" /></div>
                                                </td>
                                                <td>
                                                    <div style="font-weight: 600;"><c:out value="${upApt.patient.firstName} ${upApt.patient.lastName}" /></div>
                                                    <div style="font-size: 0.8125rem; color: var(--text-secondary);">Ref: <c:out value="${upApt.referenceNumber}" /></div>
                                                </td>
                                                <td>
                                                    <div><c:out value="${upApt.patient.phoneNumber != null ? upApt.patient.phoneNumber : '—'}" /></div>
                                                    <div style="font-size: 0.8125rem; color: var(--text-secondary);"><c:out value="${upApt.patient.email}" /></div>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${upApt.status == 'CONFIRMED'}"><span class="badge badge-success">Confirmed</span></c:when>
                                                        <c:when test="${upApt.status == 'PENDING'}"><span class="badge badge-warning">Pending</span></c:when>
                                                        <c:otherwise><span class="badge badge-secondary"><c:out value="${upApt.status}" /></span></c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="max-width: 220px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                                    <c:out value="${upApt.reasonForVisit != null ? upApt.reasonForVisit : '—'}" />
                                                </td>
                                                <td style="text-align: right;">
                                                    <a href="${ctx}/doctor/appointments/view?id=${upApt.id}" class="btn btn-secondary btn-sm">
                                                        <i data-lucide="eye"></i> <span>Details</span>
                                                    </a>
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
