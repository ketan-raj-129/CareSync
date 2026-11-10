<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Sign In — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="public-layout">
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <main class="auth-page-wrapper">
        <div class="auth-card">
            <div class="auth-header">
                <div class="brand-icon-pill">
                    <i data-lucide="stethoscope"></i>
                </div>
                <h1 class="auth-title">Welcome back</h1>
                <p class="auth-subtitle">Sign in to your CareSync account</p>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <form action="${ctx}/login" method="POST">
                <!-- Synchronizer Token Pattern (CSRF Defense) -->
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <c:if test="${not empty param.redirect}">
                    <input type="hidden" name="redirect" value="<c:out value='${param.redirect}' />">
                </c:if>

                <div class="form-group">
                    <label for="email" class="form-label">Email Address</label>
                    <input type="email" id="email" name="email" class="form-control" 
                           placeholder="name@domain.com" autocomplete="email" 
                           value="<c:out value='${preservedEmail != null ? preservedEmail : param.email}' />" required>
                </div>

                <div class="form-group">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-2);">
                        <label for="password" class="form-label" style="margin-bottom: 0;">Password</label>
                        <a href="#" onclick="CareSync.showToast('Password recovery is scheduled for a future phase.', 'info'); return false;" style="font-size: 0.8125rem;">Forgot password?</a>
                    </div>
                    <div class="password-field-wrapper">
                        <input type="password" id="password" name="password" class="form-control" placeholder="••••••••" autocomplete="current-password" required>
                        <button type="button" class="password-toggle-btn" data-target="password" aria-label="Toggle password visibility">
                            <i data-lucide="eye"></i>
                        </button>
                    </div>
                </div>

                <div class="form-group">
                    <div class="form-check">
                        <input type="checkbox" id="rememberMe" name="rememberMe" class="form-check-input">
                        <label for="rememberMe" class="form-text" style="margin: 0; cursor: pointer; color: var(--text-secondary);">Remember my email</label>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: var(--space-2);">
                    <span>Sign In</span>
                    <i data-lucide="arrow-right"></i>
                </button>
            </form>

            <div style="text-align: center; margin-top: var(--space-6); padding-top: var(--space-5); border-top: 1px solid var(--border-subtle); font-size: 0.875rem; color: var(--text-secondary);">
                Don't have an account yet? 
                <a href="${ctx}/register-patient" style="font-weight: 600;">Create Patient Account</a>
            </div>
        </div>
    </main>

    <jsp:include page="/WEB-INF/views/common/public-footer.jsp" />
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
