package com.caresync.service;

import com.caresync.dto.PaginatedResult;
import com.caresync.model.Patient;
import com.caresync.model.UserStatus;

import java.util.Optional;

public interface AdminPatientService {

    PaginatedResult<Patient> getPatients(String query, UserStatus status, int page, int pageSize);

    Optional<Patient> getPatientDetails(Long patientId);

    boolean updatePatientStatus(Long patientId, UserStatus newStatus, Long actorAdminId, String ipAddress, String userAgent);
}
