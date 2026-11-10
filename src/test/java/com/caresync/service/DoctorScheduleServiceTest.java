package com.caresync.service;

import com.caresync.dao.AppointmentDao;
import com.caresync.dao.AuditLogDao;
import com.caresync.dao.DoctorAvailabilityDao;
import com.caresync.dao.DoctorAvailabilityExceptionDao;
import com.caresync.exception.ValidationException;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.AuditLog;
import com.caresync.model.DoctorAvailability;
import com.caresync.model.DoctorAvailabilityException;
import com.caresync.model.ExceptionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

@DisplayName("CareSync DoctorScheduleService Tests")
class DoctorScheduleServiceTest {

    private FakeDoctorAvailabilityDao availabilityDao;
    private FakeDoctorAvailabilityExceptionDao exceptionDao;
    private FakeAppointmentDao appointmentDao;
    private FakeAuditLogDao auditLogDao;
    private DoctorScheduleService scheduleService;

    private static final Long DOCTOR_A_ID = 301L;
    private static final Long DOCTOR_B_ID = 302L;
    private static final Long ACTOR_USER_ID = 60L;

    @BeforeEach
    void setUp() {
        availabilityDao = new FakeDoctorAvailabilityDao();
        exceptionDao = new FakeDoctorAvailabilityExceptionDao();
        appointmentDao = new FakeAppointmentDao();
        auditLogDao = new FakeAuditLogDao();

        scheduleService = new DoctorScheduleServiceImpl(availabilityDao, exceptionDao, appointmentDao, auditLogDao);
    }

    @Test
    @DisplayName("Verify updateWeeklySchedule saves valid schedules and enforces startTime < endTime")
    void testUpdateWeeklyScheduleValidation() {
        DoctorAvailability day1 = new DoctorAvailability();
        day1.setDayOfWeek(1); // Monday
        day1.setStartTime(LocalTime.of(9, 0));
        day1.setEndTime(LocalTime.of(17, 0));
        day1.setSlotDurationMinutes(30);

        boolean updated = scheduleService.updateWeeklySchedule(
                DOCTOR_A_ID, List.of(day1), ACTOR_USER_ID, "127.0.0.1", "TestAgent");

        assertTrue(updated);
        assertEquals(1, scheduleService.getWeeklySchedule(DOCTOR_A_ID).size());

        // Inverted time range should fail
        DoctorAvailability invalidDay = new DoctorAvailability();
        invalidDay.setDayOfWeek(2);
        invalidDay.setStartTime(LocalTime.of(17, 0));
        invalidDay.setEndTime(LocalTime.of(9, 0));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                scheduleService.updateWeeklySchedule(
                        DOCTOR_A_ID, List.of(invalidDay), ACTOR_USER_ID, "127.0.0.1", "TestAgent")
        );
        assertTrue(ex.getMessage().contains("strictly before end time"));
    }

    @Test
    @DisplayName("Verify addException rejects dates in the past")
    void testAddExceptionPastDateRejected() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Kolkata")).minusDays(1);
        DoctorAvailabilityException ex = new DoctorAvailabilityException();
        ex.setExceptionDate(yesterday);
        ex.setFullDay(true);
        ex.setExceptionType(ExceptionType.LEAVE);

        ValidationException thrown = assertThrows(ValidationException.class, () ->
                scheduleService.addException(DOCTOR_A_ID, ex, ACTOR_USER_ID, "127.0.0.1", "TestAgent")
        );
        assertTrue(thrown.getMessage().contains("past"));
    }

    @Test
    @DisplayName("Verify addException detects and prevents conflict with existing active appointment")
    void testAddExceptionConflictDetection() {
        LocalDate futureDate = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(5);

        // Pre-existing confirmed appointment on that date
        Appointment apt = new Appointment(99L);
        apt.setDoctorId(DOCTOR_A_ID);
        apt.setAppointmentDate(futureDate);
        apt.setStartTime(LocalTime.of(10, 0));
        apt.setEndTime(LocalTime.of(10, 30));
        apt.setStatus(AppointmentStatus.CONFIRMED);
        apt.setReferenceNumber("REF-99");
        appointmentDao.save(apt);

        // Doctor attempts full-day leave
        DoctorAvailabilityException leave = new DoctorAvailabilityException();
        leave.setExceptionDate(futureDate);
        leave.setFullDay(true);
        leave.setExceptionType(ExceptionType.LEAVE);

        ValidationException thrown = assertThrows(ValidationException.class, () ->
                scheduleService.addException(DOCTOR_A_ID, leave, ACTOR_USER_ID, "127.0.0.1", "TestAgent")
        );
        assertTrue(thrown.getMessage().contains("Active appointment REF-99 exists"));
    }

    @Test
    @DisplayName("Verify deleteException enforces doctor ownership")
    void testDeleteExceptionOwnership() {
        LocalDate futureDate = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(3);
        DoctorAvailabilityException ex = new DoctorAvailabilityException();
        ex.setId(88L);
        ex.setDoctorId(DOCTOR_A_ID);
        ex.setExceptionDate(futureDate);
        ex.setFullDay(true);
        ex.setExceptionType(ExceptionType.HOLIDAY);
        exceptionDao.save(ex);

        // Doctor B tries to delete Doctor A's exception
        ValidationException thrown = assertThrows(ValidationException.class, () ->
                scheduleService.deleteException(88L, DOCTOR_B_ID, ACTOR_USER_ID, "127.0.0.1", "TestAgent")
        );
        assertTrue(thrown.getMessage().contains("Unauthorized attempt"));

        // Doctor A can delete their own exception
        boolean deleted = scheduleService.deleteException(88L, DOCTOR_A_ID, ACTOR_USER_ID, "127.0.0.1", "TestAgent");
        assertTrue(deleted);
    }

    // =========================================================================
    // Test DAO Stubs
    // =========================================================================

    private static class FakeDoctorAvailabilityDao implements DoctorAvailabilityDao {
        private final Map<Long, DoctorAvailability> store = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(1);

        @Override
        public List<DoctorAvailability> findByDoctorId(Long doctorId) {
            return store.values().stream()
                    .filter(a -> doctorId.equals(a.getDoctorId()))
                    .collect(Collectors.toList());
        }

        @Override
        public Optional<DoctorAvailability> findByDoctorAndDay(Long doctorId, int dayOfWeek) {
            return store.values().stream()
                    .filter(a -> doctorId.equals(a.getDoctorId()) && a.getDayOfWeek() == dayOfWeek)
                    .findFirst();
        }

        @Override
        public DoctorAvailability save(DoctorAvailability entity) {
            if (entity.getId() == null) entity.setId(seq.getAndIncrement());
            store.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public boolean update(DoctorAvailability entity) {
            store.put(entity.getId(), entity);
            return true;
        }

        @Override public Optional<DoctorAvailability> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<DoctorAvailability> findAll() { return new ArrayList<>(store.values()); }
        @Override public boolean deleteById(Long id) { return store.remove(id) != null; }
        @Override public boolean deleteByDoctorId(Long d) { return true; }
    }

    private static class FakeDoctorAvailabilityExceptionDao implements DoctorAvailabilityExceptionDao {
        private final Map<Long, DoctorAvailabilityException> store = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(1);

        @Override
        public List<DoctorAvailabilityException> findByDoctorId(Long doctorId) {
            return store.values().stream()
                    .filter(a -> doctorId.equals(a.getDoctorId()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<DoctorAvailabilityException> findExceptionsByDoctorAndDate(Long doctorId, LocalDate date) {
            return store.values().stream()
                    .filter(a -> doctorId.equals(a.getDoctorId()) && date.equals(a.getExceptionDate()))
                    .collect(Collectors.toList());
        }

        @Override
        public DoctorAvailabilityException save(DoctorAvailabilityException entity) {
            if (entity.getId() == null) entity.setId(seq.getAndIncrement());
            store.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public Optional<DoctorAvailabilityException> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public boolean deleteById(Long id) {
            return store.remove(id) != null;
        }

        @Override public List<DoctorAvailabilityException> findAll() { return new ArrayList<>(store.values()); }
        @Override public boolean update(DoctorAvailabilityException entity) { return true; }
    }

    private static class FakeAppointmentDao implements AppointmentDao {
        private final Map<Long, Appointment> store = new HashMap<>();

        @Override
        public Appointment save(Appointment appointment) {
            store.put(appointment.getId(), appointment);
            return appointment;
        }

        @Override
        public List<Appointment> findByDoctorAndDate(Long doctorId, LocalDate date) {
            return store.values().stream()
                    .filter(a -> doctorId.equals(a.getDoctorId()) && date.equals(a.getAppointmentDate()))
                    .collect(Collectors.toList());
        }

        @Override public Optional<Appointment> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public Optional<Appointment> findWithDetailsById(Long id) { return findById(id); }
        @Override public Optional<Appointment> findByReference(String ref) { return Optional.empty(); }
        @Override public List<Appointment> findByPatientId(Long p) { return List.of(); }
        @Override public List<Appointment> findByDoctorId(Long d) { return List.of(); }
        @Override public List<Appointment> findWithDetailsByDoctorId(Long d) { return List.of(); }
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
    }
}
