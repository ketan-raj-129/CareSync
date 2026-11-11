package com.caresync.dto;

import com.caresync.model.ExceptionType;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Data transfer object for administrator-managed clinic-wide holidays and operating exceptions.
 */
public class ClinicHolidayRequestDto implements Serializable {

    private LocalDate holidayDate;
    private String reason;
    private ExceptionType exceptionType = ExceptionType.HOLIDAY;

    public ClinicHolidayRequestDto() {}

    public ClinicHolidayRequestDto(LocalDate holidayDate, String reason) {
        this(holidayDate, reason, ExceptionType.HOLIDAY);
    }

    public ClinicHolidayRequestDto(LocalDate holidayDate, String reason, boolean isAvailable) {
        this(holidayDate, reason, ExceptionType.HOLIDAY);
    }

    public ClinicHolidayRequestDto(LocalDate holidayDate, String reason, ExceptionType exceptionType) {
        this.holidayDate = holidayDate;
        this.reason = reason;
        this.exceptionType = exceptionType != null ? exceptionType : ExceptionType.HOLIDAY;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ExceptionType getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(ExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }
}
