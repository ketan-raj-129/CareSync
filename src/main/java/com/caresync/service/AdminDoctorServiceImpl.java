package com.caresync.service;

import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dao.DoctorDao;
import com.caresync.dao.DoctorDaoImpl;
import com.caresync.dao.UserDao;
import com.caresync.dao.UserDaoImpl;
import com.caresync.dto.PaginatedResult;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Doctor;
import com.caresync.model.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AdminDoctorServiceImpl implements AdminDoctorService {

    private static final Logger logger = LoggerFactory.getLogger(AdminDoctorServiceImpl.class);

    private final DoctorDao doctorDao;
    private final UserDao userDao;
    private final AuditLogDao auditLogDao;

    public AdminDoctorServiceImpl() {
        this(new DoctorDaoImpl(), new UserDaoImpl(), new AuditLogDaoImpl());
    }

    public AdminDoctorServiceImpl(DoctorDao doctorDao, UserDao userDao, AuditLogDao auditLogDao) {
        this.doctorDao = Objects.requireNonNull(doctorDao, "doctorDao cannot be null");
        this.userDao = Objects.requireNonNull(userDao, "userDao cannot be null");
        this.auditLogDao = Objects.requireNonNull(auditLogDao, "auditLogDao cannot be null");
    }

    @Override
    public PaginatedResult<Doctor> getDoctors(String query, Long specialtyId, UserStatus status,
                                             Boolean available, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = pageSize > 0 ? pageSize : 10;
        int offset = (safePage - 1) * safeSize;

        long total = doctorDao.countFiltered(query, specialtyId, status, available);
        List<Doctor> list = doctorDao.findPaginated(query, specialtyId, status, available, offset, safeSize);

        return new PaginatedResult<>(list, total, safePage, safeSize);
    }

    @Override
    public Optional<Doctor> getDoctorDetails(Long doctorId) {
        if (doctorId == null) return Optional.empty();
        return doctorDao.findById(doctorId);
    }

    @Override
    public boolean updateDoctorStatus(Long doctorId, UserStatus newStatus, Long actorAdminId,
                                      String ipAddress, String userAgent) {
        if (doctorId == null || newStatus == null || actorAdminId == null) {
            throw new ValidationException("Doctor ID, status, and administrator ID cannot be null");
        }

        Optional<Doctor> doctorOpt = doctorDao.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            throw new ResourceNotFoundException("Doctor", doctorId);
        }

        Doctor doctor = doctorOpt.get();
        Long userId = doctor.getId(); // user_id

        boolean updated = userDao.updateStatus(userId, newStatus);
        if (updated) {
            logger.info("Admin ID {} updated doctor ID {} (user ID {}) status to {}",
                    actorAdminId, doctorId, userId, newStatus);
            auditLogDao.log(actorAdminId, "DOCTOR_STATUS_UPDATED", "doctor_profiles", doctorId,
                    ipAddress, userAgent, "Doctor ID " + doctorId + " account status updated to " + newStatus);
        }
        return updated;
    }

    @Override
    public boolean toggleDoctorAvailability(Long doctorId, boolean isAvailable, Long actorAdminId,
                                            String ipAddress, String userAgent) {
        if (doctorId == null || actorAdminId == null) {
            throw new ValidationException("Doctor ID and administrator ID cannot be null");
        }

        Optional<Doctor> doctorOpt = doctorDao.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            throw new ResourceNotFoundException("Doctor", doctorId);
        }

        boolean updated = doctorDao.updateAvailability(doctorId, isAvailable);
        if (updated) {
            logger.info("Admin ID {} toggled doctor ID {} availability to {}",
                    actorAdminId, doctorId, isAvailable);
            auditLogDao.log(actorAdminId, "DOCTOR_AVAILABILITY_TOGGLED", "doctor_profiles", doctorId,
                    ipAddress, userAgent, "Doctor ID " + doctorId + " availability toggled to " + isAvailable);
        }
        return updated;
    }
}
