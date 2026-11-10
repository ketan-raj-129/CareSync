package com.caresync.service;

import com.caresync.model.Doctor;
import com.caresync.model.Specialty;

import java.util.List;
import java.util.Optional;

/**
 * Service for exploring medical practitioners and specialties taxonomy.
 */
public interface DoctorDiscoveryService {

    List<Specialty> getActiveSpecialties();

    List<Doctor> searchDoctors(Long specialtyId, String query);

    Optional<Doctor> getDoctorById(Long doctorId);
}

