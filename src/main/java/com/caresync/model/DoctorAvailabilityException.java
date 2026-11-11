package com.caresync.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents doctor availability exceptions such as full-day leaves,
 * partial time-offs, emergency absences, and clinic-wide holidays.
 */
public class DoctorAvailabilityException extends BaseModel {

    private Long doctorId; // NULL indicates clinic-wide holiday
    private LocalDate exceptionDate;
    private boolean fullDay = true;
    private LocalTime startTime;
    private LocalTime endTime;
    private ExceptionType exceptionType = ExceptionType.LEAVE;
    private String reason;

    public DoctorAvailabilityException() {
        super();
    }

    public DoctorAvailabilityException(Long id) {
        super(id);
    }

    public Long getExceptionId() {
        return getId();
    }

    public void setExceptionId(Long exceptionId) {
        setId(exceptionId);
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public boolean isClinicWideHoliday() {
        return doctorId == null;
    }

    public LocalDate getExceptionDate() {
        return exceptionDate;
    }

    public void setExceptionDate(LocalDate exceptionDate) {
        this.exceptionDate = exceptionDate;
    }

    public boolean isFullDay() {
        return fullDay;
    }

    public void setFullDay(boolean fullDay) {
        this.fullDay = fullDay;
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

    public ExceptionType getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(ExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

