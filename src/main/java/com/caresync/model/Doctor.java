package com.caresync.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Healthcare provider entity extending User with licensing, specialty, and schedule templates.
 */
public class Doctor extends User {

    private Long doctorProfileId;
    private Long specialtyId;
    private Specialty specialty;
    private String licenseNumber;
    private String qualification;
    private int experienceYears;
    private BigDecimal consultationFee = BigDecimal.ZERO;
    private String bio;
    private String roomNumber;
    private BigDecimal averageRating = BigDecimal.ZERO;
    private int totalReviews = 0;
    private boolean available = true;

    private List<DoctorAvailability> availabilities = new ArrayList<>();
    private List<DoctorAvailabilityException> availabilityExceptions = new ArrayList<>();

    public Doctor() {
        super();
        setRole(Role.DOCTOR);
    }

    public Doctor(Long id) {
        super(id);
        setRole(Role.DOCTOR);
    }

    public Long getDoctorProfileId() {
        return doctorProfileId;
    }

    public void setDoctorProfileId(Long doctorProfileId) {
        this.doctorProfileId = doctorProfileId;
    }

    public Long getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(Long specialtyId) {
        this.specialtyId = specialtyId;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
        if (specialty != null) {
            this.specialtyId = specialty.getId();
        }
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber != null ? licenseNumber.trim().toUpperCase() : null;
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
        if (experienceYears < 0) {
            throw new IllegalArgumentException("Experience years cannot be negative");
        }
        this.experienceYears = experienceYears;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        if (consultationFee != null && consultationFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Consultation fee cannot be negative");
        }
        this.consultationFee = consultationFee != null ? consultationFee : BigDecimal.ZERO;
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

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating != null ? averageRating : BigDecimal.ZERO;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<DoctorAvailability> getAvailabilities() {
        return availabilities;
    }

    public void setAvailabilities(List<DoctorAvailability> availabilities) {
        this.availabilities = availabilities != null ? availabilities : new ArrayList<>();
    }

    public List<DoctorAvailabilityException> getAvailabilityExceptions() {
        return availabilityExceptions;
    }

    public void setAvailabilityExceptions(List<DoctorAvailabilityException> availabilityExceptions) {
        this.availabilityExceptions = availabilityExceptions != null ? availabilityExceptions : new ArrayList<>();
    }

    @Override
    public String getDashboardUrl() {
        return "/doctor/dashboard";
    }
}

