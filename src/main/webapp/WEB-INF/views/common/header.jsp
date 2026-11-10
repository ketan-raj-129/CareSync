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
            } catch (e) {}
        })();
    </script>

    <!-- CareSync Design System Stylesheets -->
    <link rel="stylesheet" href="${ctx}/assets/css/caresync.css">

    <!-- Lucide Icons (Official SVG Icon Library) -->
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="${bodyClass}">

