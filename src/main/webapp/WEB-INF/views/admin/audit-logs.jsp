<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="activeMenu" value="audit-logs" />
<c:set var="pageTitle" value="System Audit Logs & Compliance — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Security &amp; Operational Audit Trail</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Append-only compliance log, cryptographic tamper-evidence, and governance event registry
                    </p>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Append-only Notice Banner -->
            <div class="alert alert-info" style="margin-bottom: var(--space-6);">
                <i data-lucide="shield-check" style="flex-shrink: 0;"></i>
                <div>
                    <strong>Immutable Compliance Log:</strong> In accordance with healthcare audit requirements, audit records are strictly append-only and cryptographically hashed. Records cannot be edited, purged, or truncated through the administration portal.
                </div>
            </div>

            <!-- Filter Toolbar -->
            <div class="card" style="margin-bottom: var(--space-6);">
                <div class="card-body">
                    <form method="get" action="${ctx}/admin/audit-logs" style="display: flex; gap: var(--space-4); flex-wrap: wrap; align-items: flex-end;">
                        <div style="flex: 1; min-width: 180px;">
                            <label class="form-label" for="action">Action Keyword</label>
                            <input type="text" class="form-control" id="action" name="action" placeholder="e.g. AUTH, UPDATE, CANCEL..." value="<c:out value='${selectedAction}' />">
                        </div>
                        <div style="min-width: 160px;">
                            <label class="form-label" for="targetEntity">Target Entity</label>
                            <input type="text" class="form-control" id="targetEntity" name="targetEntity" placeholder="e.g. USER, APPOINTMENT..." value="<c:out value='${selectedTargetEntity}' />">
                        </div>
                        <div style="min-width: 140px;">
                            <label class="form-label" for="userId">Actor User ID</label>
                            <input type="number" class="form-control" id="userId" name="userId" placeholder="e.g. 1" value="<c:out value='${selectedUserId}' />">
                        </div>
                        <div style="display: flex; gap: var(--space-2);">
                            <button type="submit" class="btn btn-primary">
                                <i data-lucide="search"></i>
                                <span>Filter</span>
                            </button>
                            <a href="${ctx}/admin/audit-logs" class="btn btn-secondary">Reset</a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Audit Logs Table -->
            <div class="card">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <h2 class="card-title" style="font-size: 1.125rem;">
                        Audit Log Records (<c:out value="${auditLogsResult.totalCount}" /> entries)
                    </h2>
                </div>
                <div class="card-body" style="padding: 0;">
                    <c:choose>
                        <c:when test="${not empty auditLogsResult.items}">
                            <div class="table-responsive">
                                <table class="table" style="margin-bottom: 0; font-size: 0.875rem;">
                                    <thead>
                                        <tr>
                                            <th>Log ID</th>
                                            <th>Timestamp (UTC)</th>
                                            <th>Action</th>
                                            <th>Target Entity</th>
                                            <th>Target ID</th>
                                            <th>Actor ID</th>
                                            <th>IP Address</th>
                                            <th>Details</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="log" items="${auditLogsResult.items}">
                                            <tr>
                                                <td style="font-family: monospace;">#<c:out value="${log.id}" /></td>
                                                <td style="color: var(--text-secondary); white-space: nowrap;">
                                                    <c:out value="${log.createdAt}" />
                                                </td>
                                                <td>
                                                    <span class="badge badge-info" style="font-family: monospace; font-size: 0.75rem;">
                                                        <c:out value="${log.action}" />
                                                    </span>
                                                </td>
                                                <td><c:out value="${log.targetEntity}" /></td>
                                                <td style="font-family: monospace;">
                                                    <c:out value="${log.targetId != null ? log.targetId : '—'}" />
                                                </td>
                                                <td>
                                                    <span class="badge badge-secondary">
                                                        #<c:out value="${log.userId != null ? log.userId : 'SYS'}" />
                                                    </span>
                                                </td>
                                                <td style="font-family: monospace; font-size: 0.8125rem;">
                                                    <c:out value="${log.ipAddress != null ? log.ipAddress : '—'}" />
                                                </td>
                                                <td style="max-width: 320px; font-family: monospace; font-size: 0.75rem; color: var(--text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                                                    <c:out value="${log.details != null ? log.details : '—'}" />
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>

                            <!-- Pagination -->
                            <c:if test="${auditLogsResult.totalPages > 1}">
                                <div style="display: flex; justify-content: space-between; align-items: center; padding: var(--space-4); border-top: 1px solid var(--border-color);">
                                    <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                        Page <c:out value="${auditLogsResult.currentPage}" /> of <c:out value="${auditLogsResult.totalPages}" />
                                    </div>
                                    <div style="display: flex; gap: var(--space-2);">
                                        <c:if test="${auditLogsResult.hasPreviousPage()}">
                                            <a href="${ctx}/admin/audit-logs?page=${auditLogsResult.currentPage - 1}&action=${selectedAction}&targetEntity=${selectedTargetEntity}&userId=${selectedUserId}" class="btn btn-secondary btn-sm">Previous</a>
                                        </c:if>
                                        <c:if test="${auditLogsResult.hasNextPage()}">
                                            <a href="${ctx}/admin/audit-logs?page=${auditLogsResult.currentPage + 1}&action=${selectedAction}&targetEntity=${selectedTargetEntity}&userId=${selectedUserId}" class="btn btn-secondary btn-sm">Next</a>
                                        </c:if>
                                    </div>
                                </div>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state" style="padding: var(--space-8) var(--space-4);">
                                <div class="empty-state-icon">
                                    <i data-lucide="shield-check"></i>
                                </div>
                                <h3 class="empty-state-title">No audit records found</h3>
                                <p class="empty-state-desc">No events match the specified filter parameters.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
