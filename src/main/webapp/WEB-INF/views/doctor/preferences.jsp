<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="DOCTOR" />
<c:set var="activeMenu" value="preferences" />
<c:set var="pageTitle" value="Practitioner Communication Preferences — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Practitioner Communication Preferences</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Configure email delivery channels for clinical scheduling and operational clinic alerts
                    </p>
                </div>
            </div>

            <c:if test="${not empty sessionScope.flashSuccess}">
                <div class="alert alert-success" style="display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-5);">
                    <i data-lucide="check-circle" style="color: var(--success); flex-shrink: 0;"></i>
                    <span style="font-weight: 500;"><c:out value="${sessionScope.flashSuccess}" /></span>
                </div>
                <c:remove var="flashSuccess" scope="session" />
            </c:if>

            <div class="card" style="max-width: 720px; padding: var(--space-6);">
                <form action="${ctx}/doctor/preferences" method="POST">
                    <input type="hidden" name="csrfToken" value="${csrfToken}" />

                    <h3 style="font-size: 1.125rem; font-weight: 600; margin-top: 0; margin-bottom: var(--space-4); display: flex; align-items: center; gap: var(--space-2);">
                        <i data-lucide="mail" style="color: var(--primary);"></i>
                        Practitioner Email Dispatch Channels
                    </h3>

                    <p style="color: var(--text-secondary); font-size: 0.875rem; margin-bottom: var(--space-5);">
                        Choose which operational events should trigger an external email dispatch to your registered doctor email address.
                    </p>

                    <div style="display: flex; flex-direction: column; gap: var(--space-4); margin-bottom: var(--space-6);">
                        <div style="display: flex; align-items: flex-start; gap: var(--space-3); padding: var(--space-3); border-radius: 6px; background-color: var(--color-gray-50, #F9FAFB);">
                            <input type="checkbox" id="emailConfirmations" name="emailConfirmations"
                                   style="margin-top: 4px; width: 18px; height: 18px; accent-color: var(--primary);"
                                   ${preferences.emailConfirmations ? 'checked' : ''} />
                            <label for="emailConfirmations" style="cursor: pointer; flex-grow: 1;">
                                <strong style="display: block; font-size: 0.9375rem; color: var(--text-primary);">New Booking Requests</strong>
                                <span style="font-size: 0.8125rem; color: var(--text-secondary);">
                                    Receive an email dispatch whenever a patient books a new consultation slot in your calendar.
                                </span>
                            </label>
                        </div>

                        <div style="display: flex; align-items: flex-start; gap: var(--space-3); padding: var(--space-3); border-radius: 6px; background-color: var(--color-gray-50, #F9FAFB);">
                            <input type="checkbox" id="emailCancellations" name="emailCancellations"
                                   style="margin-top: 4px; width: 18px; height: 18px; accent-color: var(--primary);"
                                   ${preferences.emailCancellations ? 'checked' : ''} />
                            <label for="emailCancellations" style="cursor: pointer; flex-grow: 1;">
                                <strong style="display: block; font-size: 0.9375rem; color: var(--text-primary);">Patient Cancellations</strong>
                                <span style="font-size: 0.8125rem; color: var(--text-secondary);">
                                    Be notified via email when a patient cancels or reschedules an upcoming consultation.
                                </span>
                            </label>
                        </div>

                        <div style="display: flex; align-items: flex-start; gap: var(--space-3); padding: var(--space-3); border-radius: 6px; background-color: var(--color-gray-50, #F9FAFB);">
                            <input type="checkbox" id="emailAccountAlerts" name="emailAccountAlerts"
                                   style="margin-top: 4px; width: 18px; height: 18px; accent-color: var(--primary);"
                                   ${preferences.emailAccountAlerts ? 'checked' : ''} />
                            <label for="emailAccountAlerts" style="cursor: pointer; flex-grow: 1;">
                                <strong style="display: block; font-size: 0.9375rem; color: var(--text-primary);">Administrative &amp; Verification Alerts</strong>
                                <span style="font-size: 0.8125rem; color: var(--text-secondary);">
                                    Crucial administrative notifications including clinic holiday announcements and credential updates.
                                </span>
                            </label>
                        </div>
                    </div>

                    <div style="display: flex; justify-content: flex-end; gap: var(--space-3);">
                        <a href="${ctx}/doctor/dashboard" class="btn btn-secondary">Cancel</a>
                        <button type="submit" class="btn btn-primary">
                            <i data-lucide="save"></i>
                            <span>Save Preferences</span>
                        </button>
                    </div>
                </form>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

