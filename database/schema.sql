-- ==============================================================================
-- CareSync — Relational Database Schema DDL
-- Target RDBMS: PostgreSQL 16 (Neon Cloud Serverless)
-- Database Name: caresync_db
-- Application Role: caresync_app
-- Architecture: 3NF Normalized Relational Design (14 Tables)
-- ==============================================================================

-- 1. Enable Required PostgreSQL Extensions
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- 2. Drop Tables if Re-creating (Careful: Destructive in Development)
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS system_settings CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS feedback CASCADE;
DROP TABLE IF EXISTS prescription_items CASCADE;
DROP TABLE IF EXISTS prescriptions CASCADE;
DROP TABLE IF EXISTS medical_records CASCADE;
DROP TABLE IF EXISTS appointments CASCADE;
DROP TABLE IF EXISTS doctor_availability_exceptions CASCADE;
DROP TABLE IF EXISTS doctor_availability CASCADE;
DROP TABLE IF EXISTS doctor_profiles CASCADE;
DROP TABLE IF EXISTS specialties CASCADE;
DROP TABLE IF EXISTS patient_profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ==============================================================================
-- TABLE 1: users
-- Core authentication credentials, roles, and status flags
-- ==============================================================================
CREATE TABLE users (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    password_salt VARCHAR(64) NOT NULL,
    hash_iterations INT NOT NULL DEFAULT 600000 CHECK (hash_iterations >= 600000),
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'DOCTOR', 'PATIENT')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(25),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_status ON users(role, status);

-- ==============================================================================
-- TABLE 2: patient_profiles
-- Demographic and health baseline data for registered patients
-- ==============================================================================
CREATE TABLE patient_profiles (
    patient_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE RESTRICT,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20) NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY')),
    blood_group VARCHAR(10) CHECK (blood_group IN ('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-')),
    address TEXT,
    emergency_contact_name VARCHAR(150),
    emergency_contact_phone VARCHAR(25),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_patient_profiles_user_id ON patient_profiles(user_id);

-- ==============================================================================
-- TABLE 3: specialties
-- Medical specialty taxonomy with Lucide icon references
-- ==============================================================================
CREATE TABLE specialties (
    specialty_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_name VARCHAR(50) DEFAULT 'stethoscope',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- TABLE 4: doctor_profiles
-- Professional qualifications, licensing, and practice attributes for doctors
-- ==============================================================================
CREATE TABLE doctor_profiles (
    doctor_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE RESTRICT,
    specialty_id BIGINT NOT NULL REFERENCES specialties(specialty_id) ON DELETE RESTRICT,
    license_number VARCHAR(100) NOT NULL UNIQUE,
    qualification VARCHAR(255) NOT NULL,
    experience_years INT NOT NULL DEFAULT 0 CHECK (experience_years >= 0),
    consultation_fee NUMERIC(10,2) NOT NULL DEFAULT 0.00 CHECK (consultation_fee >= 0),
    bio TEXT,
    room_number VARCHAR(50),
    average_rating NUMERIC(3,2) NOT NULL DEFAULT 0.00 CHECK (average_rating BETWEEN 0.00 AND 5.00),
    total_reviews INT NOT NULL DEFAULT 0 CHECK (total_reviews >= 0),
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_doctor_profiles_user_id ON doctor_profiles(user_id);
CREATE INDEX idx_doctor_profiles_specialty ON doctor_profiles(specialty_id);
CREATE INDEX idx_doctor_profiles_active ON doctor_profiles(is_available, average_rating DESC);

-- ==============================================================================
-- TABLE 5: doctor_availability
-- Weekly recurring schedule templates (1 = Monday, 7 = Sunday)
-- ==============================================================================
CREATE TABLE doctor_availability (
    availability_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES doctor_profiles(doctor_id) ON DELETE CASCADE,
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INT NOT NULL DEFAULT 30 CHECK (slot_duration_minutes IN (15, 20, 30, 45, 60)),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_time_order CHECK (start_time < end_time),
    CONSTRAINT uq_doctor_day_start UNIQUE (doctor_id, day_of_week, start_time)
);

CREATE INDEX idx_doctor_availability_lookup ON doctor_availability(doctor_id, day_of_week) WHERE is_active = TRUE;

-- ==============================================================================
-- TABLE 6: doctor_availability_exceptions
-- Full-day leaves, partial time-offs, emergency absences, and clinic holidays
-- ==============================================================================
CREATE TABLE doctor_availability_exceptions (
    exception_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    doctor_id BIGINT REFERENCES doctor_profiles(doctor_id) ON DELETE CASCADE,
    exception_date DATE NOT NULL,
    is_full_day BOOLEAN NOT NULL DEFAULT TRUE,
    start_time TIME,
    end_time TIME,
    exception_type VARCHAR(30) NOT NULL CHECK (exception_type IN ('LEAVE', 'SICK', 'CONFERENCE', 'HOLIDAY', 'EMERGENCY')),
    reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_partial_day_times CHECK (
        (is_full_day = TRUE) OR 
        (start_time IS NOT NULL AND end_time IS NOT NULL AND start_time < end_time)
    )
);

CREATE INDEX idx_doctor_exceptions_date ON doctor_availability_exceptions(doctor_id, exception_date);
CREATE INDEX idx_clinic_holidays ON doctor_availability_exceptions(exception_date) WHERE doctor_id IS NULL;

-- ==============================================================================
-- TABLE 7: appointments
-- Consultations with PostgreSQL GiST range exclusion constraints
-- ==============================================================================
CREATE TABLE appointments (
    appointment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    appointment_reference VARCHAR(36) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL REFERENCES patient_profiles(patient_id) ON DELETE RESTRICT,
    doctor_id BIGINT NOT NULL REFERENCES doctor_profiles(doctor_id) ON DELETE RESTRICT,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_range TSRANGE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    reason_for_visit TEXT,
    cancellation_reason TEXT,
    cancelled_by_user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_apt_time CHECK (start_time < end_time),
    
    -- Exclusion Constraint: Prohibits overlapping active bookings for the same doctor
    CONSTRAINT ex_no_overlapping_doctor_appointments 
    EXCLUDE USING gist (
        doctor_id WITH =,
        slot_range WITH &&
    ) WHERE (status IN ('PENDING', 'CONFIRMED')),

    -- Exclusion Constraint: Prohibits overlapping active bookings for the same patient
    CONSTRAINT ex_no_overlapping_patient_appointments 
    EXCLUDE USING gist (
        patient_id WITH =,
        slot_range WITH &&
    ) WHERE (status IN ('PENDING', 'CONFIRMED'))
);

CREATE INDEX idx_appointments_doctor_date ON appointments(doctor_id, appointment_date, start_time);
CREATE INDEX idx_appointments_patient_date ON appointments(patient_id, appointment_date, start_time);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_reference ON appointments(appointment_reference);

-- ==============================================================================
-- TABLE 8: medical_records
-- Permanent consultation findings, diagnoses, and doctor clinical remarks
-- ==============================================================================
CREATE TABLE medical_records (
    record_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE REFERENCES appointments(appointment_id) ON DELETE RESTRICT,
    patient_id BIGINT NOT NULL REFERENCES patient_profiles(patient_id) ON DELETE RESTRICT,
    doctor_id BIGINT NOT NULL REFERENCES doctor_profiles(doctor_id) ON DELETE RESTRICT,
    symptoms TEXT NOT NULL,
    diagnosis TEXT NOT NULL,
    treatment_plan TEXT,
    clinical_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_medical_records_patient ON medical_records(patient_id);
CREATE INDEX idx_medical_records_doctor ON medical_records(doctor_id);

-- ==============================================================================
-- TABLE 9: prescriptions
-- Prescription header record bound 1:1 to a consultation encounter
-- ==============================================================================
CREATE TABLE prescriptions (
    prescription_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE REFERENCES appointments(appointment_id) ON DELETE RESTRICT,
    patient_id BIGINT NOT NULL REFERENCES patient_profiles(patient_id) ON DELETE RESTRICT,
    doctor_id BIGINT NOT NULL REFERENCES doctor_profiles(doctor_id) ON DELETE RESTRICT,
    general_advice TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_prescriptions_patient ON prescriptions(patient_id);

-- ==============================================================================
-- TABLE 10: prescription_items
-- Normalized medication line items (1:N relationship with prescriptions)
-- ==============================================================================
CREATE TABLE prescription_items (
    item_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    prescription_id BIGINT NOT NULL REFERENCES prescriptions(prescription_id) ON DELETE CASCADE,
    medicine_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    frequency VARCHAR(100) NOT NULL,
    duration VARCHAR(100) NOT NULL,
    instructions TEXT
);

CREATE INDEX idx_prescription_items_rx ON prescription_items(prescription_id);

-- ==============================================================================
-- TABLE 11: feedback
-- Post-consultation patient satisfaction review (1:1 with completed appointment)
-- ==============================================================================
CREATE TABLE feedback (
    feedback_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE REFERENCES appointments(appointment_id) ON DELETE RESTRICT,
    patient_id BIGINT NOT NULL REFERENCES patient_profiles(patient_id) ON DELETE RESTRICT,
    doctor_id BIGINT NOT NULL REFERENCES doctor_profiles(doctor_id) ON DELETE RESTRICT,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comments TEXT,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_feedback_doctor ON feedback(doctor_id) WHERE is_published = TRUE;

-- ==============================================================================
-- TABLE 12: notifications
-- In-app alert notifications for all registered users
-- ==============================================================================
CREATE TABLE notifications (
    notification_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL CHECK (
        notification_type IN ('APPOINTMENT_BOOKED', 'APPOINTMENT_CONFIRMED', 'APPOINTMENT_CANCELLED', 'PRESCRIPTION_READY', 'SYSTEM_ALERT')
    ),
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read, created_at DESC);

-- ==============================================================================
-- TABLE 13: system_settings
-- Clinic operational parameters managed by Administrators
-- ==============================================================================
CREATE TABLE system_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by_user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL
);

-- ==============================================================================
-- TABLE 14: audit_logs
-- Append-only tamper-evident security audit trail with cryptographic hash chaining
-- ==============================================================================
CREATE TABLE audit_logs (
    log_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    target_entity VARCHAR(50),
    target_id BIGINT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    details TEXT,
    log_hash VARCHAR(64) NOT NULL,
    prev_log_hash VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user_date ON audit_logs(user_id, created_at DESC);
CREATE INDEX idx_audit_logs_action ON audit_logs(action, created_at DESC);

-- ==============================================================================
-- Security Privileges: Append-Only Protection on audit_logs
-- ==============================================================================
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'caresync_app') THEN
        GRANT SELECT, INSERT ON audit_logs TO caresync_app;
        REVOKE UPDATE, DELETE, TRUNCATE ON audit_logs FROM caresync_app;
        GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO caresync_app;
        GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO caresync_app;
    END IF;
END $$;

