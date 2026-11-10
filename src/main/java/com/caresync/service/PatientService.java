package com.caresync.service;

import com.caresync.dto.PatientProfileUpdateDto;
import com.caresync.model.Patient;

import java.util.Optional;

/**
 * Service managing patient profile retrieval and demographic updates.
 */
public interface PatientService {

    Optional<Patient> getPatientByUserId(Long userId);

    Optional<Patient> getPatientById(Long patientId);

    boolean updatePatientProfile(Long userId, Long patientId, PatientProfileUpdateDto dto,
                                 Long actorUserId, String ipAddress, String userAgent);
}

