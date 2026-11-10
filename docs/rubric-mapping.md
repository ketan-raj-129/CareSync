# CareSync — University Evaluation Rubric Mapping

**Document Version:** 1.1.0  
**Target Course:** Advanced Java Web Development  
**Total Marks:** 33 Marks  
**Author:** Lead Architect & Academic Evaluator  

---

## 1. Rubric Distribution Summary

| Criteria # | Evaluation Area | Maximum Marks | Key CareSync Architectural Pillars |
|---|---|---|---|
| **Area 1** | Problem Understanding & Solution Design | **8 Marks** | Requirement specifications, 3-tier SaaS domain model, role-based workflows, healthcare edge cases, doctor availability exceptions & time-off. |
| **Area 2** | Core Java Concepts & OOP Mastery | **10 Marks** | OOP inheritance hierarchy (`BaseModel` -> `User` -> `Admin`, `Doctor`, `Patient`), polymorphism, interfaces, generics, collections, custom exceptions, multithreading, and concurrency. |
| **Area 3** | Database Integration & Pure JDBC | **8 Marks** | 14-table 3NF Neon PostgreSQL schema, HikariCP scale-to-zero pool (`minimumIdle = 0`), parameterized queries, explicit ACID transactions, range-based GiST exclusion locking (`tsrange`). |
| **Area 4** | Jakarta Servlets & Web Integration | **7 Marks** | MVC architecture, front controller pattern, interceptor filters (Auth, RBAC, CSRF, Encoding), hardened session cookies (`HttpOnly`, `SameSite=Lax`, `Secure`), JSP 3.1. |
| **Total** | **Comprehensive Evaluation** | **33 Marks** | **Full Academic & Practical Compliance** |

---

## 2. In-Depth Rubric Mapping & Demonstration Evidence

### Criterion 1: Problem Understanding & Solution Design (8 Marks)

| Sub-Criteria | What Feature Demonstrates It | Java Class / Package / Artifact | Practical Demonstration & Evidence for Examiner |
|---|---|---|---|
| **Real-World Healthcare Problem Understanding** | Clear separation between Patient, Doctor, and Admin roles; automated slot availability; doctor availability exceptions & time-off; digital prescription writing. | `docs/SRS.md`, `docs/use-cases.md`, `com.caresync.model.*` | Demonstrate full clinical workflow: Patient finds a cardiologist -> books a slot -> Doctor opens consultation view -> writes diagnosis & multi-item prescription -> Patient downloads printable prescription. |
| **Domain Modeling & Edge Case Design** | Prevention of appointment double-booking; care relationship access boundaries (BR-002); non-destructive record preservation; full/partial leave tracking. | `AppointmentServiceImpl.java`, `DoctorAvailabilityException.java`, `docs/database-design.md`, `docs/erd.md` | Demonstrate that doctors cannot view records of patients not assigned to them; demonstrate that deactivating a doctor preserves patient medical history; demonstrate that doctor time-off suppresses booking slots. |
| **Architectural Separation of Concerns** | Layered MVC + Service + DAO design with zero controller-to-database leakage and no heavyweight frameworks. | `com.caresync.servlet.*`, `com.caresync.service.*`, `com.caresync.dao.*` | Walk through code structure: show that Servlets only deal with HTTP/DTOs, Services encapsulate business rules and transactions, and DAOs handle pure SQL. |

---

### Criterion 2: Core Java Concepts & OOP Mastery (10 Marks)

| Core Java Concept | Concrete Implementation & Use Case | Exact Classes & Interfaces | Code Evidence & Demonstration Details |
|---|---|---|---|
| **Encapsulation & Domain Invariants** | Immutable identifiers, private state, domain validation in setters (preventing negative fees, enforcing minimum 600,000 PBKDF2 iterations). | `User.java`, `Doctor.java`, `Appointment.java` | Show private fields with getters/setters validating invariants (e.g., `consultationFee.compareTo(BigDecimal.ZERO) >= 0`, `hashIterations >= 600000`). |
| **Inheritance Hierarchy** | Meaningful inheritance: `BaseModel` -> `User` -> `Admin`, `Doctor`, `Patient`. Shared identity, authentication, and status. | `BaseModel.java`, `User.java`, `Admin.java`, `Doctor.java`, `Patient.java` | Code review showing clean inheritance: `Admin` adds system governance; `Doctor` adds clinical attributes; `Patient` adds health demographics. |
| **Polymorphism & Dynamic Dispatch** | Dynamic dashboard routing (`user.getDashboardUrl()`), role-specific record access permissions (`user.canAccessPatientRecord()`), interface realizations. | `User.java`, `Admin.java`, `Doctor.java`, `Patient.java`, `AppointmentService.java` | Demonstrate calling `user.getDashboardUrl()` polymorphically returning `/admin/dashboard`, `/doctor/dashboard`, or `/patient/dashboard` at runtime. |
| **Method Overloading** | Flexible query signatures in DAOs and services for single-date vs. date-range vs. status-filtered appointments. | `AppointmentDao.java`, `DoctorDao.java` | Show overloaded methods: `findByDoctor(Long id)`, `findByDoctor(Long id, LocalDate date)`, `findByDoctor(Long id, LocalDate s, LocalDate e)`. |
| **Interfaces & Abstraction** | Complete decoupling of business contracts from persistence engines via interfaces. | `GenericDao<T, ID>`, `UserDao`, `AppointmentService` | Display `GenericDao<T, ID>` defining standard CRUD, implemented cleanly across all DAOs. |
| **Generics** | Type-safe generic data access and generic HTTP response envelopes. | `GenericDao<T, ID>`, `ApiResponse<T>`, `PagedResult<T>` | Show compile-time type safety preventing manual casts when querying database entities. |
| **Collections Framework** | `List` for medication items, `Set<DayOfWeek>` for active doctor days, `List<DoctorAvailabilityException>` for time-off, `Map<LocalDate, List<TimeSlotDTO>>` for slot calendars. | `AppointmentServiceImpl.java`, `Prescription.java`, `Doctor.java` | Demonstrate Java Streams manipulating and grouping time slots by date and availability. |
| **Custom Exception Hierarchy** | Structured business and security exceptions mapped to HTTP response codes. | `CareSyncException`, `SlotConflictException` (PostgreSQL `23P01`), `UnauthorizedException`, `ValidationException` | Throw `SlotConflictException` during concurrent booking; show controller handling it without exposing raw SQL errors. |
| **Modern Java Date/Time API (`java.time`)** | Native handling of dates, times, durations, and timestamps without legacy `java.util.Date`. | `LocalDate`, `LocalTime`, `Instant`, `Duration` in `model.*` and `DateTimeUtil.java` | Display 30-minute slot calculations using `LocalTime.plus(Duration.ofMinutes(slotDuration))`. |
| **Multithreading & Concurrency** | Periodic background appointment reminders; synchronous transactional execution for critical audit records. | `util.AppThreadPool`, `listener.AppContextListener`, `AuditService` | Demonstrate `ScheduledExecutorService` running every 15 minutes to flag appointments and queue notification alerts in background daemon threads. |

---

### Criterion 3: Database Integration & Pure JDBC (8 Marks)

| Sub-Criteria | What Feature Demonstrates It | Java Class / Package / SQL | Practical Demonstration & Evidence for Examiner |
|---|---|---|---|
| **Normalized Relational Schema (3NF)** | 14 normalized tables (`users`, `patient_profiles`, `doctor_profiles`, `doctor_availability_exceptions`, `appointments`, `prescriptions`, etc.). | `database/schema.sql`, `docs/database-design.md`, `docs/erd.md` | Present ERD diagram; explain foreign key constraints, `ON DELETE RESTRICT` for medical integrity, and 1:N prescription item normalization. |
| **Neon Cloud Scale-to-Zero Pooling** | HikariCP integration with `minimumIdle = 0` allowing Neon serverless compute to suspend cleanly when idle. | `DatabaseConfig.java`, `AppContextListener.java` | Show HikariCP pool configuration; verify cold-start connection recovery using `connectionTimeout = 30000ms` and `connection.isValid(5)`. |
| **SQL Injection Immunity via PreparedStatement** | Parameterized queries for every database read/write operation. Strictly zero SQL string concatenation. | `UserDaoImpl.java`, `AppointmentDaoImpl.java` | Inspect DAO source code; show that every query uses `PreparedStatement` with typed parameter binding (`setString`, `setLong`, `setTimestamp`). |
| **Explicit ACID Transactions & Locking** | Atomic booking checkout and consultation recording with rollback on error; synchronous audit log recording. | `AppointmentServiceImpl.java` | Live demo: simulate transaction rollback by forcing an error mid-checkout; prove that no partial appointment or orphaned audit entry is persisted. |
| **Database Concurrency Controls** | PostgreSQL GiST range exclusion constraint (`ex_no_overlapping_doctor_appointments` using `tsrange`) and `SELECT ... FOR UPDATE` locking. | `schema.sql`, `AppointmentDaoImpl.java` | Demonstrate concurrent checkout scenario: two parallel threads attempting to book overlapping slots; one succeeds, the second catches `SlotConflictException` (`23P01`). |

---

### Criterion 4: Jakarta Servlets & Web Integration (7 Marks)

| Sub-Criteria | What Feature Demonstrates It | Java Class / Package / Web Config | Practical Demonstration & Evidence for Examiner |
|---|---|---|---|
| **MVC Controller Architecture** | Jakarta Servlets 6.0 acting as controllers coordinating HTTP requests and JSP views. | `com.caresync.servlet.*`, `web.xml` | Show servlet annotations (`@WebServlet`) or `web.xml` mapping; demonstrate clean parameter extraction and forwarding via `RequestDispatcher`. |
| **Interceptor Filter Pipeline** | Global filters for character encoding (UTF-8), security headers, authentication, and RBAC. | `CharacterEncodingFilter`, `AuthenticationFilter`, `AuthorizationFilter` | Attempt to access `/admin/dashboard` as a Patient; demonstrate `AuthorizationFilter` blocking the request with HTTP 403. |
| **Hardened Session Lifecycle & Cookies** | Safe session initialization, timeout handling (30 mins), session fixation protection, and cookies configured with `HttpOnly`, `SameSite=Lax`, and `Secure`. | `AuthServlet.java`, `SessionListener.java`, `web.xml` | Inspect browser cookies: show `CARESYNC_SESSIONID` with `HttpOnly`, `SameSite`, and `Secure` flags; demonstrate session invalidation on logout. |
| **CSRF Protection** | Synchronizer token pattern implemented natively without third-party frameworks. | `CsrfFilter.java`, `common/header.jsp` | Show hidden `_csrf` input in booking form; submit form without token via curl or Postman to prove request is rejected with HTTP 403. |
| **JSP 3.1 & Secure View Rendering** | Dynamic JSP templates using JSTL (`c:forEach`, `c:if`, `c:out`) and partial components (`header.jsp`, `sidebar.jsp`, `footer.jsp`). | `/WEB-INF/views/**` | Show that JSPs are hidden behind `/WEB-INF/` preventing direct URL browsing; verify all dynamic content is escaped against XSS. |
