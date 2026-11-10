# CareSync — Phase 0 Executive Summary

**Project:** CareSync — Online Healthcare Management System  
**Phase:** Phase 0 (Requirements Analysis & System Design — Refined)  
**Status:** Completed & Ready for Review  
**Date:** November 2026  

---

## 1. Documentation Deliverables Created & Refined

During Phase 0, the complete architectural blueprint was authored across 11 detailed documentation files:

| File Path | Description | Key Contents |
|---|---|---|
| `docs/SRS.md` | Software Requirements Specification | 25+ Functional Requirements (FR IDs), NFRs, 10 Business Rules (BR-001 to BR-010), Traceability Matrix, Range-based Concurrency & Booking Sequence. |
| `docs/architecture.md` | Application Architecture Specification | Layered MVC + Service + DAO design, package structures (`com.caresync.*`), request lifecycle, HikariCP scale-to-zero pool, session management, RBAC filter pipeline, and physical project tree. |
| `docs/database-design.md`| Relational Database Specification | 14 normalized tables for PostgreSQL 16 on Neon Cloud (`caresync_db`, role `caresync_app`), data types, constraints, GiST range exclusion constraints, indexes, and double-booking prevention. |
| `docs/erd.md` | Entity-Relationship Diagram | Mermaid ER diagram detailing 14 tables including `DOCTOR_AVAILABILITY_EXCEPTIONS`, primary/foreign keys, and referential integrity (`ON DELETE RESTRICT`). |
| `docs/use-cases.md` | Use Case Analysis | Mermaid Use Case model, 15 comprehensive use cases (Actors, Preconditions, Main Flow, Alternate Flows, Exception Flows, Postconditions). |
| `docs/class-diagram.md`| Java OOP & Class Design | Meaningful inheritance hierarchy (`BaseModel` -> `User` -> `Admin`, `Doctor`, `Patient`), composition, interfaces, generics, custom exception hierarchy, and Mermaid Class Diagram. |
| `docs/security-design.md`| Authentication & Security Architecture | PBKDF2WithHmacSHA256 hashing (600,000 iterations baseline, unique random salt, transparent auto-upgrade), cookie hardening (`HttpOnly`, `SameSite=Lax`, `Secure`), synchronous transactional audit logging, and append-only privilege separation. |
| `docs/ui-ux-design.md` | Modern UI/UX Design System | SaaS philosophy, design tokens (Primary Teal `#176E65`, Soft Teal `#E9F3F2`, Background `#F7F9FA`, Text `#243342`, Surface `#FFFFFF`), wireframe layouts, Lucide icons, Chart.js. |
| `docs/rubric-mapping.md`| University Rubric Mapping | Comprehensive mapping targeting all 33 marks across Problem Understanding (8), Core Java (10), JDBC Integration (8), and Servlets/Web Integration (7). |
| `docs/testing.md` | Testing Strategy & Test Suites | Test pyramid, 600k PBKDF2 tests, availability exception tests, concurrent double-booking range tests, synchronous audit tests, and security tests. |
| `docs/implementation-roadmap.md` | Implementation Roadmap | 10-phase incremental roadmap (Phase 0 to Phase 9) with deliverables, tasks, and acceptance criteria. |
| `README.md` | Project Overview & Documentation Portal | Master project documentation, architecture diagrams, tech stack summary, setup instructions. |

---

## 2. Technical Corrections Applied in Phase 0 Refinement

1. **Appointment Concurrency & Double Booking:**
   - Evaluated fixed slot vs. range-based protection and implemented a collaborative two-tier defense: service-layer 30-minute grid alignment validation paired with a PostgreSQL range-based exclusion constraint (`ex_no_overlapping_doctor_appointments` using `tsrange` and `btree_gist`).
   - Guarantees at the storage engine level that overlapping active appointments (`PENDING`, `CONFIRMED`) are physically prohibited, raising SQLState `23P01` which services map to `SlotConflictException`.
2. **Password Security:**
   - Upgraded PBKDF2WithHmacSHA256 from 65,536 iterations to an industry baseline of **600,000 iterations** (OWASP 2024 standard).
   - Documented unique 16-byte cryptographically secure salts (`SecureRandom`), configurable iterations via environment variables, constant-time verification (`MessageDigest.isEqual`), performance budget (~150-250ms), and transparent automatic work factor upgrades upon login.
3. **Doctor Availability Exceptions & Time-Off:**
   - Added dedicated `doctor_availability_exceptions` table supporting full-day leave, partial-day unavailability intervals, conference travel, and clinic-wide holidays (`doctor_id = NULL`).
   - Updated database design, ERD, scheduling workflows, and test cases.
4. **Neon Serverless & HikariCP Configuration:**
   - Configured pool for Neon's scale-to-zero compute: `minimumIdle = 0` (enabling pool to drain and Neon compute to suspend when idle), `maximumPoolSize = 10`, `idleTimeout = 600000ms`, `maxLifetime = 1800000ms`, and `connectionTimeout = 30000ms` (accommodating cold-start wakeups).
   - Eliminated artificial keepalive pings that interfere with scale-to-zero.
5. **Meaningful Java OOP Class Hierarchy:**
   - Structured hierarchy: `BaseModel` -> `User` -> `Admin`, `Doctor`, `Patient`.
   - Demonstrated concrete polymorphic behavior (`getDashboardUrl()`, role-specific permissions `canAccessPatientRecord()`, and domain encapsulation).
6. **Synchronous Transactional Audit Logging & Append-Only Storage:**
   - Replaced asynchronous audit logging with synchronous execution within the caller's JDBC transaction, eliminating risk of silent audit log loss during shutdown or failures.
   - Database role `caresync_app` has `UPDATE`, `DELETE`, and `TRUNCATE` revoked on `audit_logs` (`GRANT SELECT, INSERT`).
   - Cryptographic SHA-256 hash chaining (`log_hash` and `prev_log_hash`) ensures tamper-evidence.
7. **Hardened Cookie Security:**
   - Session cookies configured with `HttpOnly = true`, `SameSite = Lax`, and `Secure = true` in HTTPS production deployments.

---

## 3. Database Schema Overview (14 Tables)

CareSync operates across 14 normalized tables in PostgreSQL 16 on Neon Cloud (`caresync_db` / `caresync_app`):
1. `users` (Credentials, role, status, 600k iteration count)
2. `patient_profiles` (Demographics, blood group, emergency contacts)
3. `doctor_profiles` (Specialty, license number, qualifications, fee, rating)
4. `specialties` (Medical taxonomy & Lucide icons)
5. `doctor_availability` (Weekly recurring schedule rules)
6. `doctor_availability_exceptions` (Doctor leaves, partial time-off, clinic holidays)
7. `appointments` (Scheduled encounters with `tsrange` exclusion constraints)
8. `medical_records` (Clinical consultation notes, symptoms, diagnosis)
9. `prescriptions` (Prescription header records)
10. `prescription_items` (Normalized medication line items)
11. `feedback` (1-to-5 star ratings & reviews)
12. `notifications` (In-app alerts)
13. `system_settings` (Dynamic configuration key-values)
14. `audit_logs` (Append-only security audit trail with hash chaining)

---

## 4. Phase 1 Objectives & Prerequisites

- **Phase 1 Goal:** Set up Maven `pom.xml`, configure Tomcat 10.1 `web.xml`, assemble directory structure, integrate Bootstrap 5.3 / Lucide assets, and construct reusable JSP layout skeletons.
- **Prerequisites:** User review and explicit approval of Phase 0 deliverables.
- **Action Required:** Awaiting user instruction to begin Phase 1.
