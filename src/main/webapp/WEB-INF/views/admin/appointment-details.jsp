<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="appointments" />
<c:set var="pageTitle" value="Appointment Audit Details — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <div style="display: flex; align-items: center; gap: var(--space-2); margin-bottom: 4px;">
                        <a href="${ctx}/admin/appointments" style="color: var(--text-secondary); text-decoration: none; font-size: 0.875rem;">
                            &larr; Back to Appointments
                        </a>
                    </div>
                    <h1 class="page-title">
                        Appointment <c:out value="${appointment.referenceNumber}" />
                    </h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        System ID: #<c:out value="${appointment.appointmentId}" /> &bull; Created: <c:out value="${appointment.createdAt}" />
                    </p>
                </div>
                <div>
                    <c:choose>
                        <c:when test="${appointment.status == 'CONFIRMED'}">
                            <span class="badge badge-success" style="font-size: 0.875rem; padding: 6px 12px;">Confirmed</span>
                        </c:when>
                        <c:when test="${appointment.status == 'PENDING'}">
                            <span class="badge badge-warning" style="font-size: 0.875rem; padding: 6px 12px;">Pending</span>
                        </c:when>
                        <c:when test="${appointment.status == 'COMPLETED'}">
                            <span class="badge badge-primary" style="font-size: 0.875rem; padding: 6px 12px;">Completed</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge badge-danger" style="font-size: 0.875rem; padding: 6px 12px;">Cancelled</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Clinical Data Segregation Banner -->
            <div class="alert alert-info" style="margin-bottom: var(--space-6);">
                <i data-lucide="shield" style="flex-shrink: 0;"></i>
                <div>
                    <strong>Data Segregation Notice:</strong> Confidential consultation encounter notes and medical prescriptions are maintained exclusively in doctor-patient clinical workflows and are not exposed to the administrative audit view.
                </div>
            </div>

            <div class="grid-2">
                <!-- Appointment & Schedule Details -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title" style="font-size: 1.125rem;">Consultation Schedule</h2>
                    </div>
                    <div class="card-body">
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4);">
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Date</label>
                                <div style="font-weight: 600; font-size: 1.125rem;"><c:out value="${appointment.appointmentDate}" /></div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Time Window</label>
                                <div style="font-weight: 600; font-size: 1.125rem;">
                                    <c:out value="${appointment.startTime}" /> – <c:out value="${appointment.endTime}" />
                                </div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Doctor</label>
                                <div style="font-weight: 500;">
                                    Dr. <c:out value="${appointment.doctor.firstName}" /> <c:out value="${appointment.doctor.lastName}" />
                                </div>
                                <div style="font-size: 0.8125rem; color: var(--text-secondary);">
                                    <c:out value="${appointment.doctor.specialty.name}" /> &bull; Room <c:out value="${appointment.doctor.roomNumber}" />
                                </div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Patient</label>
                                <div style="font-weight: 500;">
                                    <c:out value="${appointment.patient.firstName}" /> <c:out value="${appointment.patient.lastName}" />
                                </div>
                                <div style="font-size: 0.8125rem; color: var(--text-secondary);">
                                    <c:out value="${appointment.patient.email}" />
                                </div>
                            </div>
                        </div>

                        <div style="margin-top: var(--space-4); padding-top: var(--space-4); border-top: 1px solid var(--border-color);">
                            <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Patient Reason for Visit</label>
                            <div style="margin-top: 4px; color: var(--text-primary); font-size: 0.9375rem;">
                                <c:out value="${appointment.reason != null ? appointment.reason : 'General medical consultation'}" />
                            </div>
                        </div>

                        <c:if test="${appointment.status == 'CANCELLED'}">
                            <div style="margin-top: var(--space-4); padding: var(--space-4); background-color: var(--danger-soft); border-radius: var(--radius-md);">
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--danger); font-weight: 700;">Cancellation Record</label>
                                <div style="font-size: 0.875rem; color: var(--text-primary); margin-top: 4px;">
                                    <strong>Reason:</strong> <c:out value="${appointment.cancellationReason != null ? appointment.cancellationReason : 'No reason recorded'}" />
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>

                <!-- Administrative Controls -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title" style="font-size: 1.125rem;">Administrative Governance</h2>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${appointment.status == 'PENDING' || appointment.status == 'CONFIRMED'}">
                                <p style="font-size: 0.875rem; color: var(--text-secondary); margin-bottom: var(--space-4);">
                                    Administrators possess override authority to cancel upcoming consultations for emergency facility maintenance or doctor unavailability. A mandatory justification reason is logged.
                                </p>

                                <form method="post" action="${ctx}/admin/appointments/action">
                                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                    <input type="hidden" name="appointmentId" value="<c:out value='${appointment.appointmentId}' />" />
                                    <input type="hidden" name="action" value="CANCEL" />

                                    <div style="margin-bottom: var(--space-4);">
                                        <label class="form-label" for="cancelReason">Administrative Cancellation Reason *</label>
                                        <textarea class="form-control" id="cancelReason" name="reason" rows="3" required
                                                  placeholder="Provide mandatory reason for audit record..."></textarea>
                                    </div>

                                    <button type="submit" class="btn btn-danger" onclick="return confirm('Are you sure you want to administratively cancel this appointment?');">
                                        <i data-lucide="x-circle"></i>
                                        <span>Cancel Appointment</span>
                                    </button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-state" style="padding: var(--space-6) var(--space-4);">
                                    <div class="empty-state-icon" style="background-color: var(--background);">
                                        <i data-lucide="lock"></i>
                                    </div>
                                    <h3 class="empty-state-title" style="font-size: 1rem;">Booking is Terminal</h3>
                                    <p class="empty-state-desc" style="font-size: 0.8125rem;">
                                        This appointment has completed its lifecycle (<c:out value="${appointment.status}" />) and cannot be modified.
                                    </p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

