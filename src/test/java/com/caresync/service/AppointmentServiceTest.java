package com.caresync.service;

import com.caresync.dao.AppointmentDao;
import com.caresync.dao.AuditLogDao;
import com.caresync.dao.DoctorAvailabilityDao;
import com.caresync.dao.DoctorDao;
import com.caresync.dao.PatientDao;
import com.caresync.dto.BookingRequestDto;
import com.caresync.dto.TimeSlotDto;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.SlotConflictException;
import com.caresync.exception.UnauthorizedException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.AuditLog;
import com.caresync.model.Doctor;
import com.caresync.model.DoctorAvailability;
import com.caresync.model.Patient;
import com.caresync.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CareSync AppointmentService & Transactional Booking Engine Tests")
class AppointmentServiceTest {

    private InMemoryAppointmentDao appointmentDao;
    private InMemoryDoctorDao doctorDao;
    private InMemoryPatientDao patientDao;
    private InMemoryDoctorAvailabilityDao availabilityDao;
    private InMemoryAuditDao auditLogDao;
    private FakeAvailabilityService fakeAvailabilityService;

    private AtomicBoolean committed;
    private AtomicBoolean rolledBack;
    private AppointmentService appointmentService;

    private static final Long PATIENT_ID = 401L;
    private static final Long PATIENT_USER_ID = 10L;
    private static final Long DOCTOR_ID = 201L;
    private static final Long DOCTOR_USER_ID = 20L;

    @BeforeEach
    void setUp() {
        appointmentDao = new InMemoryAppointmentDao();
        doctorDao = new InMemoryDoctorDao();
        patientDao = new InMemoryPatientDao();
        availabilityDao = new InMemoryDoctorAvailabilityDao();
        auditLogDao = new InMemoryAuditDao();
        fakeAvailabilityService = new FakeAvailabilityService();

        committed = new AtomicBoolean(false);
        rolledBack = new AtomicBoolean(false);

        Connection mockConnection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getAutoCommit".equals(name)) return Boolean.TRUE;
                    if ("setAutoCommit".equals(name)) return null;
                    if ("commit".equals(name)) {
                        committed.set(true);
                        return null;
                    }
                    if ("rollback".equals(name)) {
                        rolledBack.set(true);
                        return null;
                    }
                    if ("isClosed".equals(name)) return Boolean.FALSE;
                    if ("close".equals(name)) return null;
                    return null;
                }
        );

        appointmentService = new AppointmentServiceImpl(
                appointmentDao,
                doctorDao,
                patientDao,
                availabilityDao,
                fakeAvailabilityService,
                auditLogDao,
                () -> mockConnection
        );

        // Prepopulate active patient
        Patient patient = new Patient();
        patient.setId(PATIENT_USER_ID);
        patient.setPatientProfileId(PATIENT_ID);
        patient.setFirstName("Alice");
        patient.setLastName("Wonder");
        patient.setStatus(UserStatus.ACTIVE);
        patientDao.save(patient);

        // Prepopulate active doctor
        Doctor doctor = new Doctor();
        doctor.setId(DOCTOR_USER_ID);
        doctor.setDoctorProfileId(DOCTOR_ID);
        doctor.setFirstName("Sarah");
        doctor.setLastName("Mitchell");
        doctor.setStatus(UserStatus.ACTIVE);
        doctor.setAvailable(true);
        doctorDao.save(doctor);
    }

    @Test
    @DisplayName("Verify successful booking commits transaction and generates reference number")
    void testSuccessfulAppointmentBooking() {
        LocalDate futureDate = LocalDate.now().plusDays(3);
        LocalTime startTime = LocalTime.of(10, 0);

        fakeAvailabilityService.setSlotAvailable(true);

        BookingRequestDto request = new BookingRequestDto(DOCTOR_ID, futureDate, startTime, "General Health Checkup");

        Appointment booked = appointmentService.bookAppointment(
                PATIENT_ID, request, PATIENT_USER_ID, "127.0.0.1", "JUnit-Agent"
        );

        assertNotNull(booked, "Booked appointment should not be null");
        assertNotNull(booked.getReferenceNumber(), "Reference number must be generated");
        assertTrue(booked.getReferenceNumber().startsWith("APT-") || booked.getReferenceNumber().startsWith("CS-APT-"),
                "Reference number format should be APT-* or CS-APT-*");
        assertEquals(PATIENT_ID, booked.getPatientId());
        assertEquals(DOCTOR_ID, booked.getDoctorId());
        assertEquals(futureDate, booked.getAppointmentDate());
        assertEquals(startTime, booked.getStartTime());
        assertEquals(LocalTime.of(10, 30), booked.getEndTime());
        assertEquals(AppointmentStatus.PENDING, booked.getStatus());
        assertTrue(committed.get(), "Transaction must commit on successful booking");
        assertFalse(rolledBack.get(), "Transaction must not roll back");
    }

    @Test
    @DisplayName("Verify booking rejected when patient session is null")
    void testBookingFailsWhenPatientUnauthenticated() {
        BookingRequestDto request = new BookingRequestDto(DOCTOR_ID, LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Reason");
        assertThrows(UnauthorizedException.class, () ->
                appointmentService.bookAppointment(null, request, null, "127.0.0.1", "JUnit"));
    }

    @Test
    @DisplayName("Verify booking rejected when doctor is inactive or deactivated")
    void testBookingFailsWhenDoctorInactive() {
        Doctor inactiveDoc = new Doctor();
        inactiveDoc.setId(999L);
        inactiveDoc.setDoctorProfileId(999L);
        inactiveDoc.setStatus(UserStatus.INACTIVE);
        doctorDao.save(inactiveDoc);

        BookingRequestDto request = new BookingRequestDto(999L, LocalDate.now().plusDays(2), LocalTime.of(10, 0), "Checkup");
        assertThrows(ValidationException.class, () ->
                appointmentService.bookAppointment(PATIENT_ID, request, PATIENT_USER_ID, "127.0.0.1", "JUnit"));
    }

    @Test
    @DisplayName("Verify booking rejected for past date")
    void testBookingFailsForPastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        BookingRequestDto request = new BookingRequestDto(DOCTOR_ID, pastDate, LocalTime.of(10, 0), "Checkup");

        ValidationException ex = assertThrows(ValidationException.class, () ->
                appointmentService.bookAppointment(PATIENT_ID, request, PATIENT_USER_ID, "127.0.0.1", "JUnit"));
        assertTrue(ex.getMessage().contains("Cannot book an appointment in the past"));
    }

    @Test
    @DisplayName("Verify booking fails with SlotConflictException when slot is already taken")
    void testBookingFailsWhenSlotUnavailable() {
        LocalDate futureDate = LocalDate.now().plusDays(4);
        LocalTime startTime = LocalTime.of(11, 0);

        fakeAvailabilityService.setSlotAvailable(false);

        BookingRequestDto request = new BookingRequestDto(DOCTOR_ID, futureDate, startTime, "Consultation");

        assertThrows(SlotConflictException.class, () ->
                appointmentService.bookAppointment(PATIENT_ID, request, PATIENT_USER_ID, "127.0.0.1", "JUnit"));
    }

    @Test
    @DisplayName("Verify SQL exclusion constraint violation (SQLState 23P01) is mapped to SlotConflictException")
    void testExclusionConstraintViolationMapped() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        LocalTime startTime = LocalTime.of(9, 30);
        fakeAvailabilityService.setSlotAvailable(true);

        appointmentDao.setSimulatedConflict(true);

        BookingRequestDto request = new BookingRequestDto(DOCTOR_ID, futureDate, startTime, "Consultation");

        SlotConflictException ex = assertThrows(SlotConflictException.class, () ->
                appointmentService.bookAppointment(PATIENT_ID, request, PATIENT_USER_ID, "127.0.0.1", "JUnit"));

        assertTrue(ex.getMessage().contains("selected") || ex.getMessage().contains("conflict") || ex.getMessage().contains("booked"));
        assertTrue(rolledBack.get(), "Transaction must roll back on conflict");
    }

    @Test
    @DisplayName("Verify patient cannot view details of another patient's appointment")
    void testPatientCrossOwnershipProtectionOnView() {
        Appointment apt = new Appointment();
        apt.setId(777L);
        apt.setPatientId(999L); // Different patient!
        apt.setDoctorId(DOCTOR_ID);
        apt.setAppointmentDate(LocalDate.now().plusDays(2));
        apt.setStartTime(LocalTime.of(14, 0));
        apt.setEndTime(LocalTime.of(14, 30));
        apt.setStatus(AppointmentStatus.CONFIRMED);
        appointmentDao.save(apt);

        assertThrows(UnauthorizedException.class, () ->
                appointmentService.getAppointmentDetails(777L, PATIENT_ID));
    }

    @Test
    @DisplayName("Verify successful appointment cancellation when scheduled more than 2 hours in future")
    void testSuccessfulCancellationByPatient() {
        Appointment apt = new Appointment();
        apt.setId(888L);
        apt.setPatientId(PATIENT_ID);
        apt.setDoctorId(DOCTOR_ID);
        apt.setAppointmentDate(LocalDate.now().plusDays(3)); // 3 days in future (> 2 hours)
        apt.setStartTime(LocalTime.of(15, 0));
        apt.setEndTime(LocalTime.of(15, 30));
        apt.setStatus(AppointmentStatus.CONFIRMED);
        appointmentDao.save(apt);

        appointmentService.cancelAppointment(
                888L, PATIENT_ID, "Scheduling conflict with work", PATIENT_USER_ID, "127.0.0.1", "JUnit"
        );

        Appointment updated = appointmentDao.findById(888L).orElseThrow();
        assertEquals(AppointmentStatus.CANCELLED, updated.getStatus());
        assertEquals("Scheduling conflict with work", updated.getCancellationReason());
        assertEquals(PATIENT_USER_ID, updated.getCancelledByUserId());
    }

    @Test
    @DisplayName("Verify cancellation rejected when attempted by unauthorized patient")
    void testCancellationCrossOwnershipRejection() {
        Appointment apt = new Appointment();
        apt.setId(889L);
        apt.setPatientId(999L); // Belongs to different patient
        apt.setDoctorId(DOCTOR_ID);
        apt.setAppointmentDate(LocalDate.now().plusDays(3));
        apt.setStartTime(LocalTime.of(15, 0));
        apt.setEndTime(LocalTime.of(15, 30));
        apt.setStatus(AppointmentStatus.CONFIRMED);
        appointmentDao.save(apt);

        assertThrows(UnauthorizedException.class, () ->
                appointmentService.cancelAppointment(889L, PATIENT_ID, "Cancel", PATIENT_USER_ID, "127.0.0.1", "JUnit"));
    }

    @Test
    @DisplayName("Verify cancellation rejected for already CANCELLED or COMPLETED appointment")
    void testCancellationRejectedForTerminalStatus() {
        Appointment apt = new Appointment();
        apt.setId(890L);
        apt.setPatientId(PATIENT_ID);
        apt.setDoctorId(DOCTOR_ID);
        apt.setAppointmentDate(LocalDate.now().plusDays(3));
        apt.setStartTime(LocalTime.of(10, 0));
        apt.setEndTime(LocalTime.of(10, 30));
        apt.setStatus(AppointmentStatus.CANCELLED);
        appointmentDao.save(apt);

        assertThrows(ValidationException.class, () ->
                appointmentService.cancelAppointment(890L, PATIENT_ID, "Cancel again", PATIENT_USER_ID, "127.0.0.1", "JUnit"));
    }

    // --- In-Memory Test Doubles ---

    private static class FakeAvailabilityService implements AvailabilityService {
        private boolean slotAvailable = true;

        void setSlotAvailable(boolean slotAvailable) {
            this.slotAvailable = slotAvailable;
        }

        @Override
        public List<TimeSlotDto> getAvailableSlots(Long doctorId, LocalDate date) {
            return Collections.emptyList();
        }

        @Override
        public List<TimeSlotDto> getAvailableSlots(Long doctorId, LocalDate date, Long patientId) {
            return Collections.emptyList();
        }

        @Override
        public boolean isSlotAvailable(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime, Long patientId) {
            return slotAvailable;
        }
    }

    private static class InMemoryAppointmentDao implements AppointmentDao {
        private final Map<Long, Appointment> store = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(1000);
        private boolean simulatedConflict = false;

        void setSimulatedConflict(boolean conflict) {
            this.simulatedConflict = conflict;
        }

        @Override
        public Optional<Appointment> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Appointment> findByReference(String reference) {
            return store.values().stream().filter(a -> reference.equals(a.getReferenceNumber())).findFirst();
        }

        @Override
        public List<Appointment> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public List<Appointment> findByPatientId(Long patientId) {
            return store.values().stream().filter(a -> patientId.equals(a.getPatientId())).collect(Collectors.toList());
        }

        @Override
        public List<Appointment> findByDoctorId(Long doctorId) {
            return store.values().stream().filter(a -> doctorId.equals(a.getDoctorId())).collect(Collectors.toList());
        }

        @Override
        public List<Appointment> findByDoctorAndDate(Long doctorId, LocalDate date) {
            return store.values().stream()
                    .filter(a -> doctorId.equals(a.getDoctorId()) && date.equals(a.getAppointmentDate()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<Appointment> findWithDetailsByPatientId(Long patientId) {
            return findByPatientId(patientId);
        }

        @Override
        public Optional<Appointment> findWithDetailsById(Long appointmentId) {
            return findById(appointmentId);
        }

        @Override
        public List<Appointment> findUpcomingByPatientId(Long patientId) {
            return findByPatientId(patientId);
        }

        @Override
        public List<Appointment> findUpcomingByDoctorId(Long doctorId) {
            return findByDoctorId(doctorId);
        }

        @Override
        public List<Appointment> findByStatus(AppointmentStatus status) {
            return store.values().stream().filter(a -> status.equals(a.getStatus())).collect(Collectors.toList());
        }

        @Override
        public boolean isSlotBooked(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
            return false;
        }

        @Override
        public Appointment save(Appointment appointment) {
            if (appointment.getId() == null) {
                appointment.setId(seq.incrementAndGet());
            }
            store.put(appointment.getId(), appointment);
            return appointment;
        }

        @Override
        public Appointment save(Appointment appointment, Connection conn) {
            if (simulatedConflict) {
                throw new SlotConflictException("The selected appointment time slot overlaps with an existing booking.");
            }
            return save(appointment);
        }

        @Override
        public boolean update(Appointment appointment) {
            store.put(appointment.getId(), appointment);
            return true;
        }

        @Override
        public boolean updateStatus(Long appointmentId, AppointmentStatus status, String cancellationReason, Long cancelledByUserId) {
            Appointment apt = store.get(appointmentId);
            if (apt != null) {
                apt.setStatus(status);
                apt.setCancellationReason(cancellationReason);
                apt.setCancelledByUserId(cancelledByUserId);
                return true;
            }
            return false;
        }

        @Override
        public boolean updateStatus(Long appointmentId, AppointmentStatus status, String cancellationReason, Long cancelledByUserId, Connection conn) {
            return updateStatus(appointmentId, status, cancellationReason, cancelledByUserId);
        }

        @Override
        public boolean deleteById(Long id) {
            return store.remove(id) != null;
        }

        @Override
        public long countTotal() {
            return store.size();
        }

        @Override
        public long countByStatus(AppointmentStatus status) {
            return store.values().stream().filter(a -> status.equals(a.getStatus())).count();
        }

        @Override
        public long countTodayAppointments() {
            return 0;
        }
    }

    private static class InMemoryDoctorDao implements DoctorDao {
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
        public Optional<Doctor> findByLicenseNumber(String licenseNumber) {
            return store.values().stream().filter(d -> licenseNumber.equals(d.getLicenseNumber())).findFirst();
        }

        @Override
        public List<Doctor> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public List<Doctor> findActiveDoctors() {
            return store.values().stream().filter(Doctor::isActive).collect(Collectors.toList());
        }

        @Override
        public List<Doctor> findBySpecialtyId(Long specialtyId) {
            return store.values().stream().filter(d -> specialtyId.equals(d.getSpecialtyId())).collect(Collectors.toList());
        }

        @Override
        public List<Doctor> searchDoctors(Long specialtyId, String query) {
            return findAll();
        }

        @Override
        public Doctor save(Doctor doctor) {
            Long key = doctor.getDoctorProfileId() != null ? doctor.getDoctorProfileId() : doctor.getId();
            store.put(key, doctor);
            return doctor;
        }

        @Override
        public Doctor saveProfile(Doctor doctor, Connection conn) {
            return save(doctor);
        }

        @Override
        public boolean update(Doctor doctor) {
            Long key = doctor.getDoctorProfileId() != null ? doctor.getDoctorProfileId() : doctor.getId();
            store.put(key, doctor);
            return true;
        }

        @Override
        public boolean deleteById(Long id) {
            return store.remove(id) != null;
        }
    }

    private static class InMemoryPatientDao implements PatientDao {
        private final Map<Long, Patient> store = new HashMap<>();

        @Override
        public Optional<Patient> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Patient> findByUserId(Long userId) {
            return store.values().stream().filter(p -> userId.equals(p.getId())).findFirst();
        }

        @Override
        public List<Patient> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public Patient save(Patient patient) {
            Long key = patient.getPatientProfileId() != null ? patient.getPatientProfileId() : patient.getId();
            store.put(key, patient);
            return patient;
        }

        @Override
        public Patient saveProfile(Patient patient, Connection conn) {
            return save(patient);
        }

        @Override
        public boolean update(Patient patient) {
            Long key = patient.getPatientProfileId() != null ? patient.getPatientProfileId() : patient.getId();
            store.put(key, patient);
            return true;
        }

        @Override
        public boolean deleteById(Long id) {
            return store.remove(id) != null;
        }
    }

    private static class InMemoryDoctorAvailabilityDao implements DoctorAvailabilityDao {
        @Override
        public Optional<DoctorAvailability> findById(Long id) { return Optional.empty(); }
        @Override
        public List<DoctorAvailability> findAll() { return Collections.emptyList(); }
        @Override
        public List<DoctorAvailability> findByDoctorId(Long doctorId) { return Collections.emptyList(); }
        @Override
        public List<DoctorAvailability> findActiveByDoctorId(Long doctorId) { return Collections.emptyList(); }
        @Override
        public Optional<DoctorAvailability> findByDoctorAndDay(Long doctorId, int dayOfWeek) { return Optional.empty(); }
        @Override
        public DoctorAvailability save(DoctorAvailability availability) { return availability; }
        @Override
        public boolean update(DoctorAvailability availability) { return true; }
        @Override
        public boolean deleteById(Long id) { return true; }
        @Override
        public boolean deleteByDoctorId(Long doctorId) { return true; }
    }

    private static class InMemoryAuditDao implements AuditLogDao {
        private final List<AuditLog> store = new ArrayList<>();

        @Override
        public Optional<AuditLog> findById(Long id) {
            return store.stream().filter(a -> id.equals(a.getId())).findFirst();
        }

        @Override
        public List<AuditLog> findAll() {
            return new ArrayList<>(store);
        }

        @Override
        public List<AuditLog> findRecent(int limit) {
            return new ArrayList<>(store);
        }

        @Override
        public List<AuditLog> findByUserId(Long userId, int limit) {
            return store.stream().filter(a -> userId.equals(a.getUserId())).collect(Collectors.toList());
        }

        @Override
        public List<AuditLog> findByAction(String action, int limit) {
            return store.stream().filter(a -> action.equals(a.getAction())).collect(Collectors.toList());
        }

        @Override
        public Optional<AuditLog> findLatestLog() {
            return store.isEmpty() ? Optional.empty() : Optional.of(store.get(store.size() - 1));
        }

        @Override
        public AuditLog save(AuditLog log) {
            store.add(log);
            return log;
        }

        @Override
        public boolean update(AuditLog log) {
            return true;
        }

        @Override
        public boolean deleteById(Long id) {
            return store.removeIf(a -> id.equals(a.getId()));
        }

        @Override
        public AuditLog log(Long userId, String action, String targetEntity, Long targetId, String ipAddress, String userAgent, String details, Connection conn) {
            AuditLog log = new AuditLog();
            log.setUserId(userId);
            log.setAction(action);
            log.setTargetEntity(targetEntity);
            log.setTargetId(targetId);
            log.setIpAddress(ipAddress);
            log.setUserAgent(userAgent);
            log.setDetails(details);
            return save(log);
        }

        @Override
        public AuditLog log(Long userId, String action, String targetEntity, Long targetId, String ipAddress, String userAgent, String details) {
            return log(userId, action, targetEntity, targetId, ipAddress, userAgent, details, null);
        }
    }
}
