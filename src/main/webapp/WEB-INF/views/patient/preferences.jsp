<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="activeRole" value="PATIENT" />
<c:set var="activeMenu" value="preferences" />
<c:set var="pageTitle" value="Communication Preferences — CareSync" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="dashboard-main">
        <jsp:include page="/WEB-INF/views/common/topbar.jsp" />

        <main class="dashboard-content">
            <div class="page-header">
                <div>
                    <h1 class="page-title">Communication Preferences</h1>
                    <p style="margin: 0; color: var(--text-secondary); font-size: 0.9375rem;">
                        Manage how and when CareSync contacts you regarding your consultations and medical updates
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
                <form action="${ctx}/patient/preferences" method="POST">
                    <input type="hidden" name="csrfToken" value="${csrfToken}" />

                    <h3 style="font-size: 1.125rem; font-weight: 600; margin-top: 0; margin-bottom: var(--space-4); display: flex; align-items: center; gap: var(--space-2);">
                        <i data-lucide="mail" style="color: var(--primary);"></i>
                        Email Notification Channels
                    </h3>

                    <p style="color: var(--text-secondary); font-size: 0.875rem; margin-bottom: var(--space-5);">
                        Select which operational email communications you would like to receive. In-app notifications are always delivered to your portal inbox.
                    </p>

                    <div style="display: flex; flex-direction: column; gap: var(--space-4); margin-bottom: var(--space-6);">
                        <!-- Preference 1: Confirmations -->
                        <div style="display: flex; align-items: flex-start; gap: var(--space-3); padding: var(--space-3); border-radius: 6px; background-color: var(--color-gray-50, #F9FAFB);">
                            <input type="checkbox" id="emailConfirmations" name="emailConfirmations"
                                   style="margin-top: 4px; width: 18px; height: 18px; accent-color: var(--primary);"
                                   ${preferences.emailConfirmations ? 'checked' : ''} />
                            <label for="emailConfirmations" style="cursor: pointer; flex-grow: 1;">
                                <strong style="display: block; font-size: 0.9375rem; color: var(--text-primary);">Booking & Confirmation Notices</strong>
                                <span style="font-size: 0.8125rem; color: var(--text-secondary);">
                                    Receive email confirmation when your appointment is reserved or approved by the physician.
                                </span>
                            </label>
                        </div>

                        <!-- Preference 2: Cancellations -->
                        <div style="display: flex; align-items: flex-start; gap: var(--space-3); padding: var(--space-3); border-radius: 6px; background-color: var(--color-gray-50, #F9FAFB);">
                            <input type="checkbox" id="emailCancellations" name="emailCancellations"
                                   style="margin-top: 4px; width: 18px; height: 18px; accent-color: var(--primary);"
                                   ${preferences.emailCancellations ? 'checked' : ''} />
                            <label for="emailCancellations" style="cursor: pointer; flex-grow: 1;">
                                <strong style="display: block; font-size: 0.9375rem; color: var(--text-primary);">Cancellation & Rescheduling Notices</strong>
                                <span style="font-size: 0.8125rem; color: var(--text-secondary);">
                                    Get notified immediately if a scheduled consultation is cancelled or declined.
                                </span>
                            </label>
                        </div>

                        <!-- Preference 3: Reminders -->
                        <div style="display: flex; align-items: flex-start; gap: var(--space-3); padding: var(--space-3); border-radius: 6px; background-color: var(--color-gray-50, #F9FAFB);">
                            <input type="checkbox" id="emailReminders" name="emailReminders"
                                   style="margin-top: 4px; width: 18px; height: 18px; accent-color: var(--primary);"
                                   ${preferences.emailReminders ? 'checked' : ''} />
                            <label for="emailReminders" style="cursor: pointer; flex-grow: 1;">
                                <strong style="display: block; font-size: 0.9375rem; color: var(--text-primary);">Automated Appointment Reminders</strong>
                                <span style="font-size: 0.8125rem; color: var(--text-secondary);">
                                    Receive a helpful email reminder prior to your scheduled consultation start time.
                                </span>
                            </label>
                        </div>

                        <!-- Preference 4: Account Alerts -->
                        <div style="display: flex; align-items: flex-start; gap: var(--space-3); padding: var(--space-3); border-radius: 6px; background-color: var(--color-gray-50, #F9FAFB);">
                            <input type="checkbox" id="emailAccountAlerts" name="emailAccountAlerts"
                                   style="margin-top: 4px; width: 18px; height: 18px; accent-color: var(--primary);"
                                   ${preferences.emailAccountAlerts ? 'checked' : ''} />
                            <label for="emailAccountAlerts" style="cursor: pointer; flex-grow: 1;">
                                <strong style="display: block; font-size: 0.9375rem; color: var(--text-primary);">Account & Security Alerts</strong>
                                <span style="font-size: 0.8125rem; color: var(--text-secondary);">
                                    Essential operational notices regarding your profile, credentials, and clinic policy updates.
                                </span>
                            </label>
                        </div>
                    </div>

                    <div style="display: flex; justify-content: flex-end; gap: var(--space-3);">
                        <a href="${ctx}/patient/dashboard" class="btn btn-secondary">Cancel</a>
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

