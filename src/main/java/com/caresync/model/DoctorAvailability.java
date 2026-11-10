package com.caresync.model;

import java.time.LocalTime;

/**
 * Weekly recurring availability template for a doctor.
 */
public class DoctorAvailability extends BaseModel {

    private Long doctorId;
    private int dayOfWeek; // 1 = Monday, 7 = Sunday
    private LocalTime startTime;
    private LocalTime endTime;
    private int slotDurationMinutes = 30;
    private boolean active = true;

    public DoctorAvailability() {
        super();
    }

    public DoctorAvailability(Long id) {
        super(id);
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("Day of week must be between 1 (Monday) and 7 (Sunday)");
        }
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public void setSlotDurationMinutes(int slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

