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

    default List<Appointment> findWithDetailsByPatientId(Long patientId) {
        return findByPatientId(patientId);
    }

    default List<Appointment> findWithDetailsByDoctorId(Long doctorId) {
        return findByDoctorId(doctorId);
    }

    default List<Appointment> findWithDetailsByDoctorAndDate(Long doctorId, LocalDate date) {
        return findByDoctorAndDate(doctorId, date);
    }

    default Optional<Appointment> findWithDetailsById(Long appointmentId) {
        return findById(appointmentId);
    }

    default List<Appointment> findUpcomingByPatientId(Long patientId) {
        return List.of();
    }

    default List<Appointment> findUpcomingByDoctorId(Long doctorId) {
        return List.of();
    }

    default List<Appointment> findByStatus(AppointmentStatus status) {
        return List.of();
    }

    default boolean isSlotBooked(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return false;
    }

    default Appointment save(Appointment appointment, Connection conn) {
        return save(appointment);
    }

    boolean updateStatus(Long appointmentId, AppointmentStatus status, String cancellationReason, Long cancelledByUserId);

    default boolean updateStatus(Long appointmentId, AppointmentStatus status, String cancellationReason, Long cancelledByUserId, Connection conn) {
        return updateStatus(appointmentId, status, cancellationReason, cancelledByUserId);
    }

    default long countTotal() {
        return 0L;
    }

    default long countByStatus(AppointmentStatus status) {
        return 0L;
    }

    default long countTodayAppointments() {
        return 0L;
    }

    default long countByDoctorId(Long doctorId) {
        return 0L;
    }

    default long countByDoctorAndStatus(Long doctorId, AppointmentStatus status) {
        return 0L;
    }

    default long countTodayByDoctorId(Long doctorId, LocalDate today) {
        return 0L;
    }
}
