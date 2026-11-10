package com.caresync.service;

import com.caresync.dao.AuditLogDao;
import com.caresync.dao.PatientDao;
import com.caresync.dao.UserDao;
import com.caresync.dto.AuthenticationResult;
import com.caresync.dto.PatientRegistrationDto;
import com.caresync.dto.SessionPrincipal;
import com.caresync.exception.ValidationException;
import com.caresync.model.AuditLog;
import com.caresync.model.Gender;
import com.caresync.model.Patient;
import com.caresync.model.Role;
import com.caresync.model.User;
import com.caresync.model.UserStatus;
import com.caresync.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CareSync AuthService and Authentication Flow Tests")
class AuthServiceTest {

    private InMemoryUserDao userDao;
    private InMemoryPatientDao patientDao;
    private InMemoryAuditDao auditLogDao;
    private PasswordHasher testHasher;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userDao = new InMemoryUserDao();
        patientDao = new InMemoryPatientDao();
        auditLogDao = new InMemoryAuditDao();
        testHasher = new PasswordHasher(10_000); // Fast test iteration baseline

        Connection mockConnection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("getAutoCommit".equals(method.getName())) return true;
                    if ("isClosed".equals(method.getName())) return false;
                    return null;
                }
        );

        authService = new AuthServiceImpl(userDao, patientDao, auditLogDao, testHasher, () -> mockConnection);
    }

    @Test
    @DisplayName("Verify successful authentication returns valid SessionPrincipal")
    void testSuccessfulAuthentication() {
        PasswordHasher.HashResult hash = testHasher.hashPassword("CorrectPass123!".toCharArray());
        User user = new User();
        user.setId(10L);
        user.setEmail("doctor.sarah@caresync.com");
        user.setPasswordHash(hash.hashHex());
        user.setPasswordSalt(hash.saltHex());
        user.setHashIterations(hash.iterations());
        user.setRole(Role.DOCTOR);
        user.setStatus(UserStatus.ACTIVE);
        user.setFirstName("Sarah");
        user.setLastName("Mitchell");
        userDao.save(user);

        AuthenticationResult result = authService.authenticate(
                "doctor.sarah@caresync.com",
                "CorrectPass123!".toCharArray(),
                "127.0.0.1",
                "JUnit-Test-Agent"
        );

        assertTrue(result.isSuccess(), "Authentication should succeed with valid credentials");
        assertNotNull(result.getPrincipal());
        assertEquals(10L, result.getPrincipal().getUserId());
        assertEquals("doctor.sarah@caresync.com", result.getPrincipal().getEmail());
        assertEquals(Role.DOCTOR, result.getPrincipal().getRole());
        assertEquals("Dr. Sarah Mitchell", result.getPrincipal().getDisplayName());
        assertEquals("/doctor/dashboard", result.getPrincipal().getDashboardUrl());
    }

    @Test
    @DisplayName("Verify incorrect password returns generic error message")
    void testIncorrectPassword() {
        PasswordHasher.HashResult hash = testHasher.hashPassword("RealPassword999!".toCharArray());
        User user = new User();
        user.setId(20L);
        user.setEmail("patient.john@caresync.com");
        user.setPasswordHash(hash.hashHex());
        user.setPasswordSalt(hash.saltHex());
        user.setHashIterations(hash.iterations());
        user.setRole(Role.PATIENT);
        user.setStatus(UserStatus.ACTIVE);
        userDao.save(user);

        AuthenticationResult result = authService.authenticate(
                "patient.john@caresync.com",
                "WrongPassword!".toCharArray(),
                "127.0.0.1",
                "JUnit"
        );

        assertFalse(result.isSuccess());
        assertNull(result.getPrincipal());
        assertEquals("Invalid email or password.", result.getErrorMessage(), "Must use generic error message");
    }

    @Test
    @DisplayName("Verify unknown email address returns identical generic error message")
    void testUnknownEmail() {
        AuthenticationResult result = authService.authenticate(
                "nonexistent@caresync.com",
                "SomePassword123!".toCharArray(),
                "127.0.0.1",
                "JUnit"
        );

        assertFalse(result.isSuccess());
        assertNull(result.getPrincipal());
        assertEquals("Invalid email or password.", result.getErrorMessage(),
                "Error message must be identical for non-existent email to prevent user enumeration");
    }

    @Test
    @DisplayName("Verify inactive or suspended account is rejected")
    void testInactiveAccountRejection() {
        PasswordHasher.HashResult hash = testHasher.hashPassword("Password123!".toCharArray());
        User suspendedUser = new User();
        suspendedUser.setId(30L);
        suspendedUser.setEmail("suspended@caresync.com");
        suspendedUser.setPasswordHash(hash.hashHex());
        suspendedUser.setPasswordSalt(hash.saltHex());
        suspendedUser.setHashIterations(hash.iterations());
        suspendedUser.setRole(Role.PATIENT);
        suspendedUser.setStatus(UserStatus.SUSPENDED);
        userDao.save(suspendedUser);

        AuthenticationResult result = authService.authenticate(
                "suspended@caresync.com",
                "Password123!".toCharArray(),
                "127.0.0.1",
                "JUnit"
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("inactive or suspended"));
    }

    @Test
    @DisplayName("Verify public patient registration strictly creates PATIENT role")
    void testPatientRegistrationEnforcesPatientRole() {
        PatientRegistrationDto dto = new PatientRegistrationDto();
        dto.setFirstName("Alice");
        dto.setLastName("Wonderland");
        dto.setEmail("alice@example.com");
        dto.setPassword("SecretPass888!".toCharArray());
        dto.setPhoneNumber("+1 555-123-4567");
        dto.setDateOfBirth(LocalDate.of(1995, 8, 20));
        dto.setGender(Gender.FEMALE);
        dto.setTermsAccepted(true);

        Patient registered = authService.registerPatient(dto, "127.0.0.1", "JUnit");

        assertNotNull(registered);
        assertNotNull(registered.getId());
        assertEquals(Role.PATIENT, registered.getRole(), "Registration must strictly assign Role.PATIENT");
        assertEquals("alice@example.com", registered.getEmail());

        // Verify persisted user in DAO
        Optional<User> found = userDao.findByEmail("alice@example.com");
        assertTrue(found.isPresent());
        assertEquals(Role.PATIENT, found.get().getRole());
        assertEquals(UserStatus.ACTIVE, found.get().getStatus());
        // Verify password is NOT plaintext
        assertNotEquals("SecretPass888!", found.get().getPasswordHash());
    }

    @Test
    @DisplayName("Verify registration fails when email is already registered")
    void testDuplicateEmailRegistration() {
        User existing = new User();
        existing.setId(50L);
        existing.setEmail("existing@caresync.com");
        existing.setPasswordHash("hash");
        existing.setPasswordSalt("salt");
        existing.setHashIterations(10_000);
        existing.setRole(Role.PATIENT);
        userDao.save(existing);

        PatientRegistrationDto dto = new PatientRegistrationDto();
        dto.setFirstName("Bob");
        dto.setLastName("Builder");
        dto.setEmail("existing@caresync.com");
        dto.setPassword("ValidPass123!".toCharArray());
        dto.setTermsAccepted(true);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                authService.registerPatient(dto, "127.0.0.1", "JUnit"));

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Verify input validation enforces password length and terms consent")
    void testRegistrationValidations() {
        // Password too short (< 8 chars)
        PatientRegistrationDto shortPassDto = new PatientRegistrationDto();
        shortPassDto.setFirstName("Bob");
        shortPassDto.setLastName("Smith");
        shortPassDto.setEmail("bob@example.com");
        shortPassDto.setPassword("short".toCharArray());
        shortPassDto.setTermsAccepted(true);

        assertThrows(ValidationException.class, () ->
                authService.registerPatient(shortPassDto, "127.0.0.1", "JUnit"));

        // Terms not accepted
        PatientRegistrationDto noTermsDto = new PatientRegistrationDto();
        noTermsDto.setFirstName("Bob");
        noTermsDto.setLastName("Smith");
        noTermsDto.setEmail("bob@example.com");
        noTermsDto.setPassword("ValidPassword123!".toCharArray());
        noTermsDto.setTermsAccepted(false);

        assertThrows(ValidationException.class, () ->
                authService.registerPatient(noTermsDto, "127.0.0.1", "JUnit"));
    }

    // =========================================================================
    // Lightweight In-Memory DAO Stubs for Offline Testing
    // =========================================================================

    private static class InMemoryUserDao implements UserDao {
        private final Map<Long, User> usersById = new HashMap<>();
        private final Map<String, User> usersByEmail = new HashMap<>();
        private final AtomicLong idSeq = new AtomicLong(100);

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(usersById.get(id));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(usersByEmail.get(email.toLowerCase()));
        }

        @Override
        public boolean existsByEmail(String email) {
            return usersByEmail.containsKey(email.toLowerCase());
        }

        @Override
        public User save(User user) {
            return save(user, null);
        }

        @Override
        public User save(User user, Connection conn) {
            if (user.getId() == null) {
                user.setId(idSeq.incrementAndGet());
            }
            usersById.put(user.getId(), user);
            usersByEmail.put(user.getEmail().toLowerCase(), user);
            return user;
        }

        @Override
        public boolean updatePassword(Long userId, String passwordHash, String passwordSalt, int iterations) {
            return updatePassword(userId, passwordHash, passwordSalt, iterations, null);
        }

        @Override
        public boolean updatePassword(Long userId, String passwordHash, String passwordSalt, int iterations, Connection conn) {
            User u = usersById.get(userId);
            if (u != null) {
                u.setPasswordHash(passwordHash);
                u.setPasswordSalt(passwordSalt);
                u.setHashIterations(iterations);
                return true;
            }
            return false;
        }

        @Override
        public boolean updateStatus(Long userId, UserStatus status) {
            User u = usersById.get(userId);
            if (u != null) {
                u.setStatus(status);
                return true;
            }
            return false;
        }

        @Override
        public List<User> findAll() { return new ArrayList<>(usersById.values()); }

        @Override
        public boolean update(User user) {
            usersById.put(user.getId(), user);
            return true;
        }

        @Override
        public boolean deleteById(Long id) {
            return updateStatus(id, UserStatus.INACTIVE);
        }
    }

    private static class InMemoryPatientDao implements PatientDao {
        private final Map<Long, Patient> patients = new HashMap<>();
        private final AtomicLong profileIdSeq = new AtomicLong(500);

        @Override
        public Optional<Patient> findById(Long id) { return Optional.ofNullable(patients.get(id)); }

        @Override
        public Optional<Patient> findByUserId(Long userId) { return Optional.ofNullable(patients.get(userId)); }

        @Override
        public Patient saveProfile(Patient patient, Connection conn) {
            patient.setPatientProfileId(profileIdSeq.incrementAndGet());
            patients.put(patient.getId(), patient);
            return patient;
        }

        @Override
        public List<Patient> findAll() { return new ArrayList<>(patients.values()); }

        @Override
        public Patient save(Patient entity) { return saveProfile(entity, null); }

        @Override
        public boolean update(Patient entity) { patients.put(entity.getId(), entity); return true; }

        @Override
        public boolean deleteById(Long id) { return patients.remove(id) != null; }
    }

    private static class InMemoryAuditDao implements AuditLogDao {
        private final List<AuditLog> logs = new ArrayList<>();

        @Override
        public AuditLog log(Long userId, String action, String targetEntity, Long targetId, String ipAddress, String userAgent, String details, Connection conn) {
            AuditLog al = new AuditLog();
            al.setUserId(userId);
            al.setAction(action);
            al.setTargetEntity(targetEntity);
            al.setTargetId(targetId);
            logs.add(al);
            return al;
        }

        @Override
        public AuditLog log(Long userId, String action, String targetEntity, Long targetId, String ipAddress, String userAgent, String details) {
            return log(userId, action, targetEntity, targetId, ipAddress, userAgent, details, null);
        }

        @Override
        public List<AuditLog> findRecent(int limit) { return logs; }

        @Override
        public List<AuditLog> findByUserId(Long userId, int limit) { return logs; }

        @Override
        public List<AuditLog> findByAction(String action, int limit) { return logs; }

        @Override
        public Optional<AuditLog> findLatestLog() { return logs.isEmpty() ? Optional.empty() : Optional.of(logs.get(logs.size() - 1)); }

        @Override
        public Optional<AuditLog> findById(Long id) { return Optional.empty(); }

        @Override
        public List<AuditLog> findAll() { return logs; }

        @Override
        public AuditLog save(AuditLog entity) { logs.add(entity); return entity; }

        @Override
        public boolean update(AuditLog entity) { throw new UnsupportedOperationException(); }

        @Override
        public boolean deleteById(Long id) { throw new UnsupportedOperationException(); }
    }
}

