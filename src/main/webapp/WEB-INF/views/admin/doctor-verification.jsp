<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="doctors" />
<c:set var="pageTitle" value="Doctor Credential Verification — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <div style="display: flex; align-items: center; gap: var(--space-2); margin-bottom: 4px;">
                        <a href="${ctx}/admin/doctors" style="color: var(--text-secondary); text-decoration: none; font-size: 0.875rem;">
                            &larr; Back to Doctor Registry
                        </a>
                    </div>
                    <h1 class="page-title">Physician Credential Verification</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Medical council registration and practice eligibility inspection for Dr. <c:out value="${doctor.firstName}" /> <c:out value="${doctor.lastName}" />
                    </p>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Schema Architecture Compliance Banner -->
            <div class="alert alert-info" style="margin-bottom: var(--space-6);">
                <i data-lucide="info" style="flex-shrink: 0;"></i>
                <div>
                    <strong>Verification Governance Note:</strong> CareSync Phase 6 enforces physician credential authorization through account status transitions (<code>ACTIVE</code>, <code>INACTIVE</code>, <code>SUSPENDED</code>) and practice availability. A dedicated <code>is_verified</code> column has been specified for Phase 7 database migration.
                </div>
            </div>

            <div class="grid-2">
                <!-- Credential Inspection Card -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title" style="font-size: 1.125rem;">Submitted Credentials</h2>
                    </div>
                    <div class="card-body">
                        <div style="display: flex; flex-direction: column; gap: var(--space-4);">
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Full Practitioner Name</label>
                                <div style="font-size: 1.125rem; font-weight: 600;">Dr. <c:out value="${doctor.firstName}" /> <c:out value="${doctor.lastName}" /></div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Medical Council License Number</label>
                                <div style="font-size: 1.125rem; font-family: monospace; font-weight: 700; color: var(--primary);">
                                    <c:out value="${doctor.licenseNumber}" />
                                </div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Specialty Department</label>
                                <div style="font-weight: 500;"><c:out value="${doctor.specialty != null ? doctor.specialty.name : 'Unassigned'}" /></div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Qualifications &amp; Degrees</label>
                                <div style="font-weight: 500;"><c:out value="${doctor.qualifications != null ? doctor.qualifications : 'Not specified'}" /></div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Clinical Experience</label>
                                <div style="font-weight: 500;"><c:out value="${doctor.yearsOfExperience != null ? doctor.yearsOfExperience : 0}" /> Years</div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Assigned Facility Suite</label>
                                <div style="font-weight: 500;"><c:out value="${doctor.roomNumber != null ? doctor.roomNumber : 'Pending allocation'}" /></div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Standard Consultation Fee</label>
                                <div style="font-weight: 500;">₹<c:out value="${doctor.consultationFee}" /></div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Verification Action Card -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title" style="font-size: 1.125rem;">Credential Decision &amp; Authorization</h2>
                    </div>
                    <div class="card-body">
                        <div style="margin-bottom: var(--space-6); padding: var(--space-4); background-color: var(--background); border-radius: var(--radius-md);">
                            <div style="font-size: 0.8125rem; color: var(--text-secondary); font-weight: 600; text-transform: uppercase;">Current Verification State</div>
                            <div style="display: flex; align-items: center; gap: var(--space-2); margin-top: 6px;">
                                <span class="badge ${doctor.user.status == 'ACTIVE' ? 'badge-success' : 'badge-warning'}" style="font-size: 0.875rem; padding: 4px 10px;">
                                    Account: <c:out value="${doctor.user.status}" />
                                </span>
                                <span class="badge ${doctor.available ? 'badge-success' : 'badge-secondary'}" style="font-size: 0.875rem; padding: 4px 10px;">
                                    Booking: ${doctor.available ? 'Available' : 'Unavailable'}
                                </span>
                            </div>
                        </div>

                        <!-- Approve & Activate Form -->
                        <form method="post" action="${ctx}/admin/doctors/status" style="margin-bottom: var(--space-4);">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                            <input type="hidden" name="doctorId" value="<c:out value='${doctor.doctorProfileId}' />" />
                            <input type="hidden" name="status" value="ACTIVE" />
                            <input type="hidden" name="redirect" value="verification" />

                            <div style="margin-bottom: var(--space-4);">
                                <h3 style="font-size: 1rem; margin-bottom: 4px;">Approve Practitioner Credentials</h3>
                                <p style="font-size: 0.875rem; color: var(--text-secondary); margin: 0;">
                                    Activates account status to ACTIVE and authorizes the doctor to take patient bookings.
                                </p>
                            </div>

                            <button type="submit" class="btn btn-primary" style="width: 100%;" onclick="return confirm('Approve credentials and activate Dr. ${doctor.lastName}?');">
                                <i data-lucide="check-circle"></i>
                                <span>Approve &amp; Activate Doctor</span>
                            </button>
                        </form>

                        <hr style="border: 0; border-top: 1px solid var(--border-color); margin: var(--space-6) 0;" />

                        <!-- Deactivate or Suspend Form -->
                        <form method="post" action="${ctx}/admin/doctors/status">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                            <input type="hidden" name="doctorId" value="<c:out value='${doctor.doctorProfileId}' />" />
                            <input type="hidden" name="redirect" value="verification" />

                            <div style="margin-bottom: var(--space-4);">
                                <h3 style="font-size: 1rem; margin-bottom: 4px;">Reject / Suspend Eligibility</h3>
                                <p style="font-size: 0.875rem; color: var(--text-secondary); margin: 0;">
                                    Place account in INACTIVE (reapplication) or SUSPENDED (credential hold) status.
                                </p>
                            </div>

                            <div style="margin-bottom: var(--space-4);">
                                <select class="form-select" name="status">
                                    <option value="INACTIVE" ${doctor.user.status == 'INACTIVE' ? 'selected' : ''}>Inactive (Pending corrections)</option>
                                    <option value="SUSPENDED" ${doctor.user.status == 'SUSPENDED' ? 'selected' : ''}>Suspended (Administrative hold)</option>
                                </select>
                            </div>

                            <button type="submit" class="btn btn-danger" style="width: 100%;" onclick="return confirm('Change status for Dr. ${doctor.lastName}?');">
                                <i data-lucide="x-circle"></i>
                                <span>Update Status</span>
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
