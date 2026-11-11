<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="dashboard" />
<c:set var="pageTitle" value="Administration Dashboard — CareSync" />

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
                    <h1 class="page-title">Executive Governance Overview</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Real-time clinical operations, practitioner registry, and institutional audit monitor
                    </p>
                </div>
                <div style="display: flex; gap: var(--space-3); flex-wrap: wrap;">
                    <a href="${ctx}/admin/audit-logs" class="btn btn-secondary btn-sm">
                        <i data-lucide="shield-check"></i>
                        <span>Audit Trail</span>
                    </a>
                    <a href="${ctx}/admin/specialties/create" class="btn btn-secondary btn-sm">
                        <i data-lucide="plus"></i>
                        <span>Add Specialty</span>
                    </a>
                    <a href="${ctx}/admin/holidays" class="btn btn-primary btn-sm">
                        <i data-lucide="calendar-off"></i>
                        <span>Manage Holidays</span>
                    </a>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Metric Cards Row (4 Primary Cards) -->
            <div class="grid-4" style="margin-bottom: var(--space-6);">
                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--primary-soft); color: var(--primary);">
                        <i data-lucide="users"></i>
                    </div>
                    <div>
                        <div class="stat-value"><c:out value="${metrics.totalPatients}" /></div>
                        <div class="stat-label">Registered Patients</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--info-soft); color: var(--info);">
                        <i data-lucide="user-check"></i>
                    </div>
                    <div>
                        <div class="stat-value"><c:out value="${metrics.activeDoctors}" /> / <c:out value="${metrics.totalDoctors}" /></div>
                        <div class="stat-label">Active Doctors</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--warning-soft); color: var(--warning);">
                        <i data-lucide="calendar"></i>
                    </div>
                    <div>
                        <div class="stat-value"><c:out value="${metrics.todayAppointments}" /></div>
                        <div class="stat-label">Today's Appointments</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--success-soft); color: var(--success);">
                        <i data-lucide="award"></i>
                    </div>
                    <div>
                        <div class="stat-value"><c:out value="${metrics.activeSpecialties}" /></div>
                        <div class="stat-label">Active Specialties</div>
                    </div>
                </div>
            </div>

            <!-- Appointment Status Breakdown Row -->
            <div class="card" style="margin-bottom: var(--space-6);">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <h2 class="card-title" style="font-size: 1.125rem;">Appointment Volume &amp; Status Distribution</h2>
                    <a href="${ctx}/admin/appointments" class="btn btn-secondary btn-sm" style="font-size: 0.8125rem;">
                        View All (<c:out value="${metrics.totalAppointments}" />)
                    </a>
                </div>
                <div class="card-body">
                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: var(--space-4);">
                        <div style="padding: var(--space-4); background-color: var(--background); border-radius: var(--radius-md); border-left: 4px solid var(--warning);">
                            <div style="font-size: 0.8125rem; color: var(--text-secondary); text-transform: uppercase; font-weight: 600;">Pending</div>
                            <div style="font-size: 1.5rem; font-weight: 700; color: var(--warning); margin-top: 4px;">
                                <c:out value="${metrics.pendingAppointments}" />
                            </div>
                        </div>
                        <div style="padding: var(--space-4); background-color: var(--background); border-radius: var(--radius-md); border-left: 4px solid var(--primary);">
                            <div style="font-size: 0.8125rem; color: var(--text-secondary); text-transform: uppercase; font-weight: 600;">Confirmed</div>
                            <div style="font-size: 1.5rem; font-weight: 700; color: var(--primary); margin-top: 4px;">
                                <c:out value="${metrics.confirmedAppointments}" />
                            </div>
                        </div>
                        <div style="padding: var(--space-4); background-color: var(--background); border-radius: var(--radius-md); border-left: 4px solid var(--success);">
                            <div style="font-size: 0.8125rem; color: var(--text-secondary); text-transform: uppercase; font-weight: 600;">Completed</div>
                            <div style="font-size: 1.5rem; font-weight: 700; color: var(--success); margin-top: 4px;">
                                <c:out value="${metrics.completedAppointments}" />
                            </div>
                        </div>
                        <div style="padding: var(--space-4); background-color: var(--background); border-radius: var(--radius-md); border-left: 4px solid var(--danger);">
                            <div style="font-size: 0.8125rem; color: var(--text-secondary); text-transform: uppercase; font-weight: 600;">Cancelled</div>
                            <div style="font-size: 1.5rem; font-weight: 700; color: var(--danger); margin-top: 4px;">
                                <c:out value="${metrics.cancelledAppointments}" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Two-Column Workspace Content -->
            <div class="grid-2" style="margin-bottom: var(--space-6);">
                <!-- Section 1: Pending Doctor Credential Reviews -->
                <div class="card">
                    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                        <h2 class="card-title" style="font-size: 1.125rem;">Doctor Credential Reviews</h2>
                        <span class="badge ${metrics.pendingDoctorVerifications > 0 ? 'badge-warning' : 'badge-success'}">
                            <c:out value="${metrics.pendingDoctorVerifications}" /> Inactive / Pending
                        </span>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${not empty metrics.pendingVerificationDoctors}">
                                <div class="table-responsive">
                                    <table class="table">
                                        <thead>
                                            <tr>
                                                <th>Doctor</th>
                                                <th>License</th>
                                                <th>Status</th>
                                                <th style="text-align: right;">Action</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="doc" items="${metrics.pendingVerificationDoctors}">
                                                <tr>
                                                    <td>
                                                        <div style="font-weight: 600;">Dr. <c:out value="${doc.firstName}" /> <c:out value="${doc.lastName}" /></div>
                                                        <div style="font-size: 0.8125rem; color: var(--text-secondary);"><c:out value="${doc.specialty.name}" /></div>
                                                    </td>
                                                    <td><c:out value="${doc.licenseNumber}" /></td>
                                                    <td>
                                                        <span class="badge badge-warning"><c:out value="${doc.user.status}" /></span>
                                                    </td>
                                                    <td style="text-align: right;">
                                                        <a href="${ctx}/admin/doctors/verification?id=${doc.doctorProfileId}" class="btn btn-secondary btn-sm" style="padding: 4px 8px; font-size: 0.75rem;">
                                                            Review
                                                        </a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-state" style="padding: var(--space-6) var(--space-4);">
                                    <div class="empty-state-icon" style="background-color: var(--success-soft); color: var(--success);">
                                        <i data-lucide="check-circle-2"></i>
                                    </div>
                                    <h3 class="empty-state-title" style="font-size: 1rem;">All Doctors Processed</h3>
                                    <p class="empty-state-desc" style="font-size: 0.8125rem;">
                                        All registered physician accounts are currently active.
                                    </p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- Section 2: Recent Security & Audit Events -->
                <div class="card">
                    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                        <h2 class="card-title" style="font-size: 1.125rem;">Recent Security &amp; Audit Trail</h2>
                        <a href="${ctx}/admin/audit-logs" class="btn btn-secondary btn-sm" style="font-size: 0.8125rem;">
                            View All Logs
                        </a>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${not empty metrics.recentAuditLogs}">
                                <div class="table-responsive">
                                    <table class="table">
                                        <thead>
                                            <tr>
                                                <th>Action</th>
                                                <th>Target</th>
                                                <th>User ID</th>
                                                <th>Time (UTC)</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="log" items="${metrics.recentAuditLogs}">
                                                <tr>
                                                    <td>
                                                        <span class="badge badge-info" style="font-family: monospace; font-size: 0.75rem;">
                                                            <c:out value="${log.action}" />
                                                        </span>
                                                    </td>
                                                    <td><c:out value="${log.targetEntity}" /> #<c:out value="${log.targetId}" /></td>
                                                    <td>#<c:out value="${log.userId != null ? log.userId : 'SYS'}" /></td>
                                                    <td style="font-size: 0.8125rem; color: var(--text-secondary);"><c:out value="${log.createdAt}" /></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-state" style="padding: var(--space-6) var(--space-4);">
                                    <div class="empty-state-icon" style="background-color: var(--info-soft); color: var(--info);">
                                        <i data-lucide="shield-check"></i>
                                    </div>
                                    <h3 class="empty-state-title" style="font-size: 1rem;">No recent audit events</h3>
                                    <p class="empty-state-desc" style="font-size: 0.8125rem;">
                                        Audit logs will appear here as system actions occur.
                                    </p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- Quick Navigation Shortcuts -->
            <div class="card">
                <div class="card-header">
                    <h2 class="card-title" style="font-size: 1.125rem;">Central Governance Shortcuts</h2>
                </div>
                <div class="card-body">
                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: var(--space-4);">
                        <a href="${ctx}/admin/users" class="card" style="padding: var(--space-4); text-decoration: none; color: inherit; border: 1px solid var(--border-color); display: flex; align-items: center; gap: var(--space-3); transition: border-color 0.2s;">
                            <div style="padding: 10px; background-color: var(--primary-soft); color: var(--primary); border-radius: var(--radius-md);">
                                <i data-lucide="users"></i>
                            </div>
                            <div>
                                <div style="font-weight: 600;">User Governance</div>
                                <div style="font-size: 0.8125rem; color: var(--text-secondary);">Manage roles &amp; statuses</div>
                            </div>
                        </a>
                        <a href="${ctx}/admin/doctors" class="card" style="padding: var(--space-4); text-decoration: none; color: inherit; border: 1px solid var(--border-color); display: flex; align-items: center; gap: var(--space-3); transition: border-color 0.2s;">
                            <div style="padding: 10px; background-color: var(--info-soft); color: var(--info); border-radius: var(--radius-md);">
                                <i data-lucide="user-check"></i>
                            </div>
                            <div>
                                <div style="font-weight: 600;">Doctor Registry</div>
                                <div style="font-size: 0.8125rem; color: var(--text-secondary);">Credentials &amp; availability</div>
                            </div>
                        </a>
                        <a href="${ctx}/admin/patients" class="card" style="padding: var(--space-4); text-decoration: none; color: inherit; border: 1px solid var(--border-color); display: flex; align-items: center; gap: var(--space-3); transition: border-color 0.2s;">
                            <div style="padding: 10px; background-color: var(--success-soft); color: var(--success); border-radius: var(--radius-md);">
                                <i data-lucide="heart-pulse"></i>
                            </div>
                            <div>
                                <div style="font-weight: 600;">Patient Directory</div>
                                <div style="font-size: 0.8125rem; color: var(--text-secondary);">Demographics &amp; accounts</div>
                            </div>
                        </a>
                        <a href="${ctx}/admin/specialties" class="card" style="padding: var(--space-4); text-decoration: none; color: inherit; border: 1px solid var(--border-color); display: flex; align-items: center; gap: var(--space-3); transition: border-color 0.2s;">
                            <div style="padding: 10px; background-color: var(--warning-soft); color: var(--warning); border-radius: var(--radius-md);">
                                <i data-lucide="award"></i>
                            </div>
                            <div>
                                <div style="font-weight: 600;">Medical Specialties</div>
                                <div style="font-size: 0.8125rem; color: var(--text-secondary);">Clinical departments</div>
                            </div>
                        </a>
                        <a href="${ctx}/admin/appointments" class="card" style="padding: var(--space-4); text-decoration: none; color: inherit; border: 1px solid var(--border-color); display: flex; align-items: center; gap: var(--space-3); transition: border-color 0.2s;">
                            <div style="padding: 10px; background-color: var(--primary-soft); color: var(--primary); border-radius: var(--radius-md);">
                                <i data-lucide="calendar"></i>
                            </div>
                            <div>
                                <div style="font-weight: 600;">Appointment Audit</div>
                                <div style="font-size: 0.8125rem; color: var(--text-secondary);">Master booking schedule</div>
                            </div>
                        </a>
                        <a href="${ctx}/admin/settings" class="card" style="padding: var(--space-4); text-decoration: none; color: inherit; border: 1px solid var(--border-color); display: flex; align-items: center; gap: var(--space-3); transition: border-color 0.2s;">
                            <div style="padding: 10px; background-color: var(--danger-soft); color: var(--danger); border-radius: var(--radius-md);">
                                <i data-lucide="sliders"></i>
                            </div>
                            <div>
                                <div style="font-weight: 600;">System Settings</div>
                                <div style="font-size: 0.8125rem; color: var(--text-secondary);">Clinic config &amp; policies</div>
                            </div>
                        </a>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
