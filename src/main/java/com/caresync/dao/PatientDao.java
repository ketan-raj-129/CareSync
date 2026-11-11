package com.caresync.dao;

import com.caresync.model.Patient;
import com.caresync.model.UserStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface PatientDao extends GenericDao<Patient, Long> {

    Optional<Patient> findByUserId(Long userId);

    Patient saveProfile(Patient patient, Connection conn);

    default long countTotal() {
        return 0L;
    }

    default long countActive() {
        return 0L;
    }

    default List<Patient> findPaginated(String query, UserStatus status, int offset, int limit) {
        return List.of();
    }

    default long countFiltered(String query, UserStatus status) {
        return 0L;
    }
}

