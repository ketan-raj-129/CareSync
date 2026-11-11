# CareSync — Phase 7: Notification & Communication Engine Report

**Document Version:** 1.0.0  
**Phase:** 7 of 9  
**Status:** Completed & Verified  
**Runtime:** Java 21 LTS | Apache Maven 3.9.9 | Jakarta EE 10 (Servlet 6.0, JSP 3.1, JSTL 3.0)  
**Database:** PostgreSQL 16 (Neon Cloud ready) | HikariCP Connection Pool  
**Security:** RBAC Authorization Filter | Anti-CSRF Synchronizer Tokens | Tamper-Evident SHA-256 Audit Chain | Tenant & User Privacy Isolation  
**Architecture:** Layered MVC + Service Layer + DAO Layer + Transactional Outbox & Background Engine (Pure Java, pure JDBC, Zero Spring/Hibernate)

---

## 1. Executive Summary

Phase 7 of **CareSync — Healthcare, connected.** has been fully implemented, integrated, and verified on top of the Phase 1–6 foundation while preserving all existing functionality and security controls. Phase 7 establishes the platform's **Central Notification & Communication Engine**, decoupling notification generation from transactional clinical workflows and supporting multi-channel delivery, user communication preferences, transactional outbox reliability with exponential backoff, and automated appointment reminders.

Key capabilities delivered in Phase 7:

1. **Central Notification Engine Architecture:** Reusable, decoupled event-driven notification framework consisting of `NotificationEventService`, `NotificationService`, `NotificationPreferenceService`, `NotificationDispatcher`, `NotificationTemplateService`, and `NotificationScheduler`.
2. **Lifecycle Event Integration:** Full event generation hook across all appointment lifecycle transitions:
   - `APPOINTMENT_BOOKED`: Dispatched to patient (booking confirmation) and doctor (new consultation request).
   - `APPOINTMENT_CONFIRMED`: Dispatched to patient upon doctor acceptance.
   - `APPOINTMENT_REJECTED`: Dispatched to patient with optional doctor scheduling reason.
   - `APPOINTMENT_CANCELLED`: Dispatched to counterparty when cancelled by patient, doctor, or clinic administrator.
   - `APPOINTMENT_REMINDER`: Automated lead-time consultation reminder dispatched prior to scheduled start time.
3. **In-App Notification Inbox:** Dedicated in-app inbox interfaces for Patients (`/patient/notifications`) and Doctors (`/doctor/notifications`) with paginated message history, unread filtering, individual mark-as-read, mark-all-as-read, and dynamic unread badge indicators in the topbar.
4. **User Communication Preferences:** Granular multi-channel opt-in/opt-out settings for Patients (`/patient/preferences`) and Doctors (`/doctor/preferences`) governing booking notices, cancellations, automated reminders, and administrative alerts.
5. **Transactional Outbox & Dispatcher:** Idempotent outbox storage (`notification_outbox`) tracking discrete delivery attempts with lease locking (`FOR UPDATE SKIP LOCKED`), exponential backoff retries (`30s`, `60s`, `120s`), and lease timeout recovery.
6. **Automated Reminder Scheduler:** Background scheduler (`NotificationScheduler`) integrated into `AppContextListener` that dynamically scans upcoming consultations based on the configurable `appointment.reminder.lead.hours` system setting (default: 24h) and generates reminder events without duplicate deliveries.
7. **Administrative Monitoring & Retry Console:** Central operations dashboard (`/admin/notifications`) providing real-time visibility into outbox queues, delivery statuses (`PENDING`, `PROCESSING`, `SENT`, `FAILED`), error tracking, and manual retry execution.
8. **Brand Identity & HTML Templates:** Responsive, accessible email templates formatted using the CareSync visual identity (`#176E65` primary teal, `#E9F3F2` soft teal, `#243342` dark text, `#F7F9FA` background) with corresponding plain-text fallbacks.

All **106 automated tests** pass with **0 failures, 0 errors, and 0 skipped**. The application packages cleanly into `target/caresync.war` and adheres strictly to the prohibited technologies policy (no Spring, no Hibernate, no third-party frontend frameworks).

---

## 2. Phase 6 Carry-Forward Verifications

Prior to integrating the notification engine, three key carry-forward items from previous phases were inspected:

### A. Doctor Verification State
- **Inspection Finding:** In Phase 6, it was identified that `doctor_profiles` lacked an explicit medical council verification timestamp. In Phase 7, professional verification state continues to be modeled strictly without confusing it with `users.status` or `doctor_profiles.is_available`.
- **Policy Enforcement:** Notification dispatches strictly inspect `User.isActive()` and `Doctor.isAvailable()` and do not conflate account active status with credential accreditation. The formal credential verification column addition remains proposed for a future database migration cycle without unapproved modifications.

### B. Audit Hash-Chain Concurrency
- **Inspection Finding:** Inspected `AuditLogDaoImpl` and transaction boundaries. Notification events are recorded asynchronously or post-transaction and do not interleave with clinical database transactions.
- **Policy Enforcement:** Append-only SHA-256 hash-chain integrity is strictly preserved. No audit rows are updated or reordered. Notification logging uses separate database connections or runs after transaction commits.

### C. Dynamic Scheduling Settings & Reminder Lead Times
- **Inspection Finding:** Inspected `AdminSettingsServiceImpl` and `system_settings` table.
- **Policy Enforcement:** No reminder lead time is hardcoded in the codebase. Phase 7 introduced the whitelisted configuration key `appointment.reminder.lead.hours` (default: 24), which is queried dynamically by `NotificationSchedulerImpl`. Reminder time calculations use the clinic's configured timezone (`Asia/Kolkata`) and the actual appointment date and start time.

---

## 3. Architecture & Core Components

```
                                      [ Clinical Transaction Committed ]
                                                      │
                                                      ▼
                                         NotificationEventService
                                        /                       \
                                       ▼                         ▼
                           NotificationService           NotificationPreferenceService
                            (In-App Inbox)                (User Email Opt-In Check)
                                  │                                  │
                                  ▼                                  ▼
                           notifications table            NotificationTemplateService
                                                                     │
                                                                     ▼
                                                          notification_outbox table
                                                           (Idempotency Key & Lease)
                                                                     │
                                                                     ▼
                                                          NotificationDispatcher
                                                                     │
                                                                     ▼
                                                           NotificationProvider
                                                      (MockProvider / EmailProvider)
```

### 3.1 Domain Models & Enums (`com.caresync.model`)

- **`NotificationType`:** Enum extended with `APPOINTMENT_BOOKED`, `APPOINTMENT_CONFIRMED`, `APPOINTMENT_REJECTED`, `APPOINTMENT_CANCELLED`, `APPOINTMENT_REMINDER`, and `SYSTEM_ALERT`.
- **`NotificationChannel`:** Enum representing delivery mechanisms: `IN_APP`, `EMAIL`, `SMS`.
- **`NotificationDeliveryStatus`:** Enum tracking delivery lifecycle: `PENDING`, `PROCESSING`, `SENT`, `FAILED`, `CANCELLED`.
- **`NotificationPreference`:** Domain model storing user-level email communication preferences (`emailConfirmations`, `emailCancellations`, `emailReminders`, `emailAccountAlerts`).
- **`NotificationDelivery`:** Outbox delivery model encapsulating recipient data, rendered content (HTML and plain text), idempotency key, retry attempt counts, leases, and delivery timestamps.

### 3.2 Data Access Layer (`com.caresync.dao`)

- **`NotificationDao` / `NotificationDaoImpl`:** Extended with paginated inbox queries (`findPaginatedByUserId`), user count calculations, unread counts, and administrative audit streams.
- **`NotificationPreferenceDao` / `NotificationPreferenceDaoImpl`:** Handles user communication preference retrieval and upsert operations (`ON CONFLICT (user_id) DO UPDATE`).
- **`NotificationDeliveryDao` / `NotificationDeliveryDaoImpl`:** Manages transactional outbox storage, atomic claiming with PostgreSQL `FOR UPDATE SKIP LOCKED`, status updates, exponential backoff retry scheduling, and timed-out lease recovery.

### 3.3 Service & Engine Layer (`com.caresync.service`)

- **`NotificationService` / `NotificationServiceImpl`:** Provides user inbox management, unread counts, ownership validation during mark-as-read operations, and administrative notification streaming.
- **`NotificationPreferenceService` / `NotificationPreferenceServiceImpl`:** Supplies default preferences (all opt-in) when unconfigured and persists user modifications.
- **`NotificationTemplateService` / `NotificationTemplateServiceImpl`:** Renders HTML and plain-text templates for all appointment lifecycle events, ensuring CareSync brand colors and required clinical details are present.
- **`NotificationEventService` / `NotificationEventServiceImpl`:** Central event coordinator invoked upon appointment lifecycle changes. Generates in-app notifications and enqueues outbox records with deterministic idempotency keys (`APT-{ACTION}-{ROLE}-{aptId}-{userId}`).
- **`NotificationDispatcher` / `NotificationDispatcherImpl`:** Claims pending outbox items, dispatches through configured providers, updates statuses to `SENT`, or applies exponential backoff on delivery failure.
- **`NotificationScheduler` / `NotificationSchedulerImpl`:** Background engine holding scheduled executor threads for periodic outbox dispatch (every 30s) and automated appointment reminder scans (every 3m).

### 3.4 Provider Layer (`com.caresync.notification`)

- **`NotificationProvider`:** Extensible provider interface defining `send(recipientAddress, recipientName, subject, htmlContent, textContent)`.
- **`MockNotificationProvider`:** Thread-safe, in-memory delivery provider for automated unit testing and offline verification.
- **`EmailNotificationProvider`:** Production-ready SMTP dispatch provider respecting the `MAIL_ENABLED` configuration flag (default: `false` for offline environments).

### 3.5 Servlets & Mappings (`com.caresync.servlet`)

| Controller Servlet | Mapped Route(s) | HTTP Methods | Core Functionality |
| :--- | :--- | :--- | :--- |
| **`PatientNotificationServlet`** | `/patient/notifications`, `/patient/notifications/read`, `/patient/notifications/read-all` | `GET`, `POST` | Patient in-app notification inbox, unread filtering, pagination, and mark-as-read actions. |
| **`PatientPreferenceServlet`** | `/patient/preferences` | `GET`, `POST` | Patient email communication preferences management. |
| **`DoctorNotificationServlet`** | `/doctor/notifications`, `/doctor/notifications/read`, `/doctor/notifications/read-all` | `GET`, `POST` | Doctor in-app notification inbox, unread filtering, and mark-as-read actions. |
| **`DoctorPreferenceServlet`** | `/doctor/preferences` | `GET`, `POST` | Doctor communication preferences management. |
| **`AdminNotificationServlet`** | `/admin/notifications`, `/admin/notifications/retry` | `GET`, `POST` | Admin notification queue monitor, outbox status filters, and manual retry trigger. |

---

## 4. UI Views & Visual Identity

All views are crafted using the CareSync design system with responsive layouts, Lucide icons, and CSRF token protection:

1. **Patient Notification Inbox (`/WEB-INF/views/patient/notifications.jsp`):** Categorized notifications with event-specific colored icons, unread highlight styling, direct deep links to appointment details, and unread toggle filters.
2. **Patient Preferences (`/WEB-INF/views/patient/preferences.jsp`):** Card-based preference toggles for booking notices, cancellations, reminders, and alerts with confirmation flash messaging.
3. **Doctor Notification Inbox (`/WEB-INF/views/doctor/notifications.jsp`):** Clinical alert stream showing appointment requests, cancellations, and patient booking updates.
4. **Doctor Preferences (`/WEB-INF/views/doctor/preferences.jsp`):** Practitioner alert configuration for new patient bookings, cancellations, and administrative notices.
5. **Admin Notification Console (`/WEB-INF/views/admin/notifications.jsp`):** Dual-tab operational console monitoring the transactional outbox queue (with status filters and retry buttons) and the global in-app message stream.
6. **Navigation Updates:**
   - `topbar.jsp`: Dynamic notification bell linking directly to the authenticated user's portal inbox, featuring a real-time unread badge counter.
   - `sidebar.jsp`: Integrated navigation links for Notifications and Preferences across Admin, Doctor, and Patient menus.

---

## 5. Database Migration Proposal

The migration script has been prepared at `database/migrations/V7__notification_engine.sql` containing:

- Channel and delivery status enums (`notification_channel_enum`, `notification_delivery_status_enum`).
- `notification_outbox` table with composite indexes on `(status, next_retry_at, locked_until)` and `(user_id, created_at)`.
- `user_notification_preferences` table with unique constraint on `user_id`.
- Initial seed for `appointment.reminder.lead.hours` in `system_settings` (value: `24`).

*Note:* In accordance with offline execution constraints, this migration has not been applied to production Neon and remains documented for explicit administrator deployment.

---

## 6. Verification & Automated Test Results

The test suite was expanded with `NotificationEngineTest.java` covering:

- **Branding & Templates:** Verified that email templates contain CareSync brand palette (`#176E65`, `#E9F3F2`, `#243342`), headers, footers, and clinical reference tokens.
- **Preference Management:** Verified default opt-in behavior and preference persistence.
- **Inbox & Security:** Verified unread counts, pagination, and strict security isolation preventing cross-user notification manipulation.
- **Event Dispatch & Idempotency:** Verified that event dispatching enqueues outbox items and enforces idempotency key deduplication.
- **Opt-Out Compliance:** Verified that user preference opt-outs suppress external email dispatches while preserving internal in-app notifications.
- **Dispatcher & Retry Backoff:** Verified successful delivery marking (`SENT`), error logging on simulated failures (`FAILED`), and exponential retry calculations.
- **Reminder Scanning:** Verified that scheduler scans upcoming appointments matching lead-time criteria and generates reminder notifications without duplication.

### Build and Test Summary:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.caresync.service.NotificationEngineTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.caresync.service.AppointmentServiceTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.caresync.service.DoctorAppointmentServiceTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.caresync.service.AdminPortalServiceTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
...
[INFO] Results:
[INFO] Tests run: 106, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Building war: target/caresync.war
```

---

## 7. Conclusion & Next Steps

Phase 7 is complete, verified, and packaged. All requirements of the Notification & Communication Engine have been fulfilled without regressions across previous phases.

The system is now prepared for **Phase 8: Billing, Invoicing & Financial Operations Engine** when commanded by the user.

