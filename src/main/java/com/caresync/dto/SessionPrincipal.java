package com.caresync.dto;

import com.caresync.model.Role;

import java.io.Serializable;
import java.util.Objects;

/**
 * Minimal authenticated user identity retained in the HTTP session.
 * Contains zero sensitive credentials, hashes, or database connection handles.
 */
public class SessionPrincipal implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String email;
    private final Role role;
    private final String firstName;
    private final String lastName;
    private final String displayName;

    public SessionPrincipal(Long userId, String email, Role role, String firstName, String lastName) {
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.email = Objects.requireNonNull(email, "email cannot be null");
        this.role = Objects.requireNonNull(role, "role cannot be null");
        this.firstName = firstName != null ? firstName.trim() : "";
        this.lastName = lastName != null ? lastName.trim() : "";

        String name = (this.firstName + " " + this.lastName).trim();
        if (name.isEmpty()) {
            this.displayName = this.email;
        } else if (this.role == Role.DOCTOR && !name.toLowerCase().startsWith("dr.")) {
            this.displayName = "Dr. " + name;
        } else {
            this.displayName = name;
        }
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public boolean isDoctor() {
        return this.role == Role.DOCTOR;
    }

    public boolean isPatient() {
        return this.role == Role.PATIENT;
    }

    public String getDashboardUrl() {
        return switch (this.role) {
            case ADMIN -> "/admin/dashboard";
            case DOCTOR -> "/doctor/dashboard";
            case PATIENT -> "/patient/dashboard";
        };
    }

    @Override
    public String toString() {
        return "SessionPrincipal{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}

