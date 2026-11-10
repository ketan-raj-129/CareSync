<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="500 — System Error | CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="public-layout">
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <main class="main-content" style="display: flex; align-items: center; justify-content: center; padding: var(--space-16) var(--space-6);">
        <div class="empty-state" style="max-width: 520px; border: 1px solid var(--border-color); box-shadow: var(--shadow-sm);">
            <div class="empty-state-icon" style="background-color: var(--danger-soft); color: var(--danger);">
                <i data-lucide="alert-octagon" style="width: 32px; height: 32px;"></i>
            </div>
            <h1 style="font-size: 3rem; font-weight: 800; color: var(--text-primary); margin-bottom: var(--space-1); line-height: 1;">500</h1>
            <h2 class="empty-state-title">An unexpected error occurred</h2>
            <p class="empty-state-desc">
                Our team has logged this incident. Please rest assured that no sensitive patient information was compromised. Please try again shortly.
            </p>
            <div style="display: flex; gap: var(--space-3); justify-content: center;">
                <a href="${ctx}/" class="btn btn-primary">
                    <i data-lucide="home"></i>
                    <span>Return to Safety</span>
                </a>
                <button type="button" onclick="window.location.reload();" class="btn btn-secondary">
                    <i data-lucide="refresh-cw"></i>
                    <span>Retry Page</span>
                </button>
            </div>
        </div>
    </main>

    <jsp:include page="/WEB-INF/views/common/public-footer.jsp" />
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

