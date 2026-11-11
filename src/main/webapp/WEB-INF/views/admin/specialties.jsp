<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="specialties" />
<c:set var="pageTitle" value="Specialty Management — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Medical Specialties &amp; Departments</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Manage clinical domains, department codes, and physician assignments
                    </p>
                </div>
                <div>
                    <a href="${ctx}/admin/specialties/create" class="btn btn-primary btn-sm">
                        <i data-lucide="plus"></i>
                        <span>Add Specialty</span>
                    </a>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Filter Toolbar -->
            <div class="card" style="margin-bottom: var(--space-6);">
                <div class="card-body">
                    <form method="get" action="${ctx}/admin/specialties" style="display: flex; gap: var(--space-4); flex-wrap: wrap; align-items: flex-end;">
                        <div style="flex: 1; min-width: 200px;">
                            <label class="form-label" for="q">Search Specialties</label>
                            <input type="text" class="form-control" id="q" name="q" placeholder="Name or code..." value="<c:out value='${query}' />">
                        </div>
                        <div style="min-width: 160px;">
                            <label class="form-label" for="activeOnly">Active Status</label>
                            <select class="form-select" id="activeOnly" name="activeOnly">
                                <option value="">All Specialties</option>
                                <option value="true" ${activeOnly == true ? 'selected' : ''}>Active Only</option>
                                <option value="false" ${activeOnly == false ? 'selected' : ''}>Inactive Only</option>
                            </select>
                        </div>
                        <div style="display: flex; gap: var(--space-2);">
                            <button type="submit" class="btn btn-primary">
                                <i data-lucide="search"></i>
                                <span>Filter</span>
                            </button>
                            <a href="${ctx}/admin/specialties" class="btn btn-secondary">Reset</a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Specialties Table -->
            <div class="card">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <h2 class="card-title" style="font-size: 1.125rem;">
                        Specialties (<c:out value="${specialtiesResult.totalCount}" /> total)
                    </h2>
                </div>
                <div class="card-body" style="padding: 0;">
                    <c:choose>
                        <c:when test="${not empty specialtiesResult.items}">
                            <div class="table-responsive">
                                <table class="table" style="margin-bottom: 0;">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Specialty Name</th>
                                            <th>Icon</th>
                                            <th>Description</th>
                                            <th>Associated Doctors</th>
                                            <th>Status</th>
                                            <th style="text-align: right;">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="spec" items="${specialtiesResult.items}">
                                            <tr>
                                                <td>#<c:out value="${spec.specialtyId}" /></td>
                                                <td style="font-weight: 600;"><c:out value="${spec.name}" /></td>
                                                <td>
                                                    <span class="badge badge-info" style="font-family: monospace;">
                                                        <c:out value="${spec.iconName != null ? spec.iconName : 'stethoscope'}" />
                                                    </span>
                                                </td>
                                                <td style="max-width: 320px; font-size: 0.875rem; color: var(--text-secondary);">
                                                    <c:out value="${spec.description != null ? spec.description : '—'}" />
                                                </td>
                                                <td>
                                                    <span class="badge badge-secondary">
                                                        <c:out value="${doctorCounts[spec.specialtyId] != null ? doctorCounts[spec.specialtyId] : 0}" /> doctors
                                                    </span>
                                                </td>
                                                <td>
                                                    <span class="badge ${spec.active ? 'badge-success' : 'badge-warning'}">
                                                        ${spec.active ? 'Active' : 'Inactive'}
                                                    </span>
                                                </td>
                                                <td style="text-align: right;">
                                                    <div style="display: inline-flex; gap: 6px;">
                                                        <a href="${ctx}/admin/specialties/edit?id=${spec.specialtyId}" class="btn btn-secondary btn-sm" style="padding: 4px 8px; font-size: 0.75rem;">
                                                            Edit
                                                        </a>
                                                        <form method="post" action="${ctx}/admin/specialties/toggle" style="display: inline; margin: 0;">
                                                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                                                            <input type="hidden" name="specialtyId" value="<c:out value='${spec.specialtyId}' />" />
                                                            <input type="hidden" name="active" value="${!spec.active}" />
                                                            <button type="submit" class="btn btn-secondary btn-sm" style="padding: 4px 8px; font-size: 0.75rem;">
                                                                ${spec.active ? 'Deactivate' : 'Activate'}
                                                            </button>
                                                        </form>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>

                            <!-- Pagination -->
                            <c:if test="${specialtiesResult.totalPages > 1}">
                                <div style="display: flex; justify-content: space-between; align-items: center; padding: var(--space-4); border-top: 1px solid var(--border-color);">
                                    <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                        Page <c:out value="${specialtiesResult.currentPage}" /> of <c:out value="${specialtiesResult.totalPages}" />
                                    </div>
                                    <div style="display: flex; gap: var(--space-2);">
                                        <c:if test="${specialtiesResult.hasPreviousPage()}">
                                            <a href="${ctx}/admin/specialties?page=${specialtiesResult.currentPage - 1}&q=${query}&activeOnly=${activeOnly}" class="btn btn-secondary btn-sm">Previous</a>
                                        </c:if>
                                        <c:if test="${specialtiesResult.hasNextPage()}">
                                            <a href="${ctx}/admin/specialties?page=${specialtiesResult.currentPage + 1}&q=${query}&activeOnly=${activeOnly}" class="btn btn-secondary btn-sm">Next</a>
                                        </c:if>
                                    </div>
                                </div>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state" style="padding: var(--space-8) var(--space-4);">
                                <div class="empty-state-icon">
                                    <i data-lucide="award"></i>
                                </div>
                                <h3 class="empty-state-title">No specialties found</h3>
                                <p class="empty-state-desc">No departments match your filter criteria.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
