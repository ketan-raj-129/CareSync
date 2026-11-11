package com.caresync.service;

import com.caresync.dao.AppointmentDao;
import com.caresync.dao.AppointmentDaoImpl;
import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dao.DoctorDao;
import com.caresync.dao.DoctorDaoImpl;
import com.caresync.dao.PatientDao;
import com.caresync.dao.PatientDaoImpl;
import com.caresync.dto.PaginatedResult;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.Doctor;
import com.caresync.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AdminAppointmentServiceImpl implements AdminAppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AdminAppointmentServiceImpl.class);

    private final AppointmentDao appointmentDao;
    private final AuditLogDao auditLogDao;
    private PatientDao patientDao;
    private DoctorDao doctorDao;
    private NotificationEventService notificationEventService;

    public AdminAppointmentServiceImpl() {
        this(new AppointmentDaoImpl(), new AuditLogDaoImpl(), new PatientDaoImpl(), new DoctorDaoImpl(), null);
    }

    public AdminAppointmentServiceImpl(AppointmentDao appointmentDao, AuditLogDao auditLogDao) {
        this(appointmentDao, auditLogDao, new PatientDaoImpl(), new DoctorDaoImpl(), null);
    }

    public AdminAppointmentServiceImpl(AppointmentDao appointmentDao, AuditLogDao auditLogDao,
                                       PatientDao patientDao, DoctorDao doctorDao,
                                       NotificationEventService notificationEventService) {
        this.appointmentDao = Objects.requireNonNull(appointmentDao, "appointmentDao cannot be null");
        this.auditLogDao = Objects.requireNonNull(auditLogDao, "auditLogDao cannot be null");
        this.patientDao = patientDao;
        this.doctorDao = doctorDao;
        this.notificationEventService = notificationEventService;
    }

    public void setNotificationEventService(NotificationEventService notificationEventService) {
        this.notificationEventService = notificationEventService;
    }

    public void setPatientDao(PatientDao patientDao) {
        this.patientDao = patientDao;
    }

    public void setDoctorDao(DoctorDao doctorDao) {
        this.doctorDao = doctorDao;
    }

    @Override
    public PaginatedResult<Appointment> getAppointments(String reference, Long doctorId, Long patientId,
                                                        Long specialtyId, AppointmentStatus status,
                                                        LocalDate date, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = pageSize > 0 ? pageSize : 10;
        int offset = (safePage - 1) * safeSize;

        long total = appointmentDao.countFiltered(reference, doctorId, patientId, specialtyId, status, date);
        List<Appointment> list = appointmentDao.findPaginated(reference, doctorId, patientId, specialtyId, status, date, offset, safeSize);

        return new PaginatedResult<>(list, total, safePage, safeSize);
    }

    @Override
    public Optional<Appointment> getAppointmentDetails(Long appointmentId) {
        if (appointmentId == null) return Optional.empty();
        return appointmentDao.findWithDetailsById(appointmentId);
    }

    @Override
    public boolean cancelAppointment(Long appointmentId, String reason, Long actorAdminId,
                                     String ipAddress, String userAgent) {
        if (appointmentId == null || actorAdminId == null) {
            throw new ValidationException("Appointment ID and administrator ID cannot be null");
        }

        Optional<Appointment> aptOpt = appointmentDao.findById(appointmentId);
        if (aptOpt.isEmpty()) {
            throw new ResourceNotFoundException("Appointment", appointmentId);
        }

        Appointment apt = aptOpt.get();
        if (apt.getStatus() == AppointmentStatus.COMPLETED ||
            apt.getStatus() == AppointmentStatus.CANCELLED ||
            apt.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new ValidationException("Cannot cancel appointment with terminal status: " + apt.getStatus());
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new ValidationException("Administrative cancellation requires a non-empty justification reason.");
        }

        String finalReason = "Administrative Cancellation: " + reason.trim();

        boolean updated = appointmentDao.updateStatus(appointmentId, AppointmentStatus.CANCELLED, finalReason, actorAdminId);
        if (updated) {
            logger.info("Admin ID {} cancelled appointment ID {} (Ref: {}) with reason: {}",
                    actorAdminId, appointmentId, apt.getReferenceNumber(), finalReason);
            auditLogDao.log(actorAdminId, "APPOINTMENT_CANCELLED_BY_ADMIN", "appointments", appointmentId,
                    ipAddress, userAgent, "Cancelled booking " + apt.getReferenceNumber() + ": " + finalReason);
            if (notificationEventService != null) {
                try {
                    Patient pt = patientDao != null && apt.getPatientId() != null ? patientDao.findById(apt.getPatientId()).orElse(null) : apt.getPatient();
                    Doctor doc = doctorDao != null && apt.getDoctorId() != null ? doctorDao.findById(apt.getDoctorId()).orElse(null) : apt.getDoctor();
                    notificationEventService.notifyAppointmentCancelled(apt, pt, doc, "ADMIN", finalReason);
                } catch (Exception ex) {
                    logger.warn("Failed to dispatch administrative appointment cancellation notification: {}", ex.getMessage());
                }
            }
        }
        return updated;
    }
}
