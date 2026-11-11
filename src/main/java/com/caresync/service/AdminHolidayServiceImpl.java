package com.caresync.service;

import com.caresync.dao.AppointmentDao;
import com.caresync.dao.AppointmentDaoImpl;
import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dao.DoctorAvailabilityExceptionDao;
import com.caresync.dao.DoctorAvailabilityExceptionDaoImpl;
import com.caresync.dto.ClinicHolidayRequestDto;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.DoctorAvailabilityException;
import com.caresync.model.ExceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminHolidayServiceImpl implements AdminHolidayService {

    private static final Logger logger = LoggerFactory.getLogger(AdminHolidayServiceImpl.class);
    private static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Kolkata");

    private final DoctorAvailabilityExceptionDao exceptionDao;
    private final AppointmentDao appointmentDao;
    private final AuditLogDao auditLogDao;

    public AdminHolidayServiceImpl() {
        this(new DoctorAvailabilityExceptionDaoImpl(), new AppointmentDaoImpl(), new AuditLogDaoImpl());
    }

    public AdminHolidayServiceImpl(DoctorAvailabilityExceptionDao exceptionDao,
                                   AppointmentDao appointmentDao,
                                   AuditLogDao auditLogDao) {
        this.exceptionDao = Objects.requireNonNull(exceptionDao, "exceptionDao cannot be null");
        this.appointmentDao = Objects.requireNonNull(appointmentDao, "appointmentDao cannot be null");
        this.auditLogDao = Objects.requireNonNull(auditLogDao, "auditLogDao cannot be null");
    }

    @Override
    public List<DoctorAvailabilityException> getClinicHolidays() {
        return exceptionDao.findAllClinicHolidays();
    }

    @Override
    public DoctorAvailabilityException addClinicHoliday(ClinicHolidayRequestDto dto, Long actorAdminId,
                                                        String ipAddress, String userAgent) {
        if (dto == null || actorAdminId == null) {
            throw new ValidationException("Holiday request and administrator ID cannot be null");
        }

        LocalDate today = LocalDate.now(CLINIC_ZONE);
        if (dto.getHolidayDate() == null || dto.getHolidayDate().isBefore(today)) {
            throw new ValidationException("Clinic holiday date must be today or in the future");
        }

        if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
            throw new ValidationException("Reason or description is required for a clinic holiday");
        }

        // Check if a clinic holiday already exists on this date
        List<DoctorAvailabilityException> existing = exceptionDao.findClinicHolidays(dto.getHolidayDate());
        if (!existing.isEmpty()) {
            throw new ValidationException("A clinic-wide holiday already exists for " + dto.getHolidayDate());
        }

        DoctorAvailabilityException holiday = new DoctorAvailabilityException();
        holiday.setDoctorId(null); // Explicitly NULL indicates clinic-wide holiday
        holiday.setExceptionDate(dto.getHolidayDate());
        holiday.setFullDay(true);
        holiday.setExceptionType(dto.getExceptionType() != null ? dto.getExceptionType() : ExceptionType.HOLIDAY);
        holiday.setReason(dto.getReason().trim());

        DoctorAvailabilityException saved = exceptionDao.save(holiday);

        auditLogDao.log(actorAdminId, "CLINIC_HOLIDAY_ADDED", "doctor_availability_exceptions", saved.getId(),
                ipAddress, userAgent, "Added clinic-wide holiday for " + saved.getExceptionDate() + ": " + saved.getReason());
        logger.info("Admin ID {} created clinic holiday ID {} for date: {}", actorAdminId, saved.getId(), saved.getExceptionDate());

        return saved;
    }

    @Override
    public boolean deleteClinicHoliday(Long exceptionId, Long actorAdminId, String ipAddress, String userAgent) {
        if (exceptionId == null || actorAdminId == null) {
            throw new ValidationException("Exception ID and administrator ID cannot be null");
        }

        Optional<DoctorAvailabilityException> exOpt = exceptionDao.findById(exceptionId);
        if (exOpt.isEmpty()) {
            throw new ResourceNotFoundException("ClinicHoliday", exceptionId);
        }

        DoctorAvailabilityException ex = exOpt.get();
        if (ex.getDoctorId() != null) {
            logger.warn("Security Alert: Admin ID {} attempted to delete doctor-specific exception ID {} via clinic holiday endpoint",
                    actorAdminId, exceptionId);
            throw new ValidationException("This exception belongs to an individual doctor schedule, not clinic-wide holidays");
        }

        boolean deleted = exceptionDao.deleteById(exceptionId);
        if (deleted) {
            auditLogDao.log(actorAdminId, "CLINIC_HOLIDAY_REMOVED", "doctor_availability_exceptions", exceptionId,
                    ipAddress, userAgent, "Removed clinic-wide holiday on " + ex.getExceptionDate());
            logger.info("Admin ID {} deleted clinic holiday ID {} ({})", actorAdminId, exceptionId, ex.getExceptionDate());
        }
        return deleted;
    }

    @Override
    public List<Appointment> getAffectedAppointments(LocalDate holidayDate) {
        if (holidayDate == null) return List.of();
        List<Appointment> allOnDate = appointmentDao.findByDate(holidayDate);
        return allOnDate.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.PENDING || a.getStatus() == AppointmentStatus.CONFIRMED)
                .collect(Collectors.toList());
    }
}
