<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="appointments" />
<c:set var="pageTitle" value="Appointment Administration — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Appointment Master Schedule</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Facility-wide consultation log, booking audit trail, and administrative override controls
                    </p>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Filter Toolbar -->
            <div class="card" style="margin-bottom: var(--space-6);">
                <div class="card-body">
                    <form method="get" action="${ctx}/admin/appointments" style="display: flex; gap: var(--space-4); flex-wrap: wrap; align-items: flex-end;">
                        <div style="flex: 1; min-width: 180px;">
                            <label class="form-label" for="reference">Reference #</label>
                            <input type="text" class="form-control" id="reference" name="reference" placeholder="e.g. APT-..." value="<c:out value='${reference}' />">
                        </div>
                        <div style="min-width: 160px;">
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
                            <label class="form-label" for="status">Status</label>
                            <select class="form-select" id="status" name="status">
                                <option value="">All Statuses</option>
                                <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>Pending</option>
                                <option value="CONFIRMED" ${selectedStatus == 'CONFIRMED' ? 'selected' : ''}>Confirmed</option>
                                <option value="COMPLETED" ${selectedStatus == 'COMPLETED' ? 'selected' : ''}>Completed</option>
                                <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                            </select>
                        </div>
                        <div style="min-width: 150px;">
                            <label class="form-label" for="date">Date</label>
                            <input type="date" class="form-control" id="date" name="date" value="<c:out value='${selectedDate}' />">
                        </div>
                        <div style="display: flex; gap: var(--space-2);">
                            <button type="submit" class="btn btn-primary">
                                <i data-lucide="search"></i>
                                <span>Filter</span>
                            </button>
                            <a href="${ctx}/admin/appointments" class="btn btn-secondary">Reset</a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Appointments Table -->
            <div class="card">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <h2 class="card-title" style="font-size: 1.125rem;">
                        Appointments (<c:out value="${appointmentsResult.totalCount}" /> bookings)
                    </h2>
                </div>
                <div class="card-body" style="padding: 0;">
                    <c:choose>
                        <c:when test="${not empty appointmentsResult.items}">
                            <div class="table-responsive">
                                <table class="table" style="margin-bottom: 0;">
                                    <thead>
                                        <tr>
                                            <th>Reference</th>
                                            <th>Schedule</th>
                                            <th>Doctor</th>
                                            <th>Patient</th>
                                            <th>Department</th>
                                            <th>Status</th>
                                            <th style="text-align: right;">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="apt" items="${appointmentsResult.items}">
                                            <tr>
                                                <td style="font-family: monospace; font-weight: 600;">
                                                    <c:out value="${apt.referenceNumber}" />
                                                </td>
                                                <td>
                                                    <div><c:out value="${apt.appointmentDate}" /></div>
                                                    <div style="font-size: 0.8125rem; color: var(--text-secondary);">
                                                        <c:out value="${apt.startTime}" /> – <c:out value="${apt.endTime}" />
                                                    </div>
                                                </td>
                                                <td>
                                                    <div style="font-weight: 500;">
                                                        Dr. <c:out value="${apt.doctor.firstName}" /> <c:out value="${apt.doctor.lastName}" />
                                                    </div>
                                                </td>
                                                <td>
                                                    <c:out value="${apt.patient.firstName}" /> <c:out value="${apt.patient.lastName}" />
                                                </td>
                                                <td><c:out value="${apt.doctor.specialty.name}" /></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${apt.status == 'CONFIRMED'}">
                                                            <span class="badge badge-success">Confirmed</span>
                                                        </c:when>
                                                        <c:when test="${apt.status == 'PENDING'}">
                                                            <span class="badge badge-warning">Pending</span>
                                                        </c:when>
                                                        <c:when test="${apt.status == 'COMPLETED'}">
                                                            <span class="badge badge-primary">Completed</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-danger">Cancelled</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="text-align: right;">
                                                    <a href="${ctx}/admin/appointments/view?id=${apt.appointmentId}" class="btn btn-secondary btn-sm" style="padding: 4px 10px; font-size: 0.8125rem;">
                                                        Inspect
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>

                            <!-- Pagination -->
                            <c:if test="${appointmentsResult.totalPages > 1}">
                                <div style="display: flex; justify-content: space-between; align-items: center; padding: var(--space-4); border-top: 1px solid var(--border-color);">
                                    <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                        Page <c:out value="${appointmentsResult.currentPage}" /> of <c:out value="${appointmentsResult.totalPages}" />
                                    </div>
                                    <div style="display: flex; gap: var(--space-2);">
                                        <c:if test="${appointmentsResult.hasPreviousPage()}">
                                            <a href="${ctx}/admin/appointments?page=${appointmentsResult.currentPage - 1}&reference=${reference}&specialtyId=${selectedSpecialtyId}&status=${selectedStatus}&date=${selectedDate}" class="btn btn-secondary btn-sm">Previous</a>
                                        </c:if>
                                        <c:if test="${appointmentsResult.hasNextPage()}">
                                            <a href="${ctx}/admin/appointments?page=${appointmentsResult.currentPage + 1}&reference=${reference}&specialtyId=${selectedSpecialtyId}&status=${selectedStatus}&date=${selectedDate}" class="btn btn-secondary btn-sm">Next</a>
                                        </c:if>
                                    </div>
                                </div>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state" style="padding: var(--space-8) var(--space-4);">
                                <div class="empty-state-icon">
                                    <i data-lucide="calendar"></i>
                                </div>
                                <h3 class="empty-state-title">No appointments found</h3>
                                <p class="empty-state-desc">No bookings match the search criteria.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

