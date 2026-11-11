<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="DOCTOR" />
<c:set var="activeMenu" value="availability" />
<c:set var="pageTitle" value="Manage Office Hours &amp; Leaves — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Practice Schedule &amp; Leave Exceptions</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Configure your weekly working consultation hours and schedule upcoming time-off / emergency leaves
                    </p>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/alerts.jsp" />

            <!-- Section 1: Weekly Recurring Schedule -->
            <div class="card" style="margin-bottom: var(--space-8);">
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <h2 class="card-title" style="margin: 0; font-size: 1.125rem; display: flex; align-items: center; gap: var(--space-2);">
                        <i data-lucide="calendar" style="color: var(--primary);"></i>
                        <span>Weekly Recurring Consultation Hours</span>
                    </h2>
                    <span style="font-size: 0.8125rem; color: var(--text-muted);">Timezone: Asia/Kolkata</span>
                </div>

                <div class="card-body">
                    <form action="${ctx}/doctor/availability" method="POST">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="action" value="UPDATE_SCHEDULE">

                        <div class="table-responsive">
                            <table class="table" style="margin-bottom: var(--space-6);">
                                <thead>
                                    <tr>
                                        <th style="width: 140px;">Day of Week</th>
                                        <th style="width: 120px;">Working?</th>
                                        <th>Start Time</th>
                                        <th>End Time</th>
                                        <th>Slot Duration</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="dayNum" begin="1" end="7">
                                        <c:set var="dayName" value="${dayNum == 1 ? 'Monday' : (dayNum == 2 ? 'Tuesday' : (dayNum == 3 ? 'Wednesday' : (dayNum == 4 ? 'Thursday' : (dayNum == 5 ? 'Friday' : (dayNum == 6 ? 'Saturday' : 'Sunday')))))}" />
                                        
                                        <%-- Find existing schedule for this day --%>
                                        <c:set var="matchedSched" value="${null}" />
                                        <c:forEach var="s" items="${weeklySchedule}">
                                            <c:if test="${s.dayOfWeek == dayNum}">
                                                <c:set var="matchedSched" value="${s}" />
                                            </c:if>
                                        </c:forEach>

                                        <tr>
                                            <td><strong><c:out value="${dayName}" /></strong></td>
                                            <td>
                                                <div class="form-check" style="margin: 0;">
                                                    <input type="checkbox" name="day_${dayNum}_active" value="true" class="form-check-input"
                                                           id="active_${dayNum}" ${matchedSched != null && matchedSched.active ? 'checked' : (matchedSched == null && dayNum <= 5 ? 'checked' : '')}>
                                                    <label for="active_${dayNum}" class="form-text" style="cursor: pointer; margin: 0;">Active</label>
                                                </div>
                                            </td>
                                            <td>
                                                <input type="time" name="day_${dayNum}_start" class="form-control form-control-sm"
                                                       value="${matchedSched != null ? matchedSched.startTime : '09:00'}" required>
                                            </td>
                                            <td>
                                                <input type="time" name="day_${dayNum}_end" class="form-control form-control-sm"
                                                       value="${matchedSched != null ? matchedSched.endTime : '17:00'}" required>
                                            </td>
                                            <td>
                                                <select name="day_${dayNum}_duration" class="form-select form-select-sm">
                                                    <option value="15" ${matchedSched != null && matchedSched.slotDurationMinutes == 15 ? 'selected' : ''}>15 minutes</option>
                                                    <option value="20" ${matchedSched != null && matchedSched.slotDurationMinutes == 20 ? 'selected' : ''}>20 minutes</option>
                                                    <option value="30" ${matchedSched != null && matchedSched.slotDurationMinutes == 30 ? 'selected' : (matchedSched == null ? 'selected' : '')}>30 minutes</option>
                                                    <option value="45" ${matchedSched != null && matchedSched.slotDurationMinutes == 45 ? 'selected' : ''}>45 minutes</option>
                                                    <option value="60" ${matchedSched != null && matchedSched.slotDurationMinutes == 60 ? 'selected' : ''}>60 minutes</option>
                                                </select>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <div style="display: flex; justify-content: flex-end;">
                            <button type="submit" class="btn btn-primary">
                                <i data-lucide="save"></i>
                                <span>Save Weekly Working Hours</span>
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Section 2: Time-Off & Leaves Exceptions -->
            <div style="display: grid; grid-template-columns: 1fr 1.6fr; gap: var(--space-6);">
                <!-- Add Exception Form -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title" style="margin: 0; font-size: 1.125rem; display: flex; align-items: center; gap: var(--space-2);">
                            <i data-lucide="calendar-plus" style="color: var(--primary);"></i>
                            <span>Schedule Time-Off / Leave</span>
                        </h2>
                    </div>

                    <div class="card-body">
                        <form action="${ctx}/doctor/availability" method="POST">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <input type="hidden" name="action" value="ADD_EXCEPTION">

                            <div class="form-group" style="margin-bottom: var(--space-4);">
                                <label for="exceptionDate" class="form-label">Leave Date</label>
                                <input type="date" id="exceptionDate" name="exceptionDate" class="form-control" required min="<%= java.time.LocalDate.now().toString() %>">
                            </div>

                            <div class="form-group" style="margin-bottom: var(--space-4);">
                                <label for="exceptionType" class="form-label">Absence Category</label>
                                <select id="exceptionType" name="exceptionType" class="form-select" required>
                                    <option value="LEAVE">Scheduled Vacation / Personal Leave</option>
                                    <option value="SICK">Medical / Sick Leave</option>
                                    <option value="CONFERENCE">Medical Conference / CME</option>
                                    <option value="EMERGENCY">Emergency Absence</option>
                                </select>
                            </div>

                            <div class="form-group" style="margin-bottom: var(--space-4);">
                                <div class="form-check" style="display: flex; align-items: center; gap: var(--space-2);">
                                    <input type="checkbox" id="isFullDay" name="isFullDay" value="true" class="form-check-input" checked
                                           onchange="document.getElementById('partialTimes').style.display = this.checked ? 'none' : 'grid';">
                                    <label for="isFullDay" class="form-label" style="margin: 0; cursor: pointer;">Full Day Leave</label>
                                </div>
                            </div>

                            <!-- Partial Day Time Inputs -->
                            <div id="partialTimes" style="display: none; grid-template-columns: 1fr 1fr; gap: var(--space-3); margin-bottom: var(--space-4);">
                                <div class="form-group">
                                    <label for="startTime" class="form-label">Start Time</label>
                                    <input type="time" id="startTime" name="startTime" class="form-control form-control-sm" value="09:00">
                                </div>
                                <div class="form-group">
                                    <label for="endTime" class="form-label">End Time</label>
                                    <input type="time" id="endTime" name="endTime" class="form-control form-control-sm" value="13:00">
                                </div>
                            </div>

                            <div class="form-group" style="margin-bottom: var(--space-6);">
                                <label for="reason" class="form-label">Reason / Notes (Optional)</label>
                                <input type="text" id="reason" name="reason" class="form-control" placeholder="e.g. Attending Annual Cardiology Summit">
                            </div>

                            <button type="submit" class="btn btn-primary" style="width: 100%;">
                                <i data-lucide="check"></i> <span>Confirm &amp; Block Calendar</span>
                            </button>
                        </form>
                    </div>
                </div>

                <!-- Existing Exceptions List -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title" style="margin: 0; font-size: 1.125rem; display: flex; align-items: center; gap: var(--space-2);">
                            <i data-lucide="calendar-off" style="color: var(--danger);"></i>
                            <span>Scheduled Leaves &amp; Blocked Intervals</span>
                        </h2>
                    </div>

                    <div class="card-body" style="padding: 0;">
                        <c:choose>
                            <c:when test="${empty exceptions}">
                                <div class="empty-state" style="padding: var(--space-8);">
                                    <div class="empty-state-icon">
                                        <i data-lucide="check-circle-2"></i>
                                    </div>
                                    <h3 class="empty-state-title">No leave exceptions scheduled</h3>
                                    <p class="empty-state-desc">You have no upcoming leaves or blocked times recorded.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table" style="margin: 0;">
                                        <thead>
                                            <tr>
                                                <th>Date</th>
                                                <th>Duration</th>
                                                <th>Category</th>
                                                <th>Reason</th>
                                                <th style="text-align: right;">Action</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="ex" items="${exceptions}">
                                                <tr>
                                                    <td><strong><c:out value="${ex.exceptionDate}" /></strong></td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${ex.fullDay}">
                                                                <span class="badge badge-secondary">Full Day</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <c:out value="${ex.startTime}" /> - <c:out value="${ex.endTime}" />
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <span class="badge badge-warning"><c:out value="${ex.exceptionType}" /></span>
                                                    </td>
                                                    <td style="max-width: 180px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                                        <c:out value="${ex.reason != null ? ex.reason : '—'}" />
                                                    </td>
                                                    <td style="text-align: right;">
                                                        <form action="${ctx}/doctor/availability" method="POST" style="margin: 0; display: inline;">
                                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                            <input type="hidden" name="action" value="DELETE_EXCEPTION">
                                                            <input type="hidden" name="exceptionId" value="${ex.id}">
                                                            <button type="submit" class="btn btn-secondary btn-sm" style="color: var(--danger);" title="Remove Leave" onclick="return confirm('Remove this leave exception?');">
                                                                <i data-lucide="trash-2"></i>
                                                            </button>
                                                        </form>
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
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

