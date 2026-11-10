package com.caresync.service;

import com.caresync.dao.AppointmentDao;
import com.caresync.dao.AppointmentDaoImpl;
import com.caresync.dao.DoctorAvailabilityDao;
import com.caresync.dao.DoctorAvailabilityDaoImpl;
import com.caresync.dao.DoctorAvailabilityExceptionDao;
import com.caresync.dao.DoctorAvailabilityExceptionDaoImpl;
import com.caresync.dao.DoctorDao;
import com.caresync.dao.DoctorDaoImpl;
import com.caresync.dto.TimeSlotDto;
import com.caresync.model.Appointment;
import com.caresync.model.Doctor;
import com.caresync.model.DoctorAvailability;
import com.caresync.model.DoctorAvailabilityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of real-time consultation slot generation and validation.
 * Uses Asia/Kolkata standard timezone.
 */
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(AvailabilityServiceImpl.class);
    public static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Kolkata");

    private final DoctorDao doctorDao;
    private final DoctorAvailabilityDao availabilityDao;
    private final DoctorAvailabilityExceptionDao exceptionDao;
    private final AppointmentDao appointmentDao;

    public AvailabilityServiceImpl() {
        this(new DoctorDaoImpl(), new DoctorAvailabilityDaoImpl(),
             new DoctorAvailabilityExceptionDaoImpl(), new AppointmentDaoImpl());
    }

    public AvailabilityServiceImpl(DoctorDao doctorDao, DoctorAvailabilityDao availabilityDao,
                                   DoctorAvailabilityExceptionDao exceptionDao, AppointmentDao appointmentDao) {
        this.doctorDao = doctorDao;
        this.availabilityDao = availabilityDao;
        this.exceptionDao = exceptionDao;
        this.appointmentDao = appointmentDao;
    }

    @Override
    public List<TimeSlotDto> getAvailableSlots(Long doctorId, LocalDate date) {
        return getAvailableSlots(doctorId, date, null);
    }

    @Override
    public List<TimeSlotDto> getAvailableSlots(Long doctorId, LocalDate date, Long patientId) {
        if (doctorId == null || date == null) {
            return Collections.emptyList();
        }

        LocalDate today = LocalDate.now(CLINIC_ZONE);
        if (date.isBefore(today)) {
            logger.debug("Requested availability date {} is in the past", date);
            return Collections.emptyList();
        }

        Optional<Doctor> doctorOpt = doctorDao.findById(doctorId);
        if (doctorOpt.isEmpty() || !doctorOpt.get().isAvailable() || !doctorOpt.get().isActive()) {
            logger.warn("Doctor ID {} is non-existent, deactivated, or marked unavailable", doctorId);
            return Collections.emptyList();
        }

        // 1. Check for clinic-wide holidays
        List<DoctorAvailabilityException> holidays = exceptionDao.findClinicHolidays(date);
        boolean isClinicHoliday = !holidays.isEmpty();
        String holidayName = isClinicHoliday ? holidays.get(0).getReason() : null;

        // 2. Check for doctor availability exceptions (full-day / leaves)
        List<DoctorAvailabilityException> exceptions = exceptionDao.findExceptionsByDoctorAndDate(doctorId, date);
        boolean isFullDayLeave = exceptions.stream().anyMatch(DoctorAvailabilityException::isFullDay);

        // 3. Retrieve recurring weekly schedule for selected day of week
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Mon ... 7=Sun
        Optional<DoctorAvailability> scheduleOpt = availabilityDao.findByDoctorAndDay(doctorId, dayOfWeek);
        if (scheduleOpt.isEmpty() || !scheduleOpt.get().isActive()) {
            logger.debug("Doctor ID {} has no active schedule on day of week {}", doctorId, dayOfWeek);
            return Collections.emptyList();
        }

        DoctorAvailability schedule = scheduleOpt.get();
        LocalTime workStart = schedule.getStartTime();
        LocalTime workEnd = schedule.getEndTime();
        int slotMinutes = schedule.getSlotDurationMinutes() > 0 ? schedule.getSlotDurationMinutes() : 30;

        // Retrieve existing appointments on this date
        List<Appointment> doctorAppointments = appointmentDao.findByDoctorAndDate(doctorId, date);
        List<Appointment> patientAppointments = (patientId != null) ? appointmentDao.findByPatientId(patientId) : Collections.emptyList();

        LocalTime nowTime = LocalTime.now(CLINIC_ZONE);
        List<TimeSlotDto> slots = new ArrayList<>();

        LocalTime currentSlotStart = workStart;
        while (currentSlotStart.plusMinutes(slotMinutes).compareTo(workEnd) <= 0) {
            LocalTime currentSlotEnd = currentSlotStart.plusMinutes(slotMinutes);

            boolean available = true;
            String reason = null;

            if (isClinicHoliday) {
                available = false;
                reason = "Clinic Holiday: " + (holidayName != null ? holidayName : "Closed");
            } else if (isFullDayLeave) {
                available = false;
                reason = "Doctor on Leave";
            } else if (date.isEqual(today) && currentSlotStart.isBefore(nowTime)) {
                available = false;
                reason = "Past Time Slot";
            } else {
                // Check partial-day leaves
                LocalTime finalSlotStart = currentSlotStart;
                boolean inException = exceptions.stream().anyMatch(ex ->
                        !ex.isFullDay() && ex.getStartTime() != null && ex.getEndTime() != null &&
                        finalSlotStart.isBefore(ex.getEndTime()) && currentSlotEnd.isAfter(ex.getStartTime()));

                if (inException) {
                    available = false;
                    reason = "Doctor Unavailable";
                } else {
                    // Check doctor existing active appointments
                    boolean doctorConflict = doctorAppointments.stream().anyMatch(apt ->
                            (apt.isPending() || apt.isConfirmed()) &&
                            finalSlotStart.isBefore(apt.getEndTime()) && currentSlotEnd.isAfter(apt.getStartTime()));

                    if (doctorConflict) {
                        available = false;
                        reason = "Slot Booked";
                    } else if (patientId != null) {
                        // Check patient concurrent appointment conflict
                        boolean patientConflict = patientAppointments.stream().anyMatch(apt ->
                                apt.getAppointmentDate().isEqual(date) &&
                                (apt.isPending() || apt.isConfirmed()) &&
                                finalSlotStart.isBefore(apt.getEndTime()) && currentSlotEnd.isAfter(apt.getStartTime()));

                        if (patientConflict) {
                            available = false;
                            reason = "You have an existing booking at this time";
                        }
                    }
                }
            }

            slots.add(new TimeSlotDto(currentSlotStart, currentSlotEnd, available, reason));
            currentSlotStart = currentSlotEnd;
        }

        return slots;
    }

    @Override
    public boolean isSlotAvailable(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime, Long patientId) {
        List<TimeSlotDto> slots = getAvailableSlots(doctorId, date, patientId);
        return slots.stream().anyMatch(slot ->
                slot.getStartTime().equals(startTime) &&
                slot.getEndTime().equals(endTime) &&
                slot.isAvailable());
    }
}

