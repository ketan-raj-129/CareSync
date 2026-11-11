<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="DOCTOR" />
<c:set var="activeMenu" value="prescriptions" />
<c:set var="pageTitle" value="Prescription #RX-${prescription.id} — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<style>
@media print {
    .dashboard-sidebar, .topbar, .page-header-actions, .no-print, .btn, footer {
        display: none !important;
    }
    .dashboard-main {
        margin: 0 !important;
        padding: 0 !important;
        background: #fff !important;
    }
    .dashboard-content {
        padding: 0 !important;
        background: #fff !important;
    }
    .prescription-card {
        border: none !important;
        box-shadow: none !important;
        margin: 0 !important;
        padding: 0 !important;
    }
}
.prescription-sheet {
    background: #ffffff;
    border-radius: var(--radius-lg);
    border: 1px solid var(--border-color);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    max-width: 860px;
    margin: 0 auto;
    padding: var(--space-8);
}
.rx-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    border-bottom: 2px solid var(--primary);
    padding-bottom: var(--space-6);
    margin-bottom: var(--space-6);
}
.rx-doctor-info h2 {
    color: var(--primary);
    font-size: 1.375rem;
    font-weight: 700;
    margin: 0 0 4px 0;
}
.rx-doctor-info p {
    margin: 0;
    color: var(--text-secondary);
    font-size: 0.875rem;
}
.rx-clinic-logo {
    text-align: right;
}
.rx-clinic-logo h3 {
    margin: 0;
    color: var(--primary);
    font-size: 1.25rem;
    font-weight: 800;
    letter-spacing: -0.5px;
}
.rx-patient-box {
    background: var(--bg-hover);
    border-radius: var(--radius-md);
    padding: var(--space-4) var(--space-5);
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: var(--space-4);
    margin-bottom: var(--space-6);
    font-size: 0.875rem;
}
.rx-sym {
    font-family: "Times New Roman", Times, serif;
    font-size: 2.25rem;
    font-weight: bold;
    color: var(--primary);
    line-height: 1;
    margin-bottom: var(--space-3);
}
.rx-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: var(--space-6);
}
.rx-table th {
    background: var(--bg-hover);
    padding: var(--space-3);
    font-size: 0.8125rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    color: var(--text-secondary);
    border-bottom: 1px solid var(--border-color);
}
.rx-table td {
    padding: var(--space-3);
    font-size: 0.875rem;
    border-bottom: 1px solid var(--border-color);
    vertical-align: middle;
}
.rx-footer {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-top: var(--space-10);
    padding-top: var(--space-6);
    border-top: 1px solid var(--border-color);
}
</style>

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header no-print">
                <div>
                    <h1 class="page-title">Prescription Sheet</h1>
                    <p class="page-subtitle">Prescription Reference: <strong>#RX-${prescription.id}</strong></p>
                </div>
                <div class="page-header-actions" style="display: flex; gap: var(--space-3);">
                    <a href="${ctx}/doctor/prescriptions" class="btn btn-secondary btn-sm">
                        <i data-lucide="arrow-left"></i> Back to Prescriptions
                    </a>
                    <button type="button" class="btn btn-primary btn-sm" onclick="window.print();">
                        <i data-lucide="printer"></i> Print / Save as PDF
                    </button>
                </div>
            </div>

            <div class="prescription-sheet prescription-card">
                <!-- Header -->
                <div class="rx-header">
                    <div class="rx-doctor-info">
                        <h2>Dr. <c:out value="${doctor.user.firstName} ${doctor.user.lastName}" /></h2>
                        <p><strong><c:out value="${doctor.specialty.name != null ? doctor.specialty.name : 'General Physician'}" /></strong></p>
                        <p><c:out value="${doctor.qualification != null ? doctor.qualification : 'MBBS'}" /></p>
                        <c:if test="${not empty doctor.roomNumber}">
                            <p>Consultation Suite: Room <c:out value="${doctor.roomNumber}" /></p>
                        </c:if>
                    </div>
                    <div class="rx-clinic-logo">
                        <h3>CareSync</h3>
                        <p style="margin: 0; color: var(--text-secondary); font-size: 0.8125rem;">Healthcare, connected.</p>
                        <p style="margin: 4px 0 0; color: var(--text-muted); font-size: 0.75rem;">Digital Health Records Network</p>
                    </div>
                </div>

                <!-- Patient & Rx Details -->
                <div class="rx-patient-box">
                    <div>
                        <span style="color: var(--text-secondary); font-size: 0.75rem; text-transform: uppercase; display: block;">Patient Name</span>
                        <strong>
                            <c:choose>
                                <c:when test="${not empty appointment.patient}">
                                    <c:out value="${appointment.patient.firstName} ${appointment.patient.lastName}" />
                                </c:when>
                                <c:otherwise>Patient #<c:out value="${prescription.patientId}" /></c:otherwise>
                            </c:choose>
                        </strong>
                    </div>
                    <div>
                        <span style="color: var(--text-secondary); font-size: 0.75rem; text-transform: uppercase; display: block;">Age / Gender</span>
                        <strong>
                            <c:choose>
                                <c:when test="${not empty appointment.patient}">
                                    <c:out value="${appointment.patient.gender != null ? appointment.patient.gender : '—'}" />
                                </c:when>
                                <c:otherwise>—</c:otherwise>
                            </c:choose>
                        </strong>
                    </div>
                    <div>
                        <span style="color: var(--text-secondary); font-size: 0.75rem; text-transform: uppercase; display: block;">Prescription Date</span>
                        <strong><c:out value="${prescription.createdAt}" /></strong>
                    </div>
                    <div>
                        <span style="color: var(--text-secondary); font-size: 0.75rem; text-transform: uppercase; display: block;">Encounter Ref</span>
                        <strong><c:out value="${appointment != null ? appointment.referenceNumber : prescription.appointmentId}" /></strong>
                    </div>
                </div>

                <!-- Medical Symbol -->
                <div class="rx-sym">℞</div>

                <!-- Medication Table -->
                <table class="rx-table">
                    <thead>
                        <tr>
                            <th style="width: 5%;">#</th>
                            <th style="width: 30%;">Medicine Name</th>
                            <th style="width: 15%;">Dosage</th>
                            <th style="width: 20%;">Frequency</th>
                            <th style="width: 15%;">Duration</th>
                            <th style="width: 15%;">Instructions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty prescription.items}">
                                <c:forEach var="item" items="${prescription.items}" varStatus="status">
                                    <tr>
                                        <td style="color: var(--text-muted); font-size: 0.8125rem;">${status.index + 1}</td>
                                        <td><strong><c:out value="${item.medicineName}" /></strong></td>
                                        <td><c:out value="${not empty item.dosage ? item.dosage : '—'}" /></td>
                                        <td><c:out value="${not empty item.frequency ? item.frequency : '—'}" /></td>
                                        <td><c:out value="${not empty item.duration ? item.duration : '—'}" /></td>
                                        <td><c:out value="${not empty item.instructions ? item.instructions : 'As advised'}" /></td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" style="text-align: center; color: var(--text-secondary); padding: var(--space-4);">
                                        No medication line items listed for this prescription.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

                <!-- Advice / Instructions -->
                <c:if test="${not empty prescription.generalAdvice}">
                    <div style="margin-top: var(--space-6); background: var(--bg-hover); padding: var(--space-4); border-radius: var(--radius-md);">
                        <strong style="color: var(--primary); display: block; margin-bottom: 4px; font-size: 0.875rem;">
                            General Instructions & Advice:
                        </strong>
                        <p style="margin: 0; font-size: 0.875rem; color: var(--text-primary); white-space: pre-wrap;"><c:out value="${prescription.generalAdvice}" /></p>
                    </div>
                </c:if>

                <!-- Footer & Signature -->
                <div class="rx-footer">
                    <div>
                        <p style="margin: 0; color: var(--text-muted); font-size: 0.75rem;">
                            Generated electronically by CareSync Clinical System. Valid with attending physician signature.
                        </p>
                    </div>
                    <div style="text-align: center; min-width: 200px;">
                        <div style="border-bottom: 1px solid var(--border-color); height: 40px; margin-bottom: 8px;"></div>
                        <p style="margin: 0; font-weight: 600; font-size: 0.875rem;">
                            Dr. <c:out value="${doctor.user.firstName} ${doctor.user.lastName}" />
                        </p>
                        <p style="margin: 0; color: var(--text-muted); font-size: 0.75rem;">Authorized Medical Practitioner</p>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

