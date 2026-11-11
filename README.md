# CareSync — Online Healthcare Management System

> **Healthcare, connected.**

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Jakarta EE](https://img.shields.io/badge/Jakarta%20EE-10-blue.svg)](https://jakarta.ee/)
[![Tomcat](https://img.shields.io/badge/Apache%20Tomcat-10.1-yellow.svg)](https://tomcat.apache.org/)
[![Database](https://img.shields.io/badge/PostgreSQL-Neon%20Cloud-336791.svg)](https://neon.tech/)
[![Frontend](https://img.shields.io/badge/Bootstrap-5.3-7952B3.svg)](https://getbootstrap.com/)
[![Status](https://img.shields.io/badge/Phase%206-Admin%20Portal%20Completed-10B981.svg)](#)

CareSync is a modern, web-based healthcare management SaaS application designed for university-level advanced Java web development evaluation and real-world clinical operations. It connects patients, medical practitioners (doctors), and clinic administrators within a secure, high-performance digital environment.

---

## 1. Project Overview

CareSync streamlines and digitizes outpatient clinical workflows:
- **Patients:** Register accounts, search and filter doctors by specialty and fee, book conflict-free 30-minute consultation slots, access chronological medical records, and download formatted prescriptions.
- **Doctors:** Define recurring weekly practice schedules, manage time-off exceptions, accept or decline appointment requests, conduct clinical consultations, author diagnostic notes, issue structured digital prescriptions, and review patient history under strict care relationship boundaries.
- **Administrators:** Centralized user account governance, doctor credentialing and specialty taxonomy management, clinic-wide appointment tracking, system settings configuration, and tamper-evident audit logs.

---

## 2. Mandatory Technology Stack

| Layer | Technology | Specification / Version |
|---|---|---|
| **Runtime Environment** | Java | OpenJDK 21 (LTS) |
| **Web Container** | Apache Tomcat | Version 10.1.x (Jakarta EE 10 specifications) |
| **Server-Side API** | Jakarta Servlets & JSP | Servlet 6.0, JSP 3.1, JSTL 3.0 |
| **Persistence Engine** | Pure JDBC | `PreparedStatement`, Explicit ACID Transactions (Phase 2) |
| **Connection Pooling** | HikariCP | High-performance JDBC connection pool (Phase 2) |
| **Database** | PostgreSQL 16 | Hosted on **Neon Cloud** (`caresync_db`, role: `caresync_app`) |
| **Build & Dependency Tool** | Apache Maven | Standard multi-phase build (`.war` packaging) |
| **Frontend Framework** | HTML5 / CSS3 / JavaScript | Custom CSS Tokens, Bootstrap 5.3, Lucide Icons |
| **Architecture** | Layered MVC | Browser -> Servlets -> Services -> DAOs -> JDBC -> Neon PostgreSQL |

> [!IMPORTANT]
> **Strict Framework Restrictions:** CareSync is built using **pure Java, Jakarta Servlets, JSP, JDBC, and PostgreSQL**. No Spring Boot, Spring Framework, Hibernate, JPA, or third-party ORMs are utilized, ensuring complete compliance with core academic rubrics.

---

## 3. Architecture & Design Highlights

CareSync implements a decoupled **MVC + Service + DAO** architectural pattern:
- **MVC Controllers:** Jakarta Servlets manage request routing, parameter parsing into DTOs, and view dispatching.
- **Service Tier:** Encapsulates business logic, double-booking prevention, concurrency locks, and multi-table ACID transactions.
- **DAO Tier:** Encapsulates pure JDBC with 100% parameterized `PreparedStatement` queries and zero SQL injection risk.
- **HikariCP Connection Pool:** Optimized for Neon Cloud serverless connection quotas (`minimumIdle = 0` for scale-to-zero) and TLS encryption.
- **Security Interceptor Filters:** Global filter pipeline enforcing UTF-8 encoding, security headers (CSP, HSTS), session authentication, role-based access control (RBAC), and CSRF token validation.
- **Tamper-Evident Auditing:** Comprehensive synchronous transactional `audit_logs` tracking with database-level append-only privileges (`REVOKE UPDATE, DELETE`) and SHA-256 hash chaining.

---

## 4. Design Tokens & UI Aesthetics

CareSync is designed with a modern, approachable digital health SaaS aesthetic:

- **Primary Teal:** `#176E65` (Brand primary, CTA buttons, active navigation, key headings)
- **Soft Teal:** `#E9F3F2` (Soft surface fills, badges, selected table rows)
- **Background:** `#F7F9FA` (Main viewport background)
- **Text Dark:** `#243342` (High-contrast typography)
- **Surface:** `#FFFFFF` (Card surfaces, modals, data tables)

---

## 5. Project Directory Structure

```text
CareSync/
│
├── pom.xml                               # Maven build descriptor (WAR, Java 21, Jakarta EE 10)
├── README.md                             # Project documentation and deployment guide
├── .gitignore                            # Secret and build artifact exclusions
├── .env.example                          # Environment configuration template
│
├── database/
│   ├── migrations/                       # DDL schema migrations (Phase 2)
│   └── seeds/                            # Sample specialty and practitioner seed data (Phase 2)
│
├── docs/                                 # Phase 0 Architecture and Design Specifications
│   ├── SRS.md                            # Software Requirements Specification
│   ├── architecture.md                   # System Architecture Specification
│   ├── database-design.md                # Relational Database Design (14 tables)
│   ├── erd.md                            # Entity-Relationship Diagram (Mermaid)
│   ├── use-cases.md                      # Detailed Use Cases
│   ├── class-diagram.md                  # Java OOP Class Diagram
│   ├── security-design.md                # Security and Cryptography Design
│   ├── ui-ux-design.md                   # UI/UX Wireframes & Guidelines
│   ├── rubric-mapping.md                 # University Evaluation Rubric Mapping
│   ├── testing.md                        # Quality Assurance & Testing Plan
│   ├── implementation-roadmap.md         # 10-Phase Implementation Roadmap
│   └── phase-0-summary.md                # Phase 0 Sign-Off Summary
│
├── src/
│   ├── main/
│   │   ├── java/com/caresync/
│   │   │   ├── config/                   # Configuration & singletons (Phase 2)
│   │   │   ├── model/                    # Domain models & entities (Phase 2)
│   │   │   ├── dao/                      # JDBC Data Access Objects (Phase 2)
│   │   │   ├── service/                  # Business logic services (Phase 3)
│   │   │   ├── servlet/                  # Jakarta Servlets (HomeServlet)
│   │   │   ├── filter/                   # Interceptor filters (CharacterEncodingFilter)
│   │   │   ├── exception/                # Custom exception hierarchy
│   │   │   ├── dto/                      # Data Transfer Objects
│   │   │   ├── util/                     # Pure utilities
│   │   │   └── listener/                 # Context & lifecycle listeners
│   │   │
│   │   └── webapp/
│   │       ├── assets/
│   │       │   ├── css/
│   │       │   │   ├── variables.css     # Design tokens & color variables
│   │       │   │   ├── base.css          # Typography & global resets
│   │       │   │   ├── components.css    # Buttons, cards, badges, alerts, empty states
│   │       │   │   ├── layout.css        # Navbar, dashboard sidebar, grid layouts
│   │       │   │   └── caresync.css      # Master import & landing page styles
│   │       │   ├── js/
│   │       │   │   ├── main.js           # Sidebar toggle, password toggle, alerts, Lucide
│   │       │   │   └── components.js     # Toast & component helpers
│   │       │   └── images/               # Graphic assets
│   │       │
│   │       └── WEB-INF/
│   │           ├── web.xml               # Jakarta Servlet 6.0 deployment descriptor
│   │           └── views/
│   │               ├── public/           # home.jsp (Landing Page)
│   │               ├── auth/             # login.jsp, register-patient.jsp, register-doctor.jsp
│   │               ├── admin/            # dashboard.jsp (Admin Shell)
│   │               ├── doctor/           # dashboard.jsp (Doctor Shell)
│   │               ├── patient/          # dashboard.jsp (Patient Shell)
│   │               └── common/           # header.jsp, footer.jsp, navbar.jsp, sidebar.jsp, topbar.jsp, alerts.jsp, 404.jsp, 500.jsp
│   │
│   └── test/
│       └── java/com/caresync/            # JUnit 5 Jupiter foundation tests
│
└── target/                               # Compiled WAR and test classes
```

---

## 6. Build & Setup Instructions

### 6.1 Prerequisites
Ensure that the following runtime environments are installed on your workstation:
- **Java 21 (LTS):** Verify by running `java -version`.
- **Apache Maven 3.9+:** Verify by running `mvn -version`.
- **Apache Tomcat 10.1+:** Required for running the `.war` application archive.

```bash
# Verify Java 21 LTS
java -version

# Verify Maven
mvn -version
```

### 6.2 Building the Project
From the root directory of the CareSync repository:

```bash
# 1. Clean and execute test suite
mvn clean test

# 2. Package the application WAR artifact
mvn clean package
```

Upon a successful build, the deployable Web Application Archive will be produced at:
```text
target/caresync.war
```

---

## 7. Apache Tomcat 10.1 Deployment Instructions

### Option A: Standalone Tomcat 10.1 Installation (Recommended)

1. **Download and Extract Apache Tomcat 10.1:**
   ```bash
   # Download Apache Tomcat 10.1 binary tarball
   wget https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.34/bin/apache-tomcat-10.1.34.tar.gz -O /tmp/apache-tomcat.tar.gz

   # Extract to user directory or /opt
   tar -xzf /tmp/apache-tomcat.tar.gz -C ~/
   export CATALINA_HOME=~/apache-tomcat-10.1.34
   ```

2. **Deploy the CareSync WAR:**
   ```bash
   # Copy the generated WAR artifact to Tomcat's webapps directory
   cp target/caresync.war "$CATALINA_HOME/webapps/"
   ```

3. **Start Tomcat:**
   ```bash
   "$CATALINA_HOME/bin/startup.sh"
   ```

4. **Verify Application in Browser:**
   Open your browser and navigate to:
   ```text
   http://localhost:8080/caresync/
   ```

5. **Stop Tomcat:**
   ```bash
   "$CATALINA_HOME/bin/shutdown.sh"
   ```

### Option B: Ubuntu APT Package (If already installed)

If Tomcat 10 is installed as a system service:
```bash
sudo cp target/caresync.war /var/lib/tomcat10/webapps/
sudo systemctl restart tomcat10
```

---

## 8. Available Phase 1 Routes & Previews

The application is accessible locally through the following context-aware endpoints:

| Endpoint | View | Description |
|---|---|---|
| `http://localhost:8080/caresync/` | `home.jsp` | Public Healthcare SaaS Landing Page with Hero, Feature Grid, and How It Works. |
| `http://localhost:8080/caresync/login` | `login.jsp` | Sign In interface with password visibility toggle and role portal previews. |
| `http://localhost:8080/caresync/register-patient` | `register-patient.jsp` | Patient registration layout with contact and account credentials. |
| `http://localhost:8080/caresync/register-doctor` | `register-doctor.jsp` | Healthcare provider application form with specialty and licensing inputs. |
| `http://localhost:8080/caresync/admin/dashboard` | `admin/dashboard.jsp` | Administrator dashboard shell with stat cards, sidebar, and audit queue. |
| `http://localhost:8080/caresync/doctor/dashboard` | `doctor/dashboard.jsp` | Doctor practice dashboard shell with schedule queue and empty states. |
| `http://localhost:8080/caresync/patient/dashboard` | `patient/dashboard.jsp` | Patient portal shell with upcoming visits and prescription history shell. |

---

## 9. Implementation Roadmap

- **Phase 0:** Requirements Analysis & System Design *(COMPLETED)*
- **Phase 1:** Maven Project Foundation, Tomcat Configuration & UI Skeleton *(COMPLETED)*
- **Phase 2:** Neon PostgreSQL Integration, HikariCP & JDBC DAO Foundation *(COMPLETED)*
- **Phase 3:** Authentication, PBKDF2 Hashing, Sessions & Security Filters *(COMPLETED)*
- **Phase 4:** Patient Portal, Doctor Search & Appointment Booking Engine *(COMPLETED)*
- **Phase 5:** Doctor Portal, Availability Manager, Consultations & Prescriptions *(COMPLETED)*
- **Phase 6:** Admin Portal, Doctor Credentialing, System Governance & Audit Trail *(COMPLETED)*
- **Phase 7:** Advanced Core Java Concepts, Concurrency & Scheduled Tasks
- **Phase 8:** Analytics, Chart.js Dashboards & Real-Time Alerts
- **Phase 9:** Comprehensive QA, Cross-Browser Testing & Final Submission
