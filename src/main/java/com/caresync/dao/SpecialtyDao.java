package com.caresync.dao;

import com.caresync.model.Specialty;

import java.util.List;
import java.util.Optional;

public interface SpecialtyDao extends GenericDao<Specialty, Long> {

    Optional<Specialty> findByName(String name);

    List<Specialty> findActiveSpecialties();

    default long countTotal() {
        return 0L;
    }

    default long countActive() {
        return 0L;
    }

    default long countDoctorsBySpecialtyId(Long specialtyId) {
        return 0L;
    }

    default List<Specialty> findPaginated(String query, Boolean activeOnly, int offset, int limit) {
        return List.of();
    }

    default long countFiltered(String query, Boolean activeOnly) {
        return 0L;
    }
}

