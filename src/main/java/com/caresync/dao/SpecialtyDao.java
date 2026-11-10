package com.caresync.dao;

import com.caresync.model.Specialty;

import java.util.List;
import java.util.Optional;

public interface SpecialtyDao extends GenericDao<Specialty, Long> {

    Optional<Specialty> findByName(String name);

    List<Specialty> findActiveSpecialties();
}

