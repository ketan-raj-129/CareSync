package com.caresync.dao;

import com.caresync.model.DoctorAvailability;

import java.util.List;
import java.util.Optional;

public interface DoctorAvailabilityDao extends GenericDao<DoctorAvailability, Long> {

    List<DoctorAvailability> findByDoctorId(Long doctorId);

    default List<DoctorAvailability> findActiveByDoctorId(Long doctorId) {
        return findByDoctorId(doctorId);
    }

    Optional<DoctorAvailability> findByDoctorAndDay(Long doctorId, int dayOfWeek);

    default boolean deleteByDoctorId(Long doctorId) {
        return false;
    }
}

