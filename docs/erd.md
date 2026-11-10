# CareSync — Entity-Relationship Diagram (ERD)

**Document Version:** 1.1.0  
**Database:** `caresync_db` (PostgreSQL 16 on Neon Cloud)  
**Schema Architecture:** 3NF Normalized Relational Design (14 Tables)  

---

## 1. Visual Entity-Relationship Model

```mermaid
erDiagram
    USERS ||--o| PATIENT_PROFILES : "extends (1:1)"
    USERS ||--o| DOCTOR_PROFILES : "extends (1:1)"
    USERS ||--o{ NOTIFICATIONS : "receives (1:N)"
    USERS ||--o{ AUDIT_LOGS : "triggers (1:N)"
    
    SPECIALTIES ||--o{ DOCTOR_PROFILES : "categorizes (1:N)"
    DOCTOR_PROFILES ||--o{ DOCTOR_AVAILABILITY : "defines (1:N)"
    DOCTOR_PROFILES ||--o{ DOCTOR_AVAILABILITY_EXCEPTIONS : "schedules_time_off (1:N)"
    
    PATIENT_PROFILES ||--o{ APPOINTMENTS : "books (1:N)"
    DOCTOR_PROFILES ||--o{ APPOINTMENTS : "attends (1:N)"
    
    APPOINTMENTS ||--o| MEDICAL_RECORDS : "generates (1:1)"
    APPOINTMENTS ||--o| PRESCRIPTIONS : "issues (1:1)"
    APPOINTMENTS ||--o| FEEDBACK : "receives (1:1)"
    
    PRESCRIPTIONS ||--|{ PRESCRIPTION_ITEMS : "contains (1:N)"
    
    PATIENT_PROFILES ||--o{ MEDICAL_RECORDS : "owns (1:N)"
    DOCTOR_PROFILES ||--o{ MEDICAL_RECORDS : "records (1:N)"
    
    PATIENT_PROFILES ||--o{ PRESCRIPTIONS : "receives (1:N)"
    DOCTOR_PROFILES ||--o{ PRESCRIPTIONS : "prescribes (1:N)"
    
    PATIENT_PROFILES ||--o{ FEEDBACK : "writes (1:N)"
    DOCTOR_PROFILES ||--o{ FEEDBACK : "rated_by (1:N)"

    USERS {
        bigint user_id PK
        varchar email UK
        varchar password_hash
        varchar password_salt
        int hash_iterations
        varchar role
        varchar status
        varchar first_name
        varchar last_name
        varchar phone_number
        timestamptz created_at
        timestamptz updated_at
    }

    PATIENT_PROFILES {
        bigint patient_id PK
        bigint user_id FK,UK
        date date_of_birth
        varchar gender
        varchar blood_group
        text address
        varchar emergency_contact_name
        varchar emergency_contact_phone
        timestamptz created_at
        timestamptz updated_at
    }

    SPECIALTIES {
        bigint specialty_id PK
        varchar name UK
        text description
        varchar icon_name
        boolean is_active
        timestamptz created_at
    }

    DOCTOR_PROFILES {
        bigint doctor_id PK
        bigint user_id FK,UK
        bigint specialty_id FK
        varchar license_number UK
        varchar qualification
        int experience_years
        numeric consultation_fee
        text bio
        varchar room_number
        numeric average_rating
        int total_reviews
        boolean is_available
        timestamptz created_at
        timestamptz updated_at
    }

    DOCTOR_AVAILABILITY {
        bigint availability_id PK
        bigint doctor_id FK
        int day_of_week
        time start_time
        time end_time
        int slot_duration_minutes
        boolean is_active
    }

    DOCTOR_AVAILABILITY_EXCEPTIONS {
        bigint exception_id PK
        bigint doctor_id FK
        date exception_date
        boolean is_full_day
        time start_time
        time end_time
        varchar exception_type
        text reason
        timestamptz created_at
    }

    APPOINTMENTS {
        bigint appointment_id PK
        varchar appointment_reference UK
        bigint patient_id FK
        bigint doctor_id FK
        date appointment_date
        time start_time
        time end_time
        tsrange slot_range
        varchar status
        text reason_for_visit
        text cancellation_reason
        bigint cancelled_by_user_id FK
        timestamptz created_at
        timestamptz updated_at
    }

    MEDICAL_RECORDS {
        bigint record_id PK
        bigint appointment_id FK,UK
        bigint patient_id FK
        bigint doctor_id FK
        text symptoms
        text diagnosis
        text treatment_plan
        text clinical_notes
        timestamptz created_at
        timestamptz updated_at
    }

    PRESCRIPTIONS {
        bigint prescription_id PK
        bigint appointment_id FK,UK
        bigint patient_id FK
        bigint doctor_id FK
        text general_advice
        timestamptz created_at
    }

    PRESCRIPTION_ITEMS {
        bigint item_id PK
        bigint prescription_id FK
        varchar medicine_name
        varchar dosage
        varchar frequency
        varchar duration
        text instructions
    }

    FEEDBACK {
        bigint feedback_id PK
        bigint appointment_id FK,UK
        bigint patient_id FK
        bigint doctor_id FK
        int rating
        text comments
        boolean is_published
        timestamptz created_at
    }

    NOTIFICATIONS {
        bigint notification_id PK
        bigint user_id FK
        varchar title
        text message
        varchar notification_type
        varchar related_entity_type
        bigint related_entity_id
        boolean is_read
        timestamptz created_at
    }

    SYSTEM_SETTINGS {
        varchar setting_key PK
        text setting_value
        text description
        timestamptz updated_at
        bigint updated_by_user_id FK
    }

    AUDIT_LOGS {
        bigint log_id PK
        bigint user_id FK
        varchar action
        varchar target_entity
        bigint target_id
        varchar ip_address
        varchar user_agent
        text details
        varchar log_hash
        varchar prev_log_hash
        timestamptz created_at
    }
```

---

## 2. Relational Cardinality & Integrity Specifications

| Parent Entity | Child Entity | Cardinality | FK Column | Cascade Action | Rationale |
|---|---|---|---|---|---|
| `users` | `patient_profiles` | 1 : 0..1 | `user_id` | `ON DELETE RESTRICT` | User credentials must not be deleted if demographic profile exists. |
| `users` | `doctor_profiles` | 1 : 0..1 | `user_id` | `ON DELETE RESTRICT` | User credentials must not be deleted if medical credentials exist. |
| `specialties` | `doctor_profiles` | 1 : 0..N | `specialty_id` | `ON DELETE RESTRICT` | A specialty cannot be removed if active doctors are assigned to it. |
| `doctor_profiles` | `doctor_availability` | 1 : 0..N | `doctor_id` | `ON DELETE CASCADE` | Removing a doctor profile removes their weekly schedule rules. |
| `doctor_profiles` | `doctor_availability_exceptions` | 1 : 0..N | `doctor_id` | `ON DELETE CASCADE` | Removing a doctor profile removes their scheduled leaves (NULL doctor_id = clinic holiday). |
| `patient_profiles` | `appointments` | 1 : 0..N | `patient_id` | `ON DELETE RESTRICT` | Patient records cannot be deleted while appointment history exists. |
| `doctor_profiles` | `appointments` | 1 : 0..N | `doctor_id` | `ON DELETE RESTRICT` | Doctor profiles cannot be deleted while consultation history exists. |
| `appointments` | `medical_records` | 1 : 0..1 | `appointment_id` | `ON DELETE RESTRICT` | Legal consultation notes must never be deleted. |
| `appointments` | `prescriptions` | 1 : 0..1 | `appointment_id` | `ON DELETE RESTRICT` | Prescriptions remain immutable audit records of therapy. |
| `prescriptions` | `prescription_items`| 1 : 1..N | `prescription_id`| `ON DELETE CASCADE` | Deleting an unfinalized prescription header removes child medicines. |
| `appointments` | `feedback` | 1 : 0..1 | `appointment_id` | `ON DELETE RESTRICT` | Completed consultation feedback is strictly 1:1. |
| `users` | `notifications` | 1 : 0..N | `user_id` | `ON DELETE CASCADE` | User alerts can be removed if user account is permanently purged. |
| `users` | `audit_logs` | 1 : 0..N | `user_id` | `ON DELETE SET NULL` | Audit logs are permanent; if a user is deleted, log actor becomes NULL. |
