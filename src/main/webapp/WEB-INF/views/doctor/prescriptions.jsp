<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="DOCTOR" />
<c:set var="activeMenu" value="prescriptions" />
<c:set var="pageTitle" value="Issued Prescriptions — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Prescription Registry</h1>
                    <p class="page-subtitle">View and print all authorized medical prescriptions generated during patient encounters.</p>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <div class="card">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <h2 class="card-title" style="margin: 0; font-size: 1.125rem; display: flex; align-items: center; gap: var(--space-2);">
                        <i data-lucide="file-text" style="color: var(--primary);"></i>
                        <span>Issued Prescriptions (${prescriptions != null ? prescriptions.size() : 0})</span>
                    </h2>
                </div>
                <div class="card-body" style="padding: 0;">
                    <c:choose>
                        <c:when test="${empty prescriptions}">
                            <div style="padding: var(--space-12); text-align: center;">
                                <i data-lucide="pill" style="width: 48px; height: 48px; color: var(--text-muted); stroke-width: 1.5; margin-bottom: var(--space-3);"></i>
                                <h3 style="font-size: 1.125rem; font-weight: 600; color: var(--text-primary); margin-bottom: var(--space-1);">No Prescriptions Issued</h3>
                                <p style="color: var(--text-secondary); max-width: 420px; margin: 0 auto var(--space-4);">
                                    When you conduct clinical consultations and prescribe medications, issued prescriptions will appear here for archival and printing.
                                </p>
                                <a href="${ctx}/doctor/appointments" class="btn btn-primary btn-sm">
                                    <i data-lucide="calendar"></i> Go to Appointments
                                </a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table" style="margin: 0;">
                                    <thead>
                                        <tr>
                                            <th>Rx #</th>
                                            <th>Appointment Ref</th>
                                            <th>Medications Count</th>
                                            <th>General Advice</th>
                                            <th>Issue Date</th>
                                            <th style="text-align: right;">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="rx" items="${prescriptions}">
                                            <tr>
                                                <td>
                                                    <span style="font-family: monospace; font-weight: 600; color: var(--primary);">
                                                        #RX-<c:out value="${rx.id}" />
                                                    </span>
                                                </td>
                                                <td>
                                                    <a href="${ctx}/doctor/appointments/view?id=${rx.appointmentId}" style="color: var(--primary); font-weight: 500; text-decoration: none;">
                                                        Apt #<c:out value="${rx.appointmentId}" />
                                                    </a>
                                                </td>
                                                <td>
                                                    <span class="badge badge-info">
                                                        <c:out value="${rx.items != null ? rx.items.size() : 0}" /> medicines
                                                    </span>
                                                </td>
                                                <td style="max-width: 280px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                                                    <c:out value="${not empty rx.generalAdvice ? rx.generalAdvice : '—'}" />
                                                </td>
                                                <td>
                                                    <c:out value="${rx.createdAt}" />
                                                </td>
                                                <td style="text-align: right;">
                                                    <a href="${ctx}/doctor/prescriptions/view?id=${rx.id}" class="btn btn-secondary btn-sm" title="View Prescription">
                                                        <i data-lucide="eye"></i>
                                                        <span>View & Print</span>
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

