package com.caresync.service;

import com.caresync.dto.ClinicHolidayRequestDto;
import com.caresync.model.Appointment;
import com.caresync.model.DoctorAvailabilityException;

import java.time.LocalDate;
import java.util.List;

public interface AdminHolidayService {

    List<DoctorAvailabilityException> getClinicHolidays();

    DoctorAvailabilityException addClinicHoliday(ClinicHolidayRequestDto dto, Long actorAdminId,
                                                String ipAddress, String userAgent);

    boolean deleteClinicHoliday(Long exceptionId, Long actorAdminId,
                               String ipAddress, String userAgent);

    List<Appointment> getAffectedAppointments(LocalDate holidayDate);
}
