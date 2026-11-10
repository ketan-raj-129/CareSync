<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="DOCTOR" />
<c:set var="pageTitle" value="Doctor Practice Dashboard Shell — CareSync" />

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
                    <h1 class="page-title">Doctor Practice Workspace</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Schedule management, patient consultations, and digital prescriptions
                    </p>
                </div>
                <div style="display: flex; gap: var(--space-3);">
                    <button type="button" class="btn btn-secondary btn-sm" onclick="CareSync.showToast('Time-off and availability exceptions UI activates in Phase 5.', 'info');">
                        <i data-lucide="clock"></i>
                        <span>Schedule Leave / Time-Off</span>
                    </button>
                    <button type="button" class="btn btn-primary btn-sm" onclick="CareSync.showToast('Weekly availability scheduler activates in Phase 5.', 'info');">
                        <i data-lucide="calendar"></i>
                        <span>Set Weekly Hours</span>
                    </button>
                </div>
            </div>

            <!-- Phase 1 UI Shell Notice -->
            <div class="alert alert-info">
                <i data-lucide="info" style="flex-shrink: 0;"></i>
                <div>
                    <strong>Phase 1 Dashboard Shell:</strong> This preview establishes the practitioner workspace layout. Live appointment queues, clinical consultation notes, and prescription authoring will be connected to the database in Phase 5.
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Metric Cards Row -->
            <div class="grid-4" style="margin-bottom: var(--space-8);">
                <div class="card stat-card">
                    <div class="stat-icon">
                        <i data-lucide="calendar"></i>
                    </div>
                    <div>
                        <div class="stat-value">--</div>
                        <div class="stat-label">Today's Appointments</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--success-soft); color: var(--success);">
                        <i data-lucide="check-circle-2"></i>
                    </div>
                    <div>
                        <div class="stat-value">--</div>
                        <div class="stat-label">Completed Consultations</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--warning-soft); color: var(--warning);">
                        <i data-lucide="clock"></i>
                    </div>
                    <div>
                        <div class="stat-value">--</div>
                        <div class="stat-label">Pending Requests</div>
                    </div>
                </div>

                <div class="card stat-card">
                    <div class="stat-icon" style="background-color: var(--info-soft); color: var(--info);">
                        <i data-lucide="star"></i>
                    </div>
                    <div>
                        <div class="stat-value">--</div>
                        <div class="stat-label">Average Patient Rating</div>
                    </div>
                </div>
            </div>

            <!-- Two-Column Practice Sections -->
            <div class="grid-2">
                <!-- Section 1: Today's Clinical Schedule -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title">Today's Consultation Schedule</h2>
                        <span class="badge badge-primary">Live Queue</span>
                    </div>
                    <div class="card-body">
                        <!-- Clean Empty State Component -->
                        <div class="empty-state" style="padding: var(--space-8) var(--space-4);">
                            <div class="empty-state-icon">
                                <i data-lucide="calendar-x"></i>
                            </div>
                            <h3 class="empty-state-title" style="font-size: 1.125rem;">No appointments scheduled for today</h3>
                            <p class="empty-state-desc" style="font-size: 0.875rem;">
                                Once patients book consultation slots, your daily patient queue will appear here.
                            </p>
                        </div>
                    </div>
                </div>

                <!-- Section 2: Recent Prescriptions & Records -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title">Recent Clinical Records</h2>
                        <span class="badge badge-info">Prescriptions</span>
                    </div>
                    <div class="card-body">
                        <!-- Clean Empty State Component -->
                        <div class="empty-state" style="padding: var(--space-8) var(--space-4);">
                            <div class="empty-state-icon" style="background-color: var(--info-soft); color: var(--info);">
                                <i data-lucide="file-text"></i>
                            </div>
                            <h3 class="empty-state-title" style="font-size: 1.125rem;">No clinical encounters documented yet</h3>
                            <p class="empty-state-desc" style="font-size: 0.875rem;">
                                Completed consultation notes, diagnoses, and issued prescriptions will be listed here.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

