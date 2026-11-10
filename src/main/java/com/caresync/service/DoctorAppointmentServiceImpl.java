package com.caresync.service;

import com.caresync.dao.AppointmentDao;
import com.caresync.dao.AppointmentDaoImpl;
import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DoctorAppointmentServiceImpl implements DoctorAppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorAppointmentServiceImpl.class);
    private static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Kolkata");

    private final AppointmentDao appointmentDao;
    private final AuditLogDao auditLogDao;

    public DoctorAppointmentServiceImpl() {
        this(new AppointmentDaoImpl(), new AuditLogDaoImpl());
    }

    public DoctorAppointmentServiceImpl(AppointmentDao appointmentDao, AuditLogDao auditLogDao) {
        this.appointmentDao = Objects.requireNonNull(appointmentDao, "appointmentDao cannot be null");
        this.auditLogDao = Objects.requireNonNull(auditLogDao, "auditLogDao cannot be null");
    }

    @Override
    public List<Appointment> getAppointmentsForDoctor(Long doctorId, String filter) {
        if (doctorId == null) {
            throw new ValidationException("Doctor ID cannot be null");
        }

        List<Appointment> all = appointmentDao.findWithDetailsByDoctorId(doctorId);
        if (filter == null || filter.trim().isEmpty() || "ALL".equalsIgnoreCase(filter)) {
            return all;
        }

        String normalized = filter.trim().toUpperCase();
        LocalDate today = LocalDate.now(CLINIC_ZONE);
        List<Appointment> filtered = new ArrayList<>();

        for (Appointment apt : all) {
            switch (normalized) {
                case "TODAY":
                    if (apt.getAppointmentDate() != null && apt.getAppointmentDate().isEqual(today)) {
                        filtered.add(apt);
                    }
                    break;
                case "UPCOMING":
                    if (apt.getAppointmentDate() != null &&
                            (apt.getAppointmentDate().isAfter(today) ||
                             (apt.getAppointmentDate().isEqual(today) && (apt.getStatus() == AppointmentStatus.PENDING || apt.getStatus() == AppointmentStatus.CONFIRMED))) &&
                            (apt.getStatus() == AppointmentStatus.PENDING || apt.getStatus() == AppointmentStatus.CONFIRMED)) {
                        filtered.add(apt);
                    }
                    break;
                case "PENDING":
                    if (apt.getStatus() == AppointmentStatus.PENDING) {
                        filtered.add(apt);
                    }
                    break;
                case "CONFIRMED":
                    if (apt.getStatus() == AppointmentStatus.CONFIRMED) {
                        filtered.add(apt);
                    }
                    break;
                case "COMPLETED":
                    if (apt.getStatus() == AppointmentStatus.COMPLETED) {
                        filtered.add(apt);
                    }
                    break;
                case "CANCELLED":
                    if (apt.getStatus() == AppointmentStatus.CANCELLED) {
                        filtered.add(apt);
                    }
                    break;
                default:
                    filtered.add(apt);
                    break;
            }
        }

        return filtered;
    }

    @Override
    public Optional<Appointment> getAppointmentDetails(Long appointmentId, Long doctorId) {
        if (appointmentId == null || doctorId == null) {
            return Optional.empty();
        }

        Optional<Appointment> aptOpt = appointmentDao.findWithDetailsById(appointmentId);
        if (aptOpt.isEmpty()) {
            return Optional.empty();
        }

        Appointment apt = aptOpt.get();
        if (!Objects.equals(apt.getDoctorId(), doctorId)) {
            logger.warn("Unauthorized Access: Doctor ID {} attempted to access appointment ID {} belonging to doctor ID {}",
                    doctorId, appointmentId, apt.getDoctorId());
            throw new ValidationException("Unauthorized access to appointment record");
        }

        return aptOpt;
    }

    @Override
    public boolean confirmAppointment(Long appointmentId, Long doctorId, Long actorUserId,
                                      String ipAddress, String userAgent) {
        Appointment apt = validateAndGetDoctorAppointment(appointmentId, doctorId);

        if (apt.getStatus() != AppointmentStatus.PENDING) {
            throw new ValidationException("Only PENDING appointments can be confirmed. Current status: " + apt.getStatus());
        }

        boolean updated = appointmentDao.updateStatus(appointmentId, AppointmentStatus.CONFIRMED, null, null);
        if (updated) {
            logger.info("Appointment ID {} confirmed by doctor ID {}", appointmentId, doctorId);
            auditLogDao.log(actorUserId, "APPOINTMENT_CONFIRMED", "appointments", appointmentId,
                    ipAddress, userAgent, "Doctor ID " + doctorId + " confirmed booking " + apt.getReferenceNumber());
        }
        return updated;
    }

    @Override
    public boolean rejectAppointment(Long appointmentId, Long doctorId, String reason,
                                     Long actorUserId, String ipAddress, String userAgent) {
        Appointment apt = validateAndGetDoctorAppointment(appointmentId, doctorId);

        if (apt.getStatus() != AppointmentStatus.PENDING) {
            throw new ValidationException("Only PENDING appointments can be rejected. Current status: " + apt.getStatus());
        }

        String finalReason = (reason != null && !reason.trim().isEmpty())
                ? "Declined by Doctor: " + reason.trim()
                : "Declined by Doctor due to scheduling conflict.";

        boolean updated = appointmentDao.updateStatus(appointmentId, AppointmentStatus.CANCELLED, finalReason, actorUserId);
        if (updated) {
            logger.info("Appointment ID {} rejected by doctor ID {}", appointmentId, doctorId);
            auditLogDao.log(actorUserId, "APPOINTMENT_REJECTED", "appointments", appointmentId,
                    ipAddress, userAgent, "Doctor ID " + doctorId + " rejected booking " + apt.getReferenceNumber() + ": " + finalReason);
        }
        return updated;
    }

    @Override
    public boolean completeAppointment(Long appointmentId, Long doctorId, Long actorUserId,
                                       String ipAddress, String userAgent) {
        Appointment apt = validateAndGetDoctorAppointment(appointmentId, doctorId);

        if (apt.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ValidationException("Only CONFIRMED appointments can be marked as COMPLETED. Current status: " + apt.getStatus());
        }

        boolean updated = appointmentDao.updateStatus(appointmentId, AppointmentStatus.COMPLETED, null, null);
        if (updated) {
            logger.info("Appointment ID {} completed by doctor ID {}", appointmentId, doctorId);
            auditLogDao.log(actorUserId, "APPOINTMENT_COMPLETED", "appointments", appointmentId,
                    ipAddress, userAgent, "Doctor ID " + doctorId + " marked consultation completed for " + apt.getReferenceNumber());
        }
        return updated;
    }

    @Override
    public boolean cancelAppointmentByDoctor(Long appointmentId, Long doctorId, String reason,
                                            Long actorUserId, String ipAddress, String userAgent) {
        Appointment apt = validateAndGetDoctorAppointment(appointmentId, doctorId);

        if (apt.getStatus() == AppointmentStatus.COMPLETED || apt.getStatus() == AppointmentStatus.CANCELLED || apt.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new ValidationException("Cannot cancel an appointment with terminal status: " + apt.getStatus());
        }

        String finalReason = (reason != null && !reason.trim().isEmpty())
                ? "Cancelled by Doctor: " + reason.trim()
                : "Cancelled by Doctor.";

        boolean updated = appointmentDao.updateStatus(appointmentId, AppointmentStatus.CANCELLED, finalReason, actorUserId);
        if (updated) {
            logger.info("Appointment ID {} cancelled by doctor ID {}", appointmentId, doctorId);
            auditLogDao.log(actorUserId, "APPOINTMENT_CANCELLED_BY_DOCTOR", "appointments", appointmentId,
                    ipAddress, userAgent, "Doctor ID " + doctorId + " cancelled booking " + apt.getReferenceNumber() + ": " + finalReason);
        }
        return updated;
    }

    private Appointment validateAndGetDoctorAppointment(Long appointmentId, Long doctorId) {
        if (appointmentId == null || doctorId == null) {
            throw new ValidationException("Appointment ID and Doctor ID cannot be null");
        }

        Optional<Appointment> aptOpt = appointmentDao.findById(appointmentId);
        if (aptOpt.isEmpty()) {
            throw new ResourceNotFoundException("Appointment", appointmentId);
        }

        Appointment apt = aptOpt.get();
        if (!Objects.equals(apt.getDoctorId(), doctorId)) {
            logger.warn("Security Alert: Doctor ID {} attempted state change on appointment ID {} belonging to doctor ID {}",
                    doctorId, appointmentId, apt.getDoctorId());
            throw new ValidationException("Unauthorized attempt to alter appointment belonging to another practitioner");
        }

        return apt;
    }
}
