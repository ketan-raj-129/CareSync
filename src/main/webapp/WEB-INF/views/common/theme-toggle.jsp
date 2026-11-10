<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!-- CareSync Medico-Inspired Light/Dark Mode Switch -->
<div class="theme-switch-wrapper">
    <button type="button" class="theme-switch" role="switch" aria-checked="false" aria-label="Toggle Light and Dark Mode" title="Toggle Light and Dark Mode">
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
