package com.caresync.service;

import com.caresync.config.AppConstants;
import com.caresync.config.DatabaseConfig;
import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dao.PatientDao;
import com.caresync.dao.PatientDaoImpl;
import com.caresync.dao.UserDao;
import com.caresync.dao.UserDaoImpl;
import com.caresync.dto.AuthenticationResult;
import com.caresync.dto.PatientRegistrationDto;
import com.caresync.dto.SessionPrincipal;
import com.caresync.exception.DatabaseOperationException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Gender;
import com.caresync.model.Patient;
import com.caresync.model.Role;
import com.caresync.model.User;
import com.caresync.model.UserStatus;
import com.caresync.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

/**
 * Core implementation of CareSync authentication and registration services.
 */
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserDao userDao;
    private final PatientDao patientDao;
    private final AuditLogDao auditLogDao;
    private final PasswordHasher passwordHasher;
    private final ConnectionSupplier connectionSupplier;

    @FunctionalInterface
    public interface ConnectionSupplier {
        Connection get() throws SQLException;
    }

    /**
     * Default production constructor using standard DAO singletons and DatabaseConfig pool.
     */
    public AuthServiceImpl() {
        this(new UserDaoImpl(), new PatientDaoImpl(), new AuditLogDaoImpl(), new PasswordHasher(), DatabaseConfig::getConnection);
    }

    /**
     * Dependency injection constructor for testing.
     */
    public AuthServiceImpl(UserDao userDao, PatientDao patientDao, AuditLogDao auditLogDao,
                           PasswordHasher passwordHasher, ConnectionSupplier connectionSupplier) {
        this.userDao = userDao;
        this.patientDao = patientDao;
        this.auditLogDao = auditLogDao;
        this.passwordHasher = passwordHasher;
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public AuthenticationResult authenticate(String email, char[] password, String ipAddress, String userAgent) {
        if (email == null || email.trim().isEmpty() || password == null || password.length == 0) {
            return AuthenticationResult.failure("Please enter both email and password.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> userOpt;
        try {
            userOpt = userDao.findByEmail(normalizedEmail);
        } catch (Exception e) {
            logger.error("Database error querying user by email: {}", normalizedEmail, e);
            return AuthenticationResult.failure("Authentication service temporarily unavailable.");
        }

        if (userOpt.isEmpty()) {
            logger.warn("Authentication failed: User '{}' not found", normalizedEmail);
            safeLogAudit(null, "LOGIN_FAILED_UNKNOWN_USER", "users", null, ipAddress, userAgent, "Unknown email: " + normalizedEmail);
            return AuthenticationResult.failure("Invalid email or password.");
        }

        User user = userOpt.get();

        // Check account status
        if (user.getStatus() != UserStatus.ACTIVE) {
            logger.warn("Authentication rejected for user '{}': Status is {}", normalizedEmail, user.getStatus());
            safeLogAudit(user.getId(), "LOGIN_BLOCKED_STATUS", "users", user.getId(), ipAddress, userAgent, "Status: " + user.getStatus());
            return AuthenticationResult.failure("Your account is currently inactive or suspended. Please contact clinic administration.");
        }

        // Verify password
        boolean verified = passwordHasher.verifyPassword(
                password,
                user.getPasswordHash(),
                user.getPasswordSalt(),
                user.getHashIterations()
        );

        if (!verified) {
            logger.warn("Authentication failed for user '{}': Incorrect password", normalizedEmail);
            safeLogAudit(user.getId(), "LOGIN_FAILED_BAD_CREDENTIALS", "users", user.getId(), ipAddress, userAgent, "Incorrect password attempt");
            return AuthenticationResult.failure("Invalid email or password.");
        }

        // Check if work factor rehash is needed
        boolean rehashNeeded = passwordHasher.needsRehash(user.getHashIterations());
        if (rehashNeeded) {
            logger.info("Upgrading password work factor for user '{}' to baseline {} iterations", normalizedEmail, passwordHasher.getDefaultIterations());
            PasswordHasher.HashResult newHash = passwordHasher.hashPassword(password);
            try {
                userDao.updatePassword(user.getId(), newHash.hashHex(), newHash.saltHex(), newHash.iterations());
            } catch (Exception e) {
                logger.warn("Could not immediately update rehashed password for user ID: {}", user.getId(), e);
            }
        }

        // Wipe candidate password array from memory
        Arrays.fill(password, '\0');

        SessionPrincipal principal = new SessionPrincipal(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName()
        );

        safeLogAudit(user.getId(), "LOGIN_SUCCESS", "users", user.getId(), ipAddress, userAgent, "Successful login");
        logger.info("User '{}' ({}) successfully authenticated", normalizedEmail, user.getRole());

        return AuthenticationResult.success(principal, rehashNeeded);
    }

    @Override
    public Patient registerPatient(PatientRegistrationDto dto, String ipAddress, String userAgent) {
        validateRegistrationInput(dto);

        String normalizedEmail = dto.getEmail().trim().toLowerCase();
        if (userDao.existsByEmail(normalizedEmail)) {
            safeLogAudit(null, "REGISTRATION_DUPLICATE_EMAIL", "users", null, ipAddress, userAgent, "Attempted email: " + normalizedEmail);
            throw new ValidationException("An account with this email address already exists. Please sign in or use another email.");
        }

        PasswordHasher.HashResult hashResult = passwordHasher.hashPassword(dto.getPassword());
        Arrays.fill(dto.getPassword(), '\0');

        Connection conn = null;
        boolean initialAutoCommit = true;
        try {
            conn = connectionSupplier.get();
            initialAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Create User account with strictly Role.PATIENT
            User user = new User();
            user.setEmail(normalizedEmail);
            user.setPasswordHash(hashResult.hashHex());
            user.setPasswordSalt(hashResult.saltHex());
            user.setHashIterations(hashResult.iterations());
            user.setRole(Role.PATIENT); // CRITICAL: strictly PATIENT, never ADMIN or DOCTOR
            user.setStatus(UserStatus.ACTIVE);
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setPhoneNumber(dto.getPhoneNumber());

            user = userDao.save(user, conn);

            // 2. Create Patient profile referencing the new user_id
            Patient patient = new Patient();
            patient.setId(user.getId());
            patient.setDateOfBirth(dto.getDateOfBirth() != null ? dto.getDateOfBirth() : LocalDate.of(2000, 1, 1));
            patient.setGender(dto.getGender() != null ? dto.getGender() : Gender.PREFER_NOT_TO_SAY);
            patient.setBloodGroup(dto.getBloodGroup());
            patient.setAddress(dto.getAddress());
            patient.setEmergencyContactName(dto.getEmergencyContactName());
            patient.setEmergencyContactPhone(dto.getEmergencyContactPhone());

            patient = patientDao.saveProfile(patient, conn);

            // Copy user properties to patient domain model for return
            patient.setEmail(user.getEmail());
            patient.setFirstName(user.getFirstName());
            patient.setLastName(user.getLastName());
            patient.setPhoneNumber(user.getPhoneNumber());
            patient.setRole(Role.PATIENT);
            patient.setStatus(UserStatus.ACTIVE);

            // 3. Record audit log
            safeLogAuditWithConn(user.getId(), "PATIENT_REGISTERED", "users", user.getId(), ipAddress, userAgent, "Patient self-registered", conn);

            conn.commit();
            logger.info("Successfully registered new patient account ID: {} ({})", user.getId(), normalizedEmail);
            return patient;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Transaction rollback failed during registration", rollbackEx);
                }
            }
            if ("23505".equals(e.getSQLState())) {
                throw new ValidationException("An account with this email address already exists.");
            }
            throw new DatabaseOperationException("Failed to register patient account: " + e.getMessage(), e.getSQLState(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(initialAutoCommit);
                    conn.close();
                } catch (SQLException closeEx) {
                    logger.error("Failed to close connection", closeEx);
                }
            }
        }
    }

    private void validateRegistrationInput(PatientRegistrationDto dto) {
        if (dto == null) {
            throw new ValidationException("Registration data cannot be null");
        }
        if (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First name is required.");
        }
        if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last name is required.");
        }
        if (dto.getEmail() == null || !dto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Please provide a valid email address.");
        }
        if (dto.getPassword() == null || dto.getPassword().length < 8) {
            throw new ValidationException("Password must be at least 8 characters long.");
        }
        if (!dto.isTermsAccepted()) {
            throw new ValidationException("You must agree to the Terms of Service and Privacy Notice to proceed.");
        }
    }

    private void safeLogAudit(Long userId, String action, String targetEntity, Long targetId,
                              String ipAddress, String userAgent, String details) {
        try {
            if (auditLogDao != null) {
                auditLogDao.log(userId, action, targetEntity, targetId, ipAddress, userAgent, details);
            }
        } catch (Exception e) {
            logger.warn("Could not record audit log '{}': {}", action, e.getMessage());
        }
    }

    private void safeLogAuditWithConn(Long userId, String action, String targetEntity, Long targetId,
                                      String ipAddress, String userAgent, String details, Connection conn) {
        try {
            if (auditLogDao != null) {
                auditLogDao.log(userId, action, targetEntity, targetId, ipAddress, userAgent, details, conn);
            }
        } catch (Exception e) {
            logger.warn("Could not record transactional audit log '{}': {}", action, e.getMessage());
        }
    }
}

