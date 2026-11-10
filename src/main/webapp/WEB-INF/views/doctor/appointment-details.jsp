<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="DOCTOR" />
<c:set var="activeMenu" value="appointments" />
<c:set var="pageTitle" value="Encounter Details — ${appointment.referenceNumber}" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <div style="display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-1);">
                        <h1 class="page-title" style="margin: 0;">Consultation Encounter</h1>
                        <c:choose>
                            <c:when test="${appointment.status == 'CONFIRMED'}"><span class="badge badge-success">Confirmed</span></c:when>
                            <c:when test="${appointment.status == 'PENDING'}"><span class="badge badge-warning">Pending Approval</span></c:when>
                            <c:when test="${appointment.status == 'COMPLETED'}"><span class="badge badge-info">Completed</span></c:when>
                            <c:when test="${appointment.status == 'CANCELLED'}"><span class="badge badge-danger">Cancelled</span></c:when>
                            <c:otherwise><span class="badge badge-secondary"><c:out value="${appointment.status}" /></span></c:otherwise>
                        </c:choose>
                    </div>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Reference: <strong><c:out value="${appointment.referenceNumber}" /></strong> &bull; 
                        Booked: <c:out value="${appointment.appointmentDate}" /> at <c:out value="${appointment.startTime}" /> - <c:out value="${appointment.endTime}" />
                    </p>
                </div>
                <div style="display: flex; gap: var(--space-3); flex-wrap: wrap;">
                    <a href="${ctx}/doctor/appointments" class="btn btn-secondary btn-sm">
                        <i data-lucide="arrow-left"></i>
                        <span>Back to Appointments</span>
                    </a>

                    <c:if test="${appointment.status == 'PENDING'}">
                        <form action="${ctx}/doctor/appointments/action" method="POST" style="margin: 0; display: inline;">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <input type="hidden" name="appointmentId" value="${appointment.id}">
                            <input type="hidden" name="action" value="CONFIRM">
                            <button type="submit" class="btn btn-primary btn-sm">
                                <i data-lucide="check"></i> Confirm Appointment
                            </button>
                        </form>
                    </c:if>

                    <c:if test="${appointment.status == 'CONFIRMED'}">
                        <a href="${ctx}/doctor/consultation?appointmentId=${appointment.id}" class="btn btn-primary btn-sm">
                            <i data-lucide="stethoscope"></i>
                            <span>Document Consultation</span>
                        </a>
                    </c:if>

                    <c:if test="${not empty prescription}">
                        <a href="${ctx}/doctor/prescriptions/view?id=${prescription.id}" class="btn btn-secondary btn-sm" target="_blank">
                            <i data-lucide="printer"></i>
                            <span>Print Prescription</span>
                        </a>
                    </c:if>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <div style="display: grid; grid-template-columns: 1fr 2fr; gap: var(--space-6);">
                <!-- Left: Patient Summary & Encounter Context -->
                <div style="display: flex; flex-direction: column; gap: var(--space-6);">
                    <div class="card">
                        <div class="card-header">
                            <h2 class="card-title" style="margin: 0; font-size: 1.125rem; display: flex; align-items: center; gap: var(--space-2);">
                                <i data-lucide="user" style="color: var(--primary);"></i>
                                <span>Patient Information</span>
                            </h2>
                        </div>
                        <div class="card-body" style="display: flex; flex-direction: column; gap: var(--space-3); font-size: 0.875rem;">
                            <div>
                                <span style="color: var(--text-secondary);">Full Name:</span>
                                <strong style="float: right;"><c:out value="${appointment.patient.firstName} ${appointment.patient.lastName}" /></strong>
                            </div>
                            <div>
                                <span style="color: var(--text-secondary);">Phone:</span>
                                <strong style="float: right;"><c:out value="${appointment.patient.phoneNumber != null ? appointment.patient.phoneNumber : '—'}" /></strong>
                            </div>
                            <div>
                                <span style="color: var(--text-secondary);">Email:</span>
                                <strong style="float: right;"><c:out value="${appointment.patient.email}" /></strong>
                            </div>
                            <div>
                                <span style="color: var(--text-secondary);">Date of Birth:</span>
                                <strong style="float: right;"><c:out value="${appointment.patient.dateOfBirth != null ? appointment.patient.dateOfBirth : '—'}" /></strong>
                            </div>
                            <div>
                                <span style="color: var(--text-secondary);">Blood Group:</span>
                                <strong style="float: right;">
                                    <c:choose>
                                        <c:when test="${not empty appointment.patient.bloodGroup}"><span class="badge badge-info"><c:out value="${appointment.patient.bloodGroup}" /></span></c:when>
                                        <c:otherwise>Not recorded</c:otherwise>
                                    </c:choose>
                                </strong>
                            </div>
                        </div>
                    </div>

                    <!-- Reason for Visit & Encounter Remarks -->
                    <div class="card">
                        <div class="card-header">
                            <h2 class="card-title" style="margin: 0; font-size: 1.125rem;">Reason for Consultation</h2>
                        </div>
                        <div class="card-body">
                            <p style="margin: 0; color: var(--text-primary); font-size: 0.9375rem;">
                                <c:out value="${appointment.reasonForVisit != null ? appointment.reasonForVisit : 'No specific reason provided by patient during booking.'}" />
                            </p>

                            <c:if test="${appointment.status == 'CANCELLED'}">
                                <div class="alert alert-danger" style="margin-top: var(--space-4); margin-bottom: 0;">
                                    <strong>Cancellation Note:</strong>
                                    <div><c:out value="${appointment.cancellationReason != null ? appointment.cancellationReason : 'Cancelled.'}" /></div>
                                </div>
                            </c:if>
                        </div>
                    </div>
                </div>

                <!-- Right: Clinical Records, Diagnoses & Prescriptions -->
                <div style="display: flex; flex-direction: column; gap: var(--space-6);">
                    <!-- Clinical Medical Record -->
                    <div class="card">
                        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                            <h2 class="card-title" style="margin: 0; font-size: 1.125rem; display: flex; align-items: center; gap: var(--space-2);">
                                <i data-lucide="file-heart" style="color: var(--primary);"></i>
                                <span>Clinical Findings &amp; Diagnosis</span>
                            </h2>
                            <c:if test="${appointment.status == 'CONFIRMED' || appointment.status == 'COMPLETED'}">
                                <a href="${ctx}/doctor/consultation?appointmentId=${appointment.id}" class="btn btn-secondary btn-sm">
                                    <i data-lucide="edit-3"></i> <span>${empty medicalRecord ? 'Document Consult' : 'Update Findings'}</span>
                                </a>
                            </c:if>
                        </div>

                        <div class="card-body">
                            <c:choose>
                                <c:when test="${not empty medicalRecord}">
                                    <div style="display: flex; flex-direction: column; gap: var(--space-4);">
                                        <div>
                                            <span style="font-size: 0.8125rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted); letter-spacing: 0.05em;">Presenting Symptoms</span>
                                            <p style="margin: var(--space-1) 0 0 0; color: var(--text-primary); white-space: pre-wrap;"><c:out value="${medicalRecord.symptoms}" /></p>
                                        </div>

                                        <div>
                                            <span style="font-size: 0.8125rem; font-weight: 700; text-transform: uppercase; color: var(--primary); letter-spacing: 0.05em;">Doctor's Diagnosis</span>
                                            <p style="margin: var(--space-1) 0 0 0; font-weight: 600; color: var(--text-primary);"><c:out value="${medicalRecord.diagnosis}" /></p>
                                        </div>

                                        <c:if test="${not empty medicalRecord.treatmentPlan}">
                                            <div>
                                                <span style="font-size: 0.8125rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted); letter-spacing: 0.05em;">Treatment Plan</span>
                                                <p style="margin: var(--space-1) 0 0 0; color: var(--text-primary); white-space: pre-wrap;"><c:out value="${medicalRecord.treatmentPlan}" /></p>
                                            </div>
                                        </c:if>

                                        <c:if test="${not empty medicalRecord.clinicalNotes}">
                                            <div>
                                                <span style="font-size: 0.8125rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted); letter-spacing: 0.05em;">Confidential Clinical Notes</span>
                                                <p style="margin: var(--space-1) 0 0 0; color: var(--text-secondary); background: var(--background); padding: var(--space-3); border-radius: var(--radius-sm); white-space: pre-wrap;"><c:out value="${medicalRecord.clinicalNotes}" /></p>
                                            </div>
                                        </c:if>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="empty-state" style="padding: var(--space-6);">
                                        <div class="empty-state-icon">
                                            <i data-lucide="file-question"></i>
                                        </div>
                                        <h3 class="empty-state-title">No clinical documentation recorded yet</h3>
                                        <p class="empty-state-desc">Consultation findings and diagnoses have not yet been documented for this appointment.</p>
                                        <c:if test="${appointment.status == 'CONFIRMED'}">
                                            <a href="${ctx}/doctor/consultation?appointmentId=${appointment.id}" class="btn btn-primary btn-sm">Start Consultation</a>
                                        </c:if>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- Prescription Items -->
                    <c:if test="${not empty prescription}">
                        <div class="card">
                            <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                                <h2 class="card-title" style="margin: 0; font-size: 1.125rem; display: flex; align-items: center; gap: var(--space-2);">
                                    <i data-lucide="pill" style="color: var(--primary);"></i>
                                    <span>Prescribed Medications</span>
                                </h2>
                                <a href="${ctx}/doctor/prescriptions/view?id=${prescription.id}" class="btn btn-secondary btn-sm" target="_blank">
                                    <i data-lucide="printer"></i> <span>Prescription Sheet</span>
                                </a>
                            </div>

                            <div class="card-body" style="padding: 0;">
                                <div class="table-responsive">
                                    <table class="table" style="margin: 0;">
                                        <thead>
                                            <tr>
                                                <th>Medicine Name</th>
                                                <th>Dosage</th>
                                                <th>Frequency</th>
                                                <th>Duration</th>
                                                <th>Instructions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="item" items="${prescription.items}">
                                                <tr>
                                                    <td><strong><c:out value="${item.medicineName}" /></strong></td>
                                                    <td><c:out value="${item.dosage}" /></td>
                                                    <td><c:out value="${item.frequency}" /></td>
                                                    <td><c:out value="${item.duration}" /></td>
                                                    <td><c:out value="${item.instructions != null ? item.instructions : 'As advised'}" /></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                                <c:if test="${not empty prescription.generalAdvice}">
                                    <div style="padding: var(--space-4); border-top: 1px solid var(--border-color); background: var(--background); font-size: 0.875rem;">
                                        <strong>General Lifestyle &amp; Dietary Advice:</strong>
                                        <div style="margin-top: var(--space-1); color: var(--text-secondary);"><c:out value="${prescription.generalAdvice}" /></div>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </c:if>

                    <!-- Previous Medical History -->
                    <div class="card">
                        <div class="card-header">
                            <h2 class="card-title" style="margin: 0; font-size: 1.125rem; display: flex; align-items: center; gap: var(--space-2);">
                                <i data-lucide="history" style="color: var(--text-secondary);"></i>
                                <span>Patient Medical Timeline</span>
                            </h2>
                        </div>

                        <div class="card-body">
                            <c:choose>
                                <c:when test="${empty patientHistory}">
                                    <p style="margin: 0; color: var(--text-muted); font-size: 0.875rem;">
                                        No prior clinical records found on file for this patient.
                                    </p>
                                </c:when>
                                <c:otherwise>
                                    <ul style="list-style: none; margin: 0; padding: 0;">
                                        <c:forEach var="hist" items="${patientHistory}">
                                            <li style="padding: var(--space-3) 0; border-bottom: 1px solid var(--border-color);">
                                                <div style="display: flex; justify-content: space-between; font-size: 0.8125rem; color: var(--text-muted);">
                                                    <span>Encounter Date: <c:out value="${hist.createdAt}" /></span>
                                                </div>
                                                <div style="font-weight: 600; color: var(--text-primary); margin-top: 2px;">
                                                    <c:out value="${hist.diagnosis}" />
                                                </div>
                                                <div style="font-size: 0.875rem; color: var(--text-secondary); margin-top: 2px;">
                                                    <c:out value="${hist.symptoms}" />
                                                </div>
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
