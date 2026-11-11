-- ==============================================================================
-- CareSync — Database Migration V7: Notification & Communication Engine
-- ==============================================================================
-- Architecture: Versioned PostgreSQL Migration Proposal (Phase 7)
-- Status: PROPOSED ONLY — DO NOT APPLY TO PRODUCTION NEON WITHOUT EXPLICIT APPROVAL
-- Purpose: Extends platform capabilities for multi-channel notification delivery,
--          transactional outbox tracking, delivery retries, idempotency keys,
--          and user notification preferences.
-- ==============================================================================

-- 1. Create Enums for Channels and Delivery Statuses
DO $$ BEGIN
    CREATE TYPE notification_channel_enum AS ENUM ('IN_APP', 'EMAIL', 'SMS');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE notification_delivery_status_enum AS ENUM (
        'PENDING', 'PROCESSING', 'SENT', 'FAILED', 'CANCELLED'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 2. Notification Delivery Outbox Table (Transactional Outbox Pattern)
CREATE TABLE IF NOT EXISTS notification_outbox (
    outbox_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    recipient_email VARCHAR(255),
    recipient_name VARCHAR(200),
    channel VARCHAR(20) NOT NULL DEFAULT 'IN_APP',
    event_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content_html TEXT,
    content_text TEXT NOT NULL,
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_count INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 3,
    next_retry_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    locked_until TIMESTAMPTZ,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMPTZ,
    CONSTRAINT chk_outbox_channel CHECK (channel IN ('IN_APP', 'EMAIL', 'SMS')),
    CONSTRAINT chk_outbox_status CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'FAILED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_outbox_claiming ON notification_outbox(status, next_retry_at, locked_until)
    WHERE status IN ('PENDING', 'PROCESSING', 'FAILED');

CREATE INDEX IF NOT EXISTS idx_outbox_user_history ON notification_outbox(user_id, created_at DESC);

-- 3. User Notification Preferences Table
CREATE TABLE IF NOT EXISTS user_notification_preferences (
    preference_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    email_confirmations BOOLEAN NOT NULL DEFAULT TRUE,
    email_cancellations BOOLEAN NOT NULL DEFAULT TRUE,
    email_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    email_account_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Initial Seed for Operational Settings (Reminder Lead Time)
INSERT INTO system_settings (setting_key, setting_value, description, updated_at)
VALUES (
    'appointment.reminder.lead.hours',
    '24',
    'Hours prior to consultation start time at which automated appointment reminder is triggered (Default: 24)',
    CURRENT_TIMESTAMP
)
ON CONFLICT (setting_key) DO NOTHING;

-- ==============================================================================
-- Rollback Instructions (Reference Only):
-- 1. DROP TABLE IF EXISTS user_notification_preferences;
-- 2. DROP TABLE IF EXISTS notification_outbox;
-- 3. DELETE FROM system_settings WHERE setting_key = 'appointment.reminder.lead.hours';
-- ==============================================================================

