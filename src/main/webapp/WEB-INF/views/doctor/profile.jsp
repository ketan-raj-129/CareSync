<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="DOCTOR" />
<c:set var="activeMenu" value="profile" />
<c:set var="pageTitle" value="Practitioner Profile — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Professional Practice Profile</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Review and maintain your clinical credentials, consultation tariffs, and clinic office suite
                    </p>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <div style="display: grid; grid-template-columns: 1fr 2.5fr; gap: var(--space-6);">
                <!-- Left: Profile Summary Card -->
                <div class="card" style="text-align: center; padding: var(--space-8);">
                    <div class="user-avatar-circle" style="width: 80px; height: 80px; font-size: 2rem; margin: 0 auto var(--space-4) auto; background-color: var(--primary); color: #ffffff;">
                        <c:out value="${doctor.firstName.substring(0, 1).toUpperCase()}" />
                    </div>

                    <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: var(--space-1);">
                        Dr. <c:out value="${doctor.firstName} ${doctor.lastName}" />
                    </h2>
                    <p style="color: var(--primary); font-weight: 600; margin-bottom: var(--space-3);">
                        <c:out value="${doctor.specialty.name != null ? doctor.specialty.name : 'General Medicine'}" />
                    </p>

                    <div style="display: inline-flex; align-items: center; gap: var(--space-2); padding: 0.35rem 0.85rem; border-radius: var(--radius-pill); background: var(--primary-soft); color: var(--primary); font-size: 0.8125rem; margin-bottom: var(--space-6);">
                        <i data-lucide="shield-check" style="width: 16px; height: 16px;"></i>
                        <span>License: <c:out value="${doctor.licenseNumber}" /></span>
                    </div>

                    <div style="text-align: left; border-top: 1px solid var(--border-color); padding-top: var(--space-4); display: flex; flex-direction: column; gap: var(--space-3); font-size: 0.875rem;">
                        <div>
                            <span style="color: var(--text-secondary);">Email:</span>
                            <strong style="float: right;"><c:out value="${doctor.email}" /></strong>
                        </div>
                        <div>
                            <span style="color: var(--text-secondary);">Experience:</span>
                            <strong style="float: right;"><c:out value="${doctor.experienceYears}" /> Years</strong>
                        </div>
                        <div>
                            <span style="color: var(--text-secondary);">Consultation Fee:</span>
                            <strong style="float: right;">₹<c:out value="${doctor.consultationFee}" /></strong>
                        </div>
                        <div>
                            <span style="color: var(--text-secondary);">Clinic Room:</span>
                            <strong style="float: right;"><c:out value="${doctor.roomNumber != null ? doctor.roomNumber : 'Unassigned'}" /></strong>
                        </div>
                        <div>
                            <span style="color: var(--text-secondary);">Practice Status:</span>
                            <strong style="float: right;">
                                <c:choose>
                                    <c:when test="${doctor.available}"><span class="badge badge-success">Accepting Patients</span></c:when>
                                    <c:otherwise><span class="badge badge-danger">Unavailable</span></c:otherwise>
                                </c:choose>
                            </strong>
                        </div>
                    </div>
                </div>

                <!-- Right: Profile Edit Form -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title" style="margin: 0; font-size: 1.125rem;">Update Clinical Details</h2>
                    </div>

                    <div class="card-body">
                        <form action="${ctx}/doctor/profile" method="POST">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">

                            <div class="grid-2" style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4); margin-bottom: var(--space-4);">
                                <div class="form-group">
                                    <label for="qualification" class="form-label">Medical Qualification</label>
                                    <input type="text" id="qualification" name="qualification" class="form-control"
                                           value="<c:out value='${doctor.qualification}' />" required placeholder="e.g. MBBS, MD (Internal Medicine)">
                                </div>

                                <div class="form-group">
                                    <label for="specialtyId" class="form-label">Clinical Specialty</label>
                                    <select id="specialtyId" name="specialtyId" class="form-select" required>
                                        <c:forEach var="spec" items="${specialties}">
                                            <option value="${spec.id}" ${spec.id == doctor.specialtyId ? 'selected' : ''}>
                                                <c:out value="${spec.name}" />
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>

                            <div class="grid-3" style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: var(--space-4); margin-bottom: var(--space-4);">
                                <div class="form-group">
                                    <label for="experienceYears" class="form-label">Years of Experience</label>
                                    <input type="number" id="experienceYears" name="experienceYears" class="form-control"
                                           value="<c:out value='${doctor.experienceYears}' />" min="0" max="60" required>
                                </div>

                                <div class="form-group">
                                    <label for="consultationFee" class="form-label">Consultation Fee (INR)</label>
                                    <input type="number" id="consultationFee" name="consultationFee" class="form-control"
                                           value="<c:out value='${doctor.consultationFee}' />" min="0" step="0.01" required>
                                </div>

                                <div class="form-group">
                                    <label for="roomNumber" class="form-label">Room / Consultation Suite</label>
                                    <input type="text" id="roomNumber" name="roomNumber" class="form-control"
                                           value="<c:out value='${doctor.roomNumber}' />" placeholder="e.g. Suite 304">
                                </div>
                            </div>

                            <div class="form-group" style="margin-bottom: var(--space-4);">
                                <label for="phoneNumber" class="form-label">Contact Phone Number</label>
                                <input type="text" id="phoneNumber" name="phoneNumber" class="form-control"
                                       value="<c:out value='${doctor.phoneNumber}' />" placeholder="+91 98765 43210">
                            </div>

                            <div class="form-group" style="margin-bottom: var(--space-4);">
                                <label for="bio" class="form-label">Professional Biography</label>
                                <textarea id="bio" name="bio" class="form-control" rows="4"
                                          placeholder="Share your clinical background, specialized treatments, and clinical focus areas..."><c:out value="${doctor.bio}" /></textarea>
                            </div>

                            <div class="form-group" style="margin-bottom: var(--space-6);">
                                <div class="form-check" style="display: flex; align-items: center; gap: var(--space-2);">
                                    <input type="checkbox" id="available" name="available" class="form-check-input" value="true"
                                           ${doctor.available ? 'checked' : ''} style="margin: 0; cursor: pointer;">
                                    <label for="available" class="form-label" style="margin: 0; cursor: pointer;">
                                        Accept new appointment bookings and public patient inquiries
                                    </label>
                                </div>
                            </div>

                            <div style="display: flex; justify-content: flex-end; gap: var(--space-3);">
                                <a href="${ctx}/doctor/dashboard" class="btn btn-secondary">Cancel</a>
                                <button type="submit" class="btn btn-primary">
                                    <i data-lucide="save"></i>
                                    <span>Save Profile Changes</span>
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
