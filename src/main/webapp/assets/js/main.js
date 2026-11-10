/**
 * CareSync — Main Global JavaScript
 * Handles sidebar toggles, password visibility, dismissible alerts,
 * and Lucide icon rendering.
 */

document.addEventListener('DOMContentLoaded', function () {
    // 1. Initialize Theme Engine (Light / Dark Mode)
    initThemeEngine();

    // 2. Initialize Lucide Icons if available
    initLucideIcons();

    // 3. Setup Responsive Sidebar Toggle for Dashboards
    setupSidebarToggle();

    // 4. Setup Password Visibility Toggles
    setupPasswordToggles();

    // 5. Setup Dismissible Alerts
    setupDismissibleAlerts();
});

/**
 * CareSync Medico-Inspired Theme Engine
 * Manages light / dark state, persistence in localStorage, and tactile switch animations
 */
window.toggleCareSyncTheme = function(btn) {
    const root = document.documentElement;
    const currentTheme = root.getAttribute('data-theme') === 'dark' || root.classList.contains('dark-theme') ? 'dark' : 'light';
    const nextTheme = currentTheme === 'dark' ? 'light' : 'dark';
    applyTheme(nextTheme);
};

function initThemeEngine() {
    const savedTheme = localStorage.getItem('caresync_theme');
    const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    const initialTheme = savedTheme || (prefersDark ? 'dark' : 'light');

    applyTheme(initialTheme);

    // Listen for OS theme changes if user has not set an explicit override
    if (window.matchMedia) {
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', function (e) {
            if (!localStorage.getItem('caresync_theme')) {
                applyTheme(e.matches ? 'dark' : 'light');
            }
        });
    }
}

function applyTheme(theme) {
    const isDark = theme === 'dark';
    const root = document.documentElement;
    root.setAttribute('data-theme', theme);
    root.classList.toggle('dark-theme', isDark);

    if (document.body) {
        document.body.setAttribute('data-theme', theme);
        document.body.classList.toggle('dark-theme', isDark);
    }

    try {
        localStorage.setItem('caresync_theme', theme);
    } catch (e) {
        console.warn("Could not save theme to localStorage:", e);
    }

    const themeSwitches = document.querySelectorAll('.theme-switch');
    themeSwitches.forEach(btn => {
        btn.setAttribute('aria-checked', isDark ? 'true' : 'false');
        btn.setAttribute('title', isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode');
    });

    // Dispatch event for any chart re-renders or custom components
    window.dispatchEvent(new CustomEvent('caresync:themechange', { detail: { theme: theme } }));
}

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

