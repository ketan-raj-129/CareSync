# CareSync — Database Design Specification

**Document Version:** 1.1.0 (Refined with Concurrency, Scale-to-Zero, & Availability Exceptions)  
**Target RDBMS:** PostgreSQL 16 (Neon Cloud Serverless)  
**Database Name:** `caresync_db`  
**Application Role / User:** `caresync_app`  
**Author:** Senior Database Architect & Software Engineering Team  

---

## 1. Architectural & Design Philosophy

The CareSync database schema is designed to adhere to **Third Normal Form (3NF)**, ensuring strict relational integrity, zero data anomalies, high transactional concurrency, and seamless mapping to JDBC PreparedStatement workflows.

### Key Tenets
1. **Identity & Primary Keys:** All tables utilize `BIGINT GENERATED ALWAYS AS IDENTITY` for primary keys, providing scalable 64-bit integer indexing without sequence collision or manual sequence management.
2. **Temporal Precision:** All timestamps use `TIMESTAMP WITH TIME ZONE` (`TIMESTAMPTZ`) defaulted to `CURRENT_TIMESTAMP`, guaranteeing timezone-agnostic operations across distributed cloud environments.
3. **Auditability & Append-Only Storage:** Sensitive medical and user entities employ soft-deletion (`is_active BOOLEAN DEFAULT TRUE`) combined with foreign key constraints configured with `ON DELETE RESTRICT` to ensure patient consultation records are never orphaned. Audit logs are written synchronously in the caller's transaction, and database privileges strictly prohibit `UPDATE` and `DELETE` on `audit_logs` for `caresync_app`.
4. **Availability Exception Tracking:** A dedicated `doctor_availability_exceptions` table supports doctor time-off, emergency leave, partial-day unavailability, and clinic-wide holidays.
5. **Robust Concurrency & Overlap Prevention:** Overlapping appointments are strictly prevented using a two-tier defense: service-layer grid alignment validation paired with a PostgreSQL range-based exclusion constraint (`EXCLUDE USING gist`) using `tsrange`.
6. **Neon Serverless Optimization:** HikariCP connection pool settings are engineered for Neon's scale-to-zero capability (`minimumIdle = 0`), preventing artificial keepalive pings from inhibiting compute sleep while handling cold-start connection recovery gracefully.

---

## 2. Table Specifications

### 2.1 Table: `users`
Stores authentication credentials, core role classification, and account state for all actors.

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `user_id` | `BIGINT` | `PRIMARY KEY GENERATED ALWAYS AS IDENTITY` | Unique internal user identifier |
| `email` | `VARCHAR(255)` | `NOT NULL UNIQUE` | User login email (normalized lowercase) |
| `password_hash` | `VARCHAR(255)` | `NOT NULL` | Base64-encoded PBKDF2WithHmacSHA256 hash |
| `password_salt` | `VARCHAR(64)` | `NOT NULL` | Base64-encoded cryptographic 16-byte random salt |
| `hash_iterations` | `INT` | `NOT NULL DEFAULT 600000 CHECK (hash_iterations >= 600000)` | PBKDF2 iteration count for seamless work factor upgrades |
| `role` | `VARCHAR(20)` | `NOT NULL CHECK (role IN ('ADMIN', 'DOCTOR', 'PATIENT'))` | Primary system authorization role |
| `status` | `VARCHAR(20)` | `NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))` | Account lifecycle state |
| `first_name` | `VARCHAR(100)` | `NOT NULL` | Legal first name |
| `last_name` | `VARCHAR(100)` | `NOT NULL` | Legal last name / surname |
| `phone_number` | `VARCHAR(25)` | `NULL` | Contact phone number |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Account creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Last profile update timestamp |

---

### 2.2 Table: `patient_profiles`
Maintains demographic, physical, and medical baseline data specific to patients.

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `patient_id` | `BIGINT` | `PRIMARY KEY GENERATED ALWAYS AS IDENTITY` | Unique patient profile identifier |
| `user_id` | `BIGINT` | `NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE RESTRICT` | FK to core user credentials |
| `date_of_birth` | `DATE` | `NOT NULL` | Patient date of birth |
| `gender` | `VARCHAR(20)` | `NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY'))` | Biological/legal gender |
| `blood_group` | `VARCHAR(10)` | `NULL CHECK (blood_group IN ('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'))` | Patient blood type |
| `address` | `TEXT` | `NULL` | Residential address |
| `emergency_contact_name` | `VARCHAR(150)` | `NULL` | Emergency contact individual |
| `emergency_contact_phone`| `VARCHAR(25)` | `NULL` | Emergency contact phone |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Last profile update timestamp |

---

### 2.3 Table: `specialties`
Taxonomy of medical specialties offered across the clinic.

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `specialty_id` | `BIGINT` | `PRIMARY KEY GENERATED ALWAYS AS IDENTITY` | Unique specialty identifier |
| `name` | `VARCHAR(100)` | `NOT NULL UNIQUE` | Specialty name (e.g., Cardiology, Pediatrics) |
| `description` | `TEXT` | `NULL` | Clinical scope description |
| `icon_name` | `VARCHAR(50)` | `NULL DEFAULT 'stethoscope'` | Lucide icon identifier for UI rendering |
| `is_active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Active visibility flag |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp |

---

### 2.4 Table: `doctor_profiles`
Professional credentials, clinical association, licensing, and practice details for doctors.

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `doctor_id` | `BIGINT` | `PRIMARY KEY GENERATED ALWAYS AS IDENTITY` | Unique doctor profile identifier |
| `user_id` | `BIGINT` | `NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE RESTRICT` | FK to core user credentials |
| `specialty_id` | `BIGINT` | `NOT NULL REFERENCES specialties(specialty_id) ON DELETE RESTRICT` | Primary medical specialty |
| `license_number` | `VARCHAR(100)` | `NOT NULL UNIQUE` | Medical registration/license number |
| `qualification` | `VARCHAR(255)` | `NOT NULL` | Academic degrees (e.g., MBBS, MD, FRCS) |
| `experience_years`| `INT` | `NOT NULL DEFAULT 0 CHECK (experience_years >= 0)` | Years of active medical practice |
| `consultation_fee`| `NUMERIC(10,2)`| `NOT NULL DEFAULT 0.00 CHECK (consultation_fee >= 0)` | Standard consultation fee |
| `bio` | `TEXT` | `NULL` | Professional biography & clinical focus |
| `room_number` | `VARCHAR(50)` | `NULL` | Physical consultation office/room |
| `average_rating` | `NUMERIC(3,2)` | `NOT NULL DEFAULT 0.00 CHECK (average_rating BETWEEN 0.00 AND 5.00)` | Cached aggregate feedback rating |
| `total_reviews` | `INT` | `NOT NULL DEFAULT 0 CHECK (total_reviews >= 0)` | Cached count of completed ratings |
| `is_available` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Master availability toggle |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Last profile update timestamp |

---

### 2.5 Table: `doctor_availability`
Weekly recurring schedule rules defining regular consultation windows.

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `availability_id`| `BIGINT` | `PRIMARY KEY GENERATED ALWAYS AS IDENTITY` | Unique availability rule identifier |
| `doctor_id` | `BIGINT` | `NOT NULL REFERENCES doctor_profiles(doctor_id) ON DELETE CASCADE` | Assigned doctor |
| `day_of_week` | `INT` | `NOT NULL CHECK (day_of_week BETWEEN 1 AND 7)` | Day of week (1 = Monday, 7 = Sunday) |
| `start_time` | `TIME` | `NOT NULL` | Clinic shift start time (e.g., 09:00:00) |
| `end_time` | `TIME` | `NOT NULL` | Clinic shift end time (e.g., 17:00:00) |
| `slot_duration_minutes`| `INT` | `NOT NULL DEFAULT 30 CHECK (slot_duration_minutes IN (15, 20, 30, 45, 60))` | Length of each consultation slot |
| `is_active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Active schedule status |
| `CONSTRAINT chk_time_order` | `CHECK` | `start_time < end_time` | Validates time range ordering |
| `CONSTRAINT uq_doctor_day` | `UNIQUE` | `(doctor_id, day_of_week, start_time)` | Prevents duplicate template definitions |

---

### 2.6 Table: `doctor_availability_exceptions` (Doctor Time-Off & Holidays)
Tracks full-day leaves, partial-day unavailability, emergency leaves, and clinic-wide holidays.

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `exception_id` | `BIGINT` | `PRIMARY KEY GENERATED ALWAYS AS IDENTITY` | Unique exception identifier |
| `doctor_id` | `BIGINT` | `NULL REFERENCES doctor_profiles(doctor_id) ON DELETE CASCADE` | Assigned doctor (NULL = clinic-wide holiday) |
| `exception_date`| `DATE` | `NOT NULL` | Calendar date of leave/exception |
| `is_full_day` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | True if doctor is off the entire day |
| `start_time` | `TIME` | `NULL` | Start time if partial-day time off (e.g., 14:00) |
| `end_time` | `TIME` | `NULL` | End time if partial-day time off (e.g., 17:00) |
| `exception_type`| `VARCHAR(30)` | `NOT NULL CHECK (exception_type IN ('LEAVE', 'SICK', 'CONFERENCE', 'HOLIDAY', 'EMERGENCY'))` | Category of unavailability |
| `reason` | `TEXT` | `NULL` | Optional doctor/admin notes |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Exception logging timestamp |
| `CONSTRAINT chk_partial_day_times`| `CHECK` | `(is_full_day = TRUE) OR (start_time IS NOT NULL AND end_time IS NOT NULL AND start_time < end_time)` | Enforces valid time bounds on partial leave |

---

### 2.7 Table: `appointments`
Manages scheduled consultations with discrete lifecycle states and range-based concurrency exclusion.

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `appointment_id`| `BIGINT` | `PRIMARY KEY GENERATED ALWAYS AS IDENTITY` | Unique appointment identifier |
| `appointment_reference`| `VARCHAR(36)` | `NOT NULL UNIQUE` | Human/system booking code (e.g., `CS-202611-0042`) |
| `patient_id` | `BIGINT` | `NOT NULL REFERENCES patient_profiles(patient_id) ON DELETE RESTRICT` | Booked patient |
| `doctor_id` | `BIGINT` | `NOT NULL REFERENCES doctor_profiles(doctor_id) ON DELETE RESTRICT` | Assigned doctor |
| `appointment_date`| `DATE` | `NOT NULL` | Calendar date of consultation |
| `start_time` | `TIME` | `NOT NULL` | Scheduled slot start time |
| `end_time` | `TIME` | `NOT NULL` | Scheduled slot end time |
| `slot_range` | `TSRANGE` | `NOT NULL` | Computed timestamp range for exclusion index |
| `status` | `VARCHAR(20)` | `NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW'))` | Lifecycle state |
| `reason_for_visit`| `TEXT` | `NULL` | Symptoms or chief complaint |
| `cancellation_reason`| `TEXT` | `NULL` | Explanation if cancelled |
| `cancelled_by_user_id`| `BIGINT` | `NULL REFERENCES users(user_id) ON DELETE SET NULL` | Actor who executed cancellation |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Booking timestamp |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Last status update timestamp |
| `CONSTRAINT chk_apt_time` | `CHECK` | `start_time < end_time` | Validates time sequence |

---

### 2.8 Table: `medical_records`
Permanent clinical consultation notes and diagnoses recorded by doctors.

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `record_id` | `BIGINT` | `PRIMARY KEY GENERATED ALWAYS AS IDENTITY` | Unique medical record identifier |
| `appointment_id`| `BIGINT` | `NOT NULL UNIQUE REFERENCES appointments(appointment_id) ON DELETE RESTRICT` | Associated appointment (1:1) |
| `patient_id` | `BIGINT` | `NOT NULL REFERENCES patient_profiles(patient_id) ON DELETE RESTRICT` | Target patient |
| `doctor_id` | `BIGINT` | `NOT NULL REFERENCES doctor_profiles(doctor_id) ON DELETE RESTRICT` | Attending physician |
| `symptoms` | `TEXT` | `NOT NULL` | Symptoms described and observed |
| `diagnosis` | `TEXT` | `NOT NULL` | Clinical diagnostic assessment |
| `treatment_plan`| `TEXT` | `NULL` | Prescribed clinical interventions |
| `clinical_notes`| `TEXT` | `NULL` | Confidential internal doctor remarks |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Modification timestamp |

---

### 2.9 Table: `prescriptions`
Prescription header record tied to an appointment and clinical encounter.

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `prescription_id`| `BIGINT` | `PRIMARY KEY GENERATED ALWAYS AS IDENTITY` | Unique prescription header identifier |
| `appointment_id`| `BIGINT` | `NOT NULL UNIQUE REFERENCES appointments(appointment_id) ON DELETE RESTRICT` | Associated appointment (1:1) |
| `patient_id` | `BIGINT` | `NOT NULL REFERENCES patient_profiles(patient_id) ON DELETE RESTRICT` | Target patient |
| `doctor_id` | `BIGINT` | `NOT NULL REFERENCES doctor_profiles(doctor_id) ON DELETE RESTRICT` | Prescribing physician |
| `general_advice`| `TEXT` | `NULL` | Lifestyle, diet, and rest instructions |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Issue timestamp |

---

### 2.10 Table: `prescription_items`
Normalized medication line items. A prescription contains 1 to N medication items.

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `item_id` | `BIGINT` | `PRIMARY KEY GENERATED ALWAYS AS IDENTITY` | Unique line item identifier |
| `prescription_id`| `BIGINT` | `NOT NULL REFERENCES prescriptions(prescription_id) ON DELETE CASCADE` | Parent prescription |
| `medicine_name` | `VARCHAR(255)` | `NOT NULL` | Drug commercial or generic name |
| `dosage` | `VARCHAR(100)` | `NOT NULL` | Strength (e.g., 500mg, 10ml, 1 tablet) |
| `frequency` | `VARCHAR(100)` | `NOT NULL` | Cadence (e.g., 1-0-1, Q8H) |
| `duration` | `VARCHAR(100)` | `NOT NULL` | Course length (e.g., 7 days, 2 weeks) |
| `instructions` | `TEXT` | `NULL` | Specific caveats (e.g., Take after meals) |

---

### 2.11 Table: `feedback`
Patient satisfaction ratings and qualitative reviews following a completed appointment.

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `feedback_id` | `BIGINT` | `PRIMARY KEY GENERATED ALWAYS AS IDENTITY` | Unique feedback identifier |
| `appointment_id`| `BIGINT` | `NOT NULL UNIQUE REFERENCES appointments(appointment_id) ON DELETE RESTRICT` | Completed appointment (exactly 1 review per visit) |
| `patient_id` | `BIGINT` | `NOT NULL REFERENCES patient_profiles(patient_id) ON DELETE RESTRICT` | Reviewing patient |
| `doctor_id` | `BIGINT` | `NOT NULL REFERENCES doctor_profiles(doctor_id) ON DELETE RESTRICT` | Rated doctor |
| `rating` | `INT` | `NOT NULL CHECK (rating BETWEEN 1 AND 5)` | Numeric score from 1 to 5 |
| `comments` | `TEXT` | `NULL` | Qualitative feedback review |
| `is_published` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Administrative moderation flag |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Submission timestamp |

---

### 2.12 Table: `notifications`
System and event-driven alerts for users.

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `notification_id`| `BIGINT` | `PRIMARY KEY GENERATED ALWAYS AS IDENTITY` | Unique notification identifier |
| `user_id` | `BIGINT` | `NOT NULL REFERENCES users(user_id) ON DELETE CASCADE` | Recipient user |
| `title` | `VARCHAR(200)` | `NOT NULL` | Short title / subject |
| `message` | `TEXT` | `NOT NULL` | Notification body |
| `notification_type`| `VARCHAR(50)`| `NOT NULL CHECK (notification_type IN ('APPOINTMENT_BOOKED', 'APPOINTMENT_CONFIRMED', 'APPOINTMENT_CANCELLED', 'PRESCRIPTION_READY', 'SYSTEM_ALERT'))` | Category |
| `related_entity_type`| `VARCHAR(50)`| `NULL` | Associated entity (e.g., 'APPOINTMENT') |
| `related_entity_id`| `BIGINT` | `NULL` | Associated entity ID |
| `is_read` | `BOOLEAN` | `NOT NULL DEFAULT FALSE` | Unread / read indicator |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Generated timestamp |

---

### 2.13 Table: `system_settings`
Configurable operational parameters managed by Administrators.

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `setting_key` | `VARCHAR(100)` | `PRIMARY KEY` | Configuration key (e.g., `clinic.cancellation_window_hours`) |
| `setting_value` | `TEXT` | `NOT NULL` | Configured value |
| `description` | `TEXT` | `NULL` | Purpose and units documentation |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Last updated timestamp |
| `updated_by_user_id`| `BIGINT` | `NULL REFERENCES users(user_id) ON DELETE SET NULL` | Admin user who modified setting |

---

### 2.14 Table: `audit_logs` (Synchronous & Append-Only)
Immutable audit trail capturing all critical actions and data transitions.

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `log_id` | `BIGINT` | `PRIMARY KEY GENERATED ALWAYS AS IDENTITY` | Unique audit record identifier |
| `user_id` | `BIGINT` | `NULL REFERENCES users(user_id) ON DELETE SET NULL` | Actor performing action (NULL for system/failed auth) |
| `action` | `VARCHAR(100)` | `NOT NULL` | Event tag (e.g., `USER_LOGIN_SUCCESS`, `APPOINTMENT_CANCELLED`) |
| `target_entity` | `VARCHAR(50)` | `NULL` | Affected entity (e.g., `appointments`) |
| `target_id` | `BIGINT` | `NULL` | ID of affected entity |
| `ip_address` | `VARCHAR(45)` | `NULL` | Client IP address (IPv4 or IPv6) |
| `user_agent` | `VARCHAR(255)` | `NULL` | Browser user agent |
| `details` | `TEXT` | `NULL` | Textual description of state transition |
| `log_hash` | `VARCHAR(64)` | `NOT NULL` | SHA-256 hash of log content for cryptographic verification |
| `prev_log_hash`| `VARCHAR(64)` | `NULL` | SHA-256 hash of previous record (cryptographic tamper-evident chain) |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Exact timestamp of event occurrence |

---

## 3. High-Performance Indexes & Concurrency Controls

### 3.1 Range-Based Exclusion Constraint for Appointment Overlap Prevention
To prevent overlapping active appointments (even with staggered start times or unequal durations), CareSync enables the PostgreSQL `btree_gist` extension and establishes a range-based exclusion constraint:

```sql
-- Enable GiST operator class for scalar types
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Compute slot_range column automatically or populate via trigger:
-- slot_range = tsrange((appointment_date + start_time)::timestamp, (appointment_date + end_time)::timestamp, '[)')

-- Exclusion Constraint: Prevents any overlapping active appointments for the same doctor
ALTER TABLE appointments 
ADD CONSTRAINT ex_no_overlapping_doctor_appointments
EXCLUDE USING gist (
    doctor_id WITH =,
    slot_range WITH &&
) WHERE (status IN ('PENDING', 'CONFIRMED'));

-- Additional Constraint: Prevents a patient from booking overlapping simultaneous appointments
ALTER TABLE appointments
ADD CONSTRAINT ex_no_overlapping_patient_appointments
EXCLUDE USING gist (
    patient_id WITH =,
    slot_range WITH &&
) WHERE (status IN ('PENDING', 'CONFIRMED'));
```

### 3.2 Performance Indexes
```sql
-- 1. Users lookup indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_status ON users(role, status);

-- 2. Profile indexes
CREATE INDEX idx_patient_profiles_user_id ON patient_profiles(user_id);
CREATE INDEX idx_doctor_profiles_user_id ON doctor_profiles(user_id);
CREATE INDEX idx_doctor_profiles_specialty ON doctor_profiles(specialty_id);
CREATE INDEX idx_doctor_profiles_active ON doctor_profiles(is_available, average_rating DESC);

-- 3. Availability & Exceptions indexes
CREATE INDEX idx_doctor_availability_lookup ON doctor_availability(doctor_id, day_of_week) WHERE is_active = TRUE;
CREATE INDEX idx_doctor_exceptions_date ON doctor_availability_exceptions(doctor_id, exception_date);
CREATE INDEX idx_clinic_holidays ON doctor_availability_exceptions(exception_date) WHERE doctor_id IS NULL;

-- 4. Appointment Search indexes
CREATE INDEX idx_appointments_doctor_date ON appointments(doctor_id, appointment_date, start_time);
CREATE INDEX idx_appointments_patient_date ON appointments(patient_id, appointment_date, start_time);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_reference ON appointments(appointment_reference);

-- 5. Clinical Records & Prescriptions
CREATE INDEX idx_medical_records_patient ON medical_records(patient_id);
CREATE INDEX idx_medical_records_doctor ON medical_records(doctor_id);
CREATE INDEX idx_prescriptions_patient ON prescriptions(patient_id);
CREATE INDEX idx_prescription_items_rx ON prescription_items(prescription_id);

-- 6. Feedback & Notifications
CREATE INDEX idx_feedback_doctor ON feedback(doctor_id) WHERE is_published = TRUE;
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read, created_at DESC);

-- 7. Audit log indexes
CREATE INDEX idx_audit_logs_user_date ON audit_logs(user_id, created_at DESC);
CREATE INDEX idx_audit_logs_action ON audit_logs(action, created_at DESC);
```

---

## 4. Concurrency & Double-Booking Prevention Protocol

Appointment booking operates via a collaborative two-tier defense:

```
[Patient Selects Slot]
        |
        v
[Service Layer Grid Validation] 
  - Validates slot starts on 30-min boundary (e.g., 09:00, 09:30)
  - Checks doctor_availability weekly template
  - Checks doctor_availability_exceptions (leaves & holidays)
        |
        v
[Transactional JDBC Lock: SELECT ... FOR UPDATE]
  - Locks doctor row or queries conflicting slots with FOR UPDATE
        |
        v
[PostgreSQL GiST Exclusion Constraint: ex_no_overlapping_doctor_appointments]
  - Rejects ANY overlapping tsrange [start, end) where status IN ('PENDING', 'CONFIRMED')
  - Returns SQLState 23P01 (exclusion_violation) if concurrent collision occurs
        |
        v
[Service Handles Collision Gracefully]
  - Catches SQLException (23P01)
  - Rolls back transaction cleanly
  - Returns user-friendly SlotConflictException: "This slot was just reserved. Please pick another."
```

---

## 5. Append-Only Audit Logging & Privilege Separation

To guarantee tamper-evident integrity without third-party frameworks:

1. **Synchronous Transactional Inclusion:**
   Critical audit records are inserted using the **same database connection and transaction** as the primary business operation. If an appointment booking fails or rolls back, its corresponding audit record is rolled back, preventing phantom audit trails.
2. **Database Role Privilege Revocation:**
   The application database role `caresync_app` has `UPDATE`, `DELETE`, and `TRUNCATE` privileges explicitly revoked on `audit_logs`:
   ```sql
   GRANT SELECT, INSERT ON audit_logs TO caresync_app;
   REVOKE UPDATE, DELETE, TRUNCATE ON audit_logs FROM caresync_app;
   ```
   Even if an application vulnerability exists, SQL queries executed under `caresync_app` cannot modify or delete past audit logs.
3. **Cryptographic Hash Chaining:**
   Each row computes `log_hash = SHA256(prev_log_hash + user_id + action + target_id + created_at)`. Modifying any historical row breaks the hash chain verification.

---

## 6. Neon Cloud & HikariCP Scale-to-Zero Configuration

Neon PostgreSQL employs serverless compute that suspends when inactive. To accommodate this behavior without breaking connection pools or keeping compute awake unnecessarily:

```properties
# HikariCP Configuration for Neon Serverless PostgreSQL
dataSourceClassName=org.postgresql.ds.PGSimpleDataSource
dataSource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?sslmode=require
dataSource.user=${DB_USER}
dataSource.password=${DB_PASSWORD}

# Pool Sizing
# minimumIdle = 0 allows HikariCP to close all idle connections, allowing Neon to scale to zero
minimumIdle=0
maximumPoolSize=10

# Timeouts
# Close idle connections after 10 minutes so Neon compute can suspend
idleTimeout=600000
# Maximum connection lifetime (30 minutes)
maxLifetime=1800000
# Connection timeout: 30 seconds to accommodate Neon cold-start wakeups (typically 1-3 seconds)
connectionTimeout=30000

# Connection Validation
# Validates connection health using JDBC 4.0 isValid() before dispensing from pool
validationTimeout=5000

# DO NOT configure aggressive keepalive (keepaliveTime) as it prevents Neon scale-to-zero
```

### Stale Connection Recovery Protocol
When Neon suspends compute, existing socket connections are terminated server-side. 
- When an incoming request arrives, HikariCP calls `connection.isValid(5)` before lending a pooled connection.
- If Neon was suspended, `isValid()` fails; HikariCP discards the dead connection and issues a new connection request.
- The 30,000ms `connectionTimeout` allows sufficient time for Neon to resume compute and establish a fresh TLS socket connection without throwing immediate connection failure exceptions to end users.
