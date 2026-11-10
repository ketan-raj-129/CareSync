package com.caresync.service;

import com.caresync.dao.AuditLogDao;
import com.caresync.dao.PatientDao;
import com.caresync.dao.UserDao;
import com.caresync.dto.PatientProfileUpdateDto;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.UnauthorizedException;
import com.caresync.exception.ValidationException;
import com.caresync.model.AuditLog;
import com.caresync.model.BloodGroup;
import com.caresync.model.Gender;
import com.caresync.model.Patient;
import com.caresync.model.Role;
import com.caresync.model.User;
import com.caresync.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CareSync PatientService Tests")
class PatientServiceTest {

    private InMemoryPatientDao patientDao;
    private InMemoryUserDao userDao;
    private InMemoryAuditDao auditLogDao;
    private PatientService patientService;

    private static final Long USER_ID = 101L;
    private static final Long PATIENT_ID = 501L;

    @BeforeEach
    void setUp() {
        patientDao = new InMemoryPatientDao();
        userDao = new InMemoryUserDao();
        auditLogDao = new InMemoryAuditDao();

        patientService = new PatientServiceImpl(patientDao, userDao, auditLogDao);

        User user = new User();
        user.setId(USER_ID);
        user.setEmail("alice@caresync.com");
        user.setFirstName("Alice");
        user.setLastName("Wonderland");
        user.setRole(Role.PATIENT);
        user.setStatus(UserStatus.ACTIVE);
        userDao.save(user);

        Patient patient = new Patient();
        patient.setId(USER_ID);
        patient.setPatientProfileId(PATIENT_ID);
        patient.setDateOfBirth(LocalDate.of(1995, 5, 20));
        patient.setGender(Gender.FEMALE);
        patient.setBloodGroup(BloodGroup.O_POS);
        patient.setAddress("123 Maple St");
        patientDao.save(patient);
    }

    @Test
    @DisplayName("Verify getPatientByUserId returns populated patient entity")
    void testGetPatientByUserId() {
        Optional<Patient> patientOpt = patientService.getPatientByUserId(USER_ID);
        assertTrue(patientOpt.isPresent());
        assertEquals(PATIENT_ID, patientOpt.get().getPatientProfileId());
    }

    @Test
    @DisplayName("Verify updatePatientProfile succeeds when authorized and updates demographic fields")
    void testSuccessfulPatientProfileUpdate() {
        PatientProfileUpdateDto dto = new PatientProfileUpdateDto();
        dto.setFirstName("Alicia");
        dto.setLastName("Wonderland");
        dto.setPhoneNumber("+15551234567");
        dto.setDateOfBirth(LocalDate.of(1995, 5, 20));
        dto.setGender(Gender.FEMALE);
        dto.setBloodGroup(BloodGroup.A_POS);
        dto.setAddress("456 Elm Avenue");
        dto.setEmergencyContactName("Bob Wonderland");
        dto.setEmergencyContactPhone("+15559876543");

        boolean result = patientService.updatePatientProfile(
                USER_ID, PATIENT_ID, dto, USER_ID, "127.0.0.1", "JUnit-Agent"
        );

        assertTrue(result, "Profile update should succeed");

        User updatedUser = userDao.findById(USER_ID).orElseThrow();
        assertEquals("Alicia", updatedUser.getFirstName());
        assertEquals("+15551234567", updatedUser.getPhoneNumber());

        Patient updatedPatient = patientDao.findById(PATIENT_ID).orElseThrow();
        assertEquals(BloodGroup.A_POS, updatedPatient.getBloodGroup());
        assertEquals("456 Elm Avenue", updatedPatient.getAddress());
        assertEquals("Bob Wonderland", updatedPatient.getEmergencyContactName());
    }

    @Test
    @DisplayName("Verify updatePatientProfile rejects unauthorized cross-user modifications")
    void testUnauthorizedProfileUpdateRejected() {
        PatientProfileUpdateDto dto = new PatientProfileUpdateDto();
        dto.setFirstName("Attacker");
        dto.setLastName("User");

        Long unauthorizedActor = 999L; // Different user!
        assertThrows(UnauthorizedException.class, () ->
                patientService.updatePatientProfile(USER_ID, PATIENT_ID, dto, unauthorizedActor, "127.0.0.1", "JUnit"));
    }

    @Test
    @DisplayName("Verify updatePatientProfile rejects empty first or last name")
    void testProfileUpdateValidationFailure() {
        PatientProfileUpdateDto dto = new PatientProfileUpdateDto();
        dto.setFirstName(""); // Blank
        dto.setLastName("ValidLast");

        assertThrows(ValidationException.class, () ->
                patientService.updatePatientProfile(USER_ID, PATIENT_ID, dto, USER_ID, "127.0.0.1", "JUnit"));
    }

    // --- In-Memory Test Doubles ---

    private static class InMemoryUserDao implements UserDao {
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
            return findByEmail(email).isPresent();
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public User save(User user) {
            store.put(user.getId(), user);
            return user;
        }

        @Override
        public User save(User user, Connection conn) {
            return save(user);
        }

        @Override
        public boolean update(User user) {
            store.put(user.getId(), user);
            return true;
        }

        @Override
        public boolean updatePassword(Long userId, String passwordHash, String passwordSalt, int iterations) {
            return true;
        }

        @Override
        public boolean updatePassword(Long userId, String passwordHash, String passwordSalt, int iterations, Connection conn) {
            return true;
        }

        @Override
        public boolean updateStatus(Long userId, UserStatus status) {
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

    private static class InMemoryAuditDao implements AuditLogDao {
        @Override
        public Optional<AuditLog> findById(Long id) { return Optional.empty(); }
        @Override
        public List<AuditLog> findAll() { return List.of(); }
        @Override
        public List<AuditLog> findRecent(int limit) { return List.of(); }
        @Override
        public List<AuditLog> findByUserId(Long userId, int limit) { return List.of(); }
        @Override
        public List<AuditLog> findByAction(String action, int limit) { return List.of(); }
        @Override
        public Optional<AuditLog> findLatestLog() { return Optional.empty(); }
        @Override
        public AuditLog save(AuditLog log) { return log; }
        @Override
        public boolean update(AuditLog log) { return true; }
        @Override
        public boolean deleteById(Long id) { return true; }
        @Override
        public AuditLog log(Long userId, String action, String targetEntity, Long targetId, String ipAddress, String userAgent, String details, Connection conn) {
            return new AuditLog();
        }
        @Override
        public AuditLog log(Long userId, String action, String targetEntity, Long targetId, String ipAddress, String userAgent, String details) {
            return new AuditLog();
        }
    }
}
