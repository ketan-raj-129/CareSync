# CareSync — UI/UX Design System & Wireframe Architecture

**Document Version:** 1.0.0  
**Design Paradigm:** Modern Healthcare SaaS Platform  
**Target Libraries:** Bootstrap 5.3, Lucide Icons 0.380+, Chart.js 4.4+  
**Author:** UI/UX Architect & Frontend Lead  

---

## 1. Design Philosophy & Vision

CareSync is designed to evoke the clean, approachable, and frictionless aesthetic of a premier digital health platform (comparable to modern telehealth and clinic-tech software like One Medical, Oscar Health, and Zocdoc). It completely avoids the clunky, gray, dense tabular forms typical of 1990s legacy hospital software.

### Core Principles
- **Clarity over Density:** Generous white space, clear typography hierarchy, and purposeful information chunking.
- **High Affordance & Accessibility:** Clean interactive elements with distinct hover, focus, and disabled states. High contrast ratios (WCAG AA compliant).
- **Consistent Navigation:** Unified sidebar layout for authenticated portals with role-tailored primary actions and quick search.
- **Responsive Fluidity:** Adaptive mobile-first grid supporting desktop monitors, clinical tablets, and mobile smartphones.

---

## 2. Design Tokens & Styling Guide

### 2.1 Mandatory Color Palette

CareSync strictly adheres to the approved brand design tokens:

| Token Name | Hex Code | Semantic Role | Usage Context |
|---|---|---|---|
| **Primary Teal** | `#176E65` | Brand Primary | Primary CTA buttons, active sidebar pills, brand logo, key headings, active focus borders. |
| **Soft Teal** | `#E9F3F2` | Tint & Surface Fill | Selected table rows, metric card icons background, active nav item backgrounds, badge backgrounds. |
| **Background** | `#F7F9FA` | Canvas Background | Main viewport page background, off-canvas areas, modal backdrop accents. |
| **Text Dark** | `#243342` | Primary Typography | Headers (H1-H6), body copy, table text, input labels, prominent metrics. |
| **Surface** | `#FFFFFF` | Card & Form Surfaces | Card containers, modal sheets, dropdown menus, table bodies, navigation headers. |

#### Semantic Accent Palette

| State | Primary Hex | Soft Fill Hex | Usage |
|---|---|---|---|
| **Success** | `#10B981` | `#ECFDF5` | Status: `COMPLETED`, `ACTIVE`; positive alerts; saved indicators. |
| **Warning** | `#F59E0B` | `#FEF3C7` | Status: `PENDING`; upcoming appointment reminders; slot limits. |
| **Danger** | `#EF4444` | `#FEE2E2` | Status: `CANCELLED`, `SUSPENDED`; destructive action buttons. |
| **Info / Scheduled** | `#0284C7` | `#E0F2FE` | Status: `CONFIRMED`; informational callouts; patient records. |

### 2.2 Typography Hierarchy
- **Font Family:** `Inter`, `-apple-system`, `BlinkMacSystemFont`, `"Segoe UI"`, `Roboto`, `sans-serif`.
- **Display / H1:** 28px (1.75rem), Bold (700), Line-height: 1.2
- **H2 / Section Title:** 22px (1.375rem), SemiBold (600), Line-height: 1.3
- **H3 / Card Header:** 18px (1.125rem), SemiBold (600), Line-height: 1.4
- **Body Regular:** 14px (0.875rem), Regular (400), Line-height: 1.5, Color: `#243342`
- **Caption / Meta:** 12px (0.75rem), Medium (500), Color: `#6B7280`

### 2.3 Reusable Component Library
1. **Cards:** Minimal border (`1px solid #E5E7EB`), `border-radius: 12px`, soft box shadow (`0 1px 3px rgba(0,0,0,0.05)`), background `#FFFFFF`.
2. **Buttons:**
   - `.btn-primary`: Background `#176E65`, Text `#FFFFFF`, `border-radius: 8px`, hover background `#125650`.
   - `.btn-soft`: Background `#E9F3F2`, Text `#176E65`, border none, hover background `#D7E9E7`.
   - `.btn-outline-primary`: Border `1px solid #176E65`, Color `#176E65`, hover background `#176E65`, Color `#FFFFFF`.
3. **Status Badges:** Rounded pills (`border-radius: 50rem`), padding `4px 10px`, font size `12px`, bold typography with soft background tint matching status.
4. **Icons:** Lucide SVG icons rendered at 18px (nav/inline) or 24px (card headers) with consistent 2px stroke width.

---

## 3. Global Navigation & Layout Architecture

```
+---------------------------------------------------------------------------------------------------+
|  [Logo] CareSync          [Search Bar / Quick Actions]         (Notifications 🔔) (User Menu 👤)  |
+-------------------+-------------------------------------------------------------------------------+
|                   |                                                                               |
|  NAVIGATION       |  PAGE HEADER (Breadcrumbs, Page Title, Action CTA)                            |
|  - Dashboard      |                                                                               |
|  - Appointments   |  METRIC CARDS ROW                                                             |
|  - Records        |  +----------------+ +----------------+ +----------------+ +----------------+  |
|  - Prescriptions  |  | Metric 1       | | Metric 2       | | Metric 3       | | Metric 4       |  |
|  - Availability   |  +----------------+ +----------------+ +----------------+ +----------------+  |
|  - Settings       |                                                                               |
|                   |  MAIN CONTENT AREA (Data Tables / Visual Charts / Interactive Forms)          |
|                   |                                                                               |
|  (User Profile)   |                                                                               |
|  [Sign Out]       |                                                                               |
+-------------------+-------------------------------------------------------------------------------+
```

---

## 4. Required Interfaces & Screen Specifications

### 4.1 Public & Authentication Views

| Page | Route | Key Components & Features |
|---|---|---|
| **Landing Page** | `/` | Hero section with tagline "Healthcare, connected.", Quick doctor search bar by specialty, Value proposition cards, Call to action buttons ("Book Consultation", "Doctor Portal"). |
| **Login** | `/login` | Centered minimalist card on `#F7F9FA` background; Email and Password fields; "Remember Email" toggle; Role-aware redirection; Link to registration. |
| **Patient Registration** | `/register` | 2-column clean card; personal info (name, DOB, gender, blood group), contact details, credentials with client-side password strength meter. |
| **Doctor Portal Welcome** | `/doctor/login` | Dedicated healthcare provider gateway with instructions for newly credentialed physicians to activate accounts. |

### 4.2 Patient Portal Views

```
+--------------------------------------------------------------------------------------------------+
| Patient Portal > Dashboard                                                                       |
+--------------------------------------------------------------------------------------------------+
| [Quick Action: Book New Appointment]                                                             |
|                                                                                                  |
| +------------------------------------+  +------------------------------------------------------+ |
| | Next Upcoming Appointment          |  | Health Summary                                       | |
| | Dr. Sarah Jenkins (Cardiology)     |  | - Active Prescriptions: 2                            | |
| | Nov 15, 2026 - 10:30 AM (Room 304) |  | - Recent Consultations: 4                            | |
| | Status: [CONFIRMED]                |  | - Blood Group: O+                                    | |
| | [Reschedule] [Cancel]              |  |                                                      | |
| +------------------------------------+  +------------------------------------------------------+ |
|                                                                                                  |
| Recent Prescriptions & Clinical Documents                                                        |
| +----------------------------------------------------------------------------------------------+ |
| | Date       | Doctor               | Specialty       | Diagnosis            | Actions         | |
| |------------|----------------------|-----------------|----------------------|-----------------| |
| | 2026-10-20 | Dr. Jenkins          | Cardiology      | Mild Hypertension    | [View] [Print]  | |
| | 2026-08-12 | Dr. Vance            | Dermatology     | Contact Dermatitis   | [View] [Print]  | |
| +----------------------------------------------------------------------------------------------+ |
+--------------------------------------------------------------------------------------------------+
```

- **Find Doctors (`/patient/doctors`):** Multi-filter interface (specialty badge filters, fee range slider, day of week pills, doctor search bar); renders grid of Doctor Cards with rating stars, fees, and "Book Slot" buttons.
- **Book Appointment (`/patient/book`):** Step 1: Select Doctor & View Bio; Step 2: Interactive Date Picker; Step 3: Available 30-minute time slot grid (soft-teal interactive buttons; disabled slots grayed out); Step 4: Symptoms input & confirmation modal.
- **Medical History (`/patient/records`):** Chronological timeline of clinical encounters with expandable accordion details showing symptoms, diagnoses, doctor recommendations, and linked prescriptions.
- **Prescription View (`/patient/prescriptions`):** Clean, printable prescription layout complete with clinic letterhead, doctor license number, medication tables (dosage, frequency, duration), and lifestyle instructions.

### 4.3 Doctor Portal Views

- **Doctor Dashboard (`/doctor/dashboard`):** Today's appointments schedule list; key metrics (Today's Patients, Completed Consultations, Pending Requests, Average Rating); quick toggle for "Taking Consultations Today".
- **Availability Management (`/doctor/availability`):** Weekly calendar grid where doctors configure active days, shift hours (start/end), and slot intervals (15, 20, 30, 60 mins).
- **Appointments Manager (`/doctor/appointments`):** Filter by Date / Status; Actions: "Approve Request", "Decline", "Start Consultation", "Mark No-Show".
- **Consultation Workspace (`/doctor/consultation?aptId=X`):** Split-view workspace:
  - *Left Panel:* Patient demographics, age, gender, blood group, allergies, past visit history.
  - *Right Panel:* Clinical form for Symptoms, Physical Exam, Diagnosis, and dynamic Prescription Builder (add/remove medication line items with autocomplete).
- **Patient History Review (`/doctor/patient-history?patientId=X`):** Read-only historical clinical record viewer accessible only during active care relationships.

### 4.4 Administrator Portal Views

```
+--------------------------------------------------------------------------------------------------+
| Admin Portal > Executive Dashboard                                                               |
+--------------------------------------------------------------------------------------------------+
| [Total Patients: 1,420]  [Active Doctors: 38]  [Today's Appointments: 64]  [Active Specialties: 12]  |
|                                                                                                  |
| +------------------------------------------+  +------------------------------------------------+ |
| | Weekly Appointment Volume (Chart.js)     |  | Appointments by Specialty (Doughnut)           | |
| | [Bar Chart: Mon-Sun bookings vs comp]    |  | [Cardiology 35%, Derm 25%, Peds 20%, Other]   | |
| +------------------------------------------+  +------------------------------------------------+ |
|                                                                                                  |
| Recent System Activity Audit Trail                                                               |
| +----------------------------------------------------------------------------------------------+ |
| | Timestamp         | User / Actor         | Action                   | Target       | Status  | |
| |-------------------|----------------------|--------------------------|--------------|---------| |
| | 2026-11-10 14:15  | admin@caresync.com   | DOCTOR_PROVISIONED       | Dr. Sarah J. | SUCCESS | |
| | 2026-11-10 13:50  | patient_42@test.com  | APPOINTMENT_CANCELLED    | Apt #1042    | SUCCESS | |
| +----------------------------------------------------------------------------------------------+ |
+--------------------------------------------------------------------------------------------------+
```

- **Doctor Management (`/admin/doctors`):** Master table with search, specialty filters, status badges; "Add New Doctor" modal form with license verification; Doctor deactivation toggle with confirmation modal.
- **Patient Management (`/admin/patients`):** Searchable patient directory, account status toggles (`ACTIVE` / `SUSPENDED`).
- **Specialty Taxonomy (`/admin/specialties`):** CRUD table for medical disciplines with Lucide icon assignment and status toggles.
- **System Settings (`/admin/settings`):** Key-value configuration interface for operational parameters (advance booking window, cancellation limits, clinic contact details).
- **Audit Logs (`/admin/audit-logs`):** Security audit log viewer with date pickers, actor filter, event type filter, and exportable data view.

---

## 5. Frontend Technology Integration

### 5.1 Lucide Icons Integration
Lucide icons are loaded via CDN or local JS bundle. Reusable template helper:
```html
<!-- Example Doctor Card with Lucide Icons -->
<div class="card h-100 shadow-sm">
  <div class="card-body">
    <div class="d-flex align-items-center mb-3">
      <div class="icon-shape bg-soft-teal text-primary-teal rounded-circle p-3 me-3">
        <i data-lucide="stethoscope"></i>
      </div>
      <div>
        <h5 class="card-title mb-0">Dr. Sarah Jenkins</h5>
        <span class="text-muted small">Cardiologist - 12 Yrs Exp</span>
      </div>
    </div>
    <div class="d-flex justify-content-between align-items-center mt-3">
      <span class="badge bg-soft-teal text-primary-teal"><i data-lucide="star" class="me-1"></i> 4.9 (128)</span>
      <span class="fw-bold text-dark">$75.00</span>
    </div>
  </div>
</div>
```

### 5.2 Chart.js Dashboards
Chart.js 4.x renders interactive analytics on the Admin and Doctor dashboards using the CareSync color palette:
- **Weekly Appointments Chart:** Line/Bar chart with Primary Teal (`#176E65`) bars and Soft Teal hover fills.
- **Specialty Breakdown:** Doughnut chart utilizing Primary Teal, Soft Teal, and harmonious secondary palette colors.

