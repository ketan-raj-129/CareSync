package com.caresync.service;

import com.caresync.dao.AppointmentDao;
import com.caresync.dao.AppointmentDaoImpl;
import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dao.DoctorAvailabilityDao;
import com.caresync.dao.DoctorAvailabilityDaoImpl;
import com.caresync.dao.DoctorAvailabilityExceptionDao;
import com.caresync.dao.DoctorAvailabilityExceptionDaoImpl;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.DoctorAvailability;
import com.caresync.model.DoctorAvailabilityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorScheduleServiceImpl.class);
    private static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Kolkata");

    private final DoctorAvailabilityDao availabilityDao;
    private final DoctorAvailabilityExceptionDao exceptionDao;
    private final AppointmentDao appointmentDao;
    private final AuditLogDao auditLogDao;

    public DoctorScheduleServiceImpl() {
        this(new DoctorAvailabilityDaoImpl(), new DoctorAvailabilityExceptionDaoImpl(),
             new AppointmentDaoImpl(), new AuditLogDaoImpl());
    }

    public DoctorScheduleServiceImpl(DoctorAvailabilityDao availabilityDao,
                                     DoctorAvailabilityExceptionDao exceptionDao,
                                     AppointmentDao appointmentDao,
                                     AuditLogDao auditLogDao) {
        this.availabilityDao = Objects.requireNonNull(availabilityDao, "availabilityDao cannot be null");
        this.exceptionDao = Objects.requireNonNull(exceptionDao, "exceptionDao cannot be null");
        this.appointmentDao = Objects.requireNonNull(appointmentDao, "appointmentDao cannot be null");
        this.auditLogDao = Objects.requireNonNull(auditLogDao, "auditLogDao cannot be null");
    }

    @Override
    public List<DoctorAvailability> getWeeklySchedule(Long doctorId) {
        if (doctorId == null) {
            throw new ValidationException("Doctor ID cannot be null");
        }
        List<DoctorAvailability> list = availabilityDao.findByDoctorId(doctorId);
        list.sort(Comparator.comparingInt(DoctorAvailability::getDayOfWeek));
        return list;
    }

    @Override
    public boolean updateWeeklySchedule(Long doctorId, List<DoctorAvailability> schedules,
                                        Long actorUserId, String ipAddress, String userAgent) {
        if (doctorId == null || schedules == null) {
            throw new ValidationException("Doctor ID and schedule list cannot be null");
        }

        for (DoctorAvailability avail : schedules) {
            if (avail.getDayOfWeek() < 1 || avail.getDayOfWeek() > 7) {
                throw new ValidationException("Invalid day of week: " + avail.getDayOfWeek());
            }

            if (avail.getStartTime() == null || avail.getEndTime() == null) {
                throw new ValidationException("Start time and end time are required for day " + avail.getDayOfWeek());
            }

            if (!avail.getStartTime().isBefore(avail.getEndTime())) {
                throw new ValidationException("Start time must be strictly before end time for day " + avail.getDayOfWeek());
            }

            int dur = avail.getSlotDurationMinutes();
            if (dur != 15 && dur != 20 && dur != 30 && dur != 45 && dur != 60) {
                avail.setSlotDurationMinutes(30);
            }

            avail.setDoctorId(doctorId);
            if (avail.getId() != null) {
                availabilityDao.update(avail);
            } else {
                Optional<DoctorAvailability> existing = availabilityDao.findByDoctorAndDay(doctorId, avail.getDayOfWeek());
                if (existing.isPresent()) {
                    avail.setId(existing.get().getId());
                    availabilityDao.update(avail);
                } else {
                    availabilityDao.save(avail);
                }
            }
        }

        auditLogDao.log(actorUserId, "DOCTOR_SCHEDULE_UPDATED", "doctor_availability", doctorId,
                ipAddress, userAgent, "Doctor ID " + doctorId + " updated weekly recurring practice schedule");
        return true;
    }

    @Override
    public List<DoctorAvailabilityException> getExceptions(Long doctorId) {
        if (doctorId == null) {
            throw new ValidationException("Doctor ID cannot be null");
        }
        List<DoctorAvailabilityException> list = exceptionDao.findByDoctorId(doctorId);
        list.sort(Comparator.comparing(DoctorAvailabilityException::getExceptionDate).reversed());
        return list;
    }

    @Override
    public DoctorAvailabilityException addException(Long doctorId, DoctorAvailabilityException exception,
                                                    Long actorUserId, String ipAddress, String userAgent) {
        if (doctorId == null || exception == null) {
            throw new ValidationException("Doctor ID and exception record cannot be null");
        }

        LocalDate today = LocalDate.now(CLINIC_ZONE);
        if (exception.getExceptionDate() == null || exception.getExceptionDate().isBefore(today)) {
            throw new ValidationException("Exception date must not be in the past");
        }

        if (!exception.isFullDay()) {
            if (exception.getStartTime() == null || exception.getEndTime() == null) {
                throw new ValidationException("Partial day leave requires both start time and end time");
            }
            if (!exception.getStartTime().isBefore(exception.getEndTime())) {
                throw new ValidationException("Exception start time must precede end time");
            }
        }

        // Validate whether existing booked appointments exist on this date
        List<Appointment> existingApts = appointmentDao.findByDoctorAndDate(doctorId, exception.getExceptionDate());
        for (Appointment apt : existingApts) {
            if (apt.getStatus() == AppointmentStatus.PENDING || apt.getStatus() == AppointmentStatus.CONFIRMED) {
                if (exception.isFullDay()) {
                    throw new ValidationException("Cannot schedule leave on " + exception.getExceptionDate() +
                            ": Active appointment " + apt.getReferenceNumber() + " exists. Please reschedule or cancel the appointment first.");
                } else {
                    // Check partial overlap
                    boolean overlaps = apt.getStartTime().isBefore(exception.getEndTime()) &&
                                       exception.getStartTime().isBefore(apt.getEndTime());
                    if (overlaps) {
                        throw new ValidationException("Cannot schedule partial time-off: Active appointment " +
                                apt.getReferenceNumber() + " (" + apt.getStartTime() + " - " + apt.getEndTime() + ") overlaps with the requested interval.");
                    }
                }
            }
        }

        exception.setDoctorId(doctorId);
        DoctorAvailabilityException saved = exceptionDao.save(exception);

        auditLogDao.log(actorUserId, "DOCTOR_EXCEPTION_CREATED", "doctor_availability_exceptions", saved.getId(),
                ipAddress, userAgent, "Doctor ID " + doctorId + " created leave exception on " + exception.getExceptionDate() +
                        " (" + exception.getExceptionType() + ")");

        return saved;
    }

    @Override
    public boolean deleteException(Long exceptionId, Long doctorId, Long actorUserId,
                                   String ipAddress, String userAgent) {
        if (exceptionId == null || doctorId == null) {
            throw new ValidationException("Exception ID and Doctor ID cannot be null");
        }

        Optional<DoctorAvailabilityException> exOpt = exceptionDao.findById(exceptionId);
        if (exOpt.isEmpty()) {
            throw new ResourceNotFoundException("DoctorAvailabilityException", exceptionId);
        }

        DoctorAvailabilityException ex = exOpt.get();
        if (!Objects.equals(ex.getDoctorId(), doctorId)) {
            logger.warn("Security Alert: Doctor ID {} attempted to delete exception ID {} belonging to doctor ID {}",
                    doctorId, exceptionId, ex.getDoctorId());
            throw new ValidationException("Unauthorized attempt to delete availability exception belonging to another doctor");
        }

        boolean deleted = exceptionDao.deleteById(exceptionId);
        if (deleted) {
            auditLogDao.log(actorUserId, "DOCTOR_EXCEPTION_DELETED", "doctor_availability_exceptions", exceptionId,
                    ipAddress, userAgent, "Doctor ID " + doctorId + " removed leave exception ID " + exceptionId);
        }
        return deleted;
    }
}
