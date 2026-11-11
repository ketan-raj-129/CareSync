package com.caresync.service;

import com.caresync.dao.*;
import com.caresync.dto.*;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.ValidationException;
import com.caresync.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CareSync Phase 6 Admin Services & Invariant Tests")
class AdminPortalServiceTest {

    private InMemoryUserDao userDao;
    private InMemoryPatientDao patientDao;
    private InMemoryDoctorDao doctorDao;
    private InMemorySpecialtyDao specialtyDao;
    private InMemoryAppointmentDao appointmentDao;
    private InMemoryDoctorAvailabilityExceptionDao exceptionDao;
    private InMemorySystemSettingDao settingDao;
    private InMemoryAuditLogDao auditLogDao;

    private AdminDashboardService dashboardService;
    private AdminPatientService patientService;
    private AdminDoctorService doctorService;
    private AdminSpecialtyService specialtyService;
    private AdminAppointmentService appointmentService;
    private AdminHolidayService holidayService;
    private AdminUserService userService;
    private AdminSettingsService settingsService;
    private AdminAuditLogService auditLogService;

    private static final Long ADMIN_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        userDao = new InMemoryUserDao();
        patientDao = new InMemoryPatientDao();
        doctorDao = new InMemoryDoctorDao();
        specialtyDao = new InMemorySpecialtyDao();
        appointmentDao = new InMemoryAppointmentDao();
        exceptionDao = new InMemoryDoctorAvailabilityExceptionDao();
        settingDao = new InMemorySystemSettingDao();
        auditLogDao = new InMemoryAuditLogDao();

        dashboardService = new AdminDashboardServiceImpl(
                patientDao, doctorDao, appointmentDao, specialtyDao, auditLogDao);
        patientService = new AdminPatientServiceImpl(patientDao, userDao, auditLogDao);
        doctorService = new AdminDoctorServiceImpl(doctorDao, userDao, auditLogDao);
        specialtyService = new AdminSpecialtyServiceImpl(specialtyDao, auditLogDao);
        appointmentService = new AdminAppointmentServiceImpl(appointmentDao, auditLogDao);
        holidayService = new AdminHolidayServiceImpl(exceptionDao, appointmentDao, auditLogDao);
        userService = new AdminUserServiceImpl(userDao, auditLogDao);
        settingsService = new AdminSettingsServiceImpl(settingDao, auditLogDao);
        auditLogService = new AdminAuditLogServiceImpl(auditLogDao);

        // Seed 1 active admin
        User admin = new User(ADMIN_USER_ID);
        admin.setEmail("admin@caresync.com");
        admin.setFirstName("Master");
        admin.setLastName("Admin");
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        userDao.save(admin);
    }

    @Test
    @DisplayName("AdminDashboardService: computes real aggregate statistics accurately")
    void testDashboardMetrics() {
        // Seed 2 patients
        Patient p1 = new Patient(101L);
        p1.setId(2L);
        p1.setFirstName("Alice");
        patientDao.save(p1);

        Patient p2 = new Patient(102L);
        p2.setId(3L);
        p2.setFirstName("Bob");
        patientDao.save(p2);

        // Seed 2 doctors (1 active, 1 inactive)
        User uDoc1 = new User(4L);
        uDoc1.setStatus(UserStatus.ACTIVE);
        userDao.save(uDoc1);

        Doctor doc1 = new Doctor(201L);
        doc1.setDoctorProfileId(201L);
        doc1.setId(4L);
        doc1.setStatus(UserStatus.ACTIVE);
        doc1.setAvailable(true);
        doctorDao.save(doc1);

        User uDoc2 = new User(5L);
        uDoc2.setStatus(UserStatus.INACTIVE);
        userDao.save(uDoc2);

        Doctor doc2 = new Doctor(202L);
        doc2.setDoctorProfileId(202L);
        doc2.setId(5L);
        doc2.setStatus(UserStatus.INACTIVE);
        doc2.setAvailable(false);
        doctorDao.save(doc2);

        // Seed 1 active specialty
        Specialty s1 = new Specialty(301L);
        s1.setName("Cardiology");
        s1.setDescription("Heart Care");
        s1.setIconName("heart");
        s1.setActive(true);
        specialtyDao.save(s1);

        AdminDashboardDto metrics = dashboardService.getDashboardMetrics();
        assertNotNull(metrics);
        assertEquals(2, metrics.getTotalPatients());
        assertEquals(2, metrics.getTotalDoctors());
        assertEquals(1, metrics.getActiveDoctors());
        assertEquals(1, metrics.getPendingDoctorVerifications());
        assertEquals(1, metrics.getActiveSpecialties());
    }

    @Test
    @DisplayName("AdminPatientService: updates patient status and logs audit entry")
    void testPatientStatusUpdate() {
        User u = new User(10L);
        u.setEmail("patient@test.com");
        u.setStatus(UserStatus.ACTIVE);
        userDao.save(u);

        Patient p = new Patient(50L);
        p.setPatientProfileId(50L);
        p.setId(10L);
        patientDao.save(p);

        boolean updated = patientService.updatePatientStatus(50L, UserStatus.SUSPENDED, ADMIN_USER_ID, "127.0.0.1", "Agent");
        assertTrue(updated);

        assertEquals(UserStatus.SUSPENDED, userDao.findById(10L).orElseThrow().getStatus());
        assertTrue(auditLogDao.logs.stream().anyMatch(l -> "PATIENT_STATUS_UPDATED".equals(l.getAction())));
    }

    @Test
    @DisplayName("AdminDoctorService: filters doctors and toggles clinical availability")
    void testDoctorAvailabilityToggle() {
        Doctor doc = new Doctor(300L);
        doc.setDoctorProfileId(300L);
        doc.setId(20L);
        doc.setAvailable(true);
        doctorDao.save(doc);

        boolean toggled = doctorService.toggleDoctorAvailability(300L, false, ADMIN_USER_ID, "127.0.0.1", "Agent");
        assertTrue(toggled);
        assertFalse(doctorDao.findById(300L).orElseThrow().isAvailable());
        assertTrue(auditLogDao.logs.stream().anyMatch(l -> "DOCTOR_AVAILABILITY_TOGGLED".equals(l.getAction())));
    }

    @Test
    @DisplayName("AdminSpecialtyService: validates required fields and prevents duplicate specialty names")
    void testSpecialtyManagement() {
        SpecialtyFormDto dto = new SpecialtyFormDto("Neurology", "Brain Care", "brain", true);
        Specialty created = specialtyService.createSpecialty(dto, ADMIN_USER_ID, "127.0.0.1", "Agent");

        assertNotNull(created);
        assertEquals("Neurology", created.getName());

        // Duplicate name creation must fail
        SpecialtyFormDto dupDto = new SpecialtyFormDto("Neurology", "Brain Care 2", true);
        assertThrows(RuntimeException.class, () ->
                specialtyService.createSpecialty(dupDto, ADMIN_USER_ID, "127.0.0.1", "Agent"));
    }

    @Test
    @DisplayName("AdminAppointmentService: administrative cancellation requires non-empty justification and logs audit record")
    void testAdministrativeAppointmentCancellation() {
        Appointment apt = new Appointment(500L);
        apt.setAppointmentDate(LocalDate.now().plusDays(2));
        apt.setStatus(AppointmentStatus.CONFIRMED);
        appointmentDao.save(apt);

        // Blank reason should fail
        assertThrows(ValidationException.class, () ->
                appointmentService.cancelAppointment(500L, "   ", ADMIN_USER_ID, "127.0.0.1", "Agent"));

        // Valid cancellation
        boolean cancelled = appointmentService.cancelAppointment(
                500L, "Emergency facility maintenance", ADMIN_USER_ID, "127.0.0.1", "Agent");
        assertTrue(cancelled);
        assertEquals(AppointmentStatus.CANCELLED, appointmentDao.findById(500L).orElseThrow().getStatus());
        assertTrue(auditLogDao.logs.stream().anyMatch(l -> "APPOINTMENT_CANCELLED_BY_ADMIN".equals(l.getAction())));

        // Cancelling terminal appointment should fail
        assertThrows(ValidationException.class, () ->
                appointmentService.cancelAppointment(500L, "Again", ADMIN_USER_ID, "127.0.0.1", "Agent"));
    }

    @Test
    @DisplayName("AdminHolidayService: registers clinic-wide holiday with doctor_id IS NULL and checks conflicts")
    void testClinicHolidayManagement() {
        LocalDate holidayDate = LocalDate.now().plusDays(10);

        // Seed an appointment on holiday date to test conflict checker
        Appointment apt = new Appointment(600L);
        apt.setAppointmentDate(holidayDate);
        apt.setStatus(AppointmentStatus.CONFIRMED);
        appointmentDao.save(apt);

        List<Appointment> conflicts = holidayService.getAffectedAppointments(holidayDate);
        assertEquals(1, conflicts.size());

        // Register clinic holiday
        ClinicHolidayRequestDto dto = new ClinicHolidayRequestDto(holidayDate, "National Holiday", false);
        DoctorAvailabilityException holiday = holidayService.addClinicHoliday(dto, ADMIN_USER_ID, "127.0.0.1", "Agent");

        assertNotNull(holiday);
        assertNull(holiday.getDoctorId(), "Clinic holiday must have doctorId IS NULL");
        assertEquals(holidayDate, holiday.getExceptionDate());

        // Delete holiday
        boolean deleted = holidayService.deleteClinicHoliday(holiday.getId(), ADMIN_USER_ID, "127.0.0.1", "Agent");
        assertTrue(deleted);
    }

    @Test
    @DisplayName("AdminUserService: blocks self-demotion and deactivation of the last active administrator")
    void testAdminProtectionInvariants() {
        // Attempting to deactivate the only active admin must fail
        assertThrows(ValidationException.class, () ->
                userService.updateUserStatus(ADMIN_USER_ID, UserStatus.INACTIVE, ADMIN_USER_ID, "127.0.0.1", "Agent"));

        // Attempting self-demotion from ADMIN to PATIENT must fail
        assertThrows(ValidationException.class, () ->
                userService.updateUserRole(ADMIN_USER_ID, Role.PATIENT, ADMIN_USER_ID, "127.0.0.1", "Agent"));

        // Seed a second admin
        User admin2 = new User(2L);
        admin2.setEmail("admin2@caresync.com");
        admin2.setRole(Role.ADMIN);
        admin2.setStatus(UserStatus.ACTIVE);
        userDao.save(admin2);

        // Now deactivating admin2 by admin1 should succeed because admin1 remains active
        boolean updated = userService.updateUserStatus(2L, UserStatus.INACTIVE, ADMIN_USER_ID, "127.0.0.1", "Agent");
        assertTrue(updated);
        assertEquals(UserStatus.INACTIVE, userDao.findById(2L).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("AdminSettingsService: enforces allowlist and rejects unapproved keys")
    void testSettingsAllowlist() {
        // Allowed key should succeed
        boolean updated = settingsService.updateSetting(
                "clinic.name", "CareSync Health Center", ADMIN_USER_ID, "127.0.0.1", "Agent");
        assertTrue(updated);

        // Forbidden/unapproved key must throw ValidationException
        assertThrows(ValidationException.class, () ->
                settingsService.updateSetting("security.bypass.token", "bad-val", ADMIN_USER_ID, "127.0.0.1", "Agent"));
    }

    @Test
    @DisplayName("AdminAuditLogService: paginated read-only retrieval works cleanly")
    void testAuditLogServiceReadOnly() {
        AuditLog log1 = new AuditLog();
        log1.setId(1L);
        log1.setAction("LOGIN_SUCCESS");
        log1.setTargetEntity("USER");
        log1.setUserId(ADMIN_USER_ID);
        auditLogDao.save(log1);

        PaginatedResult<AuditLog> result = auditLogService.getAuditLogs(null, null, null, 1, 10);
        assertEquals(1, result.getTotalCount());
        assertEquals("LOGIN_SUCCESS", result.getItems().get(0).getAction());
    }

    // =========================================================================
    // In-Memory Test DAOs
    // =========================================================================

    static class InMemoryUserDao implements UserDao {
        private final Map<Long, User> store = new HashMap<>();

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return store.values().stream().filter(u -> email.equalsIgnoreCase(u.getEmail())).findFirst();
        }

        @Override
        public boolean existsByEmail(String email) {
            return store.values().stream().anyMatch(u -> email.equalsIgnoreCase(u.getEmail()));
        }

        @Override
        public User save(User user) {
            store.put(user.getId(), user);
            return user;
        }

        @Override
        public boolean update(User user) {
            store.put(user.getId(), user);
            return true;
        }

        @Override
        public boolean updateStatus(Long userId, UserStatus status) {
            User u = store.get(userId);
            if (u != null) {
                u.setStatus(status);
                return true;
            }
            return false;
        }

        @Override
        public boolean updateRole(Long userId, Role role) {
            User u = store.get(userId);
            if (u != null) {
                u.setRole(role);
                return true;
            }
            return false;
        }

        @Override
        public long countActiveAdmins() {
            return store.values().stream()
                    .filter(u -> u.getRole() == Role.ADMIN && u.getStatus() == UserStatus.ACTIVE)
                    .count();
        }

        @Override
        public List<User> findPaginated(String query, Role role, UserStatus status, int offset, int limit) {
            return store.values().stream()
                    .filter(u -> query == null || (u.getEmail() != null && u.getEmail().toLowerCase().contains(query.toLowerCase())))
                    .filter(u -> role == null || u.getRole() == role)
                    .filter(u -> status == null || u.getStatus() == status)
                    .skip(offset).limit(limit).collect(Collectors.toList());
        }

        @Override
        public long countFiltered(String query, Role role, UserStatus status) {
            return store.values().stream()
                    .filter(u -> query == null || (u.getEmail() != null && u.getEmail().toLowerCase().contains(query.toLowerCase())))
                    .filter(u -> role == null || u.getRole() == role)
                    .filter(u -> status == null || u.getStatus() == status)
                    .count();
        }

        @Override public boolean deleteById(Long id) { return store.remove(id) != null; }
        @Override public List<User> findAll() { return new ArrayList<>(store.values()); }
    }

    static class InMemoryPatientDao implements PatientDao {
        private final Map<Long, Patient> store = new HashMap<>();

        @Override
        public Optional<Patient> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override
        public Patient save(Patient patient) {
            Long id = patient.getPatientProfileId() != null ? patient.getPatientProfileId() : patient.getId();
            store.put(id, patient);
            return patient;
        }
        @Override
        public Patient saveProfile(Patient patient, java.sql.Connection conn) { return save(patient); }
        @Override
        public long countTotal() { return store.size(); }
        @Override
        public List<Patient> findPaginated(String query, UserStatus status, int offset, int limit) {
            return store.values().stream().skip(offset).limit(limit).collect(Collectors.toList());
        }
        @Override
        public long countFiltered(String query, UserStatus status) { return store.size(); }
        @Override public Optional<Patient> findByUserId(Long userId) { return Optional.ofNullable(store.get(userId)); }
        @Override public boolean update(Patient patient) {
            Long id = patient.getPatientProfileId() != null ? patient.getPatientProfileId() : patient.getId();
            store.put(id, patient);
            return true;
        }
        @Override public boolean deleteById(Long id) { return store.remove(id) != null; }
        @Override public List<Patient> findAll() { return new ArrayList<>(store.values()); }
    }

    static class InMemoryDoctorDao implements DoctorDao {
        private final Map<Long, Doctor> store = new HashMap<>();

        @Override
        public Optional<Doctor> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override
        public Doctor save(Doctor doctor) {
            Long id = doctor.getDoctorProfileId() != null ? doctor.getDoctorProfileId() : doctor.getId();
            store.put(id, doctor);
            return doctor;
        }
        @Override
        public long countTotal() { return store.size(); }
        @Override
        public long countActive() { return store.values().stream().filter(Doctor::isAvailable).count(); }
        @Override
        public boolean updateAvailability(Long doctorId, boolean isAvailable) {
            Doctor d = store.get(doctorId);
            if (d != null) { d.setAvailable(isAvailable); return true; }
            return false;
        }
        @Override
        public List<Doctor> findPaginated(String query, Long specialtyId, UserStatus status, Boolean available, int offset, int limit) {
            return store.values().stream().skip(offset).limit(limit).collect(Collectors.toList());
        }
        @Override
        public long countFiltered(String query, Long specialtyId, UserStatus status, Boolean available) {
            return store.size();
        }
        @Override public Optional<Doctor> findByUserId(Long userId) { return Optional.ofNullable(store.get(userId)); }
        @Override public Optional<Doctor> findByLicenseNumber(String licenseNumber) { return Optional.empty(); }
        @Override public List<Doctor> findBySpecialtyId(Long specialtyId) { return Collections.emptyList(); }
        @Override public List<Doctor> findActiveDoctors() { return new ArrayList<>(store.values()); }
        @Override public List<Doctor> searchDoctors(Long specialtyId, String query) { return new ArrayList<>(store.values()); }
        @Override public boolean update(Doctor doctor) {
            Long id = doctor.getDoctorProfileId() != null ? doctor.getDoctorProfileId() : doctor.getId();
            store.put(id, doctor);
            return true;
        }
        @Override public boolean deleteById(Long id) { return store.remove(id) != null; }
        @Override public List<Doctor> findAll() { return new ArrayList<>(store.values()); }
    }

    static class InMemorySpecialtyDao implements SpecialtyDao {
        private final Map<Long, Specialty> store = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(100);

        @Override
        public Optional<Specialty> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override
        public Optional<Specialty> findByName(String name) {
            return store.values().stream().filter(s -> name != null && name.equalsIgnoreCase(s.getName())).findFirst();
        }
        @Override
        public Specialty save(Specialty specialty) {
            if (specialty.getId() == null) specialty.setId(seq.incrementAndGet());
            store.put(specialty.getId(), specialty);
            return specialty;
        }
        @Override
        public boolean update(Specialty specialty) {
            store.put(specialty.getId(), specialty);
            return true;
        }
        @Override
        public long countActive() { return store.values().stream().filter(Specialty::isActive).count(); }
        @Override
        public List<Specialty> findPaginated(String query, Boolean activeOnly, int offset, int limit) {
            return store.values().stream().skip(offset).limit(limit).collect(Collectors.toList());
        }
        @Override
        public long countFiltered(String query, Boolean activeOnly) { return store.size(); }
        @Override public List<Specialty> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<Specialty> findActiveSpecialties() { return store.values().stream().filter(Specialty::isActive).collect(Collectors.toList()); }
        @Override public boolean deleteById(Long id) { return store.remove(id) != null; }
    }

    static class InMemoryAppointmentDao implements AppointmentDao {
        private final Map<Long, Appointment> store = new HashMap<>();

        @Override
        public Optional<Appointment> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override
        public Appointment save(Appointment appointment) { store.put(appointment.getId(), appointment); return appointment; }
        @Override
        public boolean update(Appointment appointment) { store.put(appointment.getId(), appointment); return true; }
        @Override
        public boolean updateStatus(Long appointmentId, AppointmentStatus status, String cancellationReason, Long cancelledByUserId) {
            Appointment apt = store.get(appointmentId);
            if (apt != null) {
                apt.setStatus(status);
                apt.setCancellationReason(cancellationReason);
                return true;
            }
            return false;
        }
        @Override
        public long countByStatus(AppointmentStatus status) {
            return store.values().stream().filter(a -> a.getStatus() == status).count();
        }
        @Override
        public List<Appointment> findByDate(LocalDate date) {
            return store.values().stream().filter(a -> date.equals(a.getAppointmentDate())).collect(Collectors.toList());
        }
        @Override
        public List<Appointment> findPaginated(String reference, Long doctorId, Long patientId, Long specialtyId, AppointmentStatus status, LocalDate date, int offset, int limit) {
            return store.values().stream().skip(offset).limit(limit).collect(Collectors.toList());
        }
        @Override
        public long countFiltered(String reference, Long doctorId, Long patientId, Long specialtyId, AppointmentStatus status, LocalDate date) {
            return store.size();
        }
        @Override public Optional<Appointment> findByReference(String ref) { return Optional.empty(); }
        @Override public List<Appointment> findByDoctorId(Long doctorId) { return Collections.emptyList(); }
        @Override public List<Appointment> findByPatientId(Long patientId) { return Collections.emptyList(); }
        @Override public List<Appointment> findByDoctorAndDate(Long doctorId, LocalDate date) { return Collections.emptyList(); }
        @Override public boolean deleteById(Long id) { return store.remove(id) != null; }
        @Override public List<Appointment> findAll() { return new ArrayList<>(store.values()); }
    }

    static class InMemoryDoctorAvailabilityExceptionDao implements DoctorAvailabilityExceptionDao {
        private final Map<Long, DoctorAvailabilityException> store = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(700);

        @Override
        public Optional<DoctorAvailabilityException> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override
        public DoctorAvailabilityException save(DoctorAvailabilityException e) {
            if (e.getId() == null) e.setId(seq.incrementAndGet());
            store.put(e.getId(), e);
            return e;
        }
        @Override
        public boolean deleteById(Long id) { return store.remove(id) != null; }
        @Override
        public List<DoctorAvailabilityException> findAllClinicHolidays() {
            return store.values().stream().filter(e -> e.getDoctorId() == null).collect(Collectors.toList());
        }
        @Override public List<DoctorAvailabilityException> findByDoctorId(Long doctorId) { return Collections.emptyList(); }
        @Override public boolean update(DoctorAvailabilityException entity) { return true; }
        @Override public List<DoctorAvailabilityException> findAll() { return new ArrayList<>(store.values()); }
    }

    static class InMemorySystemSettingDao implements SystemSettingDao {
        private final Map<String, SystemSetting> store = new HashMap<>();

        @Override
        public Optional<SystemSetting> findById(String key) { return Optional.ofNullable(store.get(key)); }
        @Override
        public String getValue(String key, String defaultValue) {
            SystemSetting s = store.get(key);
            return (s != null && s.getValue() != null) ? s.getValue() : defaultValue;
        }
        @Override
        public boolean saveOrUpdate(SystemSetting setting) {
            store.put(setting.getKey(), setting);
            return true;
        }
        @Override
        public List<SystemSetting> findAll() { return new ArrayList<>(store.values()); }
        @Override public SystemSetting save(SystemSetting entity) { store.put(entity.getKey(), entity); return entity; }
        @Override public boolean update(SystemSetting entity) { store.put(entity.getKey(), entity); return true; }
        @Override public boolean deleteById(String key) { return store.remove(key) != null; }
    }

    static class InMemoryAuditLogDao implements AuditLogDao {
        final List<AuditLog> logs = new ArrayList<>();
        private final AtomicLong seq = new AtomicLong(1);

        @Override
        public AuditLog log(Long userId, String action, String targetEntity, Long targetId, String ipAddress, String userAgent, String details) {
            AuditLog log = new AuditLog();
            log.setId(seq.getAndIncrement());
            log.setUserId(userId);
            log.setAction(action);
            log.setTargetEntity(targetEntity);
            log.setTargetId(targetId);
            log.setIpAddress(ipAddress);
            log.setUserAgent(userAgent);
            log.setDetails(details);
            logs.add(log);
            return log;
        }

        @Override
        public AuditLog save(AuditLog auditLog) {
            logs.add(auditLog);
            return auditLog;
        }

        @Override
        public Optional<AuditLog> findById(Long id) {
            return logs.stream().filter(l -> id.equals(l.getId())).findFirst();
        }

        @Override
        public List<AuditLog> findPaginated(String action, Long userId, String targetEntity, int offset, int limit) {
            return logs.stream().skip(offset).limit(limit).collect(Collectors.toList());
        }

        @Override
        public long countFiltered(String action, Long userId, String targetEntity) {
            return logs.size();
        }

        @Override public List<AuditLog> findRecent(int limit) { return logs.stream().limit(limit).collect(Collectors.toList()); }
        @Override public boolean update(AuditLog entity) { return false; }
        @Override public boolean deleteById(Long id) { return false; }
        @Override public List<AuditLog> findAll() { return new ArrayList<>(logs); }
    }
}
