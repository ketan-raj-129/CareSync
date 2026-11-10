package com.caresync.service;

import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dao.PatientDao;
import com.caresync.dao.PatientDaoImpl;
import com.caresync.dao.UserDao;
import com.caresync.dao.UserDaoImpl;
import com.caresync.dto.PatientProfileUpdateDto;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.UnauthorizedException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Patient;
import com.caresync.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PatientServiceImpl implements PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientServiceImpl.class);

    private final PatientDao patientDao;
    private final UserDao userDao;
    private final AuditLogDao auditLogDao;

    public PatientServiceImpl() {
        this(new PatientDaoImpl(), new UserDaoImpl(), new AuditLogDaoImpl());
    }

    public PatientServiceImpl(PatientDao patientDao, UserDao userDao, AuditLogDao auditLogDao) {
        this.patientDao = patientDao;
        this.userDao = userDao;
        this.auditLogDao = auditLogDao;
    }

    @Override
    public Optional<Patient> getPatientByUserId(Long userId) {
        if (userId == null) return Optional.empty();
        try {
            return patientDao.findByUserId(userId);
        } catch (Exception e) {
            logger.error("Failed to query patient profile for user ID: {}", userId, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Patient> getPatientById(Long patientId) {
        if (patientId == null) return Optional.empty();
        try {
            return patientDao.findById(patientId);
        } catch (Exception e) {
            logger.error("Failed to query patient profile by patient ID: {}", patientId, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean updatePatientProfile(Long userId, Long patientId, PatientProfileUpdateDto dto,
                                        Long actorUserId, String ipAddress, String userAgent) {
        if (userId == null || patientId == null || dto == null) {
            throw new ValidationException("Invalid patient profile update parameters");
        }

        // Ownership enforcement: Only the patient themselves or an authorized administrator can update profile
        if (actorUserId != null && !actorUserId.equals(userId)) {
            logger.warn("Unauthorized attempt by user {} to update profile of user {}", actorUserId, userId);
            throw new UnauthorizedException("You are not authorized to update this patient profile.");
        }

        Optional<Patient> existingOpt = patientDao.findById(patientId);
        if (existingOpt.isEmpty()) {
            throw new ResourceNotFoundException("Patient", patientId);
        }

        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User", userId);
        }

        // Validate names
        if (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First name is required.");
        }
        if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last name is required.");
        }

        // 1. Update user fields
        User user = userOpt.get();
        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());
        user.setPhoneNumber(dto.getPhoneNumber() != null ? dto.getPhoneNumber().trim() : null);
        userDao.update(user);

        // 2. Update patient profile fields
        Patient patient = existingOpt.get();
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setBloodGroup(dto.getBloodGroup());
        patient.setAddress(dto.getAddress());
        patient.setEmergencyContactName(dto.getEmergencyContactName());
        patient.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        boolean updated = patientDao.update(patient);

        // 3. Audit log
        try {
            if (auditLogDao != null) {
                auditLogDao.log(actorUserId, "PATIENT_PROFILE_UPDATED", "patient_profiles", patientId,
                        ipAddress, userAgent, "Patient demographic data updated");
            }
        } catch (Exception e) {
            logger.warn("Failed to audit log patient profile update: {}", e.getMessage());
        }

        return updated;
    }
}

