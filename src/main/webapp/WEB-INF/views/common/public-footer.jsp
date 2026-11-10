<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<footer class="public-footer">
    <div class="footer-container">
        <div class="footer-grid">
            <!-- Brand Overview -->
            <div class="footer-col">
                <a href="${ctx}/" class="navbar-brand" style="margin-bottom: var(--space-3); display: inline-flex;">
                    <div class="brand-icon-pill">
                        <i data-lucide="stethoscope"></i>
                    </div>
                    <span>CareSync</span>
                </a>
                <p style="font-size: 0.875rem; max-width: 300px; color: var(--text-secondary);">
                    Modern online healthcare management platform connecting patients, healthcare practitioners, and clinic administrators.
                </p>
            </div>

            <!-- Patient Links -->
            <div class="footer-col">
                <h6>Patients</h6>
                <ul class="footer-links">
                    <li><a href="${ctx}/login">Patient Portal</a></li>
                    <li><a href="${ctx}/register-patient">Create Patient Account</a></li>
                    <li><a href="${ctx}/patient/dashboard">Dashboard Preview</a></li>
                </ul>
            </div>

            <!-- Doctor Links -->
            <div class="footer-col">
                <h6>Healthcare Providers</h6>
                <ul class="footer-links">
                    <li><a href="${ctx}/login">Doctor Portal</a></li>
                    <li><a href="${ctx}/register-doctor">Doctor Application</a></li>
                    <li><a href="${ctx}/doctor/dashboard">Doctor Dashboard Preview</a></li>
                </ul>
            </div>

            <!-- Governance & Security -->
            <div class="footer-col">
                <h6>Platform & Admin</h6>
                <ul class="footer-links">
                    <li><a href="${ctx}/admin/dashboard">Admin Dashboard Preview</a></li>
                    <li><a href="${ctx}/login">Admin Sign In</a></li>
                    <li><a href="https://github.com" target="_blank" rel="noopener">Documentation</a></li>
                </ul>
            </div>
        </div>

        <div class="footer-bottom">
            <span>&copy; 2026 CareSync Inc. All rights reserved. Built with Java 21 &amp; Jakarta Servlets 6.0.</span>
            <span>Healthcare, connected.</span>
        </div>
    </div>
</footer>

