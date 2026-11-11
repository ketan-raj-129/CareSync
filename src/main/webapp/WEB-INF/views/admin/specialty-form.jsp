<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="specialties" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <div style="display: flex; align-items: center; gap: var(--space-2); margin-bottom: 4px;">
                        <a href="${ctx}/admin/specialties" style="color: var(--text-secondary); text-decoration: none; font-size: 0.875rem;">
                            &larr; Back to Specialties
                        </a>
                    </div>
                    <h1 class="page-title">
                        <c:choose>
                            <c:when test="${formMode == 'edit'}">Edit Medical Specialty</c:when>
                            <c:otherwise>Register New Medical Specialty</c:otherwise>
                        </c:choose>
                    </h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Clinical department code, taxonomy descriptor, and directory status
                    </p>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <div class="card" style="max-width: 680px;">
                <div class="card-body">
                    <form method="post" action="${ctx}/admin/specialties/${formMode == 'edit' ? 'edit' : 'create'}">
                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />" />
                        
                        <c:if test="${formMode == 'edit'}">
                            <input type="hidden" name="specialtyId" value="<c:out value='${specialty.specialtyId}' />" />
                        </c:if>

                        <div style="margin-bottom: var(--space-4);">
                            <label class="form-label" for="specialtyName">Specialty Name *</label>
                            <input type="text" class="form-control" id="specialtyName" name="specialtyName" required maxlength="100"
                                   placeholder="e.g. Cardiology, Neurology, Pediatrics"
                                   value="<c:out value='${specialty != null ? specialty.name : \"\"}' />">
                        </div>

                        <div style="margin-bottom: var(--space-4);">
                            <label class="form-label" for="iconName">Display Icon (Lucide Icon Name)</label>
                            <input type="text" class="form-control" id="iconName" name="iconName" maxlength="50"
                                   placeholder="e.g. stethoscope, heart, brain, activity"
                                   value="<c:out value='${specialty != null ? specialty.iconName : \"stethoscope\"}' />">
                            <div style="font-size: 0.75rem; color: var(--text-secondary); margin-top: 4px;">
                                Lucide icon identifier displayed in public specialty badges and directory cards.
                            </div>
                        </div>

                        <div style="margin-bottom: var(--space-4);">
                            <label class="form-label" for="description">Department Description</label>
                            <textarea class="form-control" id="description" name="description" rows="3"
                                      placeholder="Brief overview of clinical scope and treatments..."><c:out value='${specialty != null ? specialty.description : \"\"}' /></textarea>
                        </div>

                        <div style="margin-bottom: var(--space-6);">
                            <div class="form-check">
                                <input type="checkbox" class="form-check-input" id="isActive" name="isActive" value="true"
                                       ${specialty == null || specialty.active ? 'checked' : ''}>
                                <label class="form-check-label" for="isActive">
                                    Specialty is Active (Visible to patients during appointment booking)
                                </label>
                            </div>
                        </div>

                        <div style="display: flex; gap: var(--space-3);">
                            <button type="submit" class="btn btn-primary">
                                <i data-lucide="check"></i>
                                <span><c:out value="${formMode == 'edit' ? 'Save Changes' : 'Create Specialty'}" /></span>
                            </button>
                            <a href="${ctx}/admin/specialties" class="btn btn-secondary">Cancel</a>
                        </div>
                    </form>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
