<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="PATIENT" />
<c:set var="pageTitle" value="My Appointments — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <!-- Sidebar Navigation -->
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <!-- Main Workspace -->
    <div class="dashboard-main">
        <!-- Topbar Navigation -->
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: var(--space-4);">
                <div>
                    <h1 class="page-title">My Appointments</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        View and manage your scheduled healthcare consultations
                    </p>
                </div>
                <div>
                    <a href="${ctx}/patient/doctors" class="btn btn-primary">
                        <i data-lucide="plus-circle"></i>
                        <span>Book New Appointment</span>
                    </a>
                </div>
            </div>

            <!-- Flash / Notification Messages -->
            <c:if test="${not empty successMessage}">
                <div class="alert alert-success" style="display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-5);">
                    <i data-lucide="check-circle" style="color: var(--success); flex-shrink: 0;"></i>
                    <span style="font-weight: 500;"><c:out value="${successMessage}" /></span>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger" style="display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-5);">
                    <i data-lucide="alert-circle" style="color: var(--danger); flex-shrink: 0;"></i>
                    <span style="font-weight: 500;"><c:out value="${errorMessage}" /></span>
                </div>
            </c:if>

            <!-- Section 1: Upcoming Appointments -->
            <div style="margin-bottom: var(--space-7);">
                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--space-4);">
                    <h2 style="font-size: 1.25rem; font-weight: 700; margin: 0; display: flex; align-items: center; gap: var(--space-2);">
                        <i data-lucide="calendar" style="color: var(--primary);"></i>
                        Upcoming Consultations
                        <span class="badge badge-primary" style="font-size: 0.8125rem; font-weight: 600; margin-left: var(--space-2);">
                            <c:out value="${upcomingAppointments != null ? upcomingAppointments.size() : 0}" />
                        </span>
                    </h2>
                </div>

                <c:choose>
                    <c:when test="${not empty upcomingAppointments}">
                        <div style="display: flex; flex-direction: column; gap: var(--space-4);">
                            <c:forEach var="apt" items="${upcomingAppointments}">
                                <div class="card" style="border-left: 4px solid var(--primary); transition: transform 0.15s ease, box-shadow 0.15s ease;">
                                    <div class="card-body" style="padding: var(--space-4) var(--space-5);">
                                        <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: var(--space-4);">
                                            
                                            <!-- Doctor & Service Details -->
                                            <div style="display: flex; gap: var(--space-4); align-items: flex-start; min-width: 260px;">
                                                <div class="user-avatar-circle" style="width: 52px; height: 52px; font-size: 1.25rem; flex-shrink: 0;">
                                                    <c:out value="${apt.doctor != null ? apt.doctor.firstName.substring(0, 1).toUpperCase() : 'D'}" />
                                                </div>
                                                <div>
                                                    <div style="display: flex; align-items: center; gap: var(--space-2); margin-bottom: 2px;">
                                                        <h3 style="font-size: 1.125rem; font-weight: 700; margin: 0; color: var(--text-primary);">
                                                            Dr. <c:out value="${apt.doctor != null ? apt.doctor.fullName : 'Specialist'}" />
                                                        </h3>
                                                    </div>
                                                    <div style="font-size: 0.875rem; color: var(--text-secondary); margin-bottom: var(--space-2);">
                                                        <c:out value="${apt.doctor != null && apt.doctor.specialty != null ? apt.doctor.specialty.name : 'General Medicine'}" />
                                                        <c:if test="${apt.doctor != null && not empty apt.doctor.roomNumber}">
                                                            &bull; Room <c:out value="${apt.doctor.roomNumber}" />
                                                        </c:if>
                                                    </div>
                                                    <div style="font-size: 0.8125rem; color: var(--text-muted); font-family: monospace;">
                                                        Ref: <c:out value="${apt.referenceNumber}" />
                                                    </div>
                                                </div>
                                            </div>

                                            <!-- Schedule & Timing -->
                                            <div style="min-width: 200px;">
                                                <div style="display: flex; align-items: center; gap: var(--space-2); font-weight: 600; color: var(--text-primary); margin-bottom: 4px;">
                                                    <i data-lucide="calendar" style="width: 16px; height: 16px; color: var(--primary);"></i>
                                                    <c:out value="${apt.appointmentDate}" />
                                                </div>
                                                <div style="display: flex; align-items: center; gap: var(--space-2); font-size: 0.875rem; color: var(--text-secondary); margin-bottom: 4px;">
                                                    <i data-lucide="clock" style="width: 16px; height: 16px; color: var(--text-muted);"></i>
                                                    <c:out value="${apt.startTime}" /> – <c:out value="${apt.endTime}" /> (IST)
                                                </div>
                                                <c:if test="${apt.doctor != null}">
                                                    <div style="font-size: 0.8125rem; color: var(--text-muted);">
                                                        Fee: <span style="font-weight: 600; color: var(--text-primary);">$<c:out value="${apt.doctor.consultationFee}" /></span>
                                                    </div>
                                                </c:if>
                                            </div>

                                            <!-- Status & Action Controls -->
                                            <div style="display: flex; flex-direction: column; align-items: flex-end; gap: var(--space-3); min-width: 160px;">
                                                <div>
                                                    <c:choose>
                                                        <c:when test="${apt.status.name() == 'CONFIRMED'}">
                                                            <span class="badge badge-success" style="font-size: 0.8125rem; padding: 4px 10px;">Confirmed</span>
                                                        </c:when>
                                                        <c:when test="${apt.status.name() == 'PENDING'}">
                                                            <span class="badge badge-warning" style="font-size: 0.8125rem; padding: 4px 10px;">Pending</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-neutral" style="font-size: 0.8125rem; padding: 4px 10px;"><c:out value="${apt.status.name()}" /></span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>

                                                <div style="display: flex; gap: var(--space-2);">
                                                    <a href="${ctx}/patient/appointments/view?id=${apt.id}" class="btn btn-secondary btn-sm" title="View Details">
                                                        <i data-lucide="eye" style="width: 14px; height: 14px;"></i>
                                                        <span>Details</span>
                                                    </a>
                                                    <c:if test="${apt.canBeCancelledByPatient()}">
                                                        <button type="button" class="btn btn-outline-danger btn-sm" 
                                                                onclick="openCancelModal('${apt.id}', '${apt.referenceNumber}', 'Dr. ${apt.doctor != null ? apt.doctor.fullName : 'Doctor'}')">
                                                            <i data-lucide="x" style="width: 14px; height: 14px;"></i>
                                                            <span>Cancel</span>
                                                        </button>
                                                    </c:if>
                                                </div>
                                            </div>

                                        </div>

                                        <c:if test="${not empty apt.reasonForVisit}">
                                            <div style="margin-top: var(--space-3); padding-top: var(--space-3); border-top: 1px solid var(--border-subtle); font-size: 0.875rem; color: var(--text-secondary);">
                                                <strong style="color: var(--text-primary); font-size: 0.8125rem; text-transform: uppercase; letter-spacing: 0.5px;">Reason for visit:</strong>
                                                <p style="margin: 4px 0 0 0;"><c:out value="${apt.reasonForVisit}" /></p>
                                            </div>
                                        </c:if>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="card" style="text-align: center; padding: var(--space-8) var(--space-4);">
                            <div style="max-width: 380px; margin: 0 auto;">
                                <div style="width: 60px; height: 60px; border-radius: 50%; background: var(--bg-hover); display: flex; align-items: center; justify-content: center; margin: 0 auto var(--space-3) auto;">
                                    <i data-lucide="calendar-check" style="width: 30px; height: 30px; color: var(--text-muted);"></i>
                                </div>
                                <h3 style="font-size: 1.125rem; font-weight: 700; margin-bottom: var(--space-2);">No Upcoming Appointments</h3>
                                <p style="color: var(--text-secondary); font-size: 0.875rem; margin-bottom: var(--space-4);">
                                    You have no scheduled consultations at this time. Browse our doctors and book your next appointment easily.
                                </p>
                                <a href="${ctx}/patient/doctors" class="btn btn-primary">
                                    <i data-lucide="search"></i>
                                    <span>Browse Doctors</span>
                                </a>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- Section 2: Consultation History (Past Consultations) -->
            <div>
                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--space-4);">
                    <h2 style="font-size: 1.25rem; font-weight: 700; margin: 0; display: flex; align-items: center; gap: var(--space-2);">
                        <i data-lucide="history" style="color: var(--text-secondary);"></i>
                        Past Consultations & History
                        <span class="badge badge-neutral" style="font-size: 0.8125rem; font-weight: 600; margin-left: var(--space-2);">
                            <c:out value="${pastAppointments != null ? pastAppointments.size() : 0}" />
                        </span>
                    </h2>
                </div>

                <c:choose>
                    <c:when test="${not empty pastAppointments}">
                        <div class="card">
                            <div class="table-responsive">
                                <table class="table" style="margin-bottom: 0;">
                                    <thead>
                                        <tr>
                                            <th>Reference</th>
                                            <th>Doctor & Specialty</th>
                                            <th>Date & Time</th>
                                            <th>Status</th>
                                            <th style="text-align: right;">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="past" items="${pastAppointments}">
                                            <tr>
                                                <td style="font-family: monospace; font-size: 0.8125rem; font-weight: 600;">
                                                    <c:out value="${past.referenceNumber}" />
                                                </td>
                                                <td>
                                                    <div style="font-weight: 600; color: var(--text-primary);">
                                                        Dr. <c:out value="${past.doctor != null ? past.doctor.fullName : 'Doctor'}" />
                                                    </div>
                                                    <div style="font-size: 0.8125rem; color: var(--text-muted);">
                                                        <c:out value="${past.doctor != null && past.doctor.specialty != null ? past.doctor.specialty.name : 'General'}" />
                                                    </div>
                                                </td>
                                                <td>
                                                    <div style="color: var(--text-primary);"><c:out value="${past.appointmentDate}" /></div>
                                                    <div style="font-size: 0.8125rem; color: var(--text-muted);"><c:out value="${past.startTime}" /> – <c:out value="${past.endTime}" /></div>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${past.status.name() == 'COMPLETED'}">
                                                            <span class="badge badge-info">Completed</span>
                                                        </c:when>
                                                        <c:when test="${past.status.name() == 'CANCELLED'}">
                                                            <span class="badge badge-danger">Cancelled</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-neutral"><c:out value="${past.status.name()}" /></span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="text-align: right;">
                                                    <a href="${ctx}/patient/appointments/view?id=${past.id}" class="btn btn-secondary btn-sm">
                                                        <i data-lucide="eye" style="width: 14px; height: 14px;"></i>
                                                        <span>View</span>
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="card" style="padding: var(--space-6); text-align: center; color: var(--text-muted);">
                            <p style="margin: 0; font-size: 0.9375rem;">No historical or past consultations found.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
    </div>
</div>

<!-- Cancellation Confirmation Modal -->
<div id="cancellationModal" style="display: none; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(15, 23, 42, 0.6); z-index: 1050; align-items: center; justify-content: center;">
    <div class="card" style="width: 100%; max-width: 480px; margin: var(--space-4); box-shadow: var(--shadow-xl); border: 1px solid var(--border-color);">
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-subtle); padding: var(--space-4);">
            <h3 style="font-size: 1.125rem; font-weight: 700; margin: 0; color: var(--text-primary); display: flex; align-items: center; gap: var(--space-2);">
                <i data-lucide="alert-triangle" style="color: var(--danger);"></i>
                Cancel Appointment
            </h3>
            <button type="button" onclick="closeCancelModal()" style="background: none; border: none; cursor: pointer; color: var(--text-muted); padding: 4px;">
                <i data-lucide="x"></i>
            </button>
        </div>
        <div class="card-body" style="padding: var(--space-5);">
            <p style="color: var(--text-secondary); font-size: 0.9375rem; margin-bottom: var(--space-4);">
                Are you sure you want to cancel consultation <strong id="modalRefNumber" style="color: var(--text-primary);"></strong> with <span id="modalDocName" style="font-weight: 600;"></span>?
            </p>
            <p style="font-size: 0.8125rem; color: var(--text-muted); background: var(--bg-hover); padding: var(--space-3); border-radius: var(--radius-md); margin-bottom: var(--space-4);">
                <i data-lucide="info" style="width: 14px; height: 14px; display: inline-block; vertical-align: middle;"></i>
                Note: Appointments can only be cancelled at least 2 hours prior to scheduled consultation time.
            </p>

            <form id="cancelAppointmentForm" action="${ctx}/patient/appointments/cancel" method="POST">
                <input type="hidden" name="csrfToken" value="${csrfToken}" />
                <input type="hidden" id="modalAppointmentId" name="appointmentId" value="" />

                <div class="form-group" style="margin-bottom: var(--space-5);">
                    <label class="form-label" for="cancellationReason">Reason for cancellation (optional):</label>
                    <textarea class="form-control" id="cancellationReason" name="cancellationReason" rows="3" 
                              placeholder="Please share why you are cancelling this appointment..."></textarea>
                </div>

                <div style="display: flex; justify-content: flex-end; gap: var(--space-3);">
                    <button type="button" class="btn btn-secondary" onclick="closeCancelModal()">Keep Appointment</button>
                    <button type="submit" class="btn btn-danger">Confirm Cancellation</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    function openCancelModal(appointmentId, refNumber, doctorName) {
        document.getElementById('modalAppointmentId').value = appointmentId;
        document.getElementById('modalRefNumber').textContent = refNumber;
        document.getElementById('modalDocName').textContent = doctorName;
        document.getElementById('cancellationModal').style.display = 'flex';
        lucide.createIcons();
    }

    function closeCancelModal() {
        document.getElementById('cancellationModal').style.display = 'none';
        document.getElementById('cancellationReason').value = '';
    }

    window.addEventListener('click', function(event) {
        var modal = document.getElementById('cancellationModal');
        if (event.target === modal) {
            closeCancelModal();
        }
    });
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

