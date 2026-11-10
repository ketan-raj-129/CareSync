/**
 * CareSync — Reusable UI Component Helpers
 */

const CareSync = {
    /**
     * Display a temporary toast notification in the UI
     * @param {string} message - Message text
     * @param {string} type - 'success', 'warning', 'danger', or 'info'
     */
    showToast: function (message, type = 'info') {
        let container = document.getElementById('caresync-toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'caresync-toast-container';
            container.style.position = 'fixed';
            container.style.bottom = '24px';
            container.style.right = '24px';
            container.style.zIndex = '9999';
            container.style.display = 'flex';
            container.style.flexDirection = 'column';
            container.style.gap = '8px';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = `alert alert-${type}`;
        toast.style.boxShadow = 'var(--shadow-lg)';
        toast.style.margin = '0';
        toast.innerHTML = `
            <span>${message}</span>
            <button type="button" class="alert-close-btn">&times;</button>
        `;

        toast.querySelector('.alert-close-btn').addEventListener('click', () => toast.remove());
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transition = 'opacity 300ms ease';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }
};

window.CareSync = CareSync;

