package com.caresync.service;

import com.caresync.dto.AuthenticationResult;
import com.caresync.dto.PatientRegistrationDto;
import com.caresync.model.Patient;

/**
 * Service contract for authentication, patient self-registration, and credential lifecycle.
 */
public interface AuthService {

    /**
     * Authenticates a user with email and candidate password.
     *
     * @param email      user email address
     * @param password   candidate password character array
     * @param ipAddress  client IP address for audit logging
     * @param userAgent  client User-Agent string for audit logging
     * @return AuthenticationResult indicating success or generic failure
     */
    AuthenticationResult authenticate(String email, char[] password, String ipAddress, String userAgent);

    /**
     * Atomically registers a new patient user account and profile in a single database transaction.
     * Enforces Role.PATIENT strictly.
     *
     * @param dto        patient registration parameters
     * @param ipAddress  client IP address
     * @param userAgent  client User-Agent
     * @return persisted Patient entity with generated IDs
     */
    Patient registerPatient(PatientRegistrationDto dto, String ipAddress, String userAgent);
}

