<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="patients" />
<c:set var="pageTitle" value="Patient Details — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <div style="display: flex; align-items: center; gap: var(--space-2); margin-bottom: 4px;">
                        <a href="${ctx}/admin/patients" style="color: var(--text-secondary); text-decoration: none; font-size: 0.875rem;">
                            &larr; Back to Patients
                        </a>
                    </div>
                    <h1 class="page-title">
                        <c:out value="${patient.firstName}" /> <c:out value="${patient.lastName}" />
                    </h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Patient ID: #<c:out value="${patient.patientProfileId}" /> &bull; User ID: #<c:out value="${patient.userId}" />
                    </p>
                </div>
                <div>
                    <span class="badge ${patient.user.status == 'ACTIVE' ? 'badge-success' : 'badge-warning'}" style="font-size: 0.875rem; padding: 6px 12px;">
                        Account: <c:out value="${patient.user.status}" />
                    </span>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Privacy Notice Banner -->
            <div class="alert alert-info" style="margin-bottom: var(--space-6);">
                <i data-lucide="shield" style="flex-shrink: 0;"></i>
                <div>
                    <strong>Data Segregation Notice:</strong> Confidential clinical consultation notes, diagnoses, and medical prescriptions are restricted to clinical practitioners and are not displayed in the administrative portal.
                </div>
            </div>

            <div class="grid-2">
                <!-- Demographics Card -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title" style="font-size: 1.125rem;">Demographic Information</h2>
                    </div>
                    <div class="card-body">
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4);">
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Email Address</label>
                                <div style="font-weight: 500;"><c:out value="${patient.email}" /></div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Phone Number</label>
                                <div style="font-weight: 500;"><c:out value="${patient.phoneNumber != null ? patient.phoneNumber : 'Not provided'}" /></div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Gender</label>
                                <div style="font-weight: 500;"><c:out value="${patient.gender != null ? patient.gender : 'Not specified'}" /></div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Date of Birth</label>
                                <div style="font-weight: 500;"><c:out value="${patient.dateOfBirth != null ? patient.dateOfBirth : 'Not specified'}" /></div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Blood Group</label>
                                <div style="font-weight: 500;">
                                    <c:choose>
                                        <c:when test="${not empty patient.bloodGroup}">
                                            <span class="badge badge-info"><c:out value="${patient.bloodGroup}" /></span>
                                        </c:when>
                                        <c:otherwise>Not recorded</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Registered Date</label>
                                <div style="font-weight: 500;"><c:out value="${patient.createdAt}" /></div>
                            </div>
                        </div>

                        <div style="margin-top: var(--space-4); padding-top: var(--space-4); border-top: 1px solid var(--border-color);">
                            <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Residential Address</label>
                            <div style="font-weight: 500; margin-top: 4px;">
                                <c:out value="${patient.address != null ? patient.address : 'No address registered'}" />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Emergency Contact & Account Governance Card -->
                <div style="display: flex; flex-direction: column; gap: var(--space-6);">
                    <div class="card">
                        <div class="card-header">
                            <h2 class="card-title" style="font-size: 1.125rem;">Emergency Contact</h2>
                        </div>
                        <div class="card-body">
                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4);">
                                <div>
                                    <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Contact Name</label>
                                    <div style="font-weight: 500;"><c:out value="${patient.emergencyContactName != null ? patient.emergencyContactName : 'Not recorded'}" /></div>
                                </div>
                                <div>
                                    <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Contact Phone</label>
                                    <div style="font-weight: 500;"><c:out value="${patient.emergencyContactPhone != null ? patient.emergencyContactPhone : 'Not recorded'}" /></div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Account Status Administration -->
                    <div class="card">
                        <div class="card-header">
                            <h2 class="card-title" style="font-size: 1.125rem;">Account Governance</h2>
                        </div>
                        <div class="card-body">
                            <form method="post" action="${ctx}/admin/patients/status">
                                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                <input type="hidden" name="patientId" value="<c:out value='${patient.patientProfileId}' />" />
                                
                                <div style="margin-bottom: var(--space-4);">
                                    <label class="form-label" for="newStatus">Change Patient Account Status</label>
                                    <select class="form-select" id="newStatus" name="status">
                                        <option value="ACTIVE" ${patient.user.status == 'ACTIVE' ? 'selected' : ''}>Active (Normal access)</option>
                                        <option value="INACTIVE" ${patient.user.status == 'INACTIVE' ? 'selected' : ''}>Inactive (Deactivated)</option>
                                        <option value="SUSPENDED" ${patient.user.status == 'SUSPENDED' ? 'selected' : ''}>Suspended (Administrative hold)</option>
                                    </select>
                                </div>

                                <button type="submit" class="btn btn-primary" onclick="return confirm('Update account status for this patient?');">
                                    <i data-lucide="shield-check"></i>
                                    <span>Update Status</span>
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

