<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger" role="alert">
        <i data-lucide="alert-circle" style="color: var(--danger); flex-shrink: 0;"></i>
        <div><c:out value="${errorMessage}" /></div>
        <button type="button" class="alert-close-btn" aria-label="Dismiss alert">&times;</button>
    </div>
</c:if>

<c:if test="${not empty successMessage}">
    <div class="alert alert-success" role="alert">
        <i data-lucide="check-circle-2" style="color: var(--success); flex-shrink: 0;"></i>
        <div><c:out value="${successMessage}" /></div>
        <button type="button" class="alert-close-btn" aria-label="Dismiss alert">&times;</button>
    </div>
</c:if>

<c:if test="${not empty warningMessage}">
    <div class="alert alert-warning" role="alert">
        <i data-lucide="alert-triangle" style="color: var(--warning); flex-shrink: 0;"></i>
        <div><c:out value="${warningMessage}" /></div>
        <button type="button" class="alert-close-btn" aria-label="Dismiss alert">&times;</button>
    </div>
</c:if>

<c:if test="${not empty infoMessage}">
    <div class="alert alert-info" role="alert">
        <i data-lucide="info" style="color: var(--info); flex-shrink: 0;"></i>
        <div><c:out value="${infoMessage}" /></div>
        <button type="button" class="alert-close-btn" aria-label="Dismiss alert">&times;</button>
    </div>
</c:if>

