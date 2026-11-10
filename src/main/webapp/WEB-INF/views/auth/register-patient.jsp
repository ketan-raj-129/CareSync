<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Create Patient Account — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="public-layout">
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <main class="auth-page-wrapper">
        <div class="auth-card auth-card-wide">
            <div class="auth-header">
                <div class="brand-icon-pill">
                    <i data-lucide="user-plus"></i>
                </div>
                <h1 class="auth-title">Create your Patient Account</h1>
                <p class="auth-subtitle">Join CareSync to connect with medical specialists and manage your health</p>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <form action="${ctx}/register-patient" method="POST">
                <!-- Synchronizer Token Pattern (CSRF Defense) -->
                <input type="hidden" name="csrfToken" value="${csrfToken}">

                <div class="grid-2">
                    <div class="form-group">
                        <label for="firstName" class="form-label">First Name *</label>
                        <input type="text" id="firstName" name="firstName" class="form-control" 
                               placeholder="Jane" value="<c:out value='${preservedFirstName}' />" required>
                    </div>

                    <div class="form-group">
                        <label for="lastName" class="form-label">Last Name *</label>
                        <input type="text" id="lastName" name="lastName" class="form-control" 
                               placeholder="Doe" value="<c:out value='${preservedLastName}' />" required>
                    </div>
                </div>

                <div class="grid-2">
                    <div class="form-group">
                        <label for="email" class="form-label">Email Address *</label>
                        <input type="email" id="email" name="email" class="form-control" 
                               placeholder="jane.doe@domain.com" value="<c:out value='${preservedEmail}' />" required>
                    </div>

                    <div class="form-group">
                        <label for="phone" class="form-label">Phone Number</label>
                        <input type="tel" id="phone" name="phone" class="form-control" 
                               placeholder="+1 (555) 000-0000" value="<c:out value='${preservedPhone}' />">
                    </div>
                </div>

                <div class="grid-2">
                    <div class="form-group">
                        <label for="dateOfBirth" class="form-label">Date of Birth</label>
                        <input type="date" id="dateOfBirth" name="dateOfBirth" class="form-control" 
                               value="<c:out value='${preservedDob}' />">
                    </div>

                    <div class="form-group">
                        <label for="gender" class="form-label">Gender</label>
                        <select id="gender" name="gender" class="form-control">
                            <option value="PREFER_NOT_TO_SAY">Prefer not to say</option>
                            <option value="FEMALE">Female</option>
                            <option value="MALE">Male</option>
                            <option value="OTHER">Other</option>
                        </select>
                    </div>
                </div>

                <div class="grid-2">
                    <div class="form-group">
                        <label for="bloodGroup" class="form-label">Blood Group</label>
                        <select id="bloodGroup" name="bloodGroup" class="form-control">
                            <option value="">Unknown / Select</option>
                            <option value="A+">A+</option>
                            <option value="A-">A-</option>
                            <option value="B+">B+</option>
                            <option value="B-">B-</option>
                            <option value="AB+">AB+</option>
                            <option value="AB-">AB-</option>
                            <option value="O+">O+</option>
                            <option value="O-">O-</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="address" class="form-label">Address</label>
                        <input type="text" id="address" name="address" class="form-control" 
                               placeholder="123 Health Ave, Suite 4B" value="<c:out value='${preservedAddress}' />">
                    </div>
                </div>

                <div class="grid-2">
                    <div class="form-group">
                        <label for="emergencyContactName" class="form-label">Emergency Contact Name</label>
                        <input type="text" id="emergencyContactName" name="emergencyContactName" class="form-control" 
                               placeholder="Emergency Contact" value="<c:out value='${preservedEmergencyName}' />">
                    </div>

                    <div class="form-group">
                        <label for="emergencyContactPhone" class="form-label">Emergency Contact Phone</label>
                        <input type="tel" id="emergencyContactPhone" name="emergencyContactPhone" class="form-control" 
                               placeholder="+1 (555) 999-9999" value="<c:out value='${preservedEmergencyPhone}' />">
                    </div>
                </div>

                <div class="grid-2">
                    <div class="form-group">
                        <label for="password" class="form-label">Password *</label>
                        <div class="password-field-wrapper">
                            <input type="password" id="password" name="password" class="form-control" 
                                   placeholder="Minimum 8 characters" autocomplete="new-password" required>
                            <button type="button" class="password-toggle-btn" data-target="password" aria-label="Toggle password visibility">
                                <i data-lucide="eye"></i>
                            </button>
                        </div>
                        <div class="form-text">Must be at least 8 characters. PBKDF2-HMAC-SHA256 hashed.</div>
                    </div>

                    <div class="form-group">
                        <label for="confirmPassword" class="form-label">Confirm Password *</label>
                        <div class="password-field-wrapper">
                            <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" 
                                   placeholder="Re-enter password" autocomplete="new-password" required>
                            <button type="button" class="password-toggle-btn" data-target="confirmPassword" aria-label="Toggle password visibility">
                                <i data-lucide="eye"></i>
                            </button>
                        </div>
                    </div>
                </div>

                <div class="form-group" style="margin-top: var(--space-2);">
                    <div class="form-check">
                        <input type="checkbox" id="termsConsent" name="termsConsent" class="form-check-input" required>
                        <label for="termsConsent" class="form-text" style="margin: 0; cursor: pointer; color: var(--text-secondary);">
                            I agree to the CareSync Terms of Service and Patient Privacy Notice.
                        </label>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: var(--space-3);">
                    <span>Create Account</span>
                    <i data-lucide="arrow-right"></i>
                </button>
            </form>

            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: var(--space-6); padding-top: var(--space-5); border-top: 1px solid var(--border-subtle); font-size: 0.875rem;">
                <span style="color: var(--text-secondary);">Already registered? <a href="${ctx}/login" style="font-weight: 600;">Sign in</a></span>
                <span>Are you a doctor? <a href="${ctx}/register-doctor" style="font-weight: 600;">Apply here</a></span>
            </div>
        </div>
    </main>

    <jsp:include page="/WEB-INF/views/common/public-footer.jsp" />
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
