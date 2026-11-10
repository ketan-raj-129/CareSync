package com.caresync.dao;

import com.caresync.exception.DatabaseOperationException;
import com.caresync.exception.SlotConflictException;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.Doctor;
import com.caresync.model.Patient;
import com.caresync.model.Specialty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AppointmentDaoImpl extends BaseDao<Appointment, Long> implements AppointmentDao {

    private static final String SELECT_BASE =
            "SELECT appointment_id, appointment_reference, patient_id, doctor_id, " +
            "appointment_date, start_time, end_time, status, reason_for_visit, " +
            "cancellation_reason, cancelled_by_user_id, created_at, updated_at " +
            "FROM appointments ";

    @Override
    public Optional<Appointment> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = SELECT_BASE + "WHERE appointment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find appointment by ID: " + id, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Appointment> findByReference(String reference) {
        if (reference == null || reference.trim().isEmpty()) return Optional.empty();
        String sql = SELECT_BASE + "WHERE appointment_reference = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reference.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find appointment by reference: " + reference, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Appointment> findAll() {
        String sql = SELECT_BASE + "ORDER BY appointment_date DESC, start_time DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve all appointments", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<Appointment> findByDoctorId(Long doctorId) {
        String sql = SELECT_BASE + "WHERE doctor_id = ? ORDER BY appointment_date DESC, start_time DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find appointments for doctor ID: " + doctorId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<Appointment> findByDoctorAndDate(Long doctorId, LocalDate date) {
        String sql = SELECT_BASE + "WHERE doctor_id = ? AND appointment_date = ? ORDER BY start_time ASC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            stmt.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find appointments for doctor ID: " + doctorId + " on date: " + date, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<Appointment> findByPatientId(Long patientId) {
        String sql = SELECT_BASE + "WHERE patient_id = ? ORDER BY appointment_date DESC, start_time DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find appointments for patient ID: " + patientId, e.getSQLState(), e);
        }
        return list;
    }

    private static final String SELECT_APPOINTMENT_DETAILS =
            "SELECT a.appointment_id, a.appointment_reference, a.patient_id, a.doctor_id, " +
            "a.appointment_date, a.start_time, a.end_time, a.status, a.reason_for_visit, " +
            "a.cancellation_reason, a.cancelled_by_user_id, a.created_at, a.updated_at, " +
            "du.first_name AS doc_first_name, du.last_name AS doc_last_name, du.email AS doc_email, du.phone_number AS doc_phone, " +
            "d.qualification AS doc_qualification, d.consultation_fee AS doc_fee, d.room_number AS doc_room, " +
            "s.specialty_id, s.name AS specialty_name, s.icon_name AS specialty_icon, " +
            "pu.first_name AS pat_first_name, pu.last_name AS pat_last_name, pu.email AS pat_email, pu.phone_number AS pat_phone, " +
            "p.blood_group, p.date_of_birth, p.gender " +
            "FROM appointments a " +
            "JOIN doctor_profiles d ON a.doctor_id = d.doctor_id " +
            "JOIN users du ON d.user_id = du.user_id " +
            "JOIN specialties s ON d.specialty_id = s.specialty_id " +
            "JOIN patient_profiles p ON a.patient_id = p.patient_id " +
            "JOIN users pu ON p.user_id = pu.user_id ";

    @Override
    public List<Appointment> findWithDetailsByPatientId(Long patientId) {
        String sql = SELECT_APPOINTMENT_DETAILS + "WHERE a.patient_id = ? ORDER BY a.appointment_date DESC, a.start_time DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointmentWithDetails(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find detailed appointments for patient ID: " + patientId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public Optional<Appointment> findWithDetailsById(Long appointmentId) {
        if (appointmentId == null) return Optional.empty();
        String sql = SELECT_APPOINTMENT_DETAILS + "WHERE a.appointment_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAppointmentWithDetails(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find detailed appointment by ID: " + appointmentId, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Appointment> findUpcomingByPatientId(Long patientId) {
        String sql = SELECT_BASE + "WHERE patient_id = ? AND (appointment_date > CURRENT_DATE OR (appointment_date = CURRENT_DATE AND end_time > CURRENT_TIME)) " +
                     "AND status IN ('PENDING', 'CONFIRMED') ORDER BY appointment_date ASC, start_time ASC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find upcoming appointments for patient ID: " + patientId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<Appointment> findUpcomingByDoctorId(Long doctorId) {
        String sql = SELECT_BASE + "WHERE doctor_id = ? AND (appointment_date > CURRENT_DATE OR (appointment_date = CURRENT_DATE AND end_time > CURRENT_TIME)) " +
                     "AND status IN ('PENDING', 'CONFIRMED') ORDER BY appointment_date ASC, start_time ASC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find upcoming appointments for doctor ID: " + doctorId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<Appointment> findByStatus(AppointmentStatus status) {
        String sql = SELECT_BASE + "WHERE status = ? ORDER BY appointment_date DESC, start_time DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find appointments by status: " + status, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public boolean isSlotBooked(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        String sql = "SELECT COUNT(*) FROM appointments " +
                     "WHERE doctor_id = ? AND appointment_date = ? " +
                     "AND status IN ('PENDING', 'CONFIRMED') " +
                     "AND slot_range && tsrange((?::date + ?::time)::timestamp, (?::date + ?::time)::timestamp, '[)')";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            stmt.setDate(2, java.sql.Date.valueOf(date));
            stmt.setDate(3, java.sql.Date.valueOf(date));
            stmt.setTime(4, java.sql.Time.valueOf(startTime));
            stmt.setDate(5, java.sql.Date.valueOf(date));
            stmt.setTime(6, java.sql.Time.valueOf(endTime));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to check if slot is booked", e.getSQLState(), e);
        }
        return false;
    }

    @Override
    public Appointment save(Appointment appointment) {
        try (Connection conn = getConnection()) {
            return save(appointment, conn);
        } catch (SQLException e) {
            handleSaveException(e, appointment);
            return null;
        }
    }

    @Override
    public Appointment save(Appointment appointment, Connection conn) {
        if (appointment.getReferenceNumber() == null || appointment.getReferenceNumber().isEmpty()) {
            appointment.setReferenceNumber("APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        String sql = "INSERT INTO appointments (appointment_reference, patient_id, doctor_id, " +
                     "appointment_date, start_time, end_time, slot_range, status, reason_for_visit, " +
                     "cancellation_reason, cancelled_by_user_id, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, " +
                     "tsrange((?::date + ?::time)::timestamp, (?::date + ?::time)::timestamp, '[)'), " +
                     "?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, appointment.getReferenceNumber());
            stmt.setLong(2, appointment.getPatientId());
            stmt.setLong(3, appointment.getDoctorId());
            stmt.setDate(4, java.sql.Date.valueOf(appointment.getAppointmentDate()));
            stmt.setTime(5, java.sql.Time.valueOf(appointment.getStartTime()));
            stmt.setTime(6, java.sql.Time.valueOf(appointment.getEndTime()));
            
            // For slot_range expression
            stmt.setDate(7, java.sql.Date.valueOf(appointment.getAppointmentDate()));
            stmt.setTime(8, java.sql.Time.valueOf(appointment.getStartTime()));
            stmt.setDate(9, java.sql.Date.valueOf(appointment.getAppointmentDate()));
            stmt.setTime(10, java.sql.Time.valueOf(appointment.getEndTime()));

            stmt.setString(11, appointment.getStatus() != null ? appointment.getStatus().name() : AppointmentStatus.PENDING.name());
            setStringOrNull(stmt, 12, appointment.getReasonForVisit());
            setStringOrNull(stmt, 13, appointment.getCancellationReason());
            setLongOrNull(stmt, 14, appointment.getCancelledByUserId());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DatabaseOperationException("Creating appointment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    appointment.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseOperationException("Creating appointment failed, no ID obtained.");
                }
            }
            return appointment;
        } catch (SQLException e) {
            handleSaveException(e, appointment);
            return null;
        }
    }

    private void handleSaveException(SQLException e, Appointment appointment) {
        // "23P01" is PostgreSQL exclusion_violation
        // "23505" is unique_violation
        if ("23P01".equals(e.getSQLState()) || (e.getMessage() != null && e.getMessage().contains("ex_no_overlapping"))) {
            throw new SlotConflictException(
                    "The selected appointment time slot [" + appointment.getStartTime() + " - " + appointment.getEndTime() + 
                    "] on " + appointment.getAppointmentDate() + " overlaps with an existing booking or is no longer available.");
        }
        throw new DatabaseOperationException("Failed to save appointment: " + e.getMessage(), e.getSQLState(), e);
    }

    @Override
    public boolean update(Appointment appointment) {
        String sql = "UPDATE appointments SET patient_id = ?, doctor_id = ?, appointment_date = ?, " +
                     "start_time = ?, end_time = ?, " +
                     "slot_range = tsrange((?::date + ?::time)::timestamp, (?::date + ?::time)::timestamp, '[)'), " +
                     "status = ?, reason_for_visit = ?, cancellation_reason = ?, cancelled_by_user_id = ?, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE appointment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, appointment.getPatientId());
            stmt.setLong(2, appointment.getDoctorId());
            stmt.setDate(3, java.sql.Date.valueOf(appointment.getAppointmentDate()));
            stmt.setTime(4, java.sql.Time.valueOf(appointment.getStartTime()));
            stmt.setTime(5, java.sql.Time.valueOf(appointment.getEndTime()));

            stmt.setDate(6, java.sql.Date.valueOf(appointment.getAppointmentDate()));
            stmt.setTime(7, java.sql.Time.valueOf(appointment.getStartTime()));
            stmt.setDate(8, java.sql.Date.valueOf(appointment.getAppointmentDate()));
            stmt.setTime(9, java.sql.Time.valueOf(appointment.getEndTime()));

            stmt.setString(10, appointment.getStatus().name());
            setStringOrNull(stmt, 11, appointment.getReasonForVisit());
            setStringOrNull(stmt, 12, appointment.getCancellationReason());
            setLongOrNull(stmt, 13, appointment.getCancelledByUserId());
            stmt.setLong(14, appointment.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23P01".equals(e.getSQLState()) || (e.getMessage() != null && e.getMessage().contains("ex_no_overlapping"))) {
                throw new SlotConflictException("Update failed: Target slot overlaps with another appointment.");
            }
            throw new DatabaseOperationException("Failed to update appointment ID: " + appointment.getId(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean updateStatus(Long appointmentId, AppointmentStatus status, String cancellationReason, Long cancelledByUserId) {
        try (Connection conn = getConnection()) {
            return updateStatus(appointmentId, status, cancellationReason, cancelledByUserId, conn);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update status for appointment: " + appointmentId, e.getSQLState(), e);
        }
    }

    @Override
    public boolean updateStatus(Long appointmentId, AppointmentStatus status, String cancellationReason, Long cancelledByUserId, Connection conn) {
        String sql = "UPDATE appointments SET status = ?, cancellation_reason = ?, cancelled_by_user_id = ?, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE appointment_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            setStringOrNull(stmt, 2, cancellationReason);
            setLongOrNull(stmt, 3, cancelledByUserId);
            stmt.setLong(4, appointmentId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update status for appointment: " + appointmentId, e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete appointment ID: " + id, e.getSQLState(), e);
        }
    }

    @Override
    public long countTotal() {
        String sql = "SELECT COUNT(*) FROM appointments";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count appointments", e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public long countByStatus(AppointmentStatus status) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE status = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count appointments by status: " + status, e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public long countTodayAppointments() {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURRENT_DATE";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count today's appointments", e.getSQLState(), e);
        }
        return 0L;
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment apt = new Appointment();
        apt.setId(rs.getLong("appointment_id"));
        apt.setReferenceNumber(rs.getString("appointment_reference"));
        apt.setPatientId(rs.getLong("patient_id"));
        apt.setDoctorId(rs.getLong("doctor_id"));
        apt.setAppointmentDate(getLocalDateOrNull(rs, "appointment_date"));
        apt.setStartTime(getLocalTimeOrNull(rs, "start_time"));
        apt.setEndTime(getLocalTimeOrNull(rs, "end_time"));
        apt.setStatus(AppointmentStatus.valueOf(rs.getString("status")));
        apt.setReasonForVisit(rs.getString("reason_for_visit"));
        apt.setCancellationReason(rs.getString("cancellation_reason"));
        apt.setCancelledByUserId(getLongOrNull(rs, "cancelled_by_user_id"));
        apt.setCreatedAt(getInstantOrNull(rs, "created_at"));
        apt.setUpdatedAt(getInstantOrNull(rs, "updated_at"));
        return apt;
    }

    private Appointment mapResultSetToAppointmentWithDetails(ResultSet rs) throws SQLException {
        Appointment apt = mapResultSetToAppointment(rs);

        Doctor doc = new Doctor();
        doc.setDoctorProfileId(rs.getLong("doctor_id"));
        doc.setFirstName(rs.getString("doc_first_name"));
        doc.setLastName(rs.getString("doc_last_name"));
        doc.setEmail(rs.getString("doc_email"));
        doc.setPhoneNumber(rs.getString("doc_phone"));
        doc.setQualification(rs.getString("doc_qualification"));
        doc.setConsultationFee(rs.getBigDecimal("doc_fee"));
        doc.setRoomNumber(rs.getString("doc_room"));

        Specialty spec = new Specialty();
        spec.setId(rs.getLong("specialty_id"));
        spec.setName(rs.getString("specialty_name"));
        spec.setIconName(rs.getString("specialty_icon"));
        doc.setSpecialty(spec);
        apt.setDoctor(doc);

        Patient pat = new Patient();
        pat.setPatientProfileId(rs.getLong("patient_id"));
        pat.setFirstName(rs.getString("pat_first_name"));
        pat.setLastName(rs.getString("pat_last_name"));
        pat.setEmail(rs.getString("pat_email"));
        pat.setPhoneNumber(rs.getString("pat_phone"));
        pat.setDateOfBirth(getLocalDateOrNull(rs, "date_of_birth"));
        apt.setPatient(pat);

        return apt;
    }
}

