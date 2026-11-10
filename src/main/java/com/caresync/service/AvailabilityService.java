package com.caresync.service;

import com.caresync.dto.TimeSlotDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service calculating discrete consultation slot availability against doctor schedules,
 * time-off leaves, clinic holidays, and existing reservations.
 */
public interface AvailabilityService {

    /**
     * Computes candidate time slots for a doctor on a specific date.
     *
     * @param doctorId doctor profile ID
     * @param date     appointment date
     * @return list of generated time slots with availability flags and reasons
     */
    List<TimeSlotDto> getAvailableSlots(Long doctorId, LocalDate date);

    /**
     * Computes candidate time slots for a doctor on a specific date, also validating against
     * the requesting patient's concurrent schedule.
     *
     * @param doctorId  doctor profile ID
     * @param date      appointment date
     * @param patientId patient profile ID
     * @return list of generated time slots
     */
    List<TimeSlotDto> getAvailableSlots(Long doctorId, LocalDate date, Long patientId);

    /**
     * Validates whether a specific time interval is completely open and free of conflicts.
     *
     * @param doctorId  doctor profile ID
     * @param date      appointment date
     * @param startTime start time
     * @param endTime   end time
     * @param patientId patient profile ID
     * @return true if slot is conflict-free and valid, false otherwise
     */
    boolean isSlotAvailable(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime, Long patientId);
}

