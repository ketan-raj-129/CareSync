<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="PATIENT" />
<c:set var="pageTitle" value="Patient Dashboard — CareSync" />

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
                    <h1 class="page-title">Welcome back, <c:out value="${not empty sessionUser.displayName ? sessionUser.displayName : patient.fullName}" /></h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        CareSync Healthcare Portal &bull; Patient ID: CS-PAT-<c:out value="${patient.patientProfileId}" />
                    </p>
                </div>
                <div style="display: flex; gap: var(--space-3);">
                    <a href="${ctx}/patient/profile" class="btn btn-secondary btn-sm">
                        <i data-lucide="user"></i>
                        <span>My Profile</span>
                    </a>
                    <a href="${ctx}/patient/doctors" class="btn btn-primary btn-sm">
                        <i data-lucide="calendar-plus"></i>
                        <span>Book Appointment</span>
                    </a>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Metric Cards Row -->
            <div class="grid-4" style="margin-bottom: var(--space-8);">
                <div class="card stat-card">
                    <div class="stat-icon">
                        <i data-lucide="calendar"></i>
                    </div>
                    <div>
                        <div class="stat-value"><c:out value="${upcomingAppointments != null ? upcomingAppointments.size() : 0}" /></div>
                        <div class="stat-label">Upcoming Visits</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--success-soft); color: var(--success);">
                        <i data-lucide="check-circle-2"></i>
                    </div>
                    <div>
                        <div class="stat-value"><c:out value="${pastAppointments != null ? pastAppointments.size() : 0}" /></div>
                        <div class="stat-label">Past Consultations</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--info-soft); color: var(--info);">
                        <i data-lucide="activity"></i>
                    </div>
                    <div>
                        <div class="stat-value"><c:out value="${totalAppointments != null ? totalAppointments : 0}" /></div>
                        <div class="stat-label">Total Appointments</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--warning-soft); color: var(--warning);">
                        <i data-lucide="heart-pulse"></i>
                    </div>
                    <div>
                        <div class="stat-value"><c:out value="${patient.bloodGroup != null ? patient.bloodGroup.displayValue : 'N/A'}" /></div>
                        <div class="stat-label">Blood Group</div>
                    </div>
                </div>
            </div>

            <!-- Two-Column Patient Health Sections -->
            <div class="grid-2">
                <!-- Section 1: Upcoming Appointments -->
                <div class="card">
                    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                        <h2 class="card-title" style="margin: 0;">Upcoming Appointments</h2>
                        <a href="${ctx}/patient/appointments" class="btn btn-secondary btn-sm" style="font-size: 0.75rem;">View All</a>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${not empty upcomingAppointments}">
                                <div style="display: flex; flex-direction: column; gap: var(--space-3);">
                                    <c:forEach var="apt" items="${upcomingAppointments}">
                                        <div style="border: 1px solid var(--border-subtle); border-radius: var(--radius-md); padding: var(--space-4); background-color: var(--surface);">
                                            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: var(--space-2);">
                                                <div>
                                                    <span style="font-weight: 700; color: var(--text-primary); font-size: 1rem;">
                                                        <c:choose>
                                                            <c:when test="${apt.doctor != null}">
                                                                Dr. <c:out value="${apt.doctor.fullName}" />
                                                            </c:when>
                                                            <c:otherwise>Attending Physician</c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                    <c:if test="${apt.doctor != null && apt.doctor.specialty != null}">
                                                        <span style="display: block; font-size: 0.8125rem; color: var(--text-secondary);">
                                                            <c:out value="${apt.doctor.specialty.name}" />
                                                        </span>
                                                    </c:if>
                                                </div>
                                                <span class="badge ${apt.status == 'CONFIRMED' ? 'badge-success' : 'badge-warning'}">
                                                    <c:out value="${apt.status}" />
                                                </span>
                                            </div>
                                            <div style="display: flex; gap: var(--space-4); font-size: 0.8125rem; color: var(--text-secondary); margin-bottom: var(--space-3);">
                                                <div style="display: flex; align-items: center; gap: 4px;">
                                                    <i data-lucide="calendar" style="width: 14px; height: 14px;"></i>
                                                    <span><c:out value="${apt.appointmentDate}" /></span>
                                                </div>
                                                <div style="display: flex; align-items: center; gap: 4px;">
                                                    <i data-lucide="clock" style="width: 14px; height: 14px;"></i>
                                                    <span><c:out value="${apt.startTime}" /> - <c:out value="${apt.endTime}" /></span>
                                                </div>
                                            </div>
                                            <div style="display: flex; justify-content: space-between; align-items: center;">
                                                <span style="font-family: monospace; font-size: 0.75rem; color: var(--text-muted);">
                                                    Ref: <c:out value="${apt.referenceNumber}" />
                                                </span>
                                                <a href="${ctx}/patient/appointments/view?id=${apt.id}" class="btn btn-secondary btn-sm" style="font-size: 0.75rem; padding: 0.25rem 0.65rem;">
                                                    <span>Details</span>
                                                    <i data-lucide="arrow-right" style="width: 12px; height: 12px;"></i>
                                                </a>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-state" style="padding: var(--space-8) var(--space-4);">
                                    <div class="empty-state-icon">
                                        <i data-lucide="calendar-plus"></i>
                                    </div>
                                    <h3 class="empty-state-title" style="font-size: 1.125rem;">No upcoming appointments</h3>
                                    <p class="empty-state-desc" style="font-size: 0.875rem;">
                                        You have no scheduled consultations. Find a verified doctor and reserve a slot anytime.
                                    </p>
                                    <a href="${ctx}/patient/doctors" class="btn btn-primary btn-sm">
                                        <i data-lucide="search"></i>
                                        <span>Browse Specialists</span>
                                    </a>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- Section 2: Quick Doctor Discovery & Profile Summary -->
                <div style="display: flex; flex-direction: column; gap: var(--space-6);">
                    <!-- Quick Actions Card -->
                    <div class="card">
                        <div class="card-header">
                            <h2 class="card-title">Quick Health Actions</h2>
                        </div>
                        <div class="card-body" style="display: flex; flex-direction: column; gap: var(--space-3);">
                            <a href="${ctx}/patient/doctors" style="display: flex; align-items: center; justify-content: space-between; padding: var(--space-4); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); text-decoration: none; color: inherit; transition: background 0.15s ease;">
                                <div style="display: flex; align-items: center; gap: var(--space-3);">
                                    <div class="brand-icon-pill" style="width: 36px; height: 36px;">
                                        <i data-lucide="stethoscope" style="width: 18px; height: 18px;"></i>
                                    </div>
                                    <div>
                                        <div style="font-weight: 600; color: var(--text-primary); font-size: 0.9375rem;">Find a Specialist</div>
                                        <div style="font-size: 0.8125rem; color: var(--text-secondary);">Browse by specialty, ratings, and consultation fee</div>
                                    </div>
                                </div>
                                <i data-lucide="chevron-right" style="color: var(--text-muted);"></i>
                            </a>

                            <a href="${ctx}/patient/appointments" style="display: flex; align-items: center; justify-content: space-between; padding: var(--space-4); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); text-decoration: none; color: inherit; transition: background 0.15s ease;">
                                <div style="display: flex; align-items: center; gap: var(--space-3);">
                                    <div class="brand-icon-pill" style="width: 36px; height: 36px; background-color: var(--info-soft); color: var(--info);">
                                        <i data-lucide="calendar" style="width: 18px; height: 18px;"></i>
                                    </div>
                                    <div>
                                        <div style="font-weight: 600; color: var(--text-primary); font-size: 0.9375rem;">My Appointments</div>
                                        <div style="font-size: 0.8125rem; color: var(--text-secondary);">View upcoming bookings and historical consultations</div>
                                    </div>
                                </div>
                                <i data-lucide="chevron-right" style="color: var(--text-muted);"></i>
                            </a>

                            <a href="${ctx}/patient/profile" style="display: flex; align-items: center; justify-content: space-between; padding: var(--space-4); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); text-decoration: none; color: inherit; transition: background 0.15s ease;">
                                <div style="display: flex; align-items: center; gap: var(--space-3);">
                                    <div class="brand-icon-pill" style="width: 36px; height: 36px; background-color: var(--success-soft); color: var(--success);">
                                        <i data-lucide="user" style="width: 18px; height: 18px;"></i>
                                    </div>
                                    <div>
                                        <div style="font-weight: 600; color: var(--text-primary); font-size: 0.9375rem;">Update Personal Info</div>
                                        <div style="font-size: 0.8125rem; color: var(--text-secondary);">Manage emergency contacts, address, and phone</div>
                                    </div>
                                </div>
                                <i data-lucide="chevron-right" style="color: var(--text-muted);"></i>
                            </a>
                        </div>
                    </div>

                    <!-- Emergency Contact Info Box -->
                    <div class="card" style="background-color: var(--surface);">
                        <div class="card-header">
                            <h2 class="card-title">Patient Profile Information</h2>
                        </div>
                        <div class="card-body" style="font-size: 0.875rem;">
                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-2); margin-bottom: var(--space-2);">
                                <span style="color: var(--text-muted);">Email:</span>
                                <span style="font-weight: 500;"><c:out value="${patient.email}" /></span>
                            </div>
                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-2); margin-bottom: var(--space-2);">
                                <span style="color: var(--text-muted);">Phone:</span>
                                <span style="font-weight: 500;"><c:out value="${patient.phoneNumber != null ? patient.phoneNumber : 'Not provided'}" /></span>
                            </div>
                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-2); margin-bottom: var(--space-2);">
                                <span style="color: var(--text-muted);">Date of Birth:</span>
                                <span style="font-weight: 500;"><c:out value="${patient.dateOfBirth != null ? patient.dateOfBirth : 'Not provided'}" /></span>
                            </div>
                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-2);">
                                <span style="color: var(--text-muted);">Emergency Contact:</span>
                                <span style="font-weight: 500;">
                                    <c:out value="${patient.emergencyContactName != null ? patient.emergencyContactName : 'None specified'}" />
                                    <c:if test="${not empty patient.emergencyContactPhone}">
                                        (<c:out value="${patient.emergencyContactPhone}" />)
                                    </c:if>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
