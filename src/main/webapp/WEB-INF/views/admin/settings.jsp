<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="settings" />
<c:set var="pageTitle" value="System Settings & Configuration — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Platform &amp; Operational Settings</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Global facility parameters, operational contact info, and booking governance rules
                    </p>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- System Settings Table -->
            <div class="card">
                <div class="card-header">
                    <h2 class="card-title" style="font-size: 1.125rem;">Active Operational Parameters</h2>
                </div>
                <div class="card-body" style="padding: 0;">
                    <c:choose>
                        <c:when test="${not empty settings}">
                            <div class="table-responsive">
                                <table class="table" style="margin-bottom: 0;">
                                    <thead>
                                        <tr>
                                            <th>Setting Key</th>
                                            <th>Description</th>
                                            <th>Current Value</th>
                                            <th style="min-width: 280px;">Update Value</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="s" items="${settings}">
                                            <tr>
                                                <td style="font-family: monospace; font-weight: 600; color: var(--primary);">
                                                    <c:out value="${s.key}" />
                                                </td>
                                                <td style="font-size: 0.875rem; color: var(--text-secondary);">
                                                    <c:out value="${s.description != null ? s.description : 'Operational setting'}" />
                                                </td>
                                                <td>
                                                    <span style="font-family: monospace; font-weight: 500; background: var(--background); padding: 2px 6px; border-radius: 4px;">
                                                        <c:out value="${s.value}" />
                                                    </span>
                                                </td>
                                                <td>
                                                    <form method="post" action="${ctx}/admin/settings" style="display: flex; gap: var(--space-2); margin: 0;">
                                                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                                        <input type="hidden" name="settingKey" value="<c:out value='${s.key}' />" />
                                                        <input type="text" class="form-control form-control-sm" name="settingValue" value="<c:out value='${s.value}' />" required style="font-size: 0.8125rem;">
                                                        <button type="submit" class="btn btn-secondary btn-sm" style="font-size: 0.75rem; white-space: nowrap;">
                                                            Save
                                                        </button>
                                                    </form>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state" style="padding: var(--space-8) var(--space-4);">
                                <div class="empty-state-icon">
                                    <i data-lucide="sliders"></i>
                                </div>
                                <h3 class="empty-state-title">No system settings found</h3>
                                <p class="empty-state-desc">The system configuration table is currently empty.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

