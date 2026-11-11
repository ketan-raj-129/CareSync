<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<aside class="dashboard-sidebar">
    <!-- Sidebar Header -->
    <div class="sidebar-header">
        <a href="${ctx}/" class="navbar-brand">
            <div class="brand-icon-pill">
                <i data-lucide="stethoscope"></i>
            </div>
            <span>CareSync</span>
        </a>
        <span class="sidebar-role-badge">
            <c:out value="${activeRole != null ? activeRole : 'PORTAL'}" />
        </span>
    </div>

    <!-- Navigation Items -->
    <ul class="sidebar-menu">
        <c:choose>
            <%-- Administrator Navigation --%>
            <c:when test="${activeRole == 'ADMIN'}">
                <li class="sidebar-section-title">Administration</li>
                <li class="sidebar-item">
                    <a href="${ctx}/admin/dashboard" class="sidebar-link ${activeMenu == 'dashboard' ? 'active' : ''}">
                        <i data-lucide="layout-dashboard"></i>
                        <span>Overview</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${ctx}/admin/users" class="sidebar-link ${activeMenu == 'users' ? 'active' : ''}">
                        <i data-lucide="users"></i>
                        <span>Users</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${ctx}/admin/doctors" class="sidebar-link ${activeMenu == 'doctors' ? 'active' : ''}">
                        <i data-lucide="user-check"></i>
                        <span>Doctors</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${ctx}/admin/patients" class="sidebar-link ${activeMenu == 'patients' ? 'active' : ''}">
                        <i data-lucide="heart-pulse"></i>
                        <span>Patients</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${ctx}/admin/appointments" class="sidebar-link ${activeMenu == 'appointments' ? 'active' : ''}">
                        <i data-lucide="calendar"></i>
                        <span>Appointments</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${ctx}/admin/specialties" class="sidebar-link ${activeMenu == 'specialties' ? 'active' : ''}">
                        <i data-lucide="award"></i>
                        <span>Specialties</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${ctx}/admin/holidays" class="sidebar-link ${activeMenu == 'holidays' ? 'active' : ''}">
                        <i data-lucide="calendar-off"></i>
                        <span>Clinic Holidays</span>
                    </a>
                </li>

                <li class="sidebar-section-title">System Governance</li>
                <li class="sidebar-item">
                    <a href="${ctx}/admin/settings" class="sidebar-link ${activeMenu == 'settings' ? 'active' : ''}">
                        <i data-lucide="sliders"></i>
                        <span>Settings</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${ctx}/admin/audit-logs" class="sidebar-link ${activeMenu == 'audit-logs' ? 'active' : ''}">
                        <i data-lucide="shield-check"></i>
                        <span>Audit Logs</span>
                    </a>
                </li>
            </c:when>

            <%-- Doctor Navigation --%>
            <c:when test="${activeRole == 'DOCTOR'}">
                <li class="sidebar-section-title">Clinical Practice</li>
                <li class="sidebar-item">
                    <a href="${ctx}/doctor/dashboard" class="sidebar-link ${activeMenu == 'dashboard' ? 'active' : ''}">
                        <i data-lucide="layout-dashboard"></i>
                        <span>Overview</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${ctx}/doctor/profile" class="sidebar-link ${activeMenu == 'profile' ? 'active' : ''}">
                        <i data-lucide="user-round"></i>
                        <span>My Profile</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${ctx}/doctor/availability" class="sidebar-link ${activeMenu == 'availability' ? 'active' : ''}">
                        <i data-lucide="clock"></i>
                        <span>Availability</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${ctx}/doctor/appointments" class="sidebar-link ${activeMenu == 'appointments' ? 'active' : ''}">
                        <i data-lucide="calendar"></i>
                        <span>Appointments</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${ctx}/doctor/prescriptions" class="sidebar-link ${activeMenu == 'prescriptions' ? 'active' : ''}">
                        <i data-lucide="pill"></i>
                        <span>Prescriptions</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="#feedback" class="sidebar-link">
                        <i data-lucide="star"></i>
                        <span>Feedback</span>
                    </a>
                </li>
            </c:when>

            <%-- Patient Navigation --%>
            <c:otherwise>
                <li class="sidebar-section-title">My Health Portal</li>
                <li class="sidebar-item">
                    <a href="${ctx}/patient/dashboard" class="sidebar-link active">
                        <i data-lucide="layout-dashboard"></i>
                        <span>Overview</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="#profile" class="sidebar-link">
                        <i data-lucide="user"></i>
                        <span>My Profile</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="#find-doctors" class="sidebar-link">
                        <i data-lucide="search"></i>
                        <span>Find Doctors</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="#appointments" class="sidebar-link">
                        <i data-lucide="calendar"></i>
                        <span>My Appointments</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="#medical-history" class="sidebar-link">
                        <i data-lucide="history"></i>
                        <span>Medical History</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="#prescriptions" class="sidebar-link">
                        <i data-lucide="pill"></i>
                        <span>Prescriptions</span>
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="#notifications" class="sidebar-link">
                        <i data-lucide="bell"></i>
                        <span>Notifications</span>
                    </a>
                </li>
            </c:otherwise>
        </c:choose>

        <li class="sidebar-section-title">Portal Switcher (Phase 1)</li>
        <li class="sidebar-item">
            <a href="${ctx}/admin/dashboard" class="sidebar-link">
                <i data-lucide="shield"></i>
                <span>Admin Shell</span>
            </a>
        </li>
        <li class="sidebar-item">
            <a href="${ctx}/doctor/dashboard" class="sidebar-link">
                <i data-lucide="stethoscope"></i>
                <span>Doctor Shell</span>
            </a>
        </li>
        <li class="sidebar-item">
            <a href="${ctx}/patient/dashboard" class="sidebar-link">
                <i data-lucide="user"></i>
                <span>Patient Shell</span>
            </a>
        </li>
    </ul>

    <!-- Sidebar Footer -->
    <div class="sidebar-footer">
        <a href="${ctx}/" class="sidebar-link" style="color: var(--text-muted);">
            <i data-lucide="arrow-left"></i>
            <span>Return to Landing</span>
        </a>
    </div>
</aside>

<!-- Backdrop overlay for mobile viewport -->
<div class="sidebar-backdrop"></div>

