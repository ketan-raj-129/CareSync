package com.caresync.service;

import com.caresync.dto.BookingRequestDto;
import com.caresync.model.Appointment;

import java.util.List;
import java.util.Optional;

/**
 * Service managing appointment bookings, concurrency protection, patient consultations,
 * and cancellation lifecycles.
 */
public interface AppointmentService {

    Appointment bookAppointment(Long patientId, BookingRequestDto request, Long actorUserId,
                                String ipAddress, String userAgent);

    List<Appointment> getAppointmentsForPatient(Long patientId);

    List<Appointment> getUpcomingAppointmentsForPatient(Long patientId);

    List<Appointment> getPastAppointmentsForPatient(Long patientId);

    Optional<Appointment> getAppointmentDetails(Long appointmentId, Long patientId);

    boolean cancelAppointment(Long appointmentId, Long patientId, String cancellationReason,
                              Long actorUserId, String ipAddress, String userAgent);
}

