<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="DOCTOR" />
<c:set var="activeMenu" value="appointments" />
<c:set var="pageTitle" value="Clinical Consultation — ${appointment.referenceNumber}" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <div style="display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-1);">
                        <h1 class="page-title" style="margin: 0;">Clinical Consultation</h1>
                        <span class="badge badge-primary">Active Encounter</span>
                    </div>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Patient: <strong><c:out value="${appointment.patient.firstName} ${appointment.patient.lastName}" /></strong> &bull; 
                        Ref: <strong><c:out value="${appointment.referenceNumber}" /></strong> &bull; 
                        Schedule: <c:out value="${appointment.appointmentDate}" /> at <c:out value="${appointment.startTime}" /> - <c:out value="${appointment.endTime}" />
                    </p>
                </div>
                <div>
                    <a href="${ctx}/doctor/appointments/view?id=${appointment.id}" class="btn btn-secondary btn-sm">
                        <i data-lucide="arrow-left"></i>
                        <span>Cancel & Back</span>
                    </a>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <form action="${ctx}/doctor/consultation" method="POST" id="consultationForm">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="appointmentId" value="${appointment.id}">

                <div style="display: grid; grid-template-columns: 1fr 2.5fr; gap: var(--space-6);">
                    <!-- Patient Summary & History Sidebar -->
                    <div style="display: flex; flex-direction: column; gap: var(--space-6);">
                        <div class="card">
                            <div class="card-header">
                                <h2 class="card-title" style="margin: 0; font-size: 1.05rem; display: flex; align-items: center; gap: var(--space-2);">
                                    <i data-lucide="user" style="color: var(--primary);"></i>
                                    <span>Patient Vitals</span>
                                </h2>
                            </div>
                            <div class="card-body" style="display: flex; flex-direction: column; gap: var(--space-3); font-size: 0.875rem;">
                                <div>
                                    <span style="color: var(--text-secondary);">Full Name:</span>
                                    <strong style="float: right;"><c:out value="${appointment.patient.firstName} ${appointment.patient.lastName}" /></strong>
                                </div>
                                <div>
                                    <span style="color: var(--text-secondary);">Gender / DOB:</span>
                                    <strong style="float: right;">
                                        <c:out value="${appointment.patient.gender != null ? appointment.patient.gender : '—'}" /> /
                                        <c:out value="${appointment.patient.dateOfBirth != null ? appointment.patient.dateOfBirth : '—'}" />
                                    </strong>
                                </div>
                                <div>
                                    <span style="color: var(--text-secondary);">Blood Group:</span>
                                    <strong style="float: right;">
                                        <c:choose>
                                            <c:when test="${not empty appointment.patient.bloodGroup}"><span class="badge badge-info"><c:out value="${appointment.patient.bloodGroup}" /></span></c:when>
                                            <c:otherwise>—</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </div>
                                <div>
                                    <span style="color: var(--text-secondary);">Contact:</span>
                                    <strong style="float: right;"><c:out value="${appointment.patient.phoneNumber != null ? appointment.patient.phoneNumber : '—'}" /></strong>
                                </div>
                            </div>
                        </div>

                        <div class="card">
                            <div class="card-header">
                                <h2 class="card-title" style="margin: 0; font-size: 1.05rem; display: flex; align-items: center; gap: var(--space-2);">
                                    <i data-lucide="message-square" style="color: var(--primary);"></i>
                                    <span>Reason for Visit</span>
                                </h2>
                            </div>
                            <div class="card-body">
                                <p style="margin: 0; font-size: 0.875rem; color: var(--text-primary); line-height: 1.5; background: var(--bg-hover); padding: var(--space-3); border-radius: var(--radius-md);">
                                    <c:out value="${appointment.reasonForVisit != null && !appointment.reasonForVisit.isEmpty() ? appointment.reasonForVisit : 'No chief complaint recorded.'}" />
                                </p>
                            </div>
                        </div>

                        <!-- Prior Medical History -->
                        <div class="card">
                            <div class="card-header">
                                <h2 class="card-title" style="margin: 0; font-size: 1.05rem; display: flex; align-items: center; gap: var(--space-2);">
                                    <i data-lucide="history" style="color: var(--primary);"></i>
                                    <span>Prior Consultations</span>
                                </h2>
                            </div>
                            <div class="card-body" style="padding: 0;">
                                <c:choose>
                                    <c:when test="${empty patientHistory}">
                                        <div style="padding: var(--space-4); text-align: center; color: var(--text-secondary); font-size: 0.875rem;">
                                            No prior records on file.
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div style="max-height: 280px; overflow-y: auto;">
                                            <c:forEach var="past" items="${patientHistory}">
                                                <div style="padding: var(--space-3) var(--space-4); border-bottom: 1px solid var(--border-color); font-size: 0.8125rem;">
                                                    <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
                                                        <strong style="color: var(--primary);"><c:out value="${past.diagnosis}" /></strong>
                                                        <span style="color: var(--text-muted); font-size: 0.75rem;"><c:out value="${past.createdAt}" /></span>
                                                    </div>
                                                    <div style="color: var(--text-secondary); font-size: 0.8125rem; white-space: pre-wrap;"><c:out value="${past.treatmentPlan}" /></div>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <!-- Clinical Documentation & Prescription Form -->
                    <div style="display: flex; flex-direction: column; gap: var(--space-6);">
                        <!-- Diagnostic & Clinical Notes -->
                        <div class="card">
                            <div class="card-header">
                                <h2 class="card-title" style="margin: 0; font-size: 1.125rem; display: flex; align-items: center; gap: var(--space-2);">
                                    <i data-lucide="clipboard-pen" style="color: var(--primary);"></i>
                                    <span>Clinical Assessment & Findings</span>
                                </h2>
                            </div>
                            <div class="card-body" style="display: flex; flex-direction: column; gap: var(--space-4);">
                                <div class="form-group">
                                    <label for="symptoms" class="form-label" style="font-weight: 600;">
                                        Presenting Symptoms <span style="color: var(--danger);">*</span>
                                    </label>
                                    <textarea id="symptoms" name="symptoms" class="form-control" rows="2" required placeholder="E.g. Fever for 3 days, sore throat, mild body aches..."><c:out value="${medicalRecord != null ? medicalRecord.symptoms : ''}" /></textarea>
                                </div>

                                <div class="form-group">
                                    <label for="diagnosis" class="form-label" style="font-weight: 600;">
                                        Clinical Diagnosis <span style="color: var(--danger);">*</span>
                                    </label>
                                    <input type="text" id="diagnosis" name="diagnosis" class="form-control" required placeholder="E.g. Acute Pharyngitis / Viral Upper Respiratory Infection" value="<c:out value="${medicalRecord != null ? medicalRecord.diagnosis : ''}" />">
                                </div>

                                <div class="form-group">
                                    <label for="treatmentPlan" class="form-label" style="font-weight: 600;">
                                        Treatment Plan / Procedure
                                    </label>
                                    <textarea id="treatmentPlan" name="treatmentPlan" class="form-control" rows="2" placeholder="Recommended treatment course, lifestyle modifications, or follow-up timelines..."><c:out value="${medicalRecord != null ? medicalRecord.treatmentPlan : ''}" /></textarea>
                                </div>

                                <div class="form-group">
                                    <label for="clinicalNotes" class="form-label" style="font-weight: 600;">
                                        Confidential Clinical Progress Notes
                                    </label>
                                    <textarea id="clinicalNotes" name="clinicalNotes" class="form-control" rows="2" placeholder="Internal physician notes, differential diagnoses, or risk observations..."><c:out value="${medicalRecord != null ? medicalRecord.clinicalNotes : ''}" /></textarea>
                                </div>
                            </div>
                        </div>

                        <!-- Prescription & Medication Sheet -->
                        <div class="card">
                            <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                                <h2 class="card-title" style="margin: 0; font-size: 1.125rem; display: flex; align-items: center; gap: var(--space-2);">
                                    <i data-lucide="pill" style="color: var(--primary);"></i>
                                    <span>Medical Prescription (Rx)</span>
                                </h2>
                                <button type="button" class="btn btn-secondary btn-sm" id="btnAddMedication" style="cursor: pointer;">
                                    <i data-lucide="plus"></i> Add Medication
                                </button>
                            </div>
                            <div class="card-body" style="display: flex; flex-direction: column; gap: var(--space-4);">
                                <div class="table-responsive">
                                    <table class="table" style="margin: 0;" id="medicationsTable">
                                        <thead>
                                            <tr>
                                                <th style="width: 25%;">Medicine Name</th>
                                                <th style="width: 15%;">Dosage</th>
                                                <th style="width: 18%;">Frequency</th>
                                                <th style="width: 15%;">Duration</th>
                                                <th style="width: 20%;">Instructions</th>
                                                <th style="width: 7%; text-align: center;">Action</th>
                                            </tr>
                                        </thead>
                                        <tbody id="medicationRows">
                                            <c:choose>
                                                <c:when test="${not empty prescription && not empty prescription.items}">
                                                    <c:forEach var="item" items="${prescription.items}">
                                                        <tr>
                                                            <td><input type="text" name="medicineName[]" class="form-control form-control-sm" value="<c:out value="${item.medicineName}" />" required></td>
                                                            <td><input type="text" name="dosage[]" class="form-control form-control-sm" value="<c:out value="${item.dosage}" />"></td>
                                                            <td><input type="text" name="frequency[]" class="form-control form-control-sm" value="<c:out value="${item.frequency}" />"></td>
                                                            <td><input type="text" name="duration[]" class="form-control form-control-sm" value="<c:out value="${item.duration}" />"></td>
                                                            <td><input type="text" name="instructions[]" class="form-control form-control-sm" value="<c:out value="${item.instructions}" />"></td>
                                                            <td style="text-align: center;">
                                                                <button type="button" class="btn btn-sm btn-outline-danger remove-med-btn" style="padding: 4px 8px; border: none; background: transparent; color: var(--danger); cursor: pointer;">
                                                                    <i data-lucide="trash-2"></i>
                                                                </button>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </c:when>
                                                <c:otherwise>
                                                    <tr>
                                                        <td><input type="text" name="medicineName[]" class="form-control form-control-sm" placeholder="E.g. Amoxicillin 500mg" required></td>
                                                        <td><input type="text" name="dosage[]" class="form-control form-control-sm" placeholder="1 Capsule"></td>
                                                        <td><input type="text" name="frequency[]" class="form-control form-control-sm" placeholder="1-0-1 (Twice daily)"></td>
                                                        <td><input type="text" name="duration[]" class="form-control form-control-sm" placeholder="5 Days"></td>
                                                        <td><input type="text" name="instructions[]" class="form-control form-control-sm" placeholder="After food"></td>
                                                        <td style="text-align: center;">
                                                            <button type="button" class="btn btn-sm btn-outline-danger remove-med-btn" style="padding: 4px 8px; border: none; background: transparent; color: var(--danger); cursor: pointer;">
                                                                <i data-lucide="trash-2"></i>
                                                            </button>
                                                        </td>
                                                    </tr>
                                                </c:otherwise>
                                            </c:choose>
                                        </tbody>
                                    </table>
                                </div>

                                <div class="form-group" style="margin-top: var(--space-2);">
                                    <label for="generalAdvice" class="form-label" style="font-weight: 600;">
                                        Dietary & General Clinical Advice
                                    </label>
                                    <textarea id="generalAdvice" name="generalAdvice" class="form-control" rows="2" placeholder="Drink plenty of warm fluids, rest for 48 hours, return if fever persists..."><c:out value="${prescription != null ? prescription.generalAdvice : ''}" /></textarea>
                                </div>
                            </div>
                        </div>

                        <!-- Submit Encounter Bar -->
                        <div class="card" style="background: var(--bg-hover);">
                            <div class="card-body" style="display: flex; justify-content: space-between; align-items: center;">
                                <div>
                                    <strong style="color: var(--text-primary); display: block;">Finalize Consultation</strong>
                                    <span style="font-size: 0.8125rem; color: var(--text-secondary);">Submitting will permanently record this medical note, create the prescription, and complete this appointment.</span>
                                </div>
                                <div style="display: flex; gap: var(--space-3);">
                                    <a href="${ctx}/doctor/appointments/view?id=${appointment.id}" class="btn btn-secondary">Cancel</a>
                                    <button type="submit" class="btn btn-primary" style="display: inline-flex; align-items: center; gap: var(--space-2);">
                                        <i data-lucide="check-circle-2"></i>
                                        <span>Save & Complete Encounter</span>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </form>
        </main>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const tableBody = document.getElementById('medicationRows');
    const addBtn = document.getElementById('btnAddMedication');

    if (addBtn && tableBody) {
        addBtn.addEventListener('click', function() {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><input type="text" name="medicineName[]" class="form-control form-control-sm" placeholder="Medicine name" required></td>
                <td><input type="text" name="dosage[]" class="form-control form-control-sm" placeholder="Dosage"></td>
                <td><input type="text" name="frequency[]" class="form-control form-control-sm" placeholder="Frequency"></td>
                <td><input type="text" name="duration[]" class="form-control form-control-sm" placeholder="Duration"></td>
                <td><input type="text" name="instructions[]" class="form-control form-control-sm" placeholder="Instructions"></td>
                <td style="text-align: center;">
                    <button type="button" class="btn btn-sm btn-outline-danger remove-med-btn" style="padding: 4px 8px; border: none; background: transparent; color: var(--danger); cursor: pointer;">
                        <i data-lucide="trash-2"></i>
                    </button>
                </td>
            `;
            tableBody.appendChild(tr);
            if (window.lucide) {
                window.lucide.createIcons();
            }
        });

        tableBody.addEventListener('click', function(e) {
            const removeBtn = e.target.closest('.remove-med-btn');
            if (removeBtn) {
                const row = removeBtn.closest('tr');
                if (tableBody.querySelectorAll('tr').length > 1) {
                    row.remove();
                } else {
                    // Just clear inputs if only one row left
                    row.querySelectorAll('input').forEach(input => input.value = '');
                }
            }
        });
    }
});
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
