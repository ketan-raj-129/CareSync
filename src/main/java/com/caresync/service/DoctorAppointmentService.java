package com.caresync.service;

import com.caresync.model.Appointment;

import java.util.List;
import java.util.Optional;

/**
 * Service managing doctor appointment review, clinical scheduling, and status state transitions.
 */
public interface DoctorAppointmentService {

    List<Appointment> getAppointmentsForDoctor(Long doctorId, String filter);

    Optional<Appointment> getAppointmentDetails(Long appointmentId, Long doctorId);

    boolean confirmAppointment(Long appointmentId, Long doctorId, Long actorUserId,
                               String ipAddress, String userAgent);

    boolean rejectAppointment(Long appointmentId, Long doctorId, String reason,
                              Long actorUserId, String ipAddress, String userAgent);

    boolean completeAppointment(Long appointmentId, Long doctorId, Long actorUserId,
                               String ipAddress, String userAgent);

    boolean cancelAppointmentByDoctor(Long appointmentId, Long doctorId, String reason,
                                     Long actorUserId, String ipAddress, String userAgent);
}

