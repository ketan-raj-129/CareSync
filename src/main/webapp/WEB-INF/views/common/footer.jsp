<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

    <!-- Application JavaScript Bundles -->
    <script src="${ctx}/assets/js/main.js?v=3.0"></script>
    <script src="${ctx}/assets/js/components.js?v=3.0"></script>
    <script>
        // Ensure Lucide renders icons upon DOM load
        if (typeof lucide !== 'undefined') {
            lucide.createIcons();
        }
    </script>
</body>
</html>

