<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="users" />
<c:set var="pageTitle" value="User Account Governance — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">User Account Governance</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Global identity directory, RBAC permissions, and administrative account controls
                    </p>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Filter Toolbar -->
            <div class="card" style="margin-bottom: var(--space-6);">
                <div class="card-body">
                    <form method="get" action="${ctx}/admin/users" style="display: flex; gap: var(--space-4); flex-wrap: wrap; align-items: flex-end;">
                        <div style="flex: 1; min-width: 220px;">
                            <label class="form-label" for="q">Search Users</label>
                            <input type="text" class="form-control" id="q" name="q" placeholder="Email address..." value="<c:out value='${query}' />">
                        </div>
                        <div style="min-width: 160px;">
                            <label class="form-label" for="role">Role</label>
                            <select class="form-select" id="role" name="role">
                                <option value="">All Roles</option>
                                <option value="ADMIN" ${selectedRole == 'ADMIN' ? 'selected' : ''}>Admin</option>
                                <option value="DOCTOR" ${selectedRole == 'DOCTOR' ? 'selected' : ''}>Doctor</option>
                                <option value="PATIENT" ${selectedRole == 'PATIENT' ? 'selected' : ''}>Patient</option>
                            </select>
                        </div>
                        <div style="min-width: 160px;">
                            <label class="form-label" for="status">Account Status</label>
                            <select class="form-select" id="status" name="status">
                                <option value="">All Statuses</option>
                                <option value="ACTIVE" ${selectedStatus == 'ACTIVE' ? 'selected' : ''}>Active</option>
                                <option value="INACTIVE" ${selectedStatus == 'INACTIVE' ? 'selected' : ''}>Inactive</option>
                                <option value="SUSPENDED" ${selectedStatus == 'SUSPENDED' ? 'selected' : ''}>Suspended</option>
                            </select>
                        </div>
                        <div style="display: flex; gap: var(--space-2);">
                            <button type="submit" class="btn btn-primary">
                                <i data-lucide="search"></i>
                                <span>Filter</span>
                            </button>
                            <a href="${ctx}/admin/users" class="btn btn-secondary">Reset</a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Users Table -->
            <div class="card">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <h2 class="card-title" style="font-size: 1.125rem;">
                        Users (<c:out value="${usersResult.totalCount}" /> total)
                    </h2>
                </div>
                <div class="card-body" style="padding: 0;">
                    <c:choose>
                        <c:when test="${not empty usersResult.items}">
                            <div class="table-responsive">
                                <table class="table" style="margin-bottom: 0;">
                                    <thead>
                                        <tr>
                                            <th>User ID</th>
                                            <th>Email Address</th>
                                            <th>Assigned Role</th>
                                            <th>Status</th>
                                            <th>Created At</th>
                                            <th>Last Login</th>
                                            <th style="text-align: right;">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="u" items="${usersResult.items}">
                                            <tr>
                                                <td>#<c:out value="${u.userId}" /></td>
                                                <td style="font-weight: 600;"><c:out value="${u.email}" /></td>
                                                <td>
                                                    <span class="badge ${u.role == 'ADMIN' ? 'badge-primary' : (u.role == 'DOCTOR' ? 'badge-info' : 'badge-secondary')}">
                                                        <c:out value="${u.role}" />
                                                    </span>
                                                </td>
                                                <td>
                                                    <span class="badge ${u.status == 'ACTIVE' ? 'badge-success' : 'badge-warning'}">
                                                        <c:out value="${u.status}" />
                                                    </span>
                                                </td>
                                                <td style="font-size: 0.8125rem; color: var(--text-secondary);"><c:out value="${u.createdAt}" /></td>
                                                <td style="font-size: 0.8125rem; color: var(--text-secondary);">
                                                    <c:out value="${u.lastLoginAt != null ? u.lastLoginAt : 'Never'}" />
                                                </td>
                                                <td style="text-align: right;">
                                                    <a href="${ctx}/admin/users/view?id=${u.userId}" class="btn btn-secondary btn-sm" style="padding: 4px 10px; font-size: 0.8125rem;">
                                                        Manage
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>

                            <!-- Pagination -->
                            <c:if test="${usersResult.totalPages > 1}">
                                <div style="display: flex; justify-content: space-between; align-items: center; padding: var(--space-4); border-top: 1px solid var(--border-color);">
                                    <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                        Page <c:out value="${usersResult.currentPage}" /> of <c:out value="${usersResult.totalPages}" />
                                    </div>
                                    <div style="display: flex; gap: var(--space-2);">
                                        <c:if test="${usersResult.hasPreviousPage()}">
                                            <a href="${ctx}/admin/users?page=${usersResult.currentPage - 1}&q=${query}&role=${selectedRole}&status=${selectedStatus}" class="btn btn-secondary btn-sm">Previous</a>
                                        </c:if>
                                        <c:if test="${usersResult.hasNextPage()}">
                                            <a href="${ctx}/admin/users?page=${usersResult.currentPage + 1}&q=${query}&role=${selectedRole}&status=${selectedStatus}" class="btn btn-secondary btn-sm">Next</a>
                                        </c:if>
                                    </div>
                                </div>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state" style="padding: var(--space-8) var(--space-4);">
                                <div class="empty-state-icon">
                                    <i data-lucide="users"></i>
                                </div>
                                <h3 class="empty-state-title">No user accounts found</h3>
                                <p class="empty-state-desc">No accounts match the specified criteria.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

