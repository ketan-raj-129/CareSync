package com.caresync.dto;

import java.math.BigDecimal;

/**
 * Data transfer object for doctor profile self-service updates.
 * Only permits modification of approved clinical practitioner fields.
 */
public class DoctorProfileUpdateDto {

    private Long specialtyId;
    private String qualification;
    private int experienceYears;
    private BigDecimal consultationFee;
    private String bio;
    private String roomNumber;
    private String phoneNumber;
    private boolean available = true;

    public DoctorProfileUpdateDto() {}

    public Long getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(Long specialtyId) {
        this.specialtyId = specialtyId;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
