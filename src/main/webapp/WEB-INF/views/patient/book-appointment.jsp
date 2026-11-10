<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="PATIENT" />
<c:set var="pageTitle" value="Book Consultation — CareSync" />

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
                    <h1 class="page-title">Reserve Consultation Slot</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Select your preferred consultation date and available 30-minute clinical interval
                    </p>
                </div>
                <a href="${ctx}/patient/doctors/view?id=${doctor.doctorProfileId}" class="btn btn-secondary btn-sm">
                    <i data-lucide="arrow-left"></i>
                    <span>Back to Doctor</span>
                </a>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <div class="grid-2" style="align-items: flex-start;">
                <!-- Left: Booking Form & Slot Selection -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title">1. Select Date &amp; Time Slot</h2>
                    </div>
                    <div class="card-body">
                        <!-- Date Selector -->
                        <div class="form-group" style="margin-bottom: var(--space-6);">
                            <label for="datePicker" class="form-label">Consultation Date</label>
                            <input type="date" id="datePicker" class="form-control" 
                                   value="${selectedDate}" 
                                   min="<%= java.time.LocalDate.now(java.time.ZoneId.of("Asia/Kolkata")) %>"
                                   onchange="loadSlots(this.value)">
                            <div class="form-text">Available times are updated in real-time according to clinic working hours.</div>
                        </div>

                        <!-- Time Slots Grid -->
                        <div class="form-group">
                            <label class="form-label" style="display: flex; justify-content: space-between; align-items: center;">
                                <span>Available Time Slots</span>
                                <span id="slotStatusBadge" style="font-size: 0.75rem; color: var(--text-muted); font-weight: normal;"></span>
                            </label>

                            <div id="slotsContainer" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(135px, 1fr)); gap: var(--space-2); margin-top: var(--space-2);">
                                <c:choose>
                                    <c:when test="${not empty slots}">
                                        <c:forEach var="slot" items="${slots}">
                                            <button type="button" 
                                                    class="btn btn-sm slot-btn ${slot.available ? 'btn-secondary' : 'btn-disabled'} ${selectedTime == slot.startTime ? 'btn-primary' : ''}" 
                                                    style="padding: var(--space-2) var(--space-3); font-size: 0.8125rem; text-align: center; justify-content: center;"
                                                    ${!slot.available ? 'disabled' : ''}
                                                    title="${slot.reason != null ? slot.reason : 'Available'}"
                                                    onclick="selectSlot('${slot.startTime}', this)">
                                                <c:out value="${slot.formattedRange}" />
                                                <c:if test="${!slot.available}">
                                                    <span style="display: block; font-size: 0.6875rem; color: var(--text-muted); margin-top: 2px;">
                                                        <c:out value="${slot.reason}" />
                                                    </span>
                                                </c:if>
                                            </button>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <div style="grid-column: 1 / -1; padding: var(--space-6); text-align: center; color: var(--text-muted); background: var(--surface-muted); border-radius: var(--radius-md);">
                                            No consultation slots are scheduled for this date. Please choose another day.
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- Reservation Submission Form -->
                        <form id="bookingForm" action="${ctx}/patient/appointments/book" method="POST" style="margin-top: var(--space-6); padding-top: var(--space-6); border-top: 1px solid var(--border-subtle);">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <input type="hidden" name="doctorId" value="${doctor.doctorProfileId}">
                            <input type="hidden" name="appointmentDate" id="appointmentDateInput" value="${selectedDate}">
                            <input type="hidden" name="startTime" id="startTimeInput" value="${selectedTime}">

                            <div class="form-group">
                                <label for="reasonForVisit" class="form-label">Chief Complaint / Reason for Visit (Optional)</label>
                                <textarea id="reasonForVisit" name="reasonForVisit" class="form-control" rows="3" 
                                          placeholder="Briefly describe your symptoms or reason for this visit..."><c:out value="${preservedReason}" /></textarea>
                                <div class="form-text">Shared confidentially with Dr. <c:out value="${doctor.fullName}" /> prior to your consultation.</div>
                            </div>

                            <button type="submit" id="confirmBookingBtn" class="btn btn-primary" style="width: 100%; margin-top: var(--space-4);" ${empty selectedTime ? 'disabled' : ''}>
                                <i data-lucide="check-circle-2"></i>
                                <span>Confirm Consultation Reservation</span>
                            </button>
                        </form>
                    </div>
                </div>

                <!-- Right: Physician & Visit Summary Card -->
                <div style="display: flex; flex-direction: column; gap: var(--space-6);">
                    <div class="card">
                        <div class="card-header">
                            <h3 class="card-title" style="margin: 0;">Physician &amp; Appointment Details</h3>
                        </div>
                        <div class="card-body">
                            <div style="display: flex; gap: var(--space-3); align-items: center; margin-bottom: var(--space-4);">
                                <div class="user-avatar-circle" style="width: 48px; height: 48px; font-size: 1.125rem;">
                                    <c:out value="${doctor.firstName.substring(0, 1).toUpperCase()}" />
                                </div>
                                <div>
                                    <div style="font-weight: 700; font-size: 1.0625rem; color: var(--text-primary);">
                                        Dr. <c:out value="${doctor.fullName}" />
                                    </div>
                                    <span class="badge badge-primary">
                                        <c:out value="${doctor.specialty != null ? doctor.specialty.name : 'Specialist'}" />
                                    </span>
                                </div>
                            </div>

                            <div style="font-size: 0.875rem; color: var(--text-secondary); display: flex; flex-direction: column; gap: var(--space-2); margin-bottom: var(--space-4);">
                                <div><strong>Qualification:</strong> <c:out value="${doctor.qualification}" /></div>
                                <div><strong>Consultation Fee:</strong> <span style="color: var(--primary); font-weight: 700;">$<c:out value="${doctor.consultationFee}" /></span></div>
                                <div><strong>Clinic Suite:</strong> <c:out value="${doctor.roomNumber != null ? doctor.roomNumber : 'Room 102'}" /></div>
                            </div>

                            <div style="padding: var(--space-3); background-color: var(--primary-soft); border-radius: var(--radius-md); font-size: 0.8125rem; color: var(--primary-dark);">
                                <div style="font-weight: 600; margin-bottom: 2px;">Selected Slot:</div>
                                <div id="selectedSlotSummary">
                                    <c:choose>
                                        <c:when test="${not empty selectedTime}">
                                            <c:out value="${selectedDate}" /> at <c:out value="${selectedTime}" />
                                        </c:when>
                                        <c:otherwise>
                                            Please choose an available time slot on the left.
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-header">
                            <h3 class="card-title" style="margin: 0;">Concurrency &amp; Reservation Guarantee</h3>
                        </div>
                        <div class="card-body" style="font-size: 0.8125rem; color: var(--text-secondary); line-height: 1.5;">
                            All appointments are validated against PostgreSQL range-based exclusion constraints. Double-booking is strictly prohibited at both the service layer and database engine level.
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<script>
function selectSlot(timeStr, btnElem) {
    document.querySelectorAll('.slot-btn').forEach(b => {
        b.classList.remove('btn-primary');
        if (!b.disabled) b.classList.add('btn-secondary');
    });
    btnElem.classList.remove('btn-secondary');
    btnElem.classList.add('btn-primary');

    document.getElementById('startTimeInput').value = timeStr;
    const dateVal = document.getElementById('appointmentDateInput').value;
    document.getElementById('selectedSlotSummary').innerText = dateVal + ' at ' + timeStr;
    document.getElementById('confirmBookingBtn').disabled = false;
}

function loadSlots(selectedDate) {
    document.getElementById('appointmentDateInput').value = selectedDate;
    document.getElementById('startTimeInput').value = '';
    document.getElementById('confirmBookingBtn').disabled = true;
    document.getElementById('selectedSlotSummary').innerText = 'Please choose an available time slot on the left.';

    const statusBadge = document.getElementById('slotStatusBadge');
    statusBadge.innerText = 'Loading slots...';

    const doctorId = '${doctor.doctorProfileId}';
    const fetchUrl = '${ctx}/patient/availability?doctorId=' + encodeURIComponent(doctorId) + '&date=' + encodeURIComponent(selectedDate);

    fetch(fetchUrl)
        .then(res => {
            if (!res.ok) throw new Error('Failed to load slots');
            return res.json();
        })
        .then(slots => {
            statusBadge.innerText = '';
            const container = document.getElementById('slotsContainer');
            container.innerHTML = '';

            if (!slots || slots.length === 0) {
                container.innerHTML = '<div style="grid-column: 1 / -1; padding: var(--space-6); text-align: center; color: var(--text-muted); background: var(--surface-muted); border-radius: var(--radius-md);">' +
                    'No consultation slots are scheduled for this date. Please choose another day.</div>';
                return;
            }

            slots.forEach(slot => {
                const btn = document.createElement('button');
                btn.type = 'button';
                btn.className = 'btn btn-sm slot-btn ' + (slot.available ? 'btn-secondary' : 'btn-disabled');
                btn.style.cssText = 'padding: var(--space-2) var(--space-3); font-size: 0.8125rem; text-align: center; justify-content: center;';
                btn.title = slot.reason ? slot.reason : 'Available';
                if (!slot.available) btn.disabled = true;

                let content = slot.formattedRange;
                if (!slot.available && slot.reason) {
                    content += '<span style="display: block; font-size: 0.6875rem; color: var(--text-muted); margin-top: 2px;">' + slot.reason + '</span>';
                }
                btn.innerHTML = content;

                btn.onclick = function() {
                    selectSlot(slot.startTime, btn);
                };
                container.appendChild(btn);
            });

            if (typeof lucide !== 'undefined') lucide.createIcons();
        })
        .catch(err => {
            statusBadge.innerText = 'Error loading slots';
            console.error(err);
        });
}
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

