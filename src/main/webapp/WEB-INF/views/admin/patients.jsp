<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="patients" />
<c:set var="pageTitle" value="Patient Management — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Patient Directory &amp; Records</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Search and manage registered patient demographics and account statuses
                    </p>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Filter & Search Toolbar -->
            <div class="card" style="margin-bottom: var(--space-6);">
                <div class="card-body">
                    <form method="get" action="${ctx}/admin/patients" style="display: flex; gap: var(--space-4); flex-wrap: wrap; align-items: flex-end;">
                        <div style="flex: 1; min-width: 220px;">
                            <label class="form-label" for="q">Search Patients</label>
                            <input type="text" class="form-control" id="q" name="q" placeholder="Name, email or phone..." value="<c:out value='${query}' />">
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
                            <a href="${ctx}/admin/patients" class="btn btn-secondary">Reset</a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Patient Table -->
            <div class="card">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <h2 class="card-title" style="font-size: 1.125rem;">
                        Patients (<c:out value="${patientsResult.totalCount}" /> total)
                    </h2>
                </div>
                <div class="card-body" style="padding: 0;">
                    <c:choose>
                        <c:when test="${not empty patientsResult.items}">
                            <div class="table-responsive">
                                <table class="table" style="margin-bottom: 0;">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Patient Name</th>
                                            <th>Email</th>
                                            <th>Phone</th>
                                            <th>Gender</th>
                                            <th>Blood Group</th>
                                            <th>Status</th>
                                            <th style="text-align: right;">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="p" items="${patientsResult.items}">
                                            <tr>
                                                <td>#<c:out value="${p.patientProfileId}" /></td>
                                                <td style="font-weight: 600;">
                                                    <c:out value="${p.firstName}" /> <c:out value="${p.lastName}" />
                                                </td>
                                                <td><c:out value="${p.email}" /></td>
                                                <td><c:out value="${p.phoneNumber != null ? p.phoneNumber : '—'}" /></td>
                                                <td><c:out value="${p.gender != null ? p.gender : '—'}" /></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty p.bloodGroup}">
                                                            <span class="badge badge-info"><c:out value="${p.bloodGroup}" /></span>
                                                        </c:when>
                                                        <c:otherwise>—</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <span class="badge ${p.user.status == 'ACTIVE' ? 'badge-success' : 'badge-warning'}">
                                                        <c:out value="${p.user.status}" />
                                                    </span>
                                                </td>
                                                <td style="text-align: right;">
                                                    <a href="${ctx}/admin/patients/view?id=${p.patientProfileId}" class="btn btn-secondary btn-sm" style="padding: 4px 10px; font-size: 0.8125rem;">
                                                        View Details
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>

                            <!-- Pagination -->
                            <c:if test="${patientsResult.totalPages > 1}">
                                <div style="display: flex; justify-content: space-between; align-items: center; padding: var(--space-4); border-top: 1px solid var(--border-color);">
                                    <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                        Page <c:out value="${patientsResult.currentPage}" /> of <c:out value="${patientsResult.totalPages}" />
                                    </div>
                                    <div style="display: flex; gap: var(--space-2);">
                                        <c:if test="${patientsResult.hasPreviousPage()}">
                                            <a href="${ctx}/admin/patients?page=${patientsResult.currentPage - 1}&q=${query}&status=${selectedStatus}" class="btn btn-secondary btn-sm">Previous</a>
                                        </c:if>
                                        <c:if test="${patientsResult.hasNextPage()}">
                                            <a href="${ctx}/admin/patients?page=${patientsResult.currentPage + 1}&q=${query}&status=${selectedStatus}" class="btn btn-secondary btn-sm">Next</a>
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
                                <h3 class="empty-state-title">No patients found</h3>
                                <p class="empty-state-desc">No patient records match the specified search filters.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
