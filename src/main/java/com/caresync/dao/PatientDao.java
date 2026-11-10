package com.caresync.dao;

import com.caresync.model.Patient;

import java.sql.Connection;
import java.util.Optional;

public interface PatientDao extends GenericDao<Patient, Long> {

    Optional<Patient> findByUserId(Long userId);

    Patient saveProfile(Patient patient, Connection conn);
}

