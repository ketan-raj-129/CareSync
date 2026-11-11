package com.caresync.service;

import com.caresync.dto.DoctorDashboardDto;
import com.caresync.dto.DoctorProfileUpdateDto;
import com.caresync.model.Doctor;

import java.util.Optional;

/**
 * Service managing practitioner dashboard metrics and profile maintenance.
 */
public interface DoctorPortalService {

    Optional<Doctor> getDoctorByUserId(Long userId);

    Optional<Doctor> getDoctorById(Long doctorId);

    DoctorDashboardDto getDashboardMetrics(Long doctorId);

    boolean updateDoctorProfile(Long doctorId, Long userId, DoctorProfileUpdateDto dto,
                                Long actorUserId, String ipAddress, String userAgent);
}

