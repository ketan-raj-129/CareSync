package com.caresync.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Scheduled consultation entity connecting patient and doctor.
 */
public class Appointment extends BaseModel {

    private String referenceNumber;
    private Long patientId;
    private Long doctorId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentStatus status = AppointmentStatus.PENDING;
    private String reasonForVisit;
    private String cancellationReason;
    private Long cancelledByUserId;

    // Associated Entities (Optional join population)
    private Patient patient;
    private Doctor doctor;

    public Appointment() {
        super();
    }

    public Appointment(Long id) {
        super(id);
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
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

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getReasonForVisit() {
        return reasonForVisit;
    }

    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Long getCancelledByUserId() {
        return cancelledByUserId;
    }

    public void setCancelledByUserId(Long cancelledByUserId) {
        this.cancelledByUserId = cancelledByUserId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            this.patientId = patient.getPatientProfileId() != null ? patient.getPatientProfileId() : patient.getId();
        }
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        if (doctor != null) {
            this.doctorId = doctor.getDoctorProfileId() != null ? doctor.getDoctorProfileId() : doctor.getId();
        }
    }

    public boolean isPending() {
        return this.status == AppointmentStatus.PENDING;
    }

    public boolean isConfirmed() {
        return this.status == AppointmentStatus.CONFIRMED;
    }

    public boolean isCompleted() {
        return this.status == AppointmentStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return this.status == AppointmentStatus.CANCELLED;
    }

    /**
     * Business Rule BR-006: Patient can cancel if scheduled start time is more than 2 hours in the future.
     */
    public boolean canBeCancelledByPatient() {
        if (status != AppointmentStatus.PENDING && status != AppointmentStatus.CONFIRMED) {
            return false;
        }
        if (appointmentDate == null || startTime == null) {
            return false;
        }
        LocalDateTime appointmentDateTime = LocalDateTime.of(appointmentDate, startTime);
        return Duration.between(LocalDateTime.now(), appointmentDateTime).toHours() >= 2;
    }
}

