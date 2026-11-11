# CareSync — Phase 6: Admin Portal & Central Administration Engine Report

**Document Version:** 1.0.0  
**Phase:** 6 of 9  
**Status:** Completed & Verified  
**Runtime:** Java 21 LTS | Apache Maven 3.9.9 | Jakarta EE 10 (Servlet 6.0, JSP 3.1, JSTL 3.0)  
**Database:** PostgreSQL 16 (Neon Cloud ready) | HikariCP Connection Pool  
**Security:** RBAC Authorization Filter (`ADMIN` only) | Tamper-Evident SHA-256 Audit Chain | Anti-CSRF Synchronizer Tokens | Anti-Lockout Defense  
**Architecture:** Layered MVC + Service Layer + DAO Layer (Pure Java, pure JDBC, Zero Spring/Hibernate)  

---

## 1. Executive Summary

Phase 6 of **CareSync — Healthcare, connected.** has been fully implemented, integrated, and verified on top of the Phase 1–5 foundation without breaking or replacing existing functionality. Phase 6 establishes the centralized administrative governance engine, delivering platform-wide control, security compliance, practitioner credential verification, and facility operations across nine core modules:

1. **Executive Administration Dashboard** (`/admin/dashboard`): Real-time database-backed institutional KPIs (registered patients, active doctors, today's consultations, active specialties, appointment status volume distribution), doctor verification review queue, recent append-only audit trail, and central governance shortcuts.
2. **Patient Directory & Account Governance** (`/admin/patients`, `/admin/patients/view`, `/admin/patients/status`): Paginated patient search (name, email, phone) and status filtering (`ACTIVE`, `INACTIVE`, `SUSPENDED`), comprehensive demographic inspect view, emergency contact details, account status management, with strict privacy segregation protecting private clinical consultation notes and prescriptions from administrative exposure.
3. **Doctor Registry & Credential Verification** (`/admin/doctors`, `/admin/doctors/view`, `/admin/doctors/verification`, `/admin/doctors/status`): Registry with multi-dimensional filtering (specialty, account status, clinical booking availability), dedicated practitioner profile view, medical council license inspection interface, and administrative practice availability overrides.
4. **Medical Specialty & Department Management** (`/admin/specialties`, `/admin/specialties/create`, `/admin/specialties/edit`, `/admin/specialties/toggle`): Department directory with associated doctor metrics, duplicate name validation, description and Lucide icon management, and active status toggling.
5. **Appointment Master Schedule & Audit Administration** (`/admin/appointments`, `/admin/appointments/view`, `/admin/appointments/action`): Global facility consultation audit table with reference lookup, specialty, date, and status filters. Provides administrative cancellation authority requiring a mandatory recorded reason, with automatic audit trail logging.
6. **Clinic-Wide Holiday & Facility Closure Management** (`/admin/holidays`, `/admin/holidays/manage`): Institutional schedule exception management (enforcing `doctor_id IS NULL` to represent clinic-wide closures), conflict inspector alerting administrators if consultations are already booked on planned closure dates, and closure deregistration.
7. **User Account & RBAC Governance** (`/admin/users`, `/admin/users/view`, `/admin/users/status`, `/admin/users/role`): Central authentication registry across all system roles (`ADMIN`, `DOCTOR`, `PATIENT`), account status governance, and role modification backed by anti-lockout invariant defenses.
8. **Platform & Operational Settings** (`/admin/settings`): Whitelisted key-value configuration editor managing institutional parameters (`clinic.name`, `clinic.contact.phone`, `clinic.contact.email`, `clinic.address`, `appointment.cancellation.cutoff.hours`, `appointment.slot.duration.default`), strictly rejecting unapproved keys.
9. **Security Audit Log & Compliance Viewer** (`/admin/audit-logs`): Strictly read-only, append-only compliance log browser with cryptographic tamper-evidence, paginated search by action keyword, actor user ID, target entity, and IP address.

All **97 automated unit, integration, and security tests** pass with **0 failures, 0 errors, and 0 skipped**. The application packages cleanly into `target/caresync.war` and is deployed to Apache Tomcat 10.1.

---

## 2. Carry-Forward Verifications from Phase 5

Before implementation, four key carry-forward items from Phase 5 were verified:

1. **Clinic-Wide Holidays Governance:**
   - *Verification:* Verified against schema requirements. Clinic-wide holidays are centrally administered by administrators using `doctor_availability_exceptions` with `doctor_id IS NULL`. Individual doctors can only manage their personal leaves with `doctor_id = ?`. `AdminHolidayServiceImpl` strictly enforces that only `doctor_id IS NULL` records are managed through `/admin/holidays`.
2. **Administrative Appointment Cancellation Justification:**
   - *Verification:* Verified in `AdminAppointmentServiceImpl`. Administrative cancellations must not be performed silently. An administrative cancellation requires a non-empty justification string (e.g., facility emergency, doctor illness), which is stored in `appointments.cancellation_reason` and recorded in `audit_logs`.
3. **Data Privacy & Clinical Boundary Invariants:**
   - *Verification:* Verified across `AdminPatientServlet`, `AdminAppointmentServlet`, and respective JSP views. Private diagnostic notes, physician progress remarks, and medical prescription line items are restricted to clinical provider modules (Phase 5) and are strictly excluded from administrative portals.
4. **Append-Only Tamper-Evident Audit Logging:**
   - *Verification:* Verified in `AdminAuditLogServlet` and `AdminAuditLogServiceImpl`. The audit log viewer is strictly read-only with no HTTP POST/PUT/DELETE handlers, maintaining the append-only integrity of the SHA-256 hash chain.

---

## 3. Component Architecture & Implementation Details

### 3.1 Web Controllers (`com.caresync.servlet`)

| Controller Servlet | Mapped Route(s) | HTTP Methods | Core Responsibilities & Security Controls |
| :--- | :--- | :--- | :--- |
| **`AdminDashboardServlet`** | `/admin/dashboard` | `GET` | Computes real database metrics, appointment breakdown, pending reviews, and recent audit trail. |
| **`AdminPatientServlet`** | `/admin/patients`, `/admin/patients/view`, `/admin/patients/status` | `GET`, `POST` | Patient directory pagination, demographic detail inspection, and account status transitions (`ACTIVE`, `INACTIVE`, `SUSPENDED`). |
| **`AdminDoctorServlet`** | `/admin/doctors`, `/admin/doctors/view`, `/admin/doctors/verification`, `/admin/doctors/status` | `GET`, `POST` | Doctor registry filtering, credential verification inspection, account status updates, and practice availability overrides. |
| **`AdminSpecialtyServlet`** | `/admin/specialties`, `/admin/specialties/create`, `/admin/specialties/edit`, `/admin/specialties/toggle` | `GET`, `POST` | Specialty department registry, add/edit form handling, duplicate name prevention, and active toggle. |
| **`AdminAppointmentServlet`** | `/admin/appointments`, `/admin/appointments/view`, `/admin/appointments/action` | `GET`, `POST` | Appointment master schedule search, audit detail inspection, and administrative cancellation with mandatory reason. |
| **`AdminHolidayServlet`** | `/admin/holidays`, `/admin/holidays/manage` | `GET`, `POST` | Institutional closure management (`doctor_id IS NULL`), closure conflict detection, and holiday deletion. |
| **`AdminUserServlet`** | `/admin/users`, `/admin/users/view`, `/admin/users/status`, `/admin/users/role` | `GET`, `POST` | Identity directory, account lifecycle management, role modification, and anti-lockout invariant enforcement. |
| **`AdminSettingsServlet`** | `/admin/settings` | `GET`, `POST` | Operational system settings editor with strict allowlist enforcement for allowed configuration keys. |
| **`AdminAuditLogServlet`** | `/admin/audit-logs` | `GET` (Read-only) | Compliance viewer for immutable audit records with action, user, and entity filtering. |

---

### 3.2 Service Layer (`com.caresync.service`)

- **`AdminDashboardService` / `AdminDashboardServiceImpl`:** Aggregates real platform statistics from `PatientDao`, `DoctorDao`, `AppointmentDao`, `SpecialtyDao`, and `AuditLogDao`.
- **`AdminPatientService` / `AdminPatientServiceImpl`:** Provides paginated search, patient profile detail loading, and audited status modifications (`PATIENT_STATUS_UPDATED`).
- **`AdminDoctorService` / `AdminDoctorServiceImpl`:** Provides filtered practitioner lookups, credential detail retrieval, status updates (`DOCTOR_STATUS_UPDATED`), and clinical availability toggling (`DOCTOR_AVAILABILITY_TOGGLED`).
- **`AdminSpecialtyService` / `AdminSpecialtyServiceImpl`:** Handles specialty CRUD, validates name uniqueness, manages Lucide icon descriptors, prevents duplicate registrations, and tracks doctor count per department.
- **`AdminAppointmentService` / `AdminAppointmentServiceImpl`:** Provides multi-filter appointment queries, audit detail views, terminal state enforcement (cannot cancel completed or cancelled appointments), and administrative cancellation with mandatory reason logging (`APPOINTMENT_CANCELLED_BY_ADMIN`).
- **`AdminHolidayService` / `AdminHolidayServiceImpl`:** Manages clinic-wide closures enforcing `doctor_id IS NULL`, prevents deleting doctor-specific leaves through holiday management, and calculates booking conflicts for any planned date.
- **`AdminUserService` / `AdminUserServiceImpl`:** Manages user account states and role changes while enforcing administrative invariants (anti-lockout defenses).
- **`AdminSettingsService` / `AdminSettingsServiceImpl`:** Validates system setting keys against an approved allowlist (`clinic.name`, `clinic.contact.phone`, `clinic.contact.email`, `clinic.address`, `appointment.cancellation.cutoff.hours`, `appointment.slot.duration.default`), preventing arbitrary key creation or unwhitelisted overrides.
- **`AdminAuditLogService` / `AdminAuditLogServiceImpl`:** Provides read-only paginated audit log inspection with sensitive information redaction.

---

### 3.3 Data Access Layer Extensions (`com.caresync.dao`)

- **`UserDao` & `UserDaoImpl`:** Added `countActiveAdmins()`, `findPaginated(...)`, and `countFiltered(...)`.
- **`PatientDao` & `PatientDaoImpl`:** Added `countTotal()`, `countActive()`, `findPaginated(...)`, and `countFiltered(...)`.
- **`DoctorDao` & `DoctorDaoImpl`:** Added `countTotal()`, `countActive()`, `countBySpecialty(...)`, `updateAvailability(...)`, `findPaginated(...)`, and `countFiltered(...)`.
- **`SpecialtyDao` & `SpecialtyDaoImpl`:** Added `countTotal()`, `countActive()`, `countDoctorsBySpecialtyId(...)`, `findPaginated(...)`, and `countFiltered(...)`.
- **`DoctorAvailabilityExceptionDao` & `DoctorAvailabilityExceptionDaoImpl`:** Added `findAllClinicHolidays()` and `countClinicHolidays()`.
- **`AppointmentDao` & `AppointmentDaoImpl`:** Added `findByDate(LocalDate)`, `findPaginated(...)`, and `countFiltered(...)`.
- **`AuditLogDao` & `AuditLogDaoImpl`:** Added `findPaginated(...)` and `countFiltered(...)`.

---

## 4. Administrative Invariants & Anti-Lockout Defenses

To ensure stability and prevent administrative lockout, `AdminUserServiceImpl` enforces the following invariants:

1. **Self-Demotion Prevention:** An authenticated administrator cannot modify their own role (`actorAdminId.equals(userId)`). Attempting self-demotion throws a `ValidationException("Administrators cannot modify their own system role.")`.
2. **Last Active Administrator Protection:** An administrator cannot be deactivated or demoted if they are the last active administrator in the system (`userDao.countActiveAdmins() <= 1`). Attempting to suspend, deactivate, or demote the last active administrator throws a `ValidationException("Cannot modify or deactivate the last active administrator in the system.")`.
3. **Mandatory Cancellation Reason:** Administrative cancellation requires a non-empty justification string. Submitting a blank reason throws a `ValidationException("Administrative cancellation requires a non-empty justification reason.")`.
4. **Settings Allowlist Enforcement:** Attempting to update a setting key not present in `ALLOWED_SETTINGS` throws a `ValidationException("Setting key '...' is not an approved operational configuration parameter.")`.
5. **Clinic Holiday Scope Enforcement:** The holiday service strictly sets `doctor_id = null` and verifies that deleted exceptions belong to clinic-wide closures (`doctorId == null`), preventing administrators from accidentally deleting individual doctor vacation schedules.

---

## 5. Doctor Verification Schema Limitation & Proposed Migration

### 5.1 Current Schema Constraint Analysis

Inspection of `database/schema.sql` revealed that the `doctor_profiles` table does not possess an `is_verified`, `verification_status`, `verified_at`, or `verified_by` column:

```sql
CREATE TABLE doctor_profiles (
    doctor_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE RESTRICT,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    specialty_id BIGINT NOT NULL REFERENCES specialties(specialty_id) ON DELETE RESTRICT,
    years_of_experience INTEGER NOT NULL CHECK (years_of_experience >= 0),
    consultation_fee NUMERIC(10, 2) NOT NULL CHECK (consultation_fee >= 0),
    room_number VARCHAR(20),
    bio TEXT,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 5.2 Phase 6 Compliant Implementation

In accordance with Phase 6 instructions prohibiting silent database changes or overloading unrelated columns:
- Doctor credential verification is implemented as an administrative review workflow (`/admin/doctors/verification?id=...`).
- The administrator inspects medical council license numbers, qualifications, clinical experience, and department allocations.
- Verification approval operates cleanly through account lifecycle activation (`users.status = 'ACTIVE'`) and clinical practice availability (`doctor_profiles.is_available = TRUE`).
- Inactive accounts (`users.status = 'INACTIVE'`) represent unverified or pending practitioner applications.
- Suspended accounts (`users.status = 'SUSPENDED'`) represent revoked or held credentials.

### 5.3 Proposed Database Migration for Phase 7

For Phase 7, the following non-breaking PostgreSQL migration is proposed:

```sql
-- Migration: Add explicit doctor credential verification tracking
ALTER TABLE doctor_profiles 
    ADD COLUMN is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN verified_at TIMESTAMPTZ,
    ADD COLUMN verified_by BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    ADD COLUMN verification_notes TEXT;

-- Index for verification queries
CREATE INDEX idx_doctor_profiles_verification ON doctor_profiles(is_verified);

-- Retroactively set existing active doctors as verified
UPDATE doctor_profiles 
SET is_verified = TRUE, verified_at = CURRENT_TIMESTAMP
WHERE user_id IN (SELECT user_id FROM users WHERE status = 'ACTIVE');
```

---

## 6. Automated Test Suite & Verification Results

A comprehensive automated test suite covers all Phase 6 components and security constraints:

| Test Class | Test Target | Tests Run | Result | Key Invariants Verified |
| :--- | :--- | :--- | :--- | :--- |
| **`AdminSecurityTest`** | `AuthenticationFilter`, `AuthorizationFilter` | 4 | **PASS** | Unauthenticated access redirects to `/login`; `PATIENT` and `DOCTOR` blocked from `/admin/*` (HTTP 403); `ADMIN` permitted. |
| **`AdminPortalServiceTest`** | All Admin Services | 9 | **PASS** | Dashboard KPI calculations, patient status updates, doctor availability toggles, specialty name uniqueness, administrative cancellations with mandatory reasons, clinic holiday closures (`doctor_id IS NULL`), anti-lockout invariants (self-demotion and last admin deactivation blocked), settings allowlist enforcement, read-only audit log retrieval. |
| **All Other System Tests** | Phases 1–5 Existing Services & Security | 84 | **PASS** | Full regression pass with zero regressions across auth, appointments, availability, clinical workflows, and doctor portal. |
| **Total Test Suite** | Entire CareSync Application | **97** | **PASS** | **100% Pass Rate (0 failures, 0 errors, 0 skipped)** |

---

## 7. Compliance Checklist

- [x] Java 21 LTS runtime preserved.
- [x] Jakarta EE 10 (Servlet 6.0, JSP 3.1, JSTL 3.0) preserved.
- [x] Zero Spring Boot or Spring Framework dependencies.
- [x] Zero Hibernate or JPA dependencies.
- [x] Clean layered MVC + Service + DAO architecture maintained.
- [x] Pure JDBC prepared statements and transaction handling maintained.
- [x] Anti-CSRF protection enforced on all state-modifying POST endpoints.
- [x] Role-Based Access Control enforced: all `/admin/*` routes strictly require `ADMIN` role.
- [x] Sensitive clinical notes and prescriptions segregated from administrative views.
- [x] Audit logs strictly append-only and read-only.
- [x] CareSync visual design identity strictly maintained:
  - Primary teal: `#176E65`
  - Soft teal: `#E9F3F2`
  - Background: `#F7F9FA`
  - Text: `#243342`
  - Card surfaces: `#FFFFFF`
- [x] Clean Maven WAR packaging verified (`target/caresync.war`).
- [x] Apache Tomcat 10.1 deployment verified and responding.
