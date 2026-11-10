<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="ADMIN" />
<c:set var="pageTitle" value="Admin Dashboard Shell — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <!-- Sidebar Navigation -->
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <!-- Main Workspace -->
    <div class="dashboard-main">
        <!-- Topbar Navigation -->
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <!-- Dashboard Content -->
        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Executive Governance Overview</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        System administration, doctor verification, and institutional audit monitor
                    </p>
                </div>
                <div style="display: flex; gap: var(--space-3);">
                    <button type="button" class="btn btn-secondary btn-sm" onclick="CareSync.showToast('Audit log exporter scheduled for Phase 6.', 'info');">
                        <i data-lucide="download"></i>
                        <span>Export Logs</span>
                    </button>
                    <button type="button" class="btn btn-primary btn-sm" onclick="CareSync.showToast('Doctor onboarding modal scheduled for Phase 6.', 'info');">
                        <i data-lucide="plus"></i>
                        <span>Add New Doctor</span>
                    </button>
                </div>
            </div>

            <!-- Phase 1 UI Shell Notice -->
            <div class="alert alert-info">
                <i data-lucide="info" style="flex-shrink: 0;"></i>
                <div>
                    <strong>Phase 1 Dashboard Shell:</strong> This preview establishes the structural layout, responsive sidebar, and design tokens for the Administrator portal. Real-time statistics, database queries, and Chart.js analytics will be connected in Phase 6 &amp; Phase 8.
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Metric Cards Row -->
            <div class="grid-4" style="margin-bottom: var(--space-8);">
                <div class="card stat-card">
                    <div class="stat-icon">
                        <i data-lucide="users"></i>
                    </div>
                    <div>
                        <div class="stat-value">--</div>
                        <div class="stat-label">Total Registered Patients</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--info-soft); color: var(--info);">
                        <i data-lucide="user-check"></i>
                    </div>
                    <div>
                        <div class="stat-value">--</div>
                        <div class="stat-label">Verified Active Doctors</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--warning-soft); color: var(--warning);">
                        <i data-lucide="calendar"></i>
                    </div>
                    <div>
                        <div class="stat-value">--</div>
                        <div class="stat-label">Today's Appointments</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--success-soft); color: var(--success);">
                        <i data-lucide="award"></i>
                    </div>
                    <div>
                        <div class="stat-value">--</div>
                        <div class="stat-label">Active Specialties</div>
                    </div>
                </div>
            </div>

            <!-- Two-Column Workspace Content -->
            <div class="grid-2">
                <!-- Section 1: Doctor Verification Queue -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title">Doctor Verification Queue</h2>
                        <span class="badge badge-primary">0 Pending</span>
                    </div>
                    <div class="card-body">
                        <!-- Clean Empty State Component -->
                        <div class="empty-state" style="padding: var(--space-8) var(--space-4);">
                            <div class="empty-state-icon">
                                <i data-lucide="check-circle-2"></i>
                            </div>
                            <h3 class="empty-state-title" style="font-size: 1.125rem;">No pending applications</h3>
                            <p class="empty-state-desc" style="font-size: 0.875rem;">
                                All physician credential submissions have been verified and processed.
                            </p>
                        </div>
                    </div>
                </div>

                <!-- Section 2: Recent Security Audit Trail -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title">Security &amp; Audit Trail</h2>
                        <span class="badge badge-info">Append-Only</span>
                    </div>
                    <div class="card-body">
                        <!-- Clean Empty State Component -->
                        <div class="empty-state" style="padding: var(--space-8) var(--space-4);">
                            <div class="empty-state-icon" style="background-color: var(--info-soft); color: var(--info);">
                                <i data-lucide="shield-check"></i>
                            </div>
                            <h3 class="empty-state-title" style="font-size: 1.125rem;">Audit log monitoring active</h3>
                            <p class="empty-state-desc" style="font-size: 0.875rem;">
                                Security events and transactional operations will be recorded here once Phase 3 activates.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

