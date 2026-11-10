# CareSync — Phase 3: Authentication, Session Management, RBAC & Security Engine Report

**Document Version:** 1.0.0  
**Phase:** 3 of 9  
**Status:** Completed & Verified  
**Runtime:** Java 21 LTS | Apache Maven 3.9.9 | Jakarta EE 10 (Servlet 6.0, JSP 3.1, JSTL 3.0)  
**Security Baseline:** PBKDF2-HMAC-SHA256 @ 600,000 iterations, 16-byte salt, 256-bit derived key  
**Architecture:** Layered MVC + Service + DAO (Pure Java, pure JDBC, Zero Spring/Hibernate)  

---

## 1. Executive Summary

Phase 3 of **CareSync — Healthcare, connected.** has been completed. The authentication architecture, PBKDF2 cryptographic password hashing engine, session lifecycle management with session fixation protection, Synchronizer Token CSRF defense, role-based access control (RBAC) interceptor filters, centralized security HTTP headers, and real controller servlets (`LoginServlet`, `RegisterServlet`, `LogoutServlet`) have been designed, implemented, and verified.

All 36 automated unit and security tests run cleanly offline with **0 failures, 0 errors, and 0 skipped**, and Maven successfully builds the deployable `target/caresync.war` (5.0MB).

---

## 2. Deliverables & Implemented Architecture

### 2.1 Password Security — PBKDF2-HMAC-SHA256 (`com.caresync.util.PasswordHasher`)

The dedicated password hashing engine strictly fulfills the production security baseline:

| Security Parameter | Production Value | Verification |
| :--- | :--- | :--- |
| **Algorithm** | `PBKDF2WithHmacSHA256` | Declared in `AppConstants.PBKDF2_ALGORITHM` |
| **Work Factor (Iterations)** | **600,000** | Enforced by default; configurable via test constructor for fast testing |
| **Salt Length** | **16 bytes (128 bits)** | Generated via `SecureRandom` per password; 32 hex characters |
| **Derived Key Length** | **256 bits (32 bytes)** | Derived via `PBEKeySpec`; 64 hex characters |
| **Comparison** | **Constant-time** | `MessageDigest.isEqual(...)` prevents timing side-channel attacks |
| **Rehashing Detection** | `needsRehash(storedIterations)` | Upgrades credentials on successful login when stored work factor < 600,000 |
| **Memory Sanitation** | Immediate clearing | Temporary `char[]` and `PBEKeySpec` passwords cleared with `Arrays.fill(..., '\0')` |
| **Credential Logging** | Strictly prohibited | Passwords, salts, and derived hashes are never output to logs |

### 2.2 Domain DTOs & Session Principal (`com.caresync.dto`)

- **`SessionPrincipal`**:
  - Encapsulates minimal authenticated user identity in the session: `userId`, `email`, `role`, `firstName`, `lastName`, and `displayName`.
  - Implements `Serializable` with `serialVersionUID = 1L`.
  - Role helper accessors: `isAdmin()`, `isDoctor()`, `isPatient()`, `getDashboardUrl()`.
  - **Zero credential retention**: Contains no passwords, hashes, salts, or JDBC connections.
- **`AuthenticationResult`**:
  - Implements authentication state encapsulation (`isSuccess()`, `getPrincipal()`, `getErrorMessage()`, `isRehashNeeded()`).
- **`PatientRegistrationDto`**:
  - Encapsulates patient self-registration form fields.

### 2.3 Service Layer (`com.caresync.service`)

- **`AuthService` & `AuthServiceImpl`**:
  - **Login Workflow**:
    1. Validates input parameters.
    2. Queries account by email using `UserDao.findByEmail(normalizedEmail)`.
    3. Returns identical generic error message (`"Invalid email or password."`) on user-not-found or bad password to prevent account enumeration.
    4. Enforces account status check: Inactive or suspended accounts are rejected with clear administrative guidance.
    5. Verifies candidate password with `PasswordHasher.verifyPassword(...)` using constant-time equality.
    6. Automatically detects if stored iterations are below baseline (< 600,000) and triggers an atomic background rehash.
    7. Clears character array memory immediately after verification.
  - **Public Patient Registration Workflow**:
    1. **Strict RBAC Rule**: Public registration strictly forces `Role.PATIENT`. Any attempts to submit or request `ADMIN` or `DOCTOR` roles from client requests are ignored.
    2. Enforces input validation (name presence, valid email format, password minimum 8 characters, mandatory consent to terms).
    3. Verifies email uniqueness via `UserDao.existsByEmail(...)`.
    4. Hashes password using PBKDF2-HMAC-SHA256 at baseline work factor.
    5. **Atomic Multi-Table Transaction**: Obtains database `Connection`, disables auto-commit, inserts `users` record, inserts `patient_profiles` record referencing the generated `user_id`, records `audit_logs` event, and commits. Rolls back both tables if any operation fails.

### 2.4 Session Management & Session Fixation Defense

- **Session Fixation Prevention**: Upon successful authentication, `LoginServlet` invokes `request.changeSessionId()` to rotate the session identifier while preserving session attributes.
- **Session Cookie Configuration (`web.xml`)**:
  - `<name>CARESYNC_SESSIONID</name>`
  - `<http-only>true</http-only>`
  - `<session-timeout>30</session-timeout>` (30 minutes of inactivity)
  - `<tracking-mode>COOKIE</tracking-mode>` (URL rewriting disabled; session identifiers never exposed in query parameters)
  - `<secure>false</secure>` in local development for HTTP localhost testing, ready to switch to `true` behind HTTPS reverse proxies.

### 2.5 Synchronizer Token CSRF Protection (`com.caresync.filter.CsrfFilter`)

- Generates 32-byte cryptographically secure random token (`SecurityUtils.generateRandomToken()`) per session.
- Automatically supplies `csrfToken` attribute to request scope for JSP views.
- Intercepts state-changing HTTP methods (`POST`, `PUT`, `PATCH`, `DELETE`).
- Compares submitted token (from form parameter `csrfToken` or header `X-CSRF-TOKEN`) with session token using constant-time `MessageDigest.isEqual`.
- Rejects missing or invalid tokens with `HTTP 403 Forbidden`.
- Rotates CSRF token upon successful authentication and logout.

### 2.6 Role-Based Access Control (RBAC) & Security Filters

- **`AuthenticationFilter`**:
  - Protects `/admin/*`, `/doctor/*`, and `/patient/*` routes.
  - Redirects unauthenticated users to `/login?redirect={encodedTarget}`.
- **`AuthorizationFilter`**:
  - Enforces strict role boundaries:
    - `/admin/*` $\to$ Requires `Role.ADMIN`
    - `/doctor/*` $\to$ Requires `Role.DOCTOR`
    - `/patient/*` $\to$ Requires `Role.PATIENT`
  - Rejects cross-role access attempts with `HTTP 403 Forbidden` and logs security warning.
- **`SecurityHeadersFilter`**:
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY`
  - `Referrer-Policy: strict-origin-when-cross-origin`
  - `Content-Security-Policy`: Restricts scripts, styles, fonts, and prohibits framing (`frame-ancestors 'none'`).
  - Sensitive route caching headers: `Cache-Control: no-cache, no-store, must-revalidate`, `Pragma: no-cache`, `Expires: 0`.

### 2.7 Servlets & View Integration

| Servlet | URL Pattern | Methods | Functionality |
| :--- | :--- | :--- | :--- |
| **`LoginServlet`** | `/login` | `GET`, `POST` | Renders sign-in form; authenticates credentials; rotates session ID; redirects to role dashboard. |
| **`RegisterServlet`** | `/register`, `/register-patient` | `GET`, `POST` | Renders patient signup form; validates inputs; executes atomic user + patient profile transaction. |
| **`LogoutServlet`** | `/logout` | `POST` (strictly) | Validates CSRF token; writes audit log; executes `session.invalidate()`; redirects to `/login?loggedOut=true`. Rejects `GET` with HTTP 405. |
| **`HomeServlet`** | `/`, `/home`, etc. | `GET` | Public navigation routing. |

- Updated JSP views:
  - [`login.jsp`](file:///media/bugopsx/A/WORKSHOP/hospital-management-system/src/main/webapp/WEB-INF/views/auth/login.jsp): Real POST action with hidden `csrfToken`, email repopulation, and dismissible alerts.
  - [`register-patient.jsp`](file:///media/bugopsx/A/WORKSHOP/hospital-management-system/src/main/webapp/WEB-INF/views/auth/register-patient.jsp): Complete form fields (DOB, gender, blood group, address, emergency contact) submitting via POST with hidden `csrfToken`.
  - [`topbar.jsp`](file:///media/bugopsx/A/WORKSHOP/hospital-management-system/src/main/webapp/WEB-INF/views/common/topbar.jsp): Displays authenticated user name and avatar initial; sign-out button submits via POST with hidden `csrfToken`.
  - [`navbar.jsp`](file:///media/bugopsx/A/WORKSHOP/hospital-management-system/src/main/webapp/WEB-INF/views/common/navbar.jsp): Dynamic role-aware navigation buttons (Dashboard/Sign Out vs. Sign In/Get Started).
  - [`403.jsp`](file:///media/bugopsx/A/WORKSHOP/hospital-management-system/src/main/webapp/WEB-INF/views/common/403.jsp): Custom error view for access denied / unauthorized access.

---

## 3. Automated Test Results & Verification

All automated tests run completely offline without requiring active Neon Cloud credentials.

```bash
mvn clean test
```

### Test Suite Execution Summary:
- **`AppFoundationTest`** (2 tests): Java 21 LTS runtime verification, package naming.
- **`DatabaseConfigTest`** (3 tests): Security constants, EnvConfig fallback tiers, safe offline database detection.
- **`DaoContractTest`** (5 tests): DAO hierarchy, SlotConflictException, DatabaseOperationException, AuditLog append-only invariants.
- **`DomainModelTest`** (7 tests): OOP models, User/Doctor/Patient inheritance, Feedback 1-5 validation, DoctorAvailability 1-7 validation.
- **`PasswordHasherTest`** (5 tests): 600,000 iteration production baseline, salt uniqueness, password verification, invalid metadata rejection, work factor rehash detection.
- **`AuthServiceTest`** (7 tests): Successful login with SessionPrincipal, bad password generic error, unknown email generic error, suspended account rejection, patient registration strictly enforcing `Role.PATIENT`, duplicate email rejection, registration validation rules.
- **`SecurityEngineTest`** (7 tests): CSRF token missing rejection (403), CSRF token invalid rejection (403), CSRF token valid acceptance, unauthenticated access redirect to `/login`, cross-role access rejection (403), authorized role access acceptance, security headers verification (nosniff, DENY, CSP).

**Grand Total:** 36 tests run, **0 failures, 0 errors, 0 skipped**.  
**Build Status:** `BUILD SUCCESS` (4.4 seconds), producing `target/caresync.war` (5.0MB).

---

## 4. Neon Integration Notes & Future Verification

- **Current Status**: No database migrations or live connections to Neon PostgreSQL were executed during Phase 3. All unit tests operate with in-memory test doubles and mock connections.
- **Live Integration Readiness**: When Neon cloud database credentials (`NEON_DB_URL`, `NEON_DB_USER`, `NEON_DB_PASSWORD`) are populated in `.env`, the existing `AuthServiceImpl`, `UserDaoImpl`, and `PatientDaoImpl` will directly connect to Neon via HikariCP and execute live queries and transactions.

