package com.caresync.exception;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Thrown when an appointment booking conflicts with an existing active appointment
 * or violates PostgreSQL GiST range-based exclusion constraints.
 */
public class SlotConflictException extends CareSyncException {

    private final Long doctorId;
    private final LocalDate date;
    private final LocalTime slotTime;

    public SlotConflictException(String message) {
        this(null, null, null, message);
    }

    public SlotConflictException(Long doctorId, LocalDate date, LocalTime slotTime, String message) {
        super("SLOT_CONFLICT_ERROR", message);
        this.doctorId = doctorId;
        this.date = date;
        this.slotTime = slotTime;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getSlotTime() {
        return slotTime;
    }
}

