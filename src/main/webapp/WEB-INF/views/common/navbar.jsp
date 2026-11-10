<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<header class="public-navbar">
    <div class="navbar-container">
        <!-- Brand Logo -->
        <a href="${ctx}/" class="navbar-brand">
            <div class="brand-icon-pill">
                <i data-lucide="stethoscope"></i>
            </div>
            <span>CareSync</span>
        </a>

        <!-- Public Navigation Links -->
        <ul class="navbar-nav">
            <li><a href="${ctx}/#home" class="nav-link">Home</a></li>
            <li><a href="${ctx}/#features" class="nav-link">Features</a></li>
            <li><a href="${ctx}/#how-it-works" class="nav-link">How It Works</a></li>
            <c:if test="${empty sessionScope.USER_PRINCIPAL}">
                <li><a href="${ctx}/register-doctor" class="nav-link">Join as Doctor</a></li>
            </c:if>
        </ul>

        <!-- Action CTAs (Role-Aware) -->
        <div class="navbar-actions">
            <c:choose>
                <c:when test="${not empty sessionScope.USER_PRINCIPAL}">
                    <a href="${ctx}${sessionScope.USER_PRINCIPAL.dashboardUrl}" class="btn btn-primary btn-sm">
                        <i data-lucide="layout-dashboard"></i>
                        <span>Dashboard</span>
                    </a>
                    <form action="${ctx}/logout" method="POST" style="display: inline; margin: 0;">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <button type="submit" class="btn btn-secondary btn-sm" title="Sign Out">
                            <i data-lucide="log-out"></i>
                            <span>Sign Out</span>
                        </button>
                    </form>
                </c:when>
                <c:otherwise>
                    <a href="${ctx}/login" class="btn btn-secondary btn-sm">Sign In</a>
                    <a href="${ctx}/register-patient" class="btn btn-primary btn-sm">Get Started</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>
