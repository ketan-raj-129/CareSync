package com.caresync.service;

import com.caresync.dto.PaginatedResult;
import com.caresync.dto.SpecialtyFormDto;
import com.caresync.model.Specialty;

import java.util.List;
import java.util.Optional;

public interface AdminSpecialtyService {

    List<Specialty> getAllSpecialties();

    PaginatedResult<Specialty> getSpecialties(String query, Boolean activeOnly, int page, int pageSize);

    Optional<Specialty> getSpecialtyById(Long id);

    Specialty createSpecialty(SpecialtyFormDto dto, Long actorAdminId, String ipAddress, String userAgent);

    boolean updateSpecialty(Long id, SpecialtyFormDto dto, Long actorAdminId, String ipAddress, String userAgent);

    boolean toggleSpecialtyActive(Long id, boolean active, Long actorAdminId, String ipAddress, String userAgent);

    long getDoctorCountForSpecialty(Long specialtyId);
}

