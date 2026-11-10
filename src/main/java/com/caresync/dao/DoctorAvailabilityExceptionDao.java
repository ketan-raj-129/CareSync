package com.caresync.dao;

import com.caresync.model.DoctorAvailabilityException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface DoctorAvailabilityExceptionDao extends GenericDao<DoctorAvailabilityException, Long> {

    List<DoctorAvailabilityException> findByDoctorId(Long doctorId);

    default List<DoctorAvailabilityException> findExceptionsByDoctorAndDate(Long doctorId, LocalDate date) {
        return List.of();
    }

    default List<DoctorAvailabilityException> findClinicHolidays(LocalDate date) {
        return List.of();
    }

    default boolean hasActiveException(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return false;
    }
}

