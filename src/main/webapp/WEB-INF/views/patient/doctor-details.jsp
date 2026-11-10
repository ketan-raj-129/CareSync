<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="PATIENT" />
<c:set var="pageTitle" value="Dr. ${doctor.fullName} — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <!-- Sidebar Navigation -->
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <!-- Main Workspace -->
    <div class="dashboard-main">
        <!-- Topbar Navigation -->
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Physician Profile</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Verified medical practitioner credentialed with CareSync
                    </p>
                </div>
                <div style="display: flex; gap: var(--space-3);">
                    <a href="${ctx}/patient/doctors" class="btn btn-secondary btn-sm">
                        <i data-lucide="arrow-left"></i>
                        <span>Back to Directory</span>
                    </a>
                    <a href="${ctx}/patient/appointments/book?doctorId=${doctor.doctorProfileId}" class="btn btn-primary btn-sm">
                        <i data-lucide="calendar-plus"></i>
                        <span>Book Appointment</span>
                    </a>
                </div>
            </div>

            <div class="grid-2" style="align-items: flex-start;">
                <!-- Left: Practitioner Credential Card -->
                <div class="card">
                    <div class="card-body">
                        <div style="display: flex; gap: var(--space-4); align-items: flex-start; margin-bottom: var(--space-6);">
                            <div class="user-avatar-circle" style="width: 64px; height: 64px; font-size: 1.5rem; flex-shrink: 0;">
                                <c:out value="${doctor.firstName.substring(0, 1).toUpperCase()}" />
                            </div>
                            <div>
                                <h2 style="font-size: 1.375rem; font-weight: 800; margin: 0 0 var(--space-1) 0; color: var(--text-primary);">
                                    Dr. <c:out value="${doctor.fullName}" />
                                </h2>
                                <div style="display: flex; gap: var(--space-2); align-items: center; flex-wrap: wrap;">
                                    <span class="badge badge-primary">
                                        <c:out value="${doctor.specialty != null ? doctor.specialty.name : 'General Practice'}" />
                                    </span>
                                    <span class="badge badge-success">Active Provider</span>
                                </div>
                            </div>
                        </div>

                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4); margin-bottom: var(--space-6); padding: var(--space-4); background-color: var(--surface-muted); border-radius: var(--radius-md);">
                            <div>
                                <span style="font-size: 0.75rem; color: var(--text-muted); display: block; text-transform: uppercase; font-weight: 600;">Experience</span>
                                <span style="font-size: 1rem; font-weight: 700; color: var(--text-primary);"><c:out value="${doctor.experienceYears}" /> Years</span>
                            </div>
                            <div>
                                <span style="font-size: 0.75rem; color: var(--text-muted); display: block; text-transform: uppercase; font-weight: 600;">Consultation Fee</span>
                                <span style="font-size: 1.125rem; font-weight: 700; color: var(--primary);">$<c:out value="${doctor.consultationFee}" /></span>
                            </div>
                            <div>
                                <span style="font-size: 0.75rem; color: var(--text-muted); display: block; text-transform: uppercase; font-weight: 600;">License Number</span>
                                <span style="font-size: 0.875rem; font-weight: 600; font-family: monospace;"><c:out value="${doctor.licenseNumber}" /></span>
                            </div>
                            <div>
                                <span style="font-size: 0.75rem; color: var(--text-muted); display: block; text-transform: uppercase; font-weight: 600;">Clinic Room</span>
                                <span style="font-size: 0.875rem; font-weight: 600;"><c:out value="${doctor.roomNumber != null ? doctor.roomNumber : 'Main Consultation Suite'}" /></span>
                            </div>
                        </div>

                        <div style="margin-bottom: var(--space-6);">
                            <h3 style="font-size: 0.9375rem; font-weight: 700; margin: 0 0 var(--space-2) 0;">Medical Qualifications</h3>
                            <p style="font-size: 0.875rem; color: var(--text-secondary); margin: 0;">
                                <c:out value="${doctor.qualification}" />
                            </p>
                        </div>

                        <c:if test="${not empty doctor.bio}">
                            <div>
                                <h3 style="font-size: 0.9375rem; font-weight: 700; margin: 0 0 var(--space-2) 0;">Professional Biography</h3>
                                <p style="font-size: 0.875rem; color: var(--text-secondary); line-height: 1.6; margin: 0;">
                                    <c:out value="${doctor.bio}" />
                                </p>
                            </div>
                        </c:if>
                    </div>
                </div>

                <!-- Right: Booking CTA & Information Card -->
                <div style="display: flex; flex-direction: column; gap: var(--space-6);">
                    <div class="card" style="border: 2px solid var(--primary-soft);">
                        <div class="card-header">
                            <h3 class="card-title" style="margin: 0;">Reserve an In-Person Consultation</h3>
                        </div>
                        <div class="card-body">
                            <p style="font-size: 0.875rem; color: var(--text-secondary); line-height: 1.5; margin-bottom: var(--space-5);">
                                Schedule a 30-minute consultation slot with Dr. <c:out value="${doctor.fullName}" />. Slots are reserved instantly upon confirmation and protected against double-booking.
                            </p>
                            <a href="${ctx}/patient/appointments/book?doctorId=${doctor.doctorProfileId}" class="btn btn-primary" style="width: 100%;">
                                <i data-lucide="calendar-check"></i>
                                <span>Check Available Slots &amp; Book</span>
                            </a>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-header">
                            <h3 class="card-title" style="margin: 0;">Clinical Guidelines &amp; Policies</h3>
                        </div>
                        <div class="card-body" style="font-size: 0.8125rem; color: var(--text-secondary); display: flex; flex-direction: column; gap: var(--space-3);">
                            <div style="display: flex; gap: var(--space-2); align-items: flex-start;">
                                <i data-lucide="check" style="width: 16px; height: 16px; color: var(--success); flex-shrink: 0; margin-top: 2px;"></i>
                                <span>Please arrive 10 minutes prior to your scheduled consultation window.</span>
                            </div>
                            <div style="display: flex; gap: var(--space-2); align-items: flex-start;">
                                <i data-lucide="check" style="width: 16px; height: 16px; color: var(--success); flex-shrink: 0; margin-top: 2px;"></i>
                                <span>Cancellations are accepted up to 2 hours prior to the scheduled start time.</span>
                            </div>
                            <div style="display: flex; gap: var(--space-2); align-items: flex-start;">
                                <i data-lucide="check" style="width: 16px; height: 16px; color: var(--success); flex-shrink: 0; margin-top: 2px;"></i>
                                <span>Digital prescriptions and diagnostic summaries are made available in your health portal immediately following visit completion.</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

