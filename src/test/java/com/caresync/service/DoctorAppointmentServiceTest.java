package com.caresync.service;

import com.caresync.dao.AppointmentDao;
import com.caresync.dao.AuditLogDao;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.AuditLog;
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

@DisplayName("CareSync DoctorAppointmentService Tests")
class DoctorAppointmentServiceTest {

    private FakeAppointmentDao appointmentDao;
    private FakeAuditLogDao auditLogDao;
    private DoctorAppointmentService appointmentService;

    private static final Long DOCTOR_A_ID = 201L;
    private static final Long DOCTOR_B_ID = 202L;
    private static final Long ACTOR_USER_ID = 50L;

    @BeforeEach
    void setUp() {
        appointmentDao = new FakeAppointmentDao();
        auditLogDao = new FakeAuditLogDao();
        appointmentService = new DoctorAppointmentServiceImpl(appointmentDao, auditLogDao);
    }

    @Test
    @DisplayName("Verify getAppointmentsForDoctor filters appointments accurately")
    void testFilterAppointments() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalDate tomorrow = today.plusDays(1);

        Appointment a1 = createAppointment(1L, DOCTOR_A_ID, today, AppointmentStatus.CONFIRMED);
        Appointment a2 = createAppointment(2L, DOCTOR_A_ID, today, AppointmentStatus.PENDING);
        Appointment a3 = createAppointment(3L, DOCTOR_A_ID, tomorrow, AppointmentStatus.CONFIRMED);
        Appointment a4 = createAppointment(4L, DOCTOR_A_ID, today, AppointmentStatus.CANCELLED);

        appointmentDao.save(a1);
        appointmentDao.save(a2);
        appointmentDao.save(a3);
        appointmentDao.save(a4);

        assertEquals(4, appointmentService.getAppointmentsForDoctor(DOCTOR_A_ID, "ALL").size());
        assertEquals(3, appointmentService.getAppointmentsForDoctor(DOCTOR_A_ID, "TODAY").size());
        assertEquals(1, appointmentService.getAppointmentsForDoctor(DOCTOR_A_ID, "PENDING").size());
        assertEquals(2, appointmentService.getAppointmentsForDoctor(DOCTOR_A_ID, "CONFIRMED").size());
        assertEquals(1, appointmentService.getAppointmentsForDoctor(DOCTOR_A_ID, "CANCELLED").size());
        assertEquals(3, appointmentService.getAppointmentsForDoctor(DOCTOR_A_ID, "UPCOMING").size());
    }

    @Test
    @DisplayName("Verify confirmAppointment transitions PENDING to CONFIRMED and logs audit")
    void testConfirmAppointmentSuccess() {
        Appointment apt = createAppointment(10L, DOCTOR_A_ID, LocalDate.now(), AppointmentStatus.PENDING);
        appointmentDao.save(apt);

        boolean confirmed = appointmentService.confirmAppointment(
                10L, DOCTOR_A_ID, ACTOR_USER_ID, "127.0.0.1", "TestAgent");

        assertTrue(confirmed);
        assertEquals(AppointmentStatus.CONFIRMED, appointmentDao.findById(10L).orElseThrow().getStatus());
        assertEquals(1, auditLogDao.getLogs().size());
        assertEquals("APPOINTMENT_CONFIRMED", auditLogDao.getLogs().get(0).getAction());
    }

    @Test
    @DisplayName("Verify confirmAppointment rejects invalid transitions from COMPLETED")
    void testConfirmAppointmentInvalidState() {
        Appointment apt = createAppointment(11L, DOCTOR_A_ID, LocalDate.now(), AppointmentStatus.COMPLETED);
        appointmentDao.save(apt);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                appointmentService.confirmAppointment(11L, DOCTOR_A_ID, ACTOR_USER_ID, "127.0.0.1", "TestAgent")
        );
        assertTrue(ex.getMessage().contains("Only PENDING appointments can be confirmed"));
    }

    @Test
    @DisplayName("Verify rejectAppointment transitions PENDING to CANCELLED with reason")
    void testRejectAppointmentSuccess() {
        Appointment apt = createAppointment(12L, DOCTOR_A_ID, LocalDate.now(), AppointmentStatus.PENDING);
        appointmentDao.save(apt);

        boolean rejected = appointmentService.rejectAppointment(
                12L, DOCTOR_A_ID, "Emergency surgery scheduled", ACTOR_USER_ID, "127.0.0.1", "TestAgent");

        assertTrue(rejected);
        Appointment updated = appointmentDao.findById(12L).orElseThrow();
        assertEquals(AppointmentStatus.CANCELLED, updated.getStatus());
        assertTrue(updated.getCancellationReason().contains("Emergency surgery"));
        assertEquals("APPOINTMENT_REJECTED", auditLogDao.getLogs().get(0).getAction());
    }

    @Test
    @DisplayName("Verify completeAppointment transitions CONFIRMED to COMPLETED")
    void testCompleteAppointmentSuccess() {
        Appointment apt = createAppointment(13L, DOCTOR_A_ID, LocalDate.now(), AppointmentStatus.CONFIRMED);
        appointmentDao.save(apt);

        boolean completed = appointmentService.completeAppointment(
                13L, DOCTOR_A_ID, ACTOR_USER_ID, "127.0.0.1", "TestAgent");

        assertTrue(completed);
        assertEquals(AppointmentStatus.COMPLETED, appointmentDao.findById(13L).orElseThrow().getStatus());
        assertEquals("APPOINTMENT_COMPLETED", auditLogDao.getLogs().get(0).getAction());
    }

    @Test
    @DisplayName("Verify IDOR defense: Doctor B cannot confirm Doctor A's appointment")
    void testDoctorCannotAlterAnotherDoctorsAppointment() {
        Appointment apt = createAppointment(14L, DOCTOR_A_ID, LocalDate.now(), AppointmentStatus.PENDING);
        appointmentDao.save(apt);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                appointmentService.confirmAppointment(14L, DOCTOR_B_ID, ACTOR_USER_ID, "127.0.0.1", "TestAgent")
        );
        assertTrue(ex.getMessage().contains("Unauthorized attempt"));
    }

    @Test
    @DisplayName("Verify IDOR defense: Doctor B cannot view Doctor A's appointment details")
    void testDoctorCannotViewAnotherDoctorsAppointment() {
        Appointment apt = createAppointment(15L, DOCTOR_A_ID, LocalDate.now(), AppointmentStatus.CONFIRMED);
        appointmentDao.save(apt);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                appointmentService.getAppointmentDetails(15L, DOCTOR_B_ID)
        );
        assertTrue(ex.getMessage().contains("Unauthorized access"));
    }

    private Appointment createAppointment(Long id, Long doctorId, LocalDate date, AppointmentStatus status) {
        Appointment apt = new Appointment(id);
        apt.setDoctorId(doctorId);
        apt.setPatientId(601L);
        apt.setAppointmentDate(date);
        apt.setStartTime(LocalTime.of(10, 0));
        apt.setEndTime(LocalTime.of(10, 30));
        apt.setStatus(status);
        apt.setReferenceNumber("REF-" + id);
        return apt;
    }

    // =========================================================================
    // Test DAO implementation
    // =========================================================================

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
        public Optional<Appointment> findWithDetailsById(Long id) {
            return findById(id);
        }

        @Override
        public List<Appointment> findWithDetailsByDoctorId(Long doctorId) {
            return store.values().stream()
                    .filter(a -> doctorId.equals(a.getDoctorId()))
                    .collect(Collectors.toList());
        }

        @Override
        public boolean updateStatus(Long id, AppointmentStatus status, String reason, Long cancelledBy) {
            Appointment apt = store.get(id);
            if (apt != null) {
                apt.setStatus(status);
                apt.setCancellationReason(reason);
                apt.setCancelledByUserId(cancelledBy);
                return true;
            }
            return false;
        }

        @Override public Optional<Appointment> findByReference(String ref) { return Optional.empty(); }
        @Override public List<Appointment> findByPatientId(Long p) { return List.of(); }
        @Override public List<Appointment> findByDoctorId(Long d) { return findWithDetailsByDoctorId(d); }
        @Override public List<Appointment> findByDoctorAndDate(Long d, LocalDate dt) { return List.of(); }
        @Override public boolean isSlotBooked(Long d, LocalDate dt, LocalTime s, LocalTime e) { return false; }
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
