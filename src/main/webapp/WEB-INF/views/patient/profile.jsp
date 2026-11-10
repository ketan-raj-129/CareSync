<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="PATIENT" />
<c:set var="pageTitle" value="My Profile — CareSync" />

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
                    <h1 class="page-title">Personal Patient Profile</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Manage your contact details, emergency contacts, and demographic medical data
                    </p>
                </div>
                <a href="${ctx}/patient/dashboard" class="btn btn-secondary btn-sm">
                    <i data-lucide="arrow-left"></i>
                    <span>Back to Dashboard</span>
                </a>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <div class="card" style="max-width: 800px;">
                <div class="card-header">
                    <h2 class="card-title">Profile Information</h2>
                </div>
                <div class="card-body">
                    <form action="${ctx}/patient/profile" method="POST">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">

                        <div class="grid-2">
                            <div class="form-group">
                                <label for="firstName" class="form-label">First Name *</label>
                                <input type="text" id="firstName" name="firstName" class="form-control" 
                                       value="<c:out value='${patient.firstName}' />" required>
                            </div>

                            <div class="form-group">
                                <label for="lastName" class="form-label">Last Name *</label>
                                <input type="text" id="lastName" name="lastName" class="form-control" 
                                       value="<c:out value='${patient.lastName}' />" required>
                            </div>
                        </div>

                        <div class="grid-2">
                            <div class="form-group">
                                <label for="email" class="form-label">Email Address (Registered Account)</label>
                                <input type="email" id="email" class="form-control" 
                                       value="<c:out value='${patient.email}' />" readonly disabled style="background-color: var(--surface-muted);">
                                <div class="form-text">Account email cannot be modified directly.</div>
                            </div>

                            <div class="form-group">
                                <label for="phone" class="form-label">Phone Number</label>
                                <input type="tel" id="phone" name="phone" class="form-control" 
                                       placeholder="+1 (555) 000-0000" value="<c:out value='${patient.phoneNumber}' />">
                            </div>
                        </div>

                        <div class="grid-2">
                            <div class="form-group">
                                <label for="dateOfBirth" class="form-label">Date of Birth</label>
                                <input type="date" id="dateOfBirth" name="dateOfBirth" class="form-control" 
                                       value="<c:out value='${patient.dateOfBirth}' />">
                            </div>

                            <div class="form-group">
                                <label for="gender" class="form-label">Gender</label>
                                <select id="gender" name="gender" class="form-control">
                                    <option value="PREFER_NOT_TO_SAY" ${patient.gender == 'PREFER_NOT_TO_SAY' ? 'selected' : ''}>Prefer not to say</option>
                                    <option value="FEMALE" ${patient.gender == 'FEMALE' ? 'selected' : ''}>Female</option>
                                    <option value="MALE" ${patient.gender == 'MALE' ? 'selected' : ''}>Male</option>
                                    <option value="OTHER" ${patient.gender == 'OTHER' ? 'selected' : ''}>Other</option>
                                </select>
                            </div>
                        </div>

                        <div class="grid-2">
                            <div class="form-group">
                                <label for="bloodGroup" class="form-label">Blood Group</label>
                                <select id="bloodGroup" name="bloodGroup" class="form-control">
                                    <option value="">Unknown / Select</option>
                                    <option value="A+" ${patient.bloodGroup != null && patient.bloodGroup.displayValue == 'A+' ? 'selected' : ''}>A+</option>
                                    <option value="A-" ${patient.bloodGroup != null && patient.bloodGroup.displayValue == 'A-' ? 'selected' : ''}>A-</option>
                                    <option value="B+" ${patient.bloodGroup != null && patient.bloodGroup.displayValue == 'B+' ? 'selected' : ''}>B+</option>
                                    <option value="B-" ${patient.bloodGroup != null && patient.bloodGroup.displayValue == 'B-' ? 'selected' : ''}>B-</option>
                                    <option value="AB+" ${patient.bloodGroup != null && patient.bloodGroup.displayValue == 'AB+' ? 'selected' : ''}>AB+</option>
                                    <option value="AB-" ${patient.bloodGroup != null && patient.bloodGroup.displayValue == 'AB-' ? 'selected' : ''}>AB-</option>
                                    <option value="O+" ${patient.bloodGroup != null && patient.bloodGroup.displayValue == 'O+' ? 'selected' : ''}>O+</option>
                                    <option value="O-" ${patient.bloodGroup != null && patient.bloodGroup.displayValue == 'O-' ? 'selected' : ''}>O-</option>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="address" class="form-label">Address</label>
                                <input type="text" id="address" name="address" class="form-control" 
                                       placeholder="123 Health Ave, Suite 4B" value="<c:out value='${patient.address}' />">
                            </div>
                        </div>

                        <div class="grid-2">
                            <div class="form-group">
                                <label for="emergencyContactName" class="form-label">Emergency Contact Name</label>
                                <input type="text" id="emergencyContactName" name="emergencyContactName" class="form-control" 
                                       placeholder="Emergency Contact" value="<c:out value='${patient.emergencyContactName}' />">
                            </div>

                            <div class="form-group">
                                <label for="emergencyContactPhone" class="form-label">Emergency Contact Phone</label>
                                <input type="tel" id="emergencyContactPhone" name="emergencyContactPhone" class="form-control" 
                                       placeholder="+1 (555) 999-9999" value="<c:out value='${patient.emergencyContactPhone}' />">
                            </div>
                        </div>

                        <div style="margin-top: var(--space-4); display: flex; justify-content: flex-end;">
                            <button type="submit" class="btn btn-primary">
                                <i data-lucide="save"></i>
                                <span>Save Changes</span>
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

