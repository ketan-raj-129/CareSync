package com.caresync.dao;

import com.caresync.model.Doctor;
import com.caresync.model.UserStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface DoctorDao extends GenericDao<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    List<Doctor> findBySpecialtyId(Long specialtyId);

    List<Doctor> findActiveDoctors();

    List<Doctor> searchDoctors(Long specialtyId, String query);

    default Doctor saveProfile(Doctor doctor, Connection conn) {
        return save(doctor);
    }

    default long countTotal() {
        return 0L;
    }

    default long countActive() {
        return 0L;
    }

    default long countBySpecialty(Long specialtyId) {
        return 0L;
    }

    default List<Doctor> findPaginated(String query, Long specialtyId, UserStatus status, Boolean available, int offset, int limit) {
        return List.of();
    }

    default long countFiltered(String query, Long specialtyId, UserStatus status, Boolean available) {
        return 0L;
    }

    default boolean updateAvailability(Long doctorId, boolean available) {
        return false;
    }
}

