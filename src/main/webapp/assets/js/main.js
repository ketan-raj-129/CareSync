/**
 * CareSync — Main Global JavaScript
 * Handles sidebar toggles, password visibility, dismissible alerts,
 * and Lucide icon rendering.
 */

document.addEventListener('DOMContentLoaded', function () {
    // 1. Initialize Lucide Icons if available
    initLucideIcons();

    // 2. Setup Responsive Sidebar Toggle for Dashboards
    setupSidebarToggle();

    // 3. Setup Password Visibility Toggles
    setupPasswordToggles();

    // 4. Setup Dismissible Alerts
    setupDismissibleAlerts();
});

/**
 * Initializes Lucide SVG icons across the document
 */
function initLucideIcons() {
    if (typeof lucide !== 'undefined' && typeof lucide.createIcons === 'function') {
        lucide.createIcons();
    }
}

/**
 * Manages responsive sidebar toggle for tablet and mobile viewports
 */
function setupSidebarToggle() {
    const toggleBtns = document.querySelectorAll('.sidebar-toggle-btn');
    const sidebar = document.querySelector('.dashboard-sidebar');
    const backdrop = document.querySelector('.sidebar-backdrop');

    if (!sidebar) return;

    toggleBtns.forEach(btn => {
        btn.addEventListener('click', function () {
            sidebar.classList.toggle('show');
            if (backdrop) {
                backdrop.classList.toggle('show');
            }
        });
    });

    if (backdrop) {
        backdrop.addEventListener('click', function () {
            sidebar.classList.remove('show');
            backdrop.classList.remove('show');
        });
    }
}

/**
 * Handles password visibility toggle buttons
 */
function setupPasswordToggles() {
    const toggleBtns = document.querySelectorAll('.password-toggle-btn');

    toggleBtns.forEach(btn => {
        btn.addEventListener('click', function () {
            const targetId = this.getAttribute('data-target');
            const targetInput = targetId ? document.getElementById(targetId) : this.parentElement.querySelector('input');

            if (!targetInput) return;

            if (targetInput.type === 'password') {
                targetInput.type = 'text';
                this.setAttribute('aria-label', 'Hide password');
                this.innerHTML = '<i data-lucide="eye-off"></i>';
            } else {
                targetInput.type = 'password';
                this.setAttribute('aria-label', 'Show password');
                this.innerHTML = '<i data-lucide="eye"></i>';
            }

            // Re-render the newly swapped icon
            initLucideIcons();
        });
    });
}

/**
 * Manages closeable alerts
 */
function setupDismissibleAlerts() {
    const closeBtns = document.querySelectorAll('.alert-close-btn');

    closeBtns.forEach(btn => {
        btn.addEventListener('click', function () {
            const alert = this.closest('.alert');
            if (alert) {
                alert.style.opacity = '0';
                alert.style.transition = 'opacity 200ms ease';
                setTimeout(() => alert.remove(), 200);
            }
        });
    });
}

