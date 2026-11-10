<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!-- CareSync Medico-Inspired Light/Dark Mode Switch -->
<div class="theme-switch-wrapper">
    <button type="button" class="theme-switch" role="switch" aria-checked="false" 
            aria-label="Toggle Light and Dark Mode" title="Toggle Light and Dark Mode"
            onclick="toggleCareSyncTheme(this)">
        
        <!-- Sun Icon with Radiating Rays (as pictured) -->
        <span class="theme-switch-sun" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="4.2" fill="currentColor"></circle>
                <line x1="12" y1="2" x2="12" y2="4.8"></line>
                <line x1="12" y1="19.2" x2="12" y2="22"></line>
                <line x1="2" y1="12" x2="4.8" y2="12"></line>
                <line x1="19.2" y1="12" x2="22" y2="12"></line>
                <line x1="4.9" y1="4.9" x2="6.9" y2="6.9"></line>
                <line x1="17.1" y1="17.1" x2="19.1" y2="19.1"></line>
                <line x1="4.9" y1="19.1" x2="6.9" y2="17.1"></line>
                <line x1="17.1" y1="6.9" x2="19.1" y2="4.9"></line>
            </svg>
        </span>

        <!-- Middle Dotted Track connecting states -->
        <span class="theme-switch-track" aria-hidden="true">
            <span class="theme-switch-dot"></span>
            <span class="theme-switch-dot"></span>
        </span>

        <!-- Moon Icon (shown in dark state) -->
        <span class="theme-switch-moon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
            </svg>
        </span>

        <!-- Tactile Sliding Knob with Concentric Inner Ring -->
        <span class="theme-switch-thumb" aria-hidden="true"></span>
    </button>
</div>

<style>
/* Self-contained CareSync Theme Switch (guaranteed zero-dependency styling) */
.theme-switch-wrapper {
    display: inline-flex !important;
    align-items: center !important;
    flex-shrink: 0 !important;
}

.theme-switch {
    position: relative !important;
    display: inline-flex !important;
    align-items: center !important;
    width: 68px !important;
    min-width: 68px !important;
    max-width: 68px !important;
    height: 36px !important;
    min-height: 36px !important;
    max-height: 36px !important;
    padding: 0 !important;
    margin: 0 !important;
    border-radius: 9999px !important;
    background: #ebeef2 !important;
    border: 1.5px solid #ffffff !important;
    box-shadow: 0 2px 6px rgba(15, 23, 42, 0.08), inset 0 1px 2px rgba(15, 23, 42, 0.06) !important;
    cursor: pointer !important;
    outline: none !important;
    user-select: none !important;
    -webkit-tap-highlight-color: transparent !important;
    transition: background-color 0.4s ease, border-color 0.4s ease, box-shadow 0.4s ease !important;
    box-sizing: border-box !important;
    overflow: hidden !important;
}

.theme-switch:focus-visible {
    box-shadow: 0 0 0 3px rgba(23, 110, 101, 0.3), inset 0 1px 2px rgba(0, 0, 0, 0.1) !important;
}

[data-theme="dark"] .theme-switch {
    background: #18222d !important;
    border-color: #273748 !important;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.4), inset 0 1px 3px rgba(0, 0, 0, 0.5) !important;
}

.theme-switch-track {
    position: absolute !important;
    left: 50% !important;
    top: 50% !important;
    transform: translate(-50%, -50%) !important;
    display: flex !important;
    align-items: center !important;
    gap: 4px !important;
    pointer-events: none !important;
    z-index: 1 !important;
}

.theme-switch-dot {
    display: inline-block !important;
    width: 4px !important;
    height: 3px !important;
    border-radius: 2px !important;
    background: #cbd5e1 !important;
    transition: background-color 0.35s ease !important;
}

[data-theme="dark"] .theme-switch-dot {
    background: #334658 !important;
}

.theme-switch-sun {
    position: absolute !important;
    left: 8px !important;
    top: 50% !important;
    width: 18px !important;
    height: 18px !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    pointer-events: none !important;
    z-index: 2 !important;
    transform: translateY(-50%) rotate(0deg) scale(1) !important;
    opacity: 1 !important;
    transition: transform 0.48s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.3s ease !important;
}

.theme-switch-sun svg {
    width: 18px !important;
    height: 18px !important;
    display: block !important;
    color: #24282c !important;
    stroke: currentColor !important;
    fill: currentColor !important;
}

.theme-switch-moon {
    position: absolute !important;
    right: 8px !important;
    top: 50% !important;
    width: 16px !important;
    height: 16px !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    pointer-events: none !important;
    z-index: 2 !important;
    transform: translateY(-50%) rotate(90deg) scale(0.3) !important;
    opacity: 0 !important;
    transition: transform 0.48s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.3s ease !important;
}

.theme-switch-moon svg {
    width: 16px !important;
    height: 16px !important;
    display: block !important;
    color: #94a3b8 !important;
    fill: currentColor !important;
}

.theme-switch-thumb {
    position: absolute !important;
    left: 4px !important;
    top: 4px !important;
    width: 26px !important;
    height: 26px !important;
    border-radius: 50% !important;
    background: #24282c !important;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.28), 0 1px 2px rgba(0, 0, 0, 0.2) !important;
    z-index: 3 !important;
    pointer-events: none !important;
    transform: translateX(32px) !important;
    transition: transform 0.48s cubic-bezier(0.34, 1.56, 0.64, 1),
                background 0.35s ease,
                box-shadow 0.35s ease !important;
}

.theme-switch-thumb::after {
    content: '' !important;
    position: absolute !important;
    left: 50% !important;
    top: 50% !important;
    transform: translate(-50%, -50%) !important;
    width: 10px !important;
    height: 10px !important;
    border-radius: 50% !important;
    background: #394047 !important;
    transition: background 0.35s ease, box-shadow 0.35s ease !important;
}

.theme-switch:hover .theme-switch-thumb {
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.35) !important;
}

.theme-switch:active .theme-switch-thumb {
    transform: translateX(32px) scaleX(1.1) scaleY(0.92) !important;
}

/* Dark Mode Active States */
[data-theme="dark"] .theme-switch-thumb {
    transform: translateX(0) !important;
    background: #1da89b !important;
    box-shadow: 0 2px 10px rgba(29, 168, 155, 0.5), 0 0 12px rgba(29, 168, 155, 0.35) !important;
}

[data-theme="dark"] .theme-switch-thumb::after {
    background: #0d151c !important;
    box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.7) !important;
}

[data-theme="dark"] .theme-switch:active .theme-switch-thumb {
    transform: translateX(0) scaleX(1.1) scaleY(0.92) !important;
}

[data-theme="dark"] .theme-switch-sun {
    transform: translateY(-50%) rotate(-90deg) scale(0.3) !important;
    opacity: 0 !important;
}

[data-theme="dark"] .theme-switch-moon {
    transform: translateY(-50%) rotate(0deg) scale(1) !important;
    opacity: 1 !important;
}
</style>

<script>
window.toggleCareSyncTheme = function(btn) {
    try {
        var root = document.documentElement;
        var current = root.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
        var next = current === 'dark' ? 'light' : 'dark';
        root.setAttribute('data-theme', next);
        try {
            localStorage.setItem('caresync_theme', next);
        } catch (e) {}

        var allSwitches = document.querySelectorAll('.theme-switch');
        allSwitches.forEach(function(s) {
            s.setAttribute('aria-checked', next === 'dark');
            s.setAttribute('title', next === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode');
        });

        window.dispatchEvent(new CustomEvent('caresync:themechange', { detail: { theme: next } }));
    } catch (e) {
        console.error("Theme toggle error:", e);
    }
};
</script>
