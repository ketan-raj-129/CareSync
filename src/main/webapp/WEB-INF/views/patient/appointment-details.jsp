<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="PATIENT" />
<c:set var="pageTitle" value="Appointment ${appointment.referenceNumber} — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <!-- Sidebar Navigation -->
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <!-- Main Workspace -->
    <div class="dashboard-main">
        <!-- Topbar Navigation -->
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <!-- Navigation Breadcrumb & Header -->
            <div style="margin-bottom: var(--space-4);">
                <a href="${ctx}/patient/appointments" class="btn btn-secondary btn-sm" style="display: inline-flex; align-items: center; gap: var(--space-2); margin-bottom: var(--space-3);">
                    <i data-lucide="arrow-left" style="width: 14px; height: 14px;"></i>
                    <span>Back to Appointments</span>
                </a>

                <div class="page-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: var(--space-4);">
                    <div>
                        <div style="display: flex; align-items: center; gap: var(--space-3); margin-bottom: 4px;">
                            <h1 class="page-title" style="margin: 0;">Appointment Details</h1>
                            <c:choose>
                                <c:when test="${appointment.status.name() == 'CONFIRMED'}">
                                    <span class="badge badge-success" style="font-size: 0.875rem;">Confirmed</span>
                                </c:when>
                                <c:when test="${appointment.status.name() == 'PENDING'}">
                                    <span class="badge badge-warning" style="font-size: 0.875rem;">Pending Confirmation</span>
                                </c:when>
                                <c:when test="${appointment.status.name() == 'COMPLETED'}">
                                    <span class="badge badge-info" style="font-size: 0.875rem;">Completed</span>
                                </c:when>
                                <c:when test="${appointment.status.name() == 'CANCELLED'}">
                                    <span class="badge badge-danger" style="font-size: 0.875rem;">Cancelled</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge badge-neutral" style="font-size: 0.875rem;"><c:out value="${appointment.status.name()}" /></span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem; font-family: monospace;">
                            Booking Reference: <c:out value="${appointment.referenceNumber}" />
                        </p>
                    </div>

                    <div>
                        <c:if test="${appointment.canBeCancelledByPatient()}">
                            <button type="button" class="btn btn-outline-danger" 
                                    onclick="openCancelModal('${appointment.id}', '${appointment.referenceNumber}', 'Dr. ${appointment.doctor != null ? appointment.doctor.fullName : 'Doctor'}')">
                                <i data-lucide="x-circle"></i>
                                <span>Cancel Appointment</span>
                            </button>
                        </c:if>
                    </div>
                </div>
            </div>

            <!-- Flash / Notification Messages -->
            <c:if test="${not empty successMessage}">
                <div class="alert alert-success" style="display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-5);">
                    <i data-lucide="check-circle" style="color: var(--success); flex-shrink: 0;"></i>
                    <span style="font-weight: 500;"><c:out value="${successMessage}" /></span>
                </div>
            </c:if>

            <div style="display: grid; grid-template-columns: 2fr 1.1fr; gap: var(--space-6); align-items: start;">
                
                <!-- Left Column: Consultation Details & Clinical Notes -->
                <div style="display: flex; flex-direction: column; gap: var(--space-5);">
                    
                    <!-- Schedule Summary Card -->
                    <div class="card">
                        <div class="card-header" style="padding: var(--space-4) var(--space-5); border-bottom: 1px solid var(--border-subtle);">
                            <h2 style="font-size: 1.125rem; font-weight: 700; margin: 0; display: flex; align-items: center; gap: var(--space-2);">
                                <i data-lucide="calendar" style="color: var(--primary);"></i>
                                Consultation Schedule
                            </h2>
                        </div>
                        <div class="card-body" style="padding: var(--space-5);">
                            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: var(--space-4);">
                                <div>
                                    <span style="display: block; font-size: 0.8125rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600; letter-spacing: 0.5px;">Date</span>
                                    <span style="font-size: 1.0625rem; font-weight: 700; color: var(--text-primary); margin-top: 2px; display: block;">
                                        <c:out value="${appointment.appointmentDate}" />
                                    </span>
                                </div>
                                <div>
                                    <span style="display: block; font-size: 0.8125rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600; letter-spacing: 0.5px;">Time Slot</span>
                                    <span style="font-size: 1.0625rem; font-weight: 700; color: var(--text-primary); margin-top: 2px; display: block;">
                                        <c:out value="${appointment.startTime}" /> – <c:out value="${appointment.endTime}" />
                                    </span>
                                    <span style="font-size: 0.75rem; color: var(--text-muted);">Indian Standard Time (Asia/Kolkata)</span>
                                </div>
                                <div>
                                    <span style="display: block; font-size: 0.8125rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600; letter-spacing: 0.5px;">Booking Created</span>
                                    <span style="font-size: 0.9375rem; font-weight: 600; color: var(--text-secondary); margin-top: 2px; display: block;">
                                        <c:out value="${appointment.createdAt}" />
                                    </span>
                                </div>
                            </div>

                            <c:if test="${not empty appointment.reasonForVisit}">
                                <div style="margin-top: var(--space-5); padding-top: var(--space-4); border-top: 1px solid var(--border-subtle);">
                                    <span style="display: block; font-size: 0.8125rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600; letter-spacing: 0.5px; margin-bottom: var(--space-2);">
                                        Reason for Consultation
                                    </span>
                                    <div style="background: var(--bg-hover); padding: var(--space-3) var(--space-4); border-radius: var(--radius-md); font-size: 0.9375rem; color: var(--text-primary); line-height: 1.5;">
                                        <c:out value="${appointment.reasonForVisit}" />
                                    </div>
                                </div>
                            </c:if>

                            <!-- Cancellation Alert if Cancelled -->
                            <c:if test="${appointment.status.name() == 'CANCELLED'}">
                                <div style="margin-top: var(--space-5); padding: var(--space-4); background: #fef2f2; border: 1px solid #fecaca; border-radius: var(--radius-md);">
                                    <div style="display: flex; align-items: center; gap: var(--space-2); color: #991b1b; font-weight: 700; margin-bottom: 4px;">
                                        <i data-lucide="alert-octagon" style="width: 18px; height: 18px;"></i>
                                        Appointment Cancelled
                                    </div>
                                    <p style="margin: 0; font-size: 0.875rem; color: #b91c1c;">
                                        Reason: <c:out value="${not empty appointment.cancellationReason ? appointment.cancellationReason : 'No cancellation reason specified.'}" />
                                    </p>
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <!-- Consultation Guidance -->
                    <div class="card">
                        <div class="card-header" style="padding: var(--space-4) var(--space-5); border-bottom: 1px solid var(--border-subtle);">
                            <h2 style="font-size: 1.125rem; font-weight: 700; margin: 0; display: flex; align-items: center; gap: var(--space-2);">
                                <i data-lucide="info" style="color: var(--primary);"></i>
                                Patient Consultation Instructions
                            </h2>
                        </div>
                        <div class="card-body" style="padding: var(--space-5); font-size: 0.875rem; color: var(--text-secondary); line-height: 1.6;">
                            <ul style="margin: 0; padding-left: var(--space-4); display: flex; flex-direction: column; gap: var(--space-2);">
                                <li>Please arrive at the clinic <strong>10 minutes prior</strong> to your scheduled slot.</li>
                                <li>Carry any previous diagnostic reports, prescription slips, or medication history.</li>
                                <li>Report directly to <strong>Room <c:out value="${appointment.doctor != null ? appointment.doctor.roomNumber : 'Front Desk'}" /></strong> upon check-in.</li>
                                <li>Cancellations or reschedules must be made at least <strong>2 hours in advance</strong>.</li>
                            </ul>
                        </div>
                    </div>

                </div>

                <!-- Right Column: Doctor & Facility Profile -->
                <div style="display: flex; flex-direction: column; gap: var(--space-5);">
                    
                    <div class="card">
                        <div class="card-header" style="padding: var(--space-4) var(--space-5); border-bottom: 1px solid var(--border-subtle);">
                            <h2 style="font-size: 1.125rem; font-weight: 700; margin: 0; display: flex; align-items: center; gap: var(--space-2);">
                                <i data-lucide="stethoscope" style="color: var(--primary);"></i>
                                Assigned Physician
                            </h2>
                        </div>
                        <div class="card-body" style="padding: var(--space-5);">
                            <div style="display: flex; gap: var(--space-4); align-items: center; margin-bottom: var(--space-4);">
                                <div class="user-avatar-circle" style="width: 56px; height: 56px; font-size: 1.375rem; flex-shrink: 0;">
                                    <c:out value="${appointment.doctor != null ? appointment.doctor.firstName.substring(0, 1).toUpperCase() : 'D'}" />
                                </div>
                                <div>
                                    <h3 style="font-size: 1.125rem; font-weight: 700; margin: 0; color: var(--text-primary);">
                                        Dr. <c:out value="${appointment.doctor != null ? appointment.doctor.fullName : 'Physician'}" />
                                    </h3>
                                    <div style="color: var(--primary); font-weight: 600; font-size: 0.875rem;">
                                        <c:out value="${appointment.doctor != null && appointment.doctor.specialty != null ? appointment.doctor.specialty.name : 'General Practice'}" />
                                    </div>
                                    <div style="color: var(--text-muted); font-size: 0.8125rem;">
                                        <c:out value="${appointment.doctor != null ? appointment.doctor.qualification : ''}" />
                                    </div>
                                </div>
                            </div>

                            <div style="display: flex; flex-direction: column; gap: var(--space-3); padding-top: var(--space-3); border-top: 1px solid var(--border-subtle); font-size: 0.875rem;">
                                <div style="display: flex; justify-content: space-between;">
                                    <span style="color: var(--text-secondary);">Room / Suite:</span>
                                    <span style="font-weight: 600; color: var(--text-primary);">
                                        Room <c:out value="${appointment.doctor != null ? appointment.doctor.roomNumber : 'N/A'}" />
                                    </span>
                                </div>
                                <div style="display: flex; justify-content: space-between;">
                                    <span style="color: var(--text-secondary);">Experience:</span>
                                    <span style="font-weight: 600; color: var(--text-primary);">
                                        <c:out value="${appointment.doctor != null ? appointment.doctor.experienceYears : '0'}" />+ years
                                    </span>
                                </div>
                                <div style="display: flex; justify-content: space-between;">
                                    <span style="color: var(--text-secondary);">Consultation Fee:</span>
                                    <span style="font-weight: 700; color: var(--primary); font-size: 1rem;">
                                        $<c:out value="${appointment.doctor != null ? appointment.doctor.consultationFee : '0.00'}" />
                                    </span>
                                </div>
                            </div>

                            <c:if test="${appointment.doctor != null}">
                                <div style="margin-top: var(--space-5);">
                                    <a href="${ctx}/patient/doctors/view?id=${appointment.doctor.doctorProfileId}" class="btn btn-secondary" style="width: 100%; text-align: center; justify-content: center;">
                                        <i data-lucide="user"></i>
                                        <span>View Doctor Profile</span>
                                    </a>
                                </div>
                            </c:if>
                        </div>
                    </div>

                </div>

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

