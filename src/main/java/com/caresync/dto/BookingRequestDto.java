package com.caresync.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Data Transfer Object capturing an appointment reservation submission.
 */
public class BookingRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long doctorId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private String reasonForVisit;

    public BookingRequestDto() {}

    public BookingRequestDto(Long doctorId, LocalDate appointmentDate, LocalTime startTime, String reasonForVisit) {
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.reasonForVisit = reasonForVisit;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public String getReasonForVisit() {
        return reasonForVisit;
    }

    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }
}

