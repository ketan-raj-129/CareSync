package com.caresync.service;

import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dao.PatientDao;
import com.caresync.dao.PatientDaoImpl;
import com.caresync.dao.UserDao;
import com.caresync.dao.UserDaoImpl;
import com.caresync.dto.PaginatedResult;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Patient;
import com.caresync.model.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AdminPatientServiceImpl implements AdminPatientService {

    private static final Logger logger = LoggerFactory.getLogger(AdminPatientServiceImpl.class);

    private final PatientDao patientDao;
    private final UserDao userDao;
    private final AuditLogDao auditLogDao;

    public AdminPatientServiceImpl() {
        this(new PatientDaoImpl(), new UserDaoImpl(), new AuditLogDaoImpl());
    }

    public AdminPatientServiceImpl(PatientDao patientDao, UserDao userDao, AuditLogDao auditLogDao) {
        this.patientDao = Objects.requireNonNull(patientDao, "patientDao cannot be null");
        this.userDao = Objects.requireNonNull(userDao, "userDao cannot be null");
        this.auditLogDao = Objects.requireNonNull(auditLogDao, "auditLogDao cannot be null");
    }

    @Override
    public PaginatedResult<Patient> getPatients(String query, UserStatus status, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = pageSize > 0 ? pageSize : 10;
        int offset = (safePage - 1) * safeSize;

        long total = patientDao.countFiltered(query, status);
        List<Patient> list = patientDao.findPaginated(query, status, offset, safeSize);

        return new PaginatedResult<>(list, total, safePage, safeSize);
    }

    @Override
    public Optional<Patient> getPatientDetails(Long patientId) {
        if (patientId == null) return Optional.empty();
        return patientDao.findById(patientId);
    }

    @Override
    public boolean updatePatientStatus(Long patientId, UserStatus newStatus, Long actorAdminId,
                                       String ipAddress, String userAgent) {
        if (patientId == null || newStatus == null || actorAdminId == null) {
            throw new ValidationException("Patient ID, status, and administrator ID cannot be null");
        }

        Optional<Patient> patientOpt = patientDao.findById(patientId);
        if (patientOpt.isEmpty()) {
            throw new ResourceNotFoundException("Patient", patientId);
        }

        Patient patient = patientOpt.get();
        Long userId = patient.getId(); // user_id

        boolean updated = userDao.updateStatus(userId, newStatus);
        if (updated) {
            logger.info("Admin ID {} updated patient ID {} (user ID {}) status to {}",
                    actorAdminId, patientId, userId, newStatus);
            auditLogDao.log(actorAdminId, "PATIENT_STATUS_UPDATED", "patient_profiles", patientId,
                    ipAddress, userAgent, "Patient ID " + patientId + " status updated to " + newStatus);
        }
        return updated;
    }
}
