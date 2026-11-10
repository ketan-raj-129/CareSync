package com.caresync.service;

import com.caresync.dao.AppointmentDao;
import com.caresync.dao.AuditLogDao;
import com.caresync.dao.MedicalRecordDao;
import com.caresync.dao.PrescriptionDao;
import com.caresync.dto.ConsultationRequestDto;
import com.caresync.dto.PrescriptionItemDto;
import com.caresync.exception.ValidationException;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.AuditLog;
import com.caresync.model.MedicalRecord;
import com.caresync.model.Prescription;
import com.caresync.model.PrescriptionItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CareSync ClinicalService Tests")
class ClinicalServiceTest {

    private FakeAppointmentDao appointmentDao;
    private FakeMedicalRecordDao medicalRecordDao;
    private FakePrescriptionDao prescriptionDao;
    private FakeAuditLogDao auditLogDao;

    private AtomicBoolean committed;
    private AtomicBoolean rolledBack;
    private ClinicalService clinicalService;

    private static final Long DOCTOR_A_ID = 401L;
    private static final Long DOCTOR_B_ID = 402L;
    private static final Long PATIENT_ID = 501L;
    private static final Long ACTOR_USER_ID = 70L;

    @BeforeEach
    void setUp() {
        appointmentDao = new FakeAppointmentDao();
        medicalRecordDao = new FakeMedicalRecordDao();
        prescriptionDao = new FakePrescriptionDao();
        auditLogDao = new FakeAuditLogDao();

        committed = new AtomicBoolean(false);
        rolledBack = new AtomicBoolean(false);

        Connection mockConnection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("commit".equals(method.getName())) {
                        committed.set(true);
                        return null;
                    }
                    if ("rollback".equals(method.getName())) {
                        rolledBack.set(true);
                        return null;
                    }
                    if ("setAutoCommit".equals(method.getName())) {
                        return null;
                    }
                    if ("getAutoCommit".equals(method.getName())) {
                        return true;
                    }
                    if ("close".equals(method.getName())) {
                        return null;
                    }
                    return null;
                }
        );

        clinicalService = new ClinicalServiceImpl(
                appointmentDao, medicalRecordDao, prescriptionDao, auditLogDao, () -> mockConnection);
    }

    @Test
    @DisplayName("Verify recordConsultation successfully documents findings, creates Rx, and completes appointment")
    void testRecordConsultationSuccess() {
        Appointment apt = new Appointment(100L);
        apt.setDoctorId(DOCTOR_A_ID);
        apt.setPatientId(PATIENT_ID);
        apt.setStatus(AppointmentStatus.CONFIRMED);
        apt.setReferenceNumber("APT-100");
        appointmentDao.save(apt);

        ConsultationRequestDto dto = new ConsultationRequestDto();
        dto.setAppointmentId(100L);
        dto.setSymptoms("High fever, dry cough, lethargy");
        dto.setDiagnosis("Acute Bronchitis");
        dto.setTreatmentPlan("Antibiotics course and respiratory rest");
        dto.setClinicalNotes("SpO2 98%, chest clear on auscultation");
        dto.setGeneralAdvice("Rest for 5 days and hydrate well");

        List<PrescriptionItemDto> items = List.of(
                new PrescriptionItemDto("Azithromycin 500mg", "1 tablet", "Once daily", "3 days", "After food"),
                new PrescriptionItemDto("Paracetamol 650mg", "1 tablet", "SOS", "5 days", "When temperature > 100F")
        );
        dto.setPrescriptionItems(items);

        boolean recorded = clinicalService.recordConsultation(
                dto, DOCTOR_A_ID, ACTOR_USER_ID, "127.0.0.1", "TestAgent");

        assertTrue(recorded);
        assertTrue(committed.get(), "Transaction must be committed");
        assertFalse(rolledBack.get(), "Transaction must not be rolled back");

        // Verify Medical Record
        Optional<MedicalRecord> recordOpt = medicalRecordDao.findByAppointmentId(100L);
        assertTrue(recordOpt.isPresent());
        assertEquals("Acute Bronchitis", recordOpt.get().getDiagnosis());

        // Verify Prescription and Items
        Optional<Prescription> rxOpt = prescriptionDao.findByAppointmentId(100L);
        assertTrue(rxOpt.isPresent());
        assertEquals(2, prescriptionDao.findItemsByPrescriptionId(rxOpt.get().getId()).size());

        // Verify Appointment Status transitioned to COMPLETED
        assertEquals(AppointmentStatus.COMPLETED, appointmentDao.findById(100L).orElseThrow().getStatus());

        // Verify Audit Log
        assertEquals(1, auditLogDao.getLogs().size());
        assertEquals("CONSULTATION_DOCUMENTED", auditLogDao.getLogs().get(0).getAction());
    }

    @Test
    @DisplayName("Verify recordConsultation fails when symptoms or diagnosis are missing")
    void testRecordConsultationValidation() {
        Appointment apt = new Appointment(101L);
        apt.setDoctorId(DOCTOR_A_ID);
        apt.setStatus(AppointmentStatus.CONFIRMED);
        appointmentDao.save(apt);

        ConsultationRequestDto dto = new ConsultationRequestDto();
        dto.setAppointmentId(101L);
        // Missing symptoms and diagnosis

        ValidationException ex = assertThrows(ValidationException.class, () ->
                clinicalService.recordConsultation(dto, DOCTOR_A_ID, ACTOR_USER_ID, "127.0.0.1", "TestAgent")
        );
        assertTrue(ex.getMessage().contains("Symptoms are required"));
    }

    @Test
    @DisplayName("Verify recordConsultation prevents unauthorized doctor access")
    void testRecordConsultationUnauthorizedDoctor() {
        Appointment apt = new Appointment(102L);
        apt.setDoctorId(DOCTOR_A_ID);
        apt.setStatus(AppointmentStatus.CONFIRMED);
        appointmentDao.save(apt);

        ConsultationRequestDto dto = new ConsultationRequestDto();
        dto.setAppointmentId(102L);
        dto.setSymptoms("Fever");
        dto.setDiagnosis("Flu");

        ValidationException ex = assertThrows(ValidationException.class, () ->
                clinicalService.recordConsultation(dto, DOCTOR_B_ID, ACTOR_USER_ID, "127.0.0.1", "TestAgent")
        );
        assertTrue(ex.getMessage().contains("Unauthorized attempt"));
    }

    @Test
    @DisplayName("Verify patient medical history access is denied if doctor has no encounters with patient (IDOR Defense)")
    void testGetPatientHistoryDeniedForUnrelatedDoctor() {
        // Patient has appointment with Doctor A only
        Appointment apt = new Appointment(103L);
        apt.setDoctorId(DOCTOR_A_ID);
        apt.setPatientId(PATIENT_ID);
        appointmentDao.save(apt);

        // Doctor B tries to access patient medical history
        ValidationException ex = assertThrows(ValidationException.class, () ->
                clinicalService.getPatientMedicalHistory(PATIENT_ID, DOCTOR_B_ID)
        );
        assertTrue(ex.getMessage().contains("unassigned patient"));

        // Doctor A can access
        assertDoesNotThrow(() -> clinicalService.getPatientMedicalHistory(PATIENT_ID, DOCTOR_A_ID));
    }

    // =========================================================================
    // Test DAO implementations
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
        public List<Appointment> findByPatientId(Long patientId) {
            return store.values().stream()
                    .filter(a -> patientId.equals(a.getPatientId()))
                    .collect(Collectors.toList());
        }

        @Override
        public boolean updateStatus(Long id, AppointmentStatus status, String reason, Long cancelledBy) {
            Appointment apt = store.get(id);
            if (apt != null) {
                apt.setStatus(status);
                return true;
            }
            return false;
        }

        @Override public Optional<Appointment> findWithDetailsById(Long id) { return findById(id); }
        @Override public Optional<Appointment> findByReference(String ref) { return Optional.empty(); }
        @Override public List<Appointment> findByDoctorId(Long d) { return List.of(); }
        @Override public List<Appointment> findWithDetailsByDoctorId(Long d) { return List.of(); }
        @Override public List<Appointment> findByDoctorAndDate(Long d, LocalDate dt) { return List.of(); }
        @Override public boolean isSlotBooked(Long d, LocalDate dt, LocalTime s, LocalTime e) { return false; }
        @Override public boolean update(Appointment entity) { return true; }
        @Override public boolean deleteById(Long id) { return store.remove(id) != null; }
        @Override public List<Appointment> findAll() { return new ArrayList<>(store.values()); }
    }

    private static class FakeMedicalRecordDao implements MedicalRecordDao {
        private final Map<Long, MedicalRecord> store = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(1);

        @Override
        public Optional<MedicalRecord> findByAppointmentId(Long appointmentId) {
            return store.values().stream()
                    .filter(r -> appointmentId.equals(r.getAppointmentId()))
                    .findFirst();
        }

        @Override
        public List<MedicalRecord> findByPatientId(Long patientId) {
            return store.values().stream()
                    .filter(r -> patientId.equals(r.getPatientId()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<MedicalRecord> findByDoctorId(Long doctorId) {
            return store.values().stream()
                    .filter(r -> doctorId.equals(r.getDoctorId()))
                    .collect(Collectors.toList());
        }

        @Override
        public MedicalRecord save(MedicalRecord record, Connection conn) {
            if (record.getId() == null) record.setId(seq.getAndIncrement());
            store.put(record.getId(), record);
            return record;
        }

        @Override
        public MedicalRecord save(MedicalRecord record) {
            return save(record, null);
        }

        @Override
        public boolean update(MedicalRecord record) {
            store.put(record.getId(), record);
            return true;
        }

        @Override public Optional<MedicalRecord> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<MedicalRecord> findAll() { return new ArrayList<>(store.values()); }
        @Override public boolean deleteById(Long id) { return store.remove(id) != null; }
    }

    private static class FakePrescriptionDao implements PrescriptionDao {
        private final Map<Long, Prescription> rxStore = new HashMap<>();
        private final Map<Long, List<PrescriptionItem>> itemsStore = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(1);
        private final AtomicLong itemSeq = new AtomicLong(1);

        @Override
        public Optional<Prescription> findByAppointmentId(Long appointmentId) {
            return rxStore.values().stream()
                    .filter(p -> appointmentId.equals(p.getAppointmentId()))
                    .findFirst();
        }

        @Override
        public List<Prescription> findByPatientId(Long patientId) {
            return rxStore.values().stream()
                    .filter(p -> patientId.equals(p.getPatientId()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<Prescription> findByDoctorId(Long doctorId) {
            return rxStore.values().stream()
                    .filter(p -> doctorId.equals(p.getDoctorId()))
                    .collect(Collectors.toList());
        }

        @Override
        public Prescription save(Prescription prescription, Connection conn) {
            if (prescription.getId() == null) prescription.setId(seq.getAndIncrement());
            rxStore.put(prescription.getId(), prescription);

            if (prescription.getItems() != null) {
                for (PrescriptionItem item : prescription.getItems()) {
                    item.setPrescriptionId(prescription.getId());
                    saveItem(item, conn);
                }
            }
            return prescription;
        }

        @Override
        public Prescription save(Prescription prescription) {
            return save(prescription, null);
        }

        @Override
        public List<PrescriptionItem> findItemsByPrescriptionId(Long prescriptionId) {
            return itemsStore.getOrDefault(prescriptionId, new ArrayList<>());
        }

        @Override
        public PrescriptionItem saveItem(PrescriptionItem item, Connection conn) {
            if (item.getId() == null) item.setId(itemSeq.getAndIncrement());
            itemsStore.computeIfAbsent(item.getPrescriptionId(), k -> new ArrayList<>()).add(item);
            return item;
        }

        @Override public Optional<Prescription> findById(Long id) { return Optional.ofNullable(rxStore.get(id)); }
        @Override public List<Prescription> findAll() { return new ArrayList<>(rxStore.values()); }
        @Override public boolean update(Prescription entity) { return true; }
        @Override public boolean deleteById(Long id) { return rxStore.remove(id) != null; }
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
