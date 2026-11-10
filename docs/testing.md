# CareSync — Comprehensive Quality Assurance & Testing Strategy

**Document Version:** 1.1.0  
**Target Platform:** Java 21, JUnit 5, Mockito, AssertJ, Jakarta EE 10, Neon PostgreSQL  
**Author:** Lead QA Engineer & Java Test Architect  

---

## 1. Testing Philosophy & Test Pyramid

CareSync enforces a multi-tiered test automation strategy to validate system correctness, security defenses, concurrency robustness, and database transaction integrity.

```
                     / \
                    /   \
                   / E2E \       <-- UI & Workflow Tests (Responsive Layout, View Navigation)
                  /-------\
                 /  Integ  \     <-- Servlet Filters, JDBC Transactions, Neon PostgreSQL
                /-----------\
               /   Service   \   <-- Business Rules, Range-based Concurrency, RBAC Boundaries
              /---------------\
             /   Unit Tests    \ <-- 600,000 Iteration PBKDF2, Slot Generators, Exceptions
            /-------------------\
```

---

## 2. Test Scenarios & Test Suite Matrix

### 2.1 Unit Testing Suite

| Test Case ID | Test Category | Target Component | Test Scenario & Input | Expected Result |
|---|---|---|---|---|
| **TC-UNIT-001** | Cryptography | `PasswordUtil` | Hash password `"SecureP@ss123"` using 600,000 iterations and 16-byte `SecureRandom` salt. | Valid 256-bit Base64 hash generated; verification with correct credentials returns `true`. |
| **TC-UNIT-002** | Cryptography | `PasswordUtil` | Pass incorrect candidate password `"WrongP@ss123"` against valid 600,000 iteration hash. | Returns `false` via constant-time comparison (`MessageDigest.isEqual`). |
| **TC-UNIT-003** | Cryptography | `UserService` | User with old iteration count (e.g. 500,000) logs in successfully while target is 600,000. | User authenticated, password transparently rehashed to 600,000 iterations, and `users` table updated. |
| **TC-UNIT-004** | Validation | `ValidationUtil` | Validate emails: `"patient@domain.com"`, `"invalid-email"`, `""`, `"user@.com"`. | Returns `true` for valid email; `false` for malformed formats. |
| **TC-UNIT-005** | Date/Time | `DateTimeUtil` | Generate 30-min slots between `09:00` and `12:00` with slot duration 30. | Generates exactly 6 slots: `09:00`, `09:30`, `10:00`, `10:30`, `11:00`, `11:30`. |
| **TC-UNIT-006** | Domain Invariant | `Doctor` | Call `doctor.setConsultationFee(new BigDecimal("-10.00"))`. | Throws `IllegalArgumentException("Consultation fee cannot be negative")`. |
| **TC-UNIT-007** | Domain Invariant | `User` | Call `user.setHashIterations(50000)`. | Throws `IllegalArgumentException("PBKDF2 iterations cannot be lower than baseline 600,000")`. |

---

### 2.2 Service Layer & Availability Exceptions Testing

| Test Case ID | Test Category | Target Service | Test Scenario & Input | Expected Result |
|---|---|---|---|---|
| **TC-SRV-001** | Availability | `AppointmentService` | Doctor has a full-day leave recorded in `doctor_availability_exceptions` on `2026-11-25`. | Dynamic slot generator returns 0 available slots for that doctor on that date. |
| **TC-SRV-002** | Availability | `AppointmentService` | Doctor has a partial-day leave from `14:00` to `17:00` on `2026-11-25`. | Morning slots (`09:00` - `13:30`) are available; afternoon slots (`14:00` - `16:30`) are omitted. |
| **TC-SRV-003** | Availability | `AppointmentService` | Clinic-wide holiday recorded with `doctor_id = NULL` on `2026-12-25`. | All doctors across all specialties display zero available booking slots on that date. |
| **TC-SRV-004** | Appointment | `AppointmentService` | Patient attempts to book appointment 90 days in advance (exceeding 60-day limit). | Throws `ValidationException("Appointments cannot be booked more than 60 days in advance")`. |
| **TC-SRV-005** | Appointment | `AppointmentService` | Patient attempts to cancel appointment scheduled in 45 minutes (within 2-hour window). | Throws `ValidationException("Appointments cannot be cancelled online within 2 hours of schedule")`. |
| **TC-SRV-006** | Clinical Record | `MedicalRecordService` | Doctor attempts to view history of a patient with whom they have no past or current appointment. | Throws `UnauthorizedException("Access Denied: No active care relationship exists")`. |

---

### 2.3 Concurrent Double-Booking Testing

| Test Case ID | Test Category | Target Component | Test Scenario & Input | Expected Result |
|---|---|---|---|---|
| **TC-CONC-001** | Concurrency (Grid Race) | `AppointmentService` | 2 parallel threads simultaneously book identical doctor, date `2026-11-20`, and slot `10:00 - 10:30`. | Thread A commits successfully. Thread B catches `SlotConflictException` (PostgreSQL GiST exclusion violation `23P01`). Exactly 1 row inserted. |
| **TC-CONC-002** | Concurrency (Varying Overlap) | PostgreSQL Engine | Thread 1 inserts appointment for `10:00 - 10:45`. Concurrent Thread 2 inserts appointment for `10:30 - 11:00` for same doctor. | PostgreSQL GiST exclusion constraint `ex_no_overlapping_doctor_appointments` detects range collision and rejects Thread 2 with error `23P01`. |
| **TC-CONC-003** | Concurrency (Patient Overlap) | PostgreSQL Engine | Patient attempts to book Doctor A at `10:00 - 10:30` while simultaneously booking Doctor B at `10:15 - 10:45`. | PostgreSQL constraint `ex_no_overlapping_patient_appointments` rejects second booking due to overlapping patient range. |

---

### 2.4 Synchronous Audit Logging & Privilege Testing

| Test Case ID | Test Category | Target Component | Test Scenario & Input | Expected Result |
|---|---|---|---|---|
| **TC-AUD-001** | Atomicity | `AppointmentService` | Force an error after appointment insertion but before transaction commit. | Both appointment row and audit log row are rolled back atomically; zero orphaned records. |
| **TC-AUD-002** | Tamper-Resistance | PostgreSQL Role Privileges | Connect as application role `caresync_app` and execute: `UPDATE audit_logs SET action = 'MODIFIED';` | PostgreSQL rejects query with error `42501 permission_denied for table audit_logs`. |
| **TC-AUD-003** | Tamper-Resistance | PostgreSQL Role Privileges | Connect as application role `caresync_app` and execute: `DELETE FROM audit_logs WHERE log_id = 1;` | PostgreSQL rejects query with error `42501 permission_denied for table audit_logs`. |
| **TC-AUD-004** | Hash Chaining | `AuditService` | Verify cryptographic hash chain on sequential audit entries. | Each entry's `prev_log_hash` matches previous entry's `log_hash`. Chain validates successfully. |

---

### 2.5 Security, Cookie & Filter Testing

| Test Case ID | Test Category | Target Component | Test Scenario & Input | Expected Result |
|---|---|---|---|---|
| **TC-SEC-001** | Cookie Security | `HttpServletResponse` | Inspect `Set-Cookie` header for session ID in HTTPS deployment. | Cookie contains `HttpOnly`, `SameSite=Lax`, and `Secure` attributes. |
| **TC-SEC-002** | RBAC Filter | `AuthorizationFilter` | Authenticated Patient attempts to access `/admin/users` or `/doctor/appointments`. | Request intercepted; returns **HTTP 403 Forbidden**; logs security event synchronously in `audit_logs`. |
| **TC-SEC-003** | Auth Filter | `AuthenticationFilter` | Unauthenticated user attempts to access `/patient/dashboard`. | Request intercepted; redirected to `/login?error=auth_required`. |
| **TC-SEC-004** | CSRF Defense | `CsrfFilter` | Submit `POST /appointments/book` without `_csrf` token parameter or with invalid token. | Request aborted; returns **HTTP 403 Forbidden: Invalid CSRF Token**. |
| **TC-SEC-005** | SQL Injection | `AuthServlet`, `UserDao` | Attempt login with email: `' OR '1'='1' --` and password: `' OR '1'='1'`. | Query safely parameterized; returns "Invalid email or password"; zero SQL execution breach. |
| **TC-SEC-006** | XSS Defense | JSP Views | Register with patient name `<script>alert('xss')</script>`. | Name rendered via `<c:out value="${user.fullName}" />`; script tags HTML-escaped as `&lt;script&gt;`. |

---

### 2.6 UI Responsiveness & Cross-Browser Testing

| Test Case ID | Test Category | Target Viewport | Screen Resolution | Expected Result |
|---|---|---|---|---|
| **TC-UI-001** | Responsive Grid | Desktop Monitor | 1920 x 1080 | Persistent left sidebar, 4-column metric cards, full data tables without horizontal overflow. |
| **TC-UI-002** | Responsive Grid | Clinical Tablet | 768 x 1024 (iPad Portrait) | Collapsible offcanvas sidebar, 2-column metric cards, responsive wrapped tables. |
| **TC-UI-003** | Responsive Grid | Smartphone | 375 x 812 (iPhone Mobile) | Hamburger navigation drawer, single-column stacked cards, touch-friendly 44px tap targets. |
| **TC-UI-004** | Visual Consistency | Cross-Browser | Chrome, Firefox, Safari, Edge | Exact color rendering matching Primary Teal `#176E65` and Soft Teal `#E9F3F2`. |
