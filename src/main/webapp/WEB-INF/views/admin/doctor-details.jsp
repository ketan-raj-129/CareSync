<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="doctors" />
<c:set var="pageTitle" value="Doctor Profile Details — CareSync" />

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
                            &larr; Back to Doctors
                        </a>
                    </div>
                    <h1 class="page-title">
                        Dr. <c:out value="${doctor.firstName}" /> <c:out value="${doctor.lastName}" />
                    </h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        <c:out value="${doctor.specialty != null ? doctor.specialty.name : 'General Practice'}" /> &bull; 
                        License #<c:out value="${doctor.licenseNumber}" /> &bull;
                        Doctor Profile #<c:out value="${doctor.doctorProfileId}" />
                    </p>
                </div>
                <div style="display: flex; gap: var(--space-3);">
                    <a href="${ctx}/admin/doctors/verification?id=${doctor.doctorProfileId}" class="btn btn-secondary btn-sm">
                        <i data-lucide="award"></i>
                        <span>Credential Review</span>
                    </a>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <div class="grid-2">
                <!-- Professional Details Card -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title" style="font-size: 1.125rem;">Professional Information</h2>
                    </div>
                    <div class="card-body">
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4);">
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">License Number</label>
                                <div style="font-weight: 600; font-family: monospace;"><c:out value="${doctor.licenseNumber}" /></div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Specialty Department</label>
                                <div style="font-weight: 500;"><c:out value="${doctor.specialty != null ? doctor.specialty.name : 'Unassigned'}" /></div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Experience</label>
                                <div style="font-weight: 500;"><c:out value="${doctor.yearsOfExperience != null ? doctor.yearsOfExperience : 0}" /> Years</div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Consultation Fee</label>
                                <div style="font-weight: 600; color: var(--primary);">₹<c:out value="${doctor.consultationFee}" /></div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Clinic Room / Suite</label>
                                <div style="font-weight: 500;"><c:out value="${doctor.roomNumber != null ? doctor.roomNumber : 'TBD'}" /></div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Practice Availability</label>
                                <div>
                                    <c:choose>
                                        <c:when test="${doctor.available}">
                                            <span class="badge badge-success">Available for Appointments</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-warning">Unavailable</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>

                        <div style="margin-top: var(--space-4); padding-top: var(--space-4); border-top: 1px solid var(--border-color);">
                            <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Qualifications &amp; Degrees</label>
                            <div style="font-weight: 500; margin-top: 4px;">
                                <c:out value="${doctor.qualifications != null ? doctor.qualifications : 'Not specified'}" />
                            </div>
                        </div>

                        <div style="margin-top: var(--space-4); padding-top: var(--space-4); border-top: 1px solid var(--border-color);">
                            <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Professional Biography</label>
                            <div style="color: var(--text-secondary); margin-top: 4px; font-size: 0.9375rem; line-height: 1.5;">
                                <c:out value="${doctor.biography != null ? doctor.biography : 'No biography provided'}" />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Governance Actions Card -->
                <div style="display: flex; flex-direction: column; gap: var(--space-6);">
                    <!-- Account Status Administration -->
                    <div class="card">
                        <div class="card-header">
                            <h2 class="card-title" style="font-size: 1.125rem;">Account Status Governance</h2>
                        </div>
                        <div class="card-body">
                            <form method="post" action="${ctx}/admin/doctors/status">
                                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                <input type="hidden" name="doctorId" value="<c:out value='${doctor.doctorProfileId}' />" />
                                
                                <div style="margin-bottom: var(--space-4);">
                                    <label class="form-label" for="docStatus">Account Status</label>
                                    <select class="form-select" id="docStatus" name="status">
                                        <option value="ACTIVE" ${doctor.user.status == 'ACTIVE' ? 'selected' : ''}>Active (Authorized to practice)</option>
                                        <option value="INACTIVE" ${doctor.user.status == 'INACTIVE' ? 'selected' : ''}>Inactive (Pending / On hold)</option>
                                        <option value="SUSPENDED" ${doctor.user.status == 'SUSPENDED' ? 'selected' : ''}>Suspended (Administrative lock)</option>
                                    </select>
                                </div>

                                <button type="submit" class="btn btn-primary" onclick="return confirm('Update doctor account status?');">
                                    <i data-lucide="shield-check"></i>
                                    <span>Update Status</span>
                                </button>
                            </form>
                        </div>
                    </div>

                    <!-- Clinical Availability Override -->
                    <div class="card">
                        <div class="card-header">
                            <h2 class="card-title" style="font-size: 1.125rem;">Practice Availability Override</h2>
                        </div>
                        <div class="card-body">
                            <p style="font-size: 0.875rem; color: var(--text-secondary); margin-bottom: var(--space-4);">
                                Administrators may toggle clinical booking availability in emergencies or during facility reorganizations.
                            </p>
                            <form method="post" action="${ctx}/admin/doctors/status">
                                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                <input type="hidden" name="doctorId" value="<c:out value='${doctor.doctorProfileId}' />" />
                                <input type="hidden" name="toggleAvailability" value="${!doctor.available}" />

                                <button type="submit" class="btn ${doctor.available ? 'btn-secondary' : 'btn-primary'}" onclick="return confirm('Toggle clinical booking availability for Dr. ${doctor.lastName}?');">
                                    <i data-lucide="${doctor.available ? 'toggle-right' : 'toggle-left'}"></i>
                                    <span>Set as ${doctor.available ? 'Unavailable' : 'Available'}</span>
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
