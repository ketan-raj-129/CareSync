<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title><c:out value="${pageTitle != null ? pageTitle : 'CareSync — Healthcare, connected.'}" /></title>

    <!-- Immediate Theme Detection (Prevents FOUC) -->
    <script>
        (function() {
            try {
                var saved = localStorage.getItem('caresync_theme');
                var prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
                var theme = saved || (prefersDark ? 'dark' : 'light');
                document.documentElement.setAttribute('data-theme', theme);
                if (theme === 'dark') {
                    document.documentElement.classList.add('dark-theme');
                }
            } catch (e) {}
        })();
    </script>

    <!-- CareSync Design System Stylesheets (Bust cache directly) -->
    <link rel="stylesheet" href="${ctx}/assets/css/variables.css?v=3.0">
    <link rel="stylesheet" href="${ctx}/assets/css/base.css?v=3.0">
    <link rel="stylesheet" href="${ctx}/assets/css/components.css?v=3.0">
    <link rel="stylesheet" href="${ctx}/assets/css/layout.css?v=3.0">
    <link rel="stylesheet" href="${ctx}/assets/css/caresync.css?v=3.0">

    <!-- Guaranteed Inline Dark Theme Styles (Zero Cache Dependency) -->
    <style id="caresync-dark-theme-override">
        :root[data-theme="dark"],
        html[data-theme="dark"],
        body[data-theme="dark"],
        .dark-theme,
        [data-theme="dark"] {
            --primary: #1da89b !important;
            --primary-hover: #22bfae !important;
            --primary-dark: #158076 !important;
            --primary-soft: rgba(29, 168, 155, 0.16) !important;
            --primary-soft-hover: rgba(29, 168, 155, 0.26) !important;

            --background: #0d151c !important;
            --surface: #141f2a !important;
            --bg-hover: #1b2938 !important;

            --text-primary: #f8fafc !important;
            --text-secondary: #94a3b8 !important;
            --text-muted: #64748b !important;
            --text-inverse: #0d151c !important;

            --border-color: #243547 !important;
            --border-subtle: #1c2b3a !important;
            --border-focus: #1da89b !important;

            --shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.35) !important;
            --shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.4), 0 1px 2px rgba(0, 0, 0, 0.25) !important;
            --shadow-md: 0 4px 10px -2px rgba(0, 0, 0, 0.5), 0 2px 4px -2px rgba(0, 0, 0, 0.35) !important;
            --shadow-lg: 0 12px 20px -4px rgba(0, 0, 0, 0.6), 0 4px 8px -2px rgba(0, 0, 0, 0.4) !important;
        }

        html[data-theme="dark"],
        html[data-theme="dark"] body,
        body[data-theme="dark"],
        .dark-theme,
        .dark-theme body {
            background-color: #0d151c !important;
            color: #f8fafc !important;
        }

        [data-theme="dark"] .public-navbar,
        [data-theme="dark"] .public-footer,
        [data-theme="dark"] .dashboard-topbar,
        [data-theme="dark"] .dashboard-sidebar,
        [data-theme="dark"] .feature-card,
        [data-theme="dark"] .card,
        [data-theme="dark"] .auth-card {
            background-color: #141f2a !important;
            border-color: #243547 !important;
            color: #f8fafc !important;
        }

        [data-theme="dark"] .hero-title,
        [data-theme="dark"] .section-title,
        [data-theme="dark"] .feature-title,
        [data-theme="dark"] h1, [data-theme="dark"] h2, [data-theme="dark"] h3,
        [data-theme="dark"] h4, [data-theme="dark"] h5, [data-theme="dark"] h6 {
            color: #f8fafc !important;
        }

        [data-theme="dark"] .hero-subtitle,
        [data-theme="dark"] p,
        [data-theme="dark"] .nav-link,
        [data-theme="dark"] .feature-card p {
            color: #94a3b8 !important;
        }

        [data-theme="dark"] .btn-secondary {
            background-color: #1c2b3a !important;
            border-color: #243547 !important;
            color: #f8fafc !important;
        }

        [data-theme="dark"] .hero-badge {
            background-color: #141f2a !important;
            border-color: #243547 !important;
            color: #1da89b !important;
        }

        [data-theme="dark"] .form-control,
        [data-theme="dark"] .form-select {
            background-color: #0d151c !important;
            border-color: #243547 !important;
            color: #f8fafc !important;
        }

        [data-theme="dark"] .hero-section {
            background: radial-gradient(circle at 50% 0%, rgba(29, 168, 155, 0.16) 0%, transparent 65%) !important;
        }
    </style>

    <!-- Lucide Icons (Official SVG Icon Library) -->
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="${bodyClass}">

