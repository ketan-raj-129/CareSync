package com.caresync.dao;

import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentDao extends GenericDao<Appointment, Long> {

    Optional<Appointment> findByReference(String reference);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByDoctorAndDate(Long doctorId, LocalDate date);

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findWithDetailsByPatientId(Long patientId);

    Optional<Appointment> findWithDetailsById(Long appointmentId);

    List<Appointment> findUpcomingByPatientId(Long patientId);

    List<Appointment> findUpcomingByDoctorId(Long doctorId);

    List<Appointment> findByStatus(AppointmentStatus status);

    boolean isSlotBooked(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime);

    Appointment save(Appointment appointment, Connection conn);

    boolean updateStatus(Long appointmentId, AppointmentStatus status, String cancellationReason, Long cancelledByUserId);

    boolean updateStatus(Long appointmentId, AppointmentStatus status, String cancellationReason, Long cancelledByUserId, Connection conn);

    long countTotal();

    long countByStatus(AppointmentStatus status);

    long countTodayAppointments();
}

