<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="users" />
<c:set var="pageTitle" value="User Account Details — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <div style="display: flex; align-items: center; gap: var(--space-2); margin-bottom: 4px;">
                        <a href="${ctx}/admin/users" style="color: var(--text-secondary); text-decoration: none; font-size: 0.875rem;">
                            &larr; Back to Users
                        </a>
                    </div>
                    <h1 class="page-title">
                        <c:out value="${userAccount.email}" />
                    </h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        User ID: #<c:out value="${userAccount.userId}" /> &bull; Role: <c:out value="${userAccount.role}" /> &bull; Status: <c:out value="${userAccount.status}" />
                    </p>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Administrative Safety Safeguards Banner -->
            <div class="alert alert-info" style="margin-bottom: var(--space-6);">
                <i data-lucide="shield-alert" style="flex-shrink: 0;"></i>
                <div>
                    <strong>Administrative Invariant Protection:</strong> The platform prohibits self-demotion of your own administrative account and strictly blocks deactivating or demoting the last active administrator to prevent lockout.
                </div>
            </div>

            <div class="grid-2">
                <!-- User Account Profile -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title" style="font-size: 1.125rem;">Account Overview</h2>
                    </div>
                    <div class="card-body">
                        <div style="display: flex; flex-direction: column; gap: var(--space-4);">
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Email Address (Login Identifier)</label>
                                <div style="font-size: 1.125rem; font-weight: 600;"><c:out value="${userAccount.email}" /></div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">System Assigned Role</label>
                                <div>
                                    <span class="badge ${userAccount.role == 'ADMIN' ? 'badge-primary' : (userAccount.role == 'DOCTOR' ? 'badge-info' : 'badge-secondary')}" style="font-size: 0.875rem; padding: 4px 10px;">
                                        <c:out value="${userAccount.role}" />
                                    </span>
                                </div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Account Lifecycle Status</label>
                                <div>
                                    <span class="badge ${userAccount.status == 'ACTIVE' ? 'badge-success' : 'badge-warning'}" style="font-size: 0.875rem; padding: 4px 10px;">
                                        <c:out value="${userAccount.status}" />
                                    </span>
                                </div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Registration Timestamp</label>
                                <div style="font-weight: 500;"><c:out value="${userAccount.createdAt}" /></div>
                            </div>
                            <div>
                                <label style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Last Authenticated At</label>
                                <div style="font-weight: 500;">
                                    <c:out value="${userAccount.lastLoginAt != null ? userAccount.lastLoginAt : 'Never authenticated'}" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Governance Actions (Role & Status) -->
                <div style="display: flex; flex-direction: column; gap: var(--space-6);">
                    <!-- Change Role Card -->
                    <div class="card">
                        <div class="card-header">
                            <h2 class="card-title" style="font-size: 1.125rem;">Modify User Role</h2>
                        </div>
                        <div class="card-body">
                            <form method="post" action="${ctx}/admin/users/role">
                                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                <input type="hidden" name="userId" value="<c:out value='${userAccount.userId}' />" />

                                <div style="margin-bottom: var(--space-4);">
                                    <label class="form-label" for="userRole">Assigned Role</label>
                                    <select class="form-select" id="userRole" name="role">
                                        <option value="PATIENT" ${userAccount.role == 'PATIENT' ? 'selected' : ''}>PATIENT (Portal access only)</option>
                                        <option value="DOCTOR" ${userAccount.role == 'DOCTOR' ? 'selected' : ''}>DOCTOR (Clinical practitioner)</option>
                                        <option value="ADMIN" ${userAccount.role == 'ADMIN' ? 'selected' : ''}>ADMIN (Central administration)</option>
                                    </select>
                                </div>

                                <button type="submit" class="btn btn-primary" onclick="return confirm('Change system role for this user account?');">
                                    <i data-lucide="shield"></i>
                                    <span>Update Role</span>
                                </button>
                            </form>
                        </div>
                    </div>

                    <!-- Change Status Card -->
                    <div class="card">
                        <div class="card-header">
                            <h2 class="card-title" style="font-size: 1.125rem;">Modify Account Status</h2>
                        </div>
                        <div class="card-body">
                            <form method="post" action="${ctx}/admin/users/status">
                                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                <input type="hidden" name="userId" value="<c:out value='${userAccount.userId}' />" />

                                <div style="margin-bottom: var(--space-4);">
                                    <label class="form-label" for="userStatus">Account Status</label>
                                    <select class="form-select" id="userStatus" name="status">
                                        <option value="ACTIVE" ${userAccount.status == 'ACTIVE' ? 'selected' : ''}>Active (Normal access)</option>
                                        <option value="INACTIVE" ${userAccount.status == 'INACTIVE' ? 'selected' : ''}>Inactive (Account disabled)</option>
                                        <option value="SUSPENDED" ${userAccount.status == 'SUSPENDED' ? 'selected' : ''}>Suspended (Administrative hold)</option>
                                    </select>
                                </div>

                                <button type="submit" class="btn btn-primary" onclick="return confirm('Change status for this user account?');">
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
