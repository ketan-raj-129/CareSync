package com.caresync.service;

import com.caresync.dto.PaginatedResult;
import com.caresync.model.Doctor;
import com.caresync.model.UserStatus;

import java.util.Optional;

public interface AdminDoctorService {

    PaginatedResult<Doctor> getDoctors(String query, Long specialtyId, UserStatus status, Boolean available, int page, int pageSize);

    Optional<Doctor> getDoctorDetails(Long doctorId);

    boolean updateDoctorStatus(Long doctorId, UserStatus newStatus, Long actorAdminId, String ipAddress, String userAgent);

    boolean toggleDoctorAvailability(Long doctorId, boolean isAvailable, Long actorAdminId, String ipAddress, String userAgent);
}
