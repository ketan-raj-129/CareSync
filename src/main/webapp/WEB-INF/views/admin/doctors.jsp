<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="doctors" />
<c:set var="pageTitle" value="Doctor Management & Verification — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Doctor Registry &amp; Verification</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Inspect medical credentials, govern practice status, and monitor clinical availability
                    </p>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Filter Toolbar -->
            <div class="card" style="margin-bottom: var(--space-6);">
                <div class="card-body">
                    <form method="get" action="${ctx}/admin/doctors" style="display: flex; gap: var(--space-4); flex-wrap: wrap; align-items: flex-end;">
                        <div style="flex: 1; min-width: 200px;">
                            <label class="form-label" for="q">Search Doctors</label>
                            <input type="text" class="form-control" id="q" name="q" placeholder="Name or license number..." value="<c:out value='${query}' />">
                        </div>
                        <div style="min-width: 180px;">
                            <label class="form-label" for="specialtyId">Specialty</label>
                            <select class="form-select" id="specialtyId" name="specialtyId">
                                <option value="">All Specialties</option>
                                <c:forEach var="spec" items="${specialties}">
                                    <option value="${spec.specialtyId}" ${selectedSpecialtyId == spec.specialtyId ? 'selected' : ''}>
                                        <c:out value="${spec.name}" />
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div style="min-width: 140px;">
                            <label class="form-label" for="status">Account Status</label>
                            <select class="form-select" id="status" name="status">
                                <option value="">All Statuses</option>
                                <option value="ACTIVE" ${selectedStatus == 'ACTIVE' ? 'selected' : ''}>Active</option>
                                <option value="INACTIVE" ${selectedStatus == 'INACTIVE' ? 'selected' : ''}>Inactive</option>
                                <option value="SUSPENDED" ${selectedStatus == 'SUSPENDED' ? 'selected' : ''}>Suspended</option>
                            </select>
                        </div>
                        <div style="min-width: 140px;">
                            <label class="form-label" for="available">Availability</label>
                            <select class="form-select" id="available" name="available">
                                <option value="">All</option>
                                <option value="true" ${selectedAvailable == true ? 'selected' : ''}>Available</option>
                                <option value="false" ${selectedAvailable == false ? 'selected' : ''}>Unavailable</option>
                            </select>
                        </div>
                        <div style="display: flex; gap: var(--space-2);">
                            <button type="submit" class="btn btn-primary">
                                <i data-lucide="search"></i>
                                <span>Filter</span>
                            </button>
                            <a href="${ctx}/admin/doctors" class="btn btn-secondary">Reset</a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Doctor Registry Table -->
            <div class="card">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <h2 class="card-title" style="font-size: 1.125rem;">
                        Doctors (<c:out value="${doctorsResult.totalCount}" /> registered)
                    </h2>
                </div>
                <div class="card-body" style="padding: 0;">
                    <c:choose>
                        <c:when test="${not empty doctorsResult.items}">
                            <div class="table-responsive">
                                <table class="table" style="margin-bottom: 0;">
                                    <thead>
                                        <tr>
                                            <th>Doctor</th>
                                            <th>License #</th>
                                            <th>Specialty</th>
                                            <th>Experience</th>
                                            <th>Fee</th>
                                            <th>Practice Status</th>
                                            <th>Account</th>
                                            <th style="text-align: right;">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="doc" items="${doctorsResult.items}">
                                            <tr>
                                                <td>
                                                    <div style="font-weight: 600;">
                                                        Dr. <c:out value="${doc.firstName}" /> <c:out value="${doc.lastName}" />
                                                    </div>
                                                    <div style="font-size: 0.8125rem; color: var(--text-secondary);">
                                                        Room <c:out value="${doc.roomNumber != null ? doc.roomNumber : 'TBD'}" />
                                                    </div>
                                                </td>
                                                <td style="font-family: monospace;"><c:out value="${doc.licenseNumber}" /></td>
                                                <td><c:out value="${doc.specialty != null ? doc.specialty.name : '—'}" /></td>
                                                <td><c:out value="${doc.yearsOfExperience != null ? doc.yearsOfExperience : 0}" /> yrs</td>
                                                <td>₹<c:out value="${doc.consultationFee}" /></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${doc.available}">
                                                            <span class="badge badge-success">Available</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-warning">Unavailable</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <span class="badge ${doc.user.status == 'ACTIVE' ? 'badge-success' : 'badge-warning'}">
                                                        <c:out value="${doc.user.status}" />
                                                    </span>
                                                </td>
                                                <td style="text-align: right;">
                                                    <div style="display: inline-flex; gap: 4px;">
                                                        <a href="${ctx}/admin/doctors/view?id=${doc.doctorProfileId}" class="btn btn-secondary btn-sm" style="padding: 4px 8px; font-size: 0.75rem;">
                                                            Profile
                                                        </a>
                                                        <a href="${ctx}/admin/doctors/verification?id=${doc.doctorProfileId}" class="btn btn-secondary btn-sm" style="padding: 4px 8px; font-size: 0.75rem;">
                                                            Verify
                                                        </a>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>

                            <!-- Pagination -->
                            <c:if test="${doctorsResult.totalPages > 1}">
                                <div style="display: flex; justify-content: space-between; align-items: center; padding: var(--space-4); border-top: 1px solid var(--border-color);">
                                    <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                        Page <c:out value="${doctorsResult.currentPage}" /> of <c:out value="${doctorsResult.totalPages}" />
                                    </div>
                                    <div style="display: flex; gap: var(--space-2);">
                                        <c:if test="${doctorsResult.hasPreviousPage()}">
                                            <a href="${ctx}/admin/doctors?page=${doctorsResult.currentPage - 1}&q=${query}&specialtyId=${selectedSpecialtyId}&status=${selectedStatus}&available=${selectedAvailable}" class="btn btn-secondary btn-sm">Previous</a>
                                        </c:if>
                                        <c:if test="${doctorsResult.hasNextPage()}">
                                            <a href="${ctx}/admin/doctors?page=${doctorsResult.currentPage + 1}&q=${query}&specialtyId=${selectedSpecialtyId}&status=${selectedStatus}&available=${selectedAvailable}" class="btn btn-secondary btn-sm">Next</a>
                                        </c:if>
                                    </div>
                                </div>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state" style="padding: var(--space-8) var(--space-4);">
                                <div class="empty-state-icon">
                                    <i data-lucide="user-check"></i>
                                </div>
                                <h3 class="empty-state-title">No doctors found</h3>
                                <p class="empty-state-desc">No physician records match your search criteria.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

