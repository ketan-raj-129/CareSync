package com.caresync.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Patient entity extending User with demographic and baseline medical data.
 */
public class Patient extends User {

    private Long patientProfileId;
    private LocalDate dateOfBirth;
    private Gender gender;
    private BloodGroup bloodGroup;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;

    public Patient() {
        super();
        setRole(Role.PATIENT);
    }

    public Patient(Long id) {
        super(id);
        setRole(Role.PATIENT);
    }

    public Long getPatientProfileId() {
        return patientProfileId;
    }

    public void setPatientProfileId(Long patientProfileId) {
        this.patientProfileId = patientProfileId;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public int getAge() {
        if (dateOfBirth == null) return 0;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    @Override
    public String getDashboardUrl() {
        return "/patient/dashboard";
    }

    @Override
    public boolean canAccessPatientRecord(Long targetPatientId, Object validator) {
        if (targetPatientId == null) return false;
        return Objects.equals(this.patientProfileId, targetPatientId) || Objects.equals(this.getId(), targetPatientId);
    }
}
