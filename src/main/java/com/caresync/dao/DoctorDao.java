package com.caresync.dao;

import com.caresync.model.Doctor;

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
}

