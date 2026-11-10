package com.caresync.service;

import com.caresync.config.DatabaseConfig;
import com.caresync.dao.AppointmentDao;
import com.caresync.dao.AppointmentDaoImpl;
import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dao.DoctorAvailabilityDao;
import com.caresync.dao.DoctorAvailabilityDaoImpl;
import com.caresync.dao.DoctorDao;
import com.caresync.dao.DoctorDaoImpl;
import com.caresync.dao.PatientDao;
import com.caresync.dao.PatientDaoImpl;
import com.caresync.dto.BookingRequestDto;
import com.caresync.exception.DatabaseOperationException;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.SlotConflictException;
import com.caresync.exception.UnauthorizedException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.Doctor;
import com.caresync.model.DoctorAvailability;
import com.caresync.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of CareSync appointment reservation and management services.
 * Enforces PostgreSQL GiST exclusion constraint protection against concurrent double-booking.
 */
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentServiceImpl.class);
    private static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Kolkata");

    private final AppointmentDao appointmentDao;
    private final DoctorDao doctorDao;
    private final PatientDao patientDao;
    private final DoctorAvailabilityDao availabilityDao;
    private final AvailabilityService availabilityService;
    private final AuditLogDao auditLogDao;
    private final ConnectionSupplier connectionSupplier;

    @FunctionalInterface
    public interface ConnectionSupplier {
        Connection get() throws SQLException;
    }

    public AppointmentServiceImpl() {
        this(new AppointmentDaoImpl(), new DoctorDaoImpl(), new PatientDaoImpl(),
             new DoctorAvailabilityDaoImpl(), new AvailabilityServiceImpl(),
             new AuditLogDaoImpl(), DatabaseConfig::getConnection);
    }

    public AppointmentServiceImpl(AppointmentDao appointmentDao, DoctorDao doctorDao, PatientDao patientDao,
                                  DoctorAvailabilityDao availabilityDao, AvailabilityService availabilityService,
                                  AuditLogDao auditLogDao, ConnectionSupplier connectionSupplier) {
        this.appointmentDao = appointmentDao;
        this.doctorDao = doctorDao;
        this.patientDao = patientDao;
        this.availabilityDao = availabilityDao;
        this.availabilityService = availabilityService;
        this.auditLogDao = auditLogDao;
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public Appointment bookAppointment(Long patientId, BookingRequestDto request, Long actorUserId,
                                       String ipAddress, String userAgent) {
        if (patientId == null) {
            throw new UnauthorizedException("Authenticated patient session is required to reserve an appointment.");
        }
        if (request == null) {
            throw new ValidationException("Booking request data cannot be null.");
        }

        Long doctorId = request.getDoctorId();
        LocalDate date = request.getAppointmentDate();
        LocalTime startTime = request.getStartTime();

        if (doctorId == null || date == null || startTime == null) {
            throw new ValidationException("Doctor, date, and appointment start time are all required.");
        }

        LocalDate today = LocalDate.now(CLINIC_ZONE);
        if (date.isBefore(today)) {
            throw new ValidationException("Cannot book an appointment in the past.");
        }
        if (date.isEqual(today) && startTime.isBefore(LocalTime.now(CLINIC_ZONE))) {
            throw new ValidationException("Cannot book an appointment time slot that has already passed today.");
        }

        // Verify patient existence and active status
        Optional<Patient> patientOpt = patientDao.findById(patientId);
        if (patientOpt.isEmpty() || !patientOpt.get().isActive()) {
            throw new ValidationException("Patient account is not active or could not be found.");
        }

        // Verify doctor existence and active status
        Optional<Doctor> doctorOpt = doctorDao.findById(doctorId);
        if (doctorOpt.isEmpty() || !doctorOpt.get().isActive() || !doctorOpt.get().isAvailable()) {
            throw new ValidationException("The selected doctor is currently unavailable for consultations.");
        }

        // Determine slot duration from doctor's schedule
        int dayOfWeek = date.getDayOfWeek().getValue();
        Optional<DoctorAvailability> scheduleOpt = availabilityDao.findByDoctorAndDay(doctorId, dayOfWeek);
        int slotMinutes = scheduleOpt.map(DoctorAvailability::getSlotDurationMinutes).orElse(30);
        LocalTime endTime = startTime.plusMinutes(slotMinutes);

        // Preliminary availability check in service layer
        boolean isAvailable = availabilityService.isSlotAvailable(doctorId, date, startTime, endTime, patientId);
        if (!isAvailable) {
            logger.warn("Booking rejected by preliminary availability check for doctor {} on {} at {}", doctorId, date, startTime);
            throw new SlotConflictException("The selected appointment time slot is no longer available. Please select another slot.");
        }

        // Prepare appointment entity
        Appointment appointment = new Appointment();
        appointment.setReferenceNumber("APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setAppointmentDate(date);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setReasonForVisit(request.getReasonForVisit());

        // Perform transactional insertion protected by PostgreSQL GiST exclusion constraint
        Connection conn = null;
        boolean initialAutoCommit = true;
        try {
            conn = connectionSupplier.get();
            initialAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            Appointment savedAppointment = appointmentDao.save(appointment, conn);

            // Audit log the reservation event
            try {
                if (auditLogDao != null) {
                    auditLogDao.log(actorUserId, "APPOINTMENT_BOOKED", "appointments", savedAppointment.getId(),
                            ipAddress, userAgent, "Reference: " + savedAppointment.getReferenceNumber(), conn);
                }
            } catch (Exception ex) {
                logger.warn("Could not write audit log for booking: {}", ex.getMessage());
            }

            conn.commit();
            logger.info("Successfully confirmed booking {} for patient {} with doctor {}",
                    savedAppointment.getReferenceNumber(), patientId, doctorId);
            return savedAppointment;
        } catch (SlotConflictException e) {
            rollbackQuietly(conn);
            logger.warn("Exclusion constraint collision during booking: {}", e.getMessage());
            throw new SlotConflictException("Sorry, this appointment slot has just been booked. Please select another available time.");
        } catch (SQLException e) {
            rollbackQuietly(conn);
            // Catch 23P01 (PostgreSQL exclusion_violation)
            if ("23P01".equals(e.getSQLState()) || (e.getMessage() != null && e.getMessage().contains("ex_no_overlapping"))) {
                logger.warn("PostgreSQL exclusion constraint 23P01 caught: {}", e.getMessage());
                throw new SlotConflictException("Sorry, this appointment slot has just been booked. Please select another available time.");
            }
            throw new DatabaseOperationException("Failed to reserve appointment: " + e.getMessage(), e.getSQLState(), e);
        } catch (Exception e) {
            rollbackQuietly(conn);
            if (e instanceof SlotConflictException sce) {
                throw sce;
            }
            throw new DatabaseOperationException("Booking transaction failed: " + e.getMessage(), e);
        } finally {
            closeQuietly(conn, initialAutoCommit);
        }
    }

    @Override
    public List<Appointment> getAppointmentsForPatient(Long patientId) {
        if (patientId == null) return Collections.emptyList();
        try {
            return appointmentDao.findWithDetailsByPatientId(patientId);
        } catch (Exception e) {
            logger.error("Failed to query appointments for patient: {}", patientId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Appointment> getUpcomingAppointmentsForPatient(Long patientId) {
        if (patientId == null) return Collections.emptyList();
        LocalDate today = LocalDate.now(CLINIC_ZONE);
        LocalTime now = LocalTime.now(CLINIC_ZONE);

        return getAppointmentsForPatient(patientId).stream()
                .filter(apt -> apt.isPending() || apt.isConfirmed())
                .filter(apt -> apt.getAppointmentDate().isAfter(today) ||
                        (apt.getAppointmentDate().isEqual(today) && apt.getEndTime().isAfter(now)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> getPastAppointmentsForPatient(Long patientId) {
        if (patientId == null) return Collections.emptyList();
        LocalDate today = LocalDate.now(CLINIC_ZONE);
        LocalTime now = LocalTime.now(CLINIC_ZONE);

        return getAppointmentsForPatient(patientId).stream()
                .filter(apt -> apt.isCompleted() || apt.isCancelled() ||
                        apt.getAppointmentDate().isBefore(today) ||
                        (apt.getAppointmentDate().isEqual(today) && apt.getEndTime().compareTo(now) <= 0))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Appointment> getAppointmentDetails(Long appointmentId, Long patientId) {
        if (appointmentId == null || patientId == null) {
            return Optional.empty();
        }

        Optional<Appointment> appointmentOpt;
        try {
            appointmentOpt = appointmentDao.findWithDetailsById(appointmentId);
        } catch (Exception e) {
            logger.error("Failed to load appointment details ID: {}", appointmentId, e);
            return Optional.empty();
        }

        if (appointmentOpt.isEmpty()) {
            return Optional.empty();
        }

        Appointment apt = appointmentOpt.get();
        // Strict ownership enforcement: Cannot access another patient's appointment
        if (!apt.getPatientId().equals(patientId)) {
            logger.warn("Unauthorized access attempt: Patient {} attempted to view appointment {} belonging to patient {}",
                    patientId, appointmentId, apt.getPatientId());
            throw new UnauthorizedException("You are not authorized to view this appointment.");
        }

        return Optional.of(apt);
    }

    @Override
    public boolean cancelAppointment(Long appointmentId, Long patientId, String cancellationReason,
                                     Long actorUserId, String ipAddress, String userAgent) {
        if (appointmentId == null || patientId == null) {
            throw new ValidationException("Appointment ID and Patient ID are required for cancellation.");
        }

        Optional<Appointment> appointmentOpt = appointmentDao.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new ResourceNotFoundException("Appointment", appointmentId);
        }

        Appointment apt = appointmentOpt.get();

        // Enforce ownership
        if (!apt.getPatientId().equals(patientId)) {
            logger.warn("Unauthorized cancellation attempt: Patient {} attempted to cancel appointment {} of patient {}",
                    patientId, appointmentId, apt.getPatientId());
            throw new UnauthorizedException("You can only cancel your own appointments.");
        }

        // Validate cancellation eligibility
        if (!apt.isPending() && !apt.isConfirmed()) {
            throw new ValidationException("Only pending or confirmed appointments can be cancelled.");
        }

        LocalDate today = LocalDate.now(CLINIC_ZONE);
        LocalTime now = LocalTime.now(CLINIC_ZONE);
        if (apt.getAppointmentDate().isBefore(today) ||
            (apt.getAppointmentDate().isEqual(today) && apt.getStartTime().isBefore(now))) {
            throw new ValidationException("Cannot cancel an appointment that has already started or passed.");
        }

        String reason = (cancellationReason != null && !cancellationReason.trim().isEmpty())
                ? cancellationReason.trim()
                : "Cancelled by patient";

        boolean updated = appointmentDao.updateStatus(appointmentId, AppointmentStatus.CANCELLED, reason, actorUserId);

        if (updated) {
            try {
                if (auditLogDao != null) {
                    auditLogDao.log(actorUserId, "APPOINTMENT_CANCELLED", "appointments", appointmentId,
                            ipAddress, userAgent, "Reason: " + reason);
                }
            } catch (Exception e) {
                logger.warn("Failed to audit log appointment cancellation: {}", e.getMessage());
            }
            logger.info("Appointment ID {} successfully cancelled by patient {}", appointmentId, patientId);
        }

        return updated;
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                logger.error("Failed to roll back database transaction", e);
            }
        }
    }

    private void closeQuietly(Connection conn, boolean initialAutoCommit) {
        if (conn != null) {
            try {
                conn.setAutoCommit(initialAutoCommit);
                conn.close();
            } catch (SQLException e) {
                logger.error("Failed to restore autocommit and close connection", e);
            }
        }
    }
}

