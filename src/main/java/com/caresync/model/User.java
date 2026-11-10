package com.caresync.model;

import java.util.Objects;

/**
 * Base User entity encapsulating authentication credentials, names, and lifecycle state.
 */
public class User extends BaseModel {

    private String email;
    private String passwordHash;
    private String passwordSalt;
    private int hashIterations = 600000;
    private Role role;
    private UserStatus status = UserStatus.ACTIVE;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    public User() {
        super();
    }

    public User(Long id) {
        super(id);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public int getHashIterations() {
        return hashIterations;
    }

    public void setHashIterations(int hashIterations) {
        if (hashIterations < 1000) {
            throw new IllegalArgumentException("PBKDF2 iterations cannot be lower than minimum 1,000");
        }
        this.hashIterations = hashIterations;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName != null ? firstName.trim() : null;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName != null ? lastName.trim() : null;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;
    }

    public String getFullName() {
        if (firstName == null && lastName == null) return "";
        if (firstName == null) return lastName;
        if (lastName == null) return firstName;
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public String getDashboardUrl() {
        if (role == null) return "/login";
        return switch (role) {
            case ADMIN -> "/admin/dashboard";
            case DOCTOR -> "/doctor/dashboard";
            case PATIENT -> "/patient/dashboard";
        };
    }

    public boolean canAccessPatientRecord(Long targetPatientId, Object validator) {
        return false;
    }
}

