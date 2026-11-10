# CareSync — Phase 5: Doctor Portal & Clinical Workflow Engine Report

**Document Version:** 1.0.0  
**Phase:** 5 of 9  
**Status:** Completed & Verified  
**Runtime:** Java 21 LTS | Apache Maven 3.9.9 | Jakarta EE 10 (Servlet 6.0, JSP 3.1, JSTL 3.0)  
**Database:** PostgreSQL 16 (Neon Cloud ready) | HikariCP Connection Pool  
**Security:** RBAC Authorization Filter | Tamper-Evident SHA-256 Audit Chain | Anti-CSRF Protection | IDOR Defenses  
**Architecture:** Layered MVC + Service Layer + DAO Layer (Pure Java, pure JDBC, Zero Spring/Hibernate)  

---

## 1. Executive Summary

Phase 5 of **CareSync — Healthcare, connected.** has been fully implemented, integrated, and verified. Building directly upon the foundation and Patient Portal from Phases 0–4, Phase 5 establishes the comprehensive clinical provider workspace for medical practitioners:

1. **Doctor Operations Dashboard** (`/doctor/dashboard`): Live practice metrics (total, today, pending approvals, confirmed, completed), today's encounter schedule, pending appointment queue with one-click actions, and recent activity log.
2. **Physician Profile & Practice Settings** (`/doctor/profile`): Read-only institutional credentials (medical license number, primary email, full legal name) alongside self-service clinical settings (qualification, years in practice, consultation fee, clinic suite/room number, biographical summary, and active availability toggle).
3. **Doctor Appointment Management** (`/doctor/appointments`, `/doctor/appointments/view`, `/doctor/appointments/action`): Filtered appointment registry (All, Today, Upcoming, Pending, Confirmed, Completed, Cancelled), patient vitals header, reason for visit, historical consultation drill-down, and audited status transitions (`CONFIRM`, `REJECT`, `COMPLETE`, `CANCEL`).
4. **Physician Schedule & Leave Engine** (`/doctor/availability`, `/doctor/availability/manage`): 7-day recurring weekly consultation hours configuration with time range validation (`startTime < endTime`) and slot duration enforcement (15, 20, 30, 45, 60 mins), combined with calendar exception management (full-day leaves, partial time-offs, emergency absences) backed by conflict detection preventing leave creation when active patient appointments exist.
5. **Clinical Consultation Workflow** (`/doctor/consultation`): End-to-end clinical encounter workspace enabling attending physicians to review chief complaints and prior patient history, record diagnostic findings (presenting symptoms, clinical diagnosis, treatment plan, confidential physician progress notes), formulate digital prescriptions (Rx) with dynamic medication line items, and finalize encounters transactionally.
6. **Prescription Registry & Printing** (`/doctor/prescriptions`, `/doctor/prescriptions/view`): Centralized repository of all medical prescriptions issued by the practitioner, complete with a dedicated, printable clinical prescription sheet (`window.print()`) formatted to medical documentation standards.

All **84 automated unit, integration, and security tests** pass with **0 failures, 0 errors, and 0 skipped**. The application builds cleanly into `target/caresync.war` (5.2MB) and is deployed to Apache Tomcat 10.1.

---

## 2. Carry-Forward Verifications from Phase 4

Before developing Phase 5, the four carry-forward verification items were audited against the codebase and requirements:

1. **Business Rule BR-006 (Two-Hour Cancellation Notice):**
   - *Verification:* Verified in `docs/SRS.md` (§5.2, BR-006). BR-006 explicitly restricts *patients* from cancelling appointments with less than 2 hours remaining before start time to prevent last-minute schedule gaps. In Phase 5, *doctors* are permitted to decline pending requests or cancel appointments when necessary (e.g. emergency surgeries or sudden illness) with a mandatory recorded reason, which is logged to `audit_logs` and stored in `cancellation_reason`.
2. **Doctor Schedule Slot Duration Configuration:**
   - *Verification:* Verified in `DoctorScheduleServiceImpl` and `AvailabilityServiceImpl`. The availability calculation dynamically respects `schedule.getSlotDurationMinutes() > 0 ? schedule.getSlotDurationMinutes() : 30` rather than hardcoding 30 minutes. Allowed durations (15, 20, 30, 45, 60 mins) are strictly validated.
3. **Appointment Reference Number Uniqueness:**
   - *Verification:* Inspected `database/schema.sql` (line 120). The table definition contains `appointment_reference VARCHAR(36) NOT NULL UNIQUE`. Every appointment generated receives a unique reference number formatted as `APT-XXXXXXXX`.
4. **Audit Log Tamper-Evidence & Concurrency:**
   - *Verification:* Inspected `AuditLogDaoImpl.java`. Each audit log entry calculates its SHA-256 hash using the previous entry's `log_hash` as salt. In concurrent environments, row locking and serialized write sequences ensure tamper-evident chain verification via `AuditLogDao.verifyIntegrity()`.

---

## 3. Implemented Architecture & Component Directory

### 3.1 Web Controllers (`com.caresync.servlet`)

| Controller Servlet | Mapped Route(s) | HTTP Methods | Core Responsibilities & Security Controls |
| :--- | :--- | :--- | :--- |
| **`DoctorDashboardServlet`** | `/doctor/dashboard` | `GET` | Fetches live metrics, today's schedule, pending approvals, and renders dashboard. |
| **`DoctorProfileServlet`** | `/doctor/profile` | `GET`, `POST` | Manages doctor credentials and professional profile. Validates non-negative fees and logs changes. |
| **`DoctorAppointmentServlet`** | `/doctor/appointments` | `GET` | Filterable queue of doctor appointments (Today, Upcoming, Pending, Confirmed, Completed, Cancelled). |
| **`DoctorAppointmentDetailsServlet`** | `/doctor/appointments/view` | `GET` | Comprehensive appointment breakdown, patient vitals, clinical notes, and prescription links. Enforces ownership. |
| **`DoctorAppointmentActionServlet`** | `/doctor/appointments/action` | `POST` | Executes status transitions (`CONFIRM`, `REJECT`, `COMPLETE`, `CANCEL`) with CSRF protection and audit logging. |
| **`DoctorScheduleManagementServlet`** | `/doctor/availability`, `/doctor/availability/manage` | `GET`, `POST` | Renders weekly schedule table and leave exceptions; processes recurring hour updates and leave submissions. |
| **`DoctorConsultationServlet`** | `/doctor/consultation` | `GET`, `POST` | Interactive clinical documentation workspace. `POST` transactionally saves medical record, prescription, and completes appointment. |
| **`DoctorPrescriptionServlet`** | `/doctor/prescriptions`, `/doctor/prescriptions/view` | `GET` | Prescription registry and printable prescription sheet view (`/view?id=...`). Enforces practitioner ownership. |

---

### 3.2 Data Transfer Objects (`com.caresync.dto`)

- **`DoctorDashboardDto`**: Aggregates total, today's, pending, confirmed, and completed appointment metrics alongside typed lists for today's visits, upcoming encounters, pending requests, and recent activity.
- **`DoctorProfileUpdateDto`**: Captures editable fields: `qualification`, `experienceYears`, `consultationFee`, `bio`, `roomNumber`, `phoneNumber`, `specialtyId`, and `available`.
- **`PrescriptionItemDto`**: Encapsulates medication line items: `medicineName`, `dosage`, `frequency`, `duration`, and `instructions`.
- **`ConsultationRequestDto`**: Packages clinical encounter data: `appointmentId`, `symptoms`, `diagnosis`, `treatmentPlan`, `clinicalNotes`, `generalAdvice`, and `List<PrescriptionItemDto>`.

---

### 3.3 Service Layer (`com.caresync.service`)

#### `DoctorPortalService` & `DoctorPortalServiceImpl`
- Calculates metrics using database aggregation (`countByDoctorId`, `countTodayByDoctorId`, `countByDoctorAndStatus`) in the `Asia/Kolkata` timezone.
- Resolves doctor entities by authenticated `userId`.
- Enforces strict actor checks: `actorUserId.equals(userId)` preventing IDOR impersonation.
- Records `DOCTOR_PROFILE_UPDATED` in `audit_logs`.

#### `DoctorAppointmentService` & `DoctorAppointmentServiceImpl`
- Enforces doctor ownership: Doctor A cannot view or transition appointments belonging to Doctor B.
- Implements strict state machine validation:
  - `CONFIRM`: Permitted only from `PENDING`.
  - `REJECT`: Permitted only from `PENDING`, captures rejection reason.
  - `COMPLETE`: Permitted only from `CONFIRMED`.
  - `CANCEL`: Permitted only on non-terminal appointments (`PENDING` or `CONFIRMED`).
- Records state change audits: `APPOINTMENT_CONFIRMED`, `APPOINTMENT_REJECTED`, `APPOINTMENT_COMPLETED`, `APPOINTMENT_CANCELLED_BY_DOCTOR`.

#### `DoctorScheduleService` & `DoctorScheduleServiceImpl`
- Validates 7-day recurring schedules ensuring `startTime < endTime` and valid slot durations.
- Enforces leave exception constraints: date must not be in the past; partial-day exceptions must have valid time ranges.
- **Active Appointment Conflict Guard**: Detects if `PENDING` or `CONFIRMED` appointments already exist during proposed leave dates/times. If conflicts exist, rejects leave creation with an informative message requiring the practitioner to reschedule or cancel affected patients first.
- Enforces exception deletion ownership: Doctors cannot delete other practitioners' exceptions.

#### `ClinicalService` & `ClinicalServiceImpl`
- **ACID Transaction Control**: Begins a single transaction (`conn.setAutoCommit(false)`), writes `medical_records`, writes `prescriptions`, writes each `prescription_items` row, updates appointment status to `COMPLETED`, logs `CONSULTATION_DOCUMENTED`, and commits. Automatically rolls back on any SQL failure.
- **Anti-IDOR Patient Privacy Guard**: In `getPatientMedicalHistory(patientId, doctorId)`, validates that the requesting doctor has had an active or historical encounter with the patient before returning records. Unrelated practitioners are denied access (HTTP 403 / `ValidationException`).
- **Prescription Isolation**: Only the prescribing doctor can view or print issued prescriptions.

---

### 3.4 Data Access Layer (`com.caresync.dao`)

- **`AppointmentDao` & `AppointmentDaoImpl`**:
  - Implemented `findWithDetailsByDoctorId(Long doctorId)` with multi-table joins on `patient_profiles` and `users`.
  - Implemented `findWithDetailsByDoctorAndDate(Long doctorId, LocalDate date)`.
  - Implemented optimized count queries: `countByDoctorId`, `countByDoctorAndStatus`, `countTodayByDoctorId`.
  - Implemented `updateStatus(Long id, AppointmentStatus status, String reason, Long cancelledBy, Connection conn)` supporting external connection transactions.
- **`MedicalRecordDao` & `MedicalRecordDaoImpl`**:
  - Implemented `findByAppointmentId`, `findByPatientId`, `findByDoctorId`.
  - Implemented `save(MedicalRecord, Connection conn)` and `update(MedicalRecord)`.
- **`PrescriptionDao` & `PrescriptionDaoImpl`**:
  - Implemented `findByAppointmentId`, `findByPatientId`, `findByDoctorId`, `findById`.
  - Implemented transactional `save(Prescription, Connection conn)` and `saveItem(PrescriptionItem, Connection conn)`.
  - Implemented `findItemsByPrescriptionId(Long prescriptionId)`.

---

## 4. JSP Presentation Tier (`src/main/webapp/WEB-INF/views/doctor/`)

All views follow the CareSync Modern Healthcare SaaS design system:
- **Primary Teal:** `#176E65`
- **Soft Teal Accent:** `#E9F3F2`
- **Surface Background:** `#F7F9FA`
- **Dark Neutral Text:** `#243342`
- **Lucide Icons:** Uniform SVG iconography throughout.

### Implemented Views:
1. **`dashboard.jsp`**: 4 metric stat cards (Total, Today's, Pending Approvals, Completed), Today's Schedule timeline, Pending Approvals table with quick Confirm/Decline buttons, upcoming appointments, and quick action cards.
2. **`profile.jsp`**: Practice details editor (specialty, qualification, experience, fee, room, bio, availability) with read-only institutional credentials and CSRF-protected form submission.
3. **`appointments.jsp`**: Tabbed status filters (All, Today, Upcoming, Pending, Confirmed, Completed, Cancelled), patient demographic summaries, badge indicators, and contextual action buttons.
4. **`appointment-details.jsp`**: Two-column layout with patient profile vitals, chief complaint, clinical progress notes, issued prescription preview, and status transition action controls.
5. **`availability.jsp`**: Recurring weekly 7-day schedule editor with start/end time inputs and slot duration selection, paired with a Leave Exception manager and active exception registry.
6. **`consultation.jsp`**: Interactive clinical encounter workstation with patient vitals sidebar, chief complaint, prior medical history timeline, diagnostic fields (symptoms, diagnosis, treatment, clinical notes), and a dynamic JavaScript medication table (medicine name, dosage, frequency, duration, instructions, Add/Remove rows).
7. **`prescriptions.jsp`**: Registry of issued prescriptions with encounter reference, item counts, general advice preview, and print action links.
8. **`prescription-details.jsp`**: Medical prescription sheet with clinic branding, physician qualifications, patient details, ℞ symbol, medication table, general clinical instructions, physician signature block, and print media styles (`@media print`).

---

## 5. Security & RBAC Verification

1. **RBAC Filtering (`AuthorizationFilter`)**:
   - `/doctor/*` routes strictly require `principal.getRole() == Role.DOCTOR`.
   - Any `PATIENT` or unauthenticated actor attempting to access `/doctor/*` is intercepted:
     - Unauthenticated: redirected to `/login?redirect=...`.
     - Non-doctor role: blocked with `HTTP 403 Forbidden`.
2. **Cross-Doctor Data Isolation (Anti-IDOR)**:
   - Every appointment, schedule exception, medical record, and prescription lookup verifies `appointment.getDoctorId().equals(authenticatedDoctorId)`.
   - Attempting to inspect or modify another doctor's record raises a `ValidationException` and logs a security alert.
3. **Patient Data Privacy Protection**:
   - Doctors cannot browse unassigned patient records. Access to a patient's historical medical notes is granted only if an encounter exists between that doctor and patient.
4. **CSRF Enforcement**:
   - All state-changing POST requests (`/doctor/profile`, `/doctor/appointments/action`, `/doctor/availability/manage`, `/doctor/consultation`) validate the session CSRF token via `CsrfFilter`. Requests with missing or tampered tokens receive `HTTP 403 Forbidden`.
5. **Audit Trail**:
   - Every state change writes an append-only, tamper-evident log in `audit_logs` capturing user ID, action type, target entity, target ID, IP address, user agent, and timestamp.

---

## 6. Automated Test Suite Summary

The automated test suite in `src/test/java/com/caresync/` was expanded to cover all Phase 5 modules:

| Test Class | Focus Area | Tests Executed | Status |
| :--- | :--- | :--- | :--- |
| **`DoctorPortalServiceTest`** | Metrics calculation, profile updates, fee validation, actor impersonation defense | 4 | Passed |
| **`DoctorAppointmentServiceTest`** | Filtered appointment queues, status transitions (Confirm, Reject, Complete, Cancel), IDOR defenses | 7 | Passed |
| **`DoctorScheduleServiceTest`** | Weekly schedule time validation, past date leave rejection, appointment conflict guard, ownership checks | 4 | Passed |
| **`ClinicalServiceTest`** | Transactional consultation recording, prescription item persistence, patient history privacy guard | 4 | Passed |
| **`DoctorSecurityTest`** | Unauthenticated redirection, PATIENT role blocking (403), DOCTOR access grant, CSRF enforcement | 4 | Passed |
| **`AppointmentServiceTest`** | Phase 4 transactional booking, slot conflict resolution, cancellation rules | 10 | Passed |
| **`AvailabilityServiceTest`** | Phase 4 slot generation, holiday integration, leave exclusions | 8 | Passed |
| **`SecurityEngineTest`** | CSRF filters, AuthenticationFilter, RBAC AuthorizationFilter, SecurityHeadersFilter | 7 | Passed |
| **`AuthServiceTest`** | Password hashing, authentication, role assignment | 7 | Passed |
| **`DomainModelTest`** | Entity constructors, getters/setters, relations | 7 | Passed |
| **`DaoContractTest`** | Data access layer contracts | 5 | Passed |
| **`PasswordHasherTest`** | PBKDF2-HMAC-SHA256 salt & iteration verification | 5 | Passed |
| **`DoctorDiscoveryServiceTest`** | Specialty search, physician listing | 3 | Passed |
| **`PatientServiceTest`** | Patient demographic updates, validation | 4 | Passed |
| **`DatabaseConfigTest`** | HikariCP pool configuration | 3 | Passed |
| **`AppFoundationTest`** | Application lifecycle and constants | 2 | Passed |
| **Total** | **All Phase 1–5 Test Suites** | **84** | **100% Passed (0 Failures, 0 Errors, 0 Skipped)** |

---

## 7. Deliverables & Git State

- **Git Branch:** `main`
- **Git Author:** `ketan-raj-129 <ketanraj129@gmail.com>`
- **WAR Package:** `target/caresync.war` (5.2 MB)
- **Tomcat 10.1 Live Service:** Running on port `8080`, root context deployed.
