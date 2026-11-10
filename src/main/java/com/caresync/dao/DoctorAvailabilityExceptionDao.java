package com.caresync.dao;

import com.caresync.model.DoctorAvailabilityException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface DoctorAvailabilityExceptionDao extends GenericDao<DoctorAvailabilityException, Long> {

    List<DoctorAvailabilityException> findByDoctorId(Long doctorId);

    List<DoctorAvailabilityException> findExceptionsByDoctorAndDate(Long doctorId, LocalDate date);

    List<DoctorAvailabilityException> findClinicHolidays(LocalDate date);

    boolean hasActiveException(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime);
}

