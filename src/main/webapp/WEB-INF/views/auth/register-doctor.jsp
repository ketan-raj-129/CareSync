<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Doctor Application — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="public-layout">
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <main class="auth-page-wrapper">
        <div class="auth-card auth-card-wide">
            <div class="auth-header">
                <div class="brand-icon-pill">
                    <i data-lucide="stethoscope"></i>
                </div>
                <h1 class="auth-title">Doctor Practitioner Onboarding</h1>
                <p class="auth-subtitle">Join the CareSync physician network to expand your clinical reach</p>
            </div>

            <!-- Phase 1 UI Prototype Notice -->
            <div class="alert alert-info" style="font-size: 0.8125rem;">
                <i data-lucide="info" style="flex-shrink: 0;"></i>
                <div>
                    <strong>Phase 1 UI Prototype:</strong> Practitioner credentialing and administrative verification activate in Phase 3 &amp; Phase 6.
                </div>
            </div>

            <form action="#" method="POST" onsubmit="event.preventDefault(); CareSync.showToast('Physician onboarding activates in Phase 3. You can explore the Doctor Dashboard preview below.', 'info');">
                <div class="grid-2">
                    <div class="form-group">
                        <label for="doctorName" class="form-label">Full Name &amp; Title</label>
                        <input type="text" id="doctorName" name="doctorName" class="form-control" placeholder="Dr. Alexander Wright" required>
                    </div>

                    <div class="form-group">
                        <label for="email" class="form-label">Professional Email</label>
                        <input type="email" id="email" name="email" class="form-control" placeholder="dr.wright@clinic.org" required>
                    </div>
                </div>

                <div class="grid-2">
                    <div class="form-group">
                        <label for="phone" class="form-label">Direct Phone</label>
                        <input type="tel" id="phone" name="phone" class="form-control" placeholder="+1 (555) 019-2834" required>
                    </div>

                    <div class="form-group">
                        <label for="specialty" class="form-label">Primary Medical Specialty</label>
                        <select id="specialty" name="specialty" class="form-select" required>
                            <option value="" disabled selected>Select specialty...</option>
                            <option value="cardiology">Cardiology</option>
                            <option value="dermatology">Dermatology</option>
                            <option value="pediatrics">Pediatrics</option>
                            <option value="neurology">Neurology</option>
                            <option value="orthopedics">Orthopedics</option>
                            <option value="internal_medicine">Internal Medicine</option>
                            <option value="general_practice">General Practice</option>
                        </select>
                    </div>
                </div>

                <div class="grid-2">
                    <div class="form-group">
                        <label for="licenseNumber" class="form-label">Medical License Number</label>
                        <input type="text" id="licenseNumber" name="licenseNumber" class="form-control" placeholder="MED-LIC-89210" required>
                    </div>

                    <div class="form-group">
                        <label for="qualification" class="form-label">Academic Degrees / Qualifications</label>
                        <input type="text" id="qualification" name="qualification" class="form-control" placeholder="MBBS, MD, FACC" required>
                    </div>
                </div>

                <div class="grid-2">
                    <div class="form-group">
                        <label for="password" class="form-label">Create Password</label>
                        <div class="password-field-wrapper">
                            <input type="password" id="password" name="password" class="form-control" placeholder="Minimum 8 characters" required>
                            <button type="button" class="password-toggle-btn" data-target="password" aria-label="Toggle password visibility">
                                <i data-lucide="eye"></i>
                            </button>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="confirmPassword" class="form-label">Confirm Password</label>
                        <div class="password-field-wrapper">
                            <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" placeholder="Re-enter password" required>
                            <button type="button" class="password-toggle-btn" data-target="confirmPassword" aria-label="Toggle password visibility">
                                <i data-lucide="eye"></i>
                            </button>
                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <div class="form-check">
                        <input type="checkbox" id="credentialConsent" name="credentialConsent" class="form-check-input" required>
                        <label for="credentialConsent" class="form-text" style="margin: 0; cursor: pointer; color: var(--text-secondary);">
                            I certify that the supplied clinical credentials and licensing information are accurate and verifiable.
                        </label>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: var(--space-2);">
                    <span>Submit Practitioner Application</span>
                    <i data-lucide="arrow-right"></i>
                </button>
            </form>

            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: var(--space-6); padding-top: var(--space-5); border-top: 1px solid var(--border-subtle); font-size: 0.875rem;">
                <span style="color: var(--text-secondary);">Already verified? <a href="${ctx}/login" style="font-weight: 600;">Sign in</a></span>
                <span>Are you a patient? <a href="${ctx}/register-patient" style="font-weight: 600;">Patient portal</a></span>
            </div>
        </div>
    </main>

    <jsp:include page="/WEB-INF/views/common/public-footer.jsp" />
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

