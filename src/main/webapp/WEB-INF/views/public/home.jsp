<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="CareSync — Healthcare, connected." />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="public-layout">
    <!-- Navigation Bar -->
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <!-- Main Content -->
    <main class="main-content">
        <!-- Hero Section -->
        <section id="home" class="hero-section">
            <div class="footer-container">
                <div class="hero-badge">
                    <i data-lucide="shield-check"></i>
                    <span>Next-Generation Healthcare SaaS</span>
                </div>
                <h1 class="hero-title">
                    Healthcare, <span>connected.</span>
                </h1>
                <p class="hero-subtitle">
                    Manage appointments, connect with doctors, and keep your healthcare information organized in one simple place.
                </p>
                <div class="hero-actions">
                    <a href="${ctx}/register-patient" class="btn btn-primary btn-lg">
                        <span>Get Started</span>
                        <i data-lucide="arrow-right"></i>
                    </a>
                    <a href="#features" class="btn btn-secondary btn-lg">Explore Features</a>
                </div>
            </div>
        </section>

        <!-- Features Section -->
        <section id="features" class="features-section">
            <div class="footer-container">
                <div class="section-header">
                    <div class="section-tag">Comprehensive Capabilities</div>
                    <h2 class="section-title">Designed for modern clinical care</h2>
                    <p style="color: var(--text-secondary); margin: 0;">
                        Everything required to connect patients, healthcare practitioners, and clinic administrators seamlessly.
                    </p>
                </div>

                <div class="grid-3">
                    <!-- Feature 1: Find Doctors -->
                    <div class="feature-card">
                        <div class="feature-icon-wrapper">
                            <i data-lucide="search"></i>
                        </div>
                        <h3 class="feature-title">Find Doctors</h3>
                        <p style="margin: 0; font-size: 0.9375rem;">
                            Discover board-certified physicians, review qualifications, consultation fees, and filter by medical specialty.
                        </p>
                    </div>

                    <!-- Feature 2: Easy Appointment Booking -->
                    <div class="feature-card">
                        <div class="feature-icon-wrapper">
                            <i data-lucide="calendar-check"></i>
                        </div>
                        <h3 class="feature-title">Easy Appointment Booking</h3>
                        <p style="margin: 0; font-size: 0.9375rem;">
                            Select real-time 30-minute consultation slots with mathematical double-booking collision prevention.
                        </p>
                    </div>

                    <!-- Feature 3: Digital Medical Records -->
                    <div class="feature-card">
                        <div class="feature-icon-wrapper">
                            <i data-lucide="file-heart"></i>
                        </div>
                        <h3 class="feature-title">Digital Medical Records</h3>
                        <p style="margin: 0; font-size: 0.9375rem;">
                            Access your complete clinical timeline, diagnostic notes, symptoms, and doctor recommendations anytime.
                        </p>
                    </div>

                    <!-- Feature 4: Prescription Management -->
                    <div class="feature-card">
                        <div class="feature-icon-wrapper">
                            <i data-lucide="pill"></i>
                        </div>
                        <h3 class="feature-title">Prescription Management</h3>
                        <p style="margin: 0; font-size: 0.9375rem;">
                            Receive structured digital prescriptions complete with dosage, administration frequency, and printable formats.
                        </p>
                    </div>

                    <!-- Feature 5: Personalized Dashboards -->
                    <div class="feature-card">
                        <div class="feature-icon-wrapper">
                            <i data-lucide="layout-grid"></i>
                        </div>
                        <h3 class="feature-title">Personalized Dashboards</h3>
                        <p style="margin: 0; font-size: 0.9375rem;">
                            Tailored workspace environments engineered specifically for Patients, Doctors, and Clinic Administrators.
                        </p>
                    </div>

                    <!-- Feature 6: Secure Healthcare Access -->
                    <div class="feature-card">
                        <div class="feature-icon-wrapper">
                            <i data-lucide="lock"></i>
                        </div>
                        <h3 class="feature-title">Secure Healthcare Access</h3>
                        <p style="margin: 0; font-size: 0.9375rem;">
                            Hardened security architecture with 600,000-iteration PBKDF2 cryptography, RBAC filters, and tamper-evident audit logs.
                        </p>
                    </div>
                </div>
            </div>
        </section>

        <!-- How It Works Section -->
        <section id="how-it-works" style="padding: var(--space-16) 0; background-color: var(--surface); border-top: 1px solid var(--border-color); border-bottom: 1px solid var(--border-color);">
            <div class="footer-container">
                <div class="section-header">
                    <div class="section-tag">Frictionless Workflow</div>
                    <h2 class="section-title">How CareSync Works</h2>
                    <p style="color: var(--text-secondary); margin: 0;">
                        Streamlined, patient-centric healthcare in three simple steps.
                    </p>
                </div>

                <div class="grid-3">
                    <div class="card" style="border: none; box-shadow: none; background: transparent; text-align: center;">
                        <div class="brand-icon-pill" style="width: 44px; height: 44px; margin: 0 auto var(--space-4) auto; font-size: 1.1rem; font-weight: 700;">1</div>
                        <h4 style="margin-bottom: var(--space-2);">Choose Your Specialist</h4>
                        <p style="font-size: 0.9375rem;">Search and compare verified doctors by specialty, consultation fee, and clinical experience.</p>
                    </div>

                    <div class="card" style="border: none; box-shadow: none; background: transparent; text-align: center;">
                        <div class="brand-icon-pill" style="width: 44px; height: 44px; margin: 0 auto var(--space-4) auto; font-size: 1.1rem; font-weight: 700;">2</div>
                        <h4 style="margin-bottom: var(--space-2);">Book a Free Time Slot</h4>
                        <p style="font-size: 0.9375rem;">Select your preferred 30-minute interval on the doctor's live calendar and confirm your visit.</p>
                    </div>

                    <div class="card" style="border: none; box-shadow: none; background: transparent; text-align: center;">
                        <div class="brand-icon-pill" style="width: 44px; height: 44px; margin: 0 auto var(--space-4) auto; font-size: 1.1rem; font-weight: 700;">3</div>
                        <h4 style="margin-bottom: var(--space-2);">Receive Quality Care</h4>
                        <p style="font-size: 0.9375rem;">Attend your appointment, receive digital diagnoses, and download your verified prescriptions.</p>
                    </div>
                </div>
            </div>
        </section>

        <!-- Portals Introduction (Patients vs Doctors) -->
        <section style="padding: var(--space-16) 0;">
            <div class="footer-container">
                <div class="grid-2">
                    <!-- Patient Intro Card -->
                    <div class="card" style="padding: var(--space-8); border-left: 4px solid var(--primary);">
                        <div class="d-flex align-items-center gap-3 mb-3" style="display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-3);">
                            <div class="brand-icon-pill" style="background-color: var(--primary-soft); color: var(--primary);">
                                <i data-lucide="user-heart"></i>
                            </div>
                            <h3 style="margin: 0;">For Patients</h3>
                        </div>
                        <p style="margin-bottom: var(--space-5);">
                            Take control of your personal health journey. Search top specialists, book appointments without waiting, track prescriptions, and review consultation notes.
                        </p>
                        <div style="display: flex; gap: var(--space-3);">
                            <a href="${ctx}/register-patient" class="btn btn-primary btn-sm">Register as Patient</a>
                            <a href="${ctx}/patient/dashboard" class="btn btn-soft btn-sm">Preview Dashboard</a>
                        </div>
                    </div>

                    <!-- Doctor Intro Card -->
                    <div class="card" style="padding: var(--space-8); border-left: 4px solid var(--info);">
                        <div class="d-flex align-items-center gap-3 mb-3" style="display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-3);">
                            <div class="brand-icon-pill" style="background-color: var(--info-soft); color: var(--info);">
                                <i data-lucide="stethoscope"></i>
                            </div>
                            <h3 style="margin: 0;">For Healthcare Providers</h3>
                        </div>
                        <p style="margin-bottom: var(--space-5);">
                            Manage your daily clinical schedule, customize your availability and time-off, document diagnostic records, and issue instant digital prescriptions.
                        </p>
                        <div style="display: flex; gap: var(--space-3);">
                            <a href="${ctx}/register-doctor" class="btn btn-secondary btn-sm">Apply as Doctor</a>
                            <a href="${ctx}/doctor/dashboard" class="btn btn-soft btn-sm">Preview Dashboard</a>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Trust & Security Banner -->
        <section style="padding: var(--space-12) 0; background-color: var(--surface); border-top: 1px solid var(--border-color);">
            <div class="footer-container" style="text-align: center;">
                <div style="max-width: 600px; margin: 0 auto;">
                    <div style="width: 48px; height: 48px; border-radius: var(--radius-pill); background-color: var(--primary-soft); color: var(--primary); display: inline-flex; align-items: center; justify-content: center; margin-bottom: var(--space-4);">
                        <i data-lucide="shield"></i>
                    </div>
                    <h3 style="margin-bottom: var(--space-2);">Built on Trust &amp; Privacy</h3>
                    <p style="color: var(--text-secondary); margin-bottom: var(--space-6); font-size: 0.9375rem;">
                        CareSync enforces role-based access control, cryptographic password key derivation (PBKDF2-600,000), append-only audit trails, and strict care relationship boundaries.
                    </p>
                    <a href="${ctx}/admin/dashboard" class="btn btn-secondary btn-sm">
                        <i data-lucide="shield-alert"></i>
                        <span>Admin Shell Preview</span>
                    </a>
                </div>
            </div>
        </section>
    </main>

    <!-- Public Footer -->
    <jsp:include page="/WEB-INF/views/common/public-footer.jsp" />
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

