package com.caresync.service;

import com.caresync.dto.PaginatedResult;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;

import java.time.LocalDate;
import java.util.Optional;

public interface AdminAppointmentService {

    PaginatedResult<Appointment> getAppointments(String reference, Long doctorId, Long patientId,
                                                Long specialtyId, AppointmentStatus status,
                                                LocalDate date, int page, int pageSize);

    Optional<Appointment> getAppointmentDetails(Long appointmentId);

    boolean cancelAppointment(Long appointmentId, String reason, Long actorAdminId,
                             String ipAddress, String userAgent);
}

