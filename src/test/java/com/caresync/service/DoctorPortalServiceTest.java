package com.caresync.service;

import com.caresync.dao.AppointmentDao;
import com.caresync.dao.AuditLogDao;
import com.caresync.dao.DoctorDao;
import com.caresync.dao.UserDao;
import com.caresync.dto.DoctorDashboardDto;
import com.caresync.dto.DoctorProfileUpdateDto;
import com.caresync.exception.ValidationException;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.AuditLog;
import com.caresync.model.Doctor;
import com.caresync.model.Role;
import com.caresync.model.User;
import com.caresync.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CareSync DoctorPortalService Tests")
class DoctorPortalServiceTest {

    private FakeDoctorDao doctorDao;
    private FakeUserDao userDao;
    private FakeAppointmentDao appointmentDao;
    private FakeAuditLogDao auditLogDao;
    private DoctorPortalService doctorPortalService;

    private static final Long DOCTOR_PROFILE_ID = 101L;
    private static final Long DOCTOR_USER_ID = 11L;

    @BeforeEach
    void setUp() {
        doctorDao = new FakeDoctorDao();
        userDao = new FakeUserDao();
        appointmentDao = new FakeAppointmentDao();
        auditLogDao = new FakeAuditLogDao();

        doctorPortalService = new DoctorPortalServiceImpl(doctorDao, userDao, appointmentDao, auditLogDao);

        // Seed doctor and user
        User user = new User(DOCTOR_USER_ID);
        user.setEmail("dr.smith@caresync.com");
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setPhoneNumber("+91 9876543210");
        user.setRole(Role.DOCTOR);
        user.setStatus(UserStatus.ACTIVE);
        userDao.save(user);

        Doctor doctor = new Doctor();
        doctor.setDoctorProfileId(DOCTOR_PROFILE_ID);
        doctor.setId(DOCTOR_USER_ID);
        doctor.setEmail(user.getEmail());
        doctor.setFirstName(user.getFirstName());
        doctor.setLastName(user.getLastName());
        doctor.setQualification("MBBS, MD");
        doctor.setExperienceYears(10);
        doctor.setConsultationFee(new BigDecimal("500.00"));
        doctor.setRoomNumber("204");
        doctor.setBio("Senior Cardiologist");
        doctor.setAvailable(true);
        doctorDao.save(doctor);
    }

    @Test
    @DisplayName("Verify getDashboardMetrics calculates live metrics and active queues correctly")
    void testGetDashboardMetrics() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalDate tomorrow = today.plusDays(1);
        LocalDate yesterday = today.minusDays(1);

        // Create appointments
        Appointment apt1 = createAppointment(1L, DOCTOR_PROFILE_ID, today, LocalTime.of(10, 0), AppointmentStatus.CONFIRMED);
        Appointment apt2 = createAppointment(2L, DOCTOR_PROFILE_ID, today, LocalTime.of(11, 0), AppointmentStatus.PENDING);
        Appointment apt3 = createAppointment(3L, DOCTOR_PROFILE_ID, tomorrow, LocalTime.of(14, 0), AppointmentStatus.CONFIRMED);
        Appointment apt4 = createAppointment(4L, DOCTOR_PROFILE_ID, yesterday, LocalTime.of(9, 0), AppointmentStatus.COMPLETED);

        appointmentDao.save(apt1);
        appointmentDao.save(apt2);
        appointmentDao.save(apt3);
        appointmentDao.save(apt4);

        DoctorDashboardDto metrics = doctorPortalService.getDashboardMetrics(DOCTOR_PROFILE_ID);

        assertNotNull(metrics);
        assertEquals(4, metrics.getTotalAppointments());
        assertEquals(2, metrics.getTodayAppointmentsCount());
        assertEquals(1, metrics.getPendingAppointmentsCount());
        assertEquals(2, metrics.getConfirmedAppointmentsCount());
        assertEquals(1, metrics.getCompletedAppointmentsCount());

        assertEquals(2, metrics.getTodayAppointments().size());
        assertEquals(1, metrics.getUpcomingAppointments().size());
        assertEquals(1, metrics.getPendingAppointments().size());
    }

    @Test
    @DisplayName("Verify updateDoctorProfile successfully updates allowed fields and logs audit")
    void testUpdateDoctorProfileSuccess() {
        DoctorProfileUpdateDto dto = new DoctorProfileUpdateDto();
        dto.setQualification("MBBS, MD, DM");
        dto.setExperienceYears(12);
        dto.setConsultationFee(new BigDecimal("750.00"));
        dto.setBio("Consultant Cardiologist & Electrophysiologist");
        dto.setRoomNumber("305");
        dto.setPhoneNumber("+91 9123456789");
        dto.setAvailable(true);

        boolean updated = doctorPortalService.updateDoctorProfile(
                DOCTOR_PROFILE_ID, DOCTOR_USER_ID, dto, DOCTOR_USER_ID, "127.0.0.1", "TestAgent");

        assertTrue(updated);

        Doctor doctor = doctorDao.findById(DOCTOR_PROFILE_ID).orElseThrow();
        assertEquals("MBBS, MD, DM", doctor.getQualification());
        assertEquals(12, doctor.getExperienceYears());
        assertEquals(new BigDecimal("750.00"), doctor.getConsultationFee());
        assertEquals("305", doctor.getRoomNumber());

        User user = userDao.findById(DOCTOR_USER_ID).orElseThrow();
        assertEquals("+91 9123456789", user.getPhoneNumber());

        assertEquals(1, auditLogDao.getLogs().size());
        assertEquals("DOCTOR_PROFILE_UPDATED", auditLogDao.getLogs().get(0).getAction());
    }

    @Test
    @DisplayName("Verify updateDoctorProfile rejects negative consultation fee")
    void testUpdateDoctorProfileRejectsNegativeFee() {
        DoctorProfileUpdateDto dto = new DoctorProfileUpdateDto();
        dto.setConsultationFee(new BigDecimal("-100.00"));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                doctorPortalService.updateDoctorProfile(
                        DOCTOR_PROFILE_ID, DOCTOR_USER_ID, dto, DOCTOR_USER_ID, "127.0.0.1", "TestAgent")
        );
        assertTrue(ex.getMessage().contains("negative"));
    }

    @Test
    @DisplayName("Verify updateDoctorProfile blocks actor impersonation (IDOR prevention)")
    void testUpdateDoctorProfileBlocksImpersonation() {
        DoctorProfileUpdateDto dto = new DoctorProfileUpdateDto();
        Long maliciousActorId = 999L;

        ValidationException ex = assertThrows(ValidationException.class, () ->
                doctorPortalService.updateDoctorProfile(
                        DOCTOR_PROFILE_ID, DOCTOR_USER_ID, dto, maliciousActorId, "127.0.0.1", "TestAgent")
        );
        assertTrue(ex.getMessage().contains("Unauthorized attempt"));
    }

    private Appointment createAppointment(Long id, Long doctorId, LocalDate date, LocalTime start, AppointmentStatus status) {
        Appointment apt = new Appointment(id);
        apt.setDoctorId(doctorId);
        apt.setPatientId(501L);
        apt.setAppointmentDate(date);
        apt.setStartTime(start);
        apt.setEndTime(start.plusMinutes(30));
        apt.setStatus(status);
        apt.setReferenceNumber("REF-" + id);
        return apt;
    }

    // =========================================================================
    // In-memory test stubs
    // =========================================================================

    private static class FakeDoctorDao implements DoctorDao {
        private final Map<Long, Doctor> store = new HashMap<>();

        @Override
        public Optional<Doctor> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Doctor> findByUserId(Long userId) {
            return store.values().stream().filter(d -> userId.equals(d.getId())).findFirst();
        }

        @Override
        public Doctor save(Doctor doctor) {
            store.put(doctor.getDoctorProfileId(), doctor);
            return doctor;
        }

        @Override
        public boolean update(Doctor doctor) {
            store.put(doctor.getDoctorProfileId(), doctor);
            return true;
        }

        @Override public Optional<Doctor> findByLicenseNumber(String l) { return Optional.empty(); }
        @Override public List<Doctor> findBySpecialtyId(Long s) { return List.of(); }
        @Override public List<Doctor> findActiveDoctors() { return List.of(); }
        @Override public List<Doctor> searchDoctors(Long s, String q) { return List.of(); }
        @Override public List<Doctor> findAll() { return new ArrayList<>(store.values()); }
        @Override public boolean deleteById(Long id) { return store.remove(id) != null; }
    }

    private static class FakeUserDao implements UserDao {
        private final Map<Long, User> store = new HashMap<>();

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(store.get(id));
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

        @Override public Optional<User> findByEmail(String email) { return Optional.empty(); }
        @Override public boolean existsByEmail(String email) { return false; }
        @Override public List<User> findAll() { return new ArrayList<>(store.values()); }
        @Override public boolean deleteById(Long id) { return store.remove(id) != null; }
    }

    private static class FakeAppointmentDao implements AppointmentDao {
        private final Map<Long, Appointment> store = new HashMap<>();

        @Override
        public Appointment save(Appointment appointment) {
            store.put(appointment.getId(), appointment);
            return appointment;
        }

        @Override
        public Optional<Appointment> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Appointment> findWithDetailsByDoctorId(Long doctorId) {
            return store.values().stream()
                    .filter(a -> doctorId.equals(a.getDoctorId()))
                    .collect(Collectors.toList());
        }

        @Override
        public long countByDoctorId(Long doctorId) {
            return store.values().stream().filter(a -> doctorId.equals(a.getDoctorId())).count();
        }

        @Override
        public long countByDoctorAndStatus(Long doctorId, AppointmentStatus status) {
            return store.values().stream()
                    .filter(a -> doctorId.equals(a.getDoctorId()) && status == a.getStatus())
                    .count();
        }

        @Override
        public long countTodayByDoctorId(Long doctorId, LocalDate today) {
            return store.values().stream()
                    .filter(a -> doctorId.equals(a.getDoctorId()) && today.equals(a.getAppointmentDate()))
                    .count();
        }

        @Override public Optional<Appointment> findByReference(String ref) { return Optional.empty(); }
        @Override public Optional<Appointment> findWithDetailsById(Long id) { return findById(id); }
        @Override public List<Appointment> findByPatientId(Long p) { return List.of(); }
        @Override public List<Appointment> findByDoctorId(Long d) { return findWithDetailsByDoctorId(d); }
        @Override public List<Appointment> findByDoctorAndDate(Long d, LocalDate dt) { return List.of(); }
        @Override public boolean isSlotBooked(Long d, LocalDate dt, LocalTime s, LocalTime e) { return false; }
        @Override public boolean updateStatus(Long id, AppointmentStatus s, String r, Long c) { return true; }
        @Override public boolean update(Appointment entity) { return true; }
        @Override public boolean deleteById(Long id) { return store.remove(id) != null; }
        @Override public List<Appointment> findAll() { return new ArrayList<>(store.values()); }
    }

    private static class FakeAuditLogDao implements AuditLogDao {
        private final List<AuditLog> logs = new ArrayList<>();
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

        @Override public AuditLog save(AuditLog entity) { logs.add(entity); return entity; }
        @Override public Optional<AuditLog> findById(Long id) { return Optional.empty(); }
        @Override public List<AuditLog> findAll() { return logs; }
        @Override public boolean update(AuditLog entity) { return false; }
        @Override public boolean deleteById(Long id) { return false; }

        public List<AuditLog> getLogs() {
            return logs;
        }
    }
}
