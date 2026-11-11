package com.caresync.service;

import com.caresync.model.DoctorAvailability;
import com.caresync.model.DoctorAvailabilityException;

import java.util.List;

/**
 * Service managing weekly recurring practice schedules and practitioner absence exceptions.
 */
public interface DoctorScheduleService {

    List<DoctorAvailability> getWeeklySchedule(Long doctorId);

    boolean updateWeeklySchedule(Long doctorId, List<DoctorAvailability> schedules,
                                Long actorUserId, String ipAddress, String userAgent);

    List<DoctorAvailabilityException> getExceptions(Long doctorId);

    DoctorAvailabilityException addException(Long doctorId, DoctorAvailabilityException exception,
                                            Long actorUserId, String ipAddress, String userAgent);

    boolean deleteException(Long exceptionId, Long doctorId, Long actorUserId,
                            String ipAddress, String userAgent);
}

