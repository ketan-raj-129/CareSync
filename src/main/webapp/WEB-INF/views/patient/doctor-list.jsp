<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="PATIENT" />
<c:set var="pageTitle" value="Find Doctors & Specialists — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <!-- Sidebar Navigation -->
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <!-- Main Workspace -->
    <div class="dashboard-main">
        <!-- Topbar Navigation -->
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Find Healthcare Specialists</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Connect with credentialed medical practitioners across specialties
                    </p>
                </div>
            </div>

            <!-- Search and Filter Bar -->
            <div class="card" style="margin-bottom: var(--space-6);">
                <div class="card-body" style="padding: var(--space-4);">
                    <form action="${ctx}/patient/doctors" method="GET" style="display: flex; gap: var(--space-3); flex-wrap: wrap;">
                        <div style="flex: 1; min-width: 240px;">
                            <input type="text" name="q" class="form-control" 
                                   placeholder="Search by physician name or qualification..." 
                                   value="<c:out value='${searchQuery}' />">
                        </div>

                        <div style="width: 220px;">
                            <select name="specialtyId" class="form-control">
                                <option value="">All Specialties</option>
                                <c:forEach var="spec" items="${specialties}">
                                    <option value="${spec.id}" ${selectedSpecialtyId == spec.id ? 'selected' : ''}>
                                        <c:out value="${spec.name}" />
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <button type="submit" class="btn btn-primary">
                            <i data-lucide="search"></i>
                            <span>Search</span>
                        </button>

                        <c:if test="${not empty searchQuery || not empty selectedSpecialtyId}">
                            <a href="${ctx}/patient/doctors" class="btn btn-secondary">Clear Filters</a>
                        </c:if>
                    </form>
                </div>
            </div>

            <!-- Doctor Directory Grid -->
            <c:choose>
                <c:when test="${not empty doctors}">
                    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: var(--space-4);">
                        <c:forEach var="doc" items="${doctors}">
                            <div class="card" style="display: flex; flex-direction: column; justify-content: space-between;">
                                <div class="card-body">
                                    <div style="display: flex; gap: var(--space-3); align-items: flex-start; margin-bottom: var(--space-3);">
                                        <div class="user-avatar-circle" style="width: 48px; height: 48px; font-size: 1.125rem; flex-shrink: 0;">
                                            <c:out value="${doc.firstName.substring(0, 1).toUpperCase()}" />
                                        </div>
                                        <div style="flex: 1; min-width: 0;">
                                            <h3 style="font-size: 1.0625rem; font-weight: 700; margin: 0 0 2px 0; color: var(--text-primary); text-overflow: ellipsis; overflow: hidden; white-space: nowrap;">
                                                Dr. <c:out value="${doc.fullName}" />
                                            </h3>
                                            <div style="display: flex; align-items: center; gap: var(--space-2); flex-wrap: wrap;">
                                                <span class="badge badge-primary">
                                                    <c:out value="${doc.specialty != null ? doc.specialty.name : 'General'}" />
                                                </span>
                                                <span style="font-size: 0.8125rem; color: var(--text-muted);">
                                                    <c:out value="${doc.experienceYears}" />+ yrs exp
                                                </span>
                                            </div>
                                        </div>
                                    </div>

                                    <div style="font-size: 0.875rem; color: var(--text-secondary); margin-bottom: var(--space-3);">
                                        <div><c:out value="${doc.qualification}" /></div>
                                        <c:if test="${not empty doc.bio}">
                                            <p style="margin: var(--space-2) 0 0 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; font-size: 0.8125rem;">
                                                <c:out value="${doc.bio}" />
                                            </p>
                                        </c:if>
                                    </div>

                                    <div style="display: flex; justify-content: space-between; align-items: center; padding-top: var(--space-3); border-top: 1px solid var(--border-subtle); font-size: 0.875rem;">
                                        <div>
                                            <span style="color: var(--text-muted); font-size: 0.75rem; display: block;">Consultation Fee</span>
                                            <span style="font-weight: 700; color: var(--primary); font-size: 1.125rem;">
                                                $<c:out value="${doc.consultationFee}" />
                                            </span>
                                        </div>

                                        <div style="display: flex; align-items: center; gap: 4px; color: var(--warning);">
                                            <i data-lucide="star" style="width: 14px; height: 14px; fill: currentColor;"></i>
                                            <span style="font-weight: 600; color: var(--text-primary); font-size: 0.875rem;">
                                                <c:out value="${doc.averageRating != null ? doc.averageRating : '5.0'}" />
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div class="card-footer" style="display: flex; gap: var(--space-2); background-color: var(--surface-muted); padding: var(--space-3) var(--space-4);">
                                    <a href="${ctx}/patient/doctors/view?id=${doc.doctorProfileId}" class="btn btn-secondary btn-sm" style="flex: 1;">
                                        View Details
                                    </a>
                                    <a href="${ctx}/patient/appointments/book?doctorId=${doc.doctorProfileId}" class="btn btn-primary btn-sm" style="flex: 1;">
                                        Book Slot
                                    </a>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="card">
                        <div class="card-body">
                            <div class="empty-state" style="padding: var(--space-12) var(--space-4);">
                                <div class="empty-state-icon">
                                    <i data-lucide="user-x"></i>
                                </div>
                                <h3 class="empty-state-title">No specialists found</h3>
                                <p class="empty-state-desc">
                                    We couldn't find any medical practitioners matching your search criteria. Try selecting another specialty or clearing the query.
                                </p>
                                <a href="${ctx}/patient/doctors" class="btn btn-secondary btn-sm">Reset Search Filters</a>
                            </div>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

