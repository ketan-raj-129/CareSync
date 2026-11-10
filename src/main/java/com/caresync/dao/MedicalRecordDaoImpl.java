package com.caresync.dao;

import com.caresync.exception.DatabaseOperationException;
import com.caresync.model.MedicalRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicalRecordDaoImpl extends BaseDao<MedicalRecord, Long> implements MedicalRecordDao {

    private static final String SELECT_BASE =
            "SELECT record_id, appointment_id, patient_id, doctor_id, " +
            "symptoms, diagnosis, treatment_plan, clinical_notes, created_at, updated_at " +
            "FROM medical_records ";

    @Override
    public Optional<MedicalRecord> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = SELECT_BASE + "WHERE record_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRecord(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find medical record by ID: " + id, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<MedicalRecord> findAll() {
        String sql = SELECT_BASE + "ORDER BY created_at DESC";
        List<MedicalRecord> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToRecord(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve medical records", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public Optional<MedicalRecord> findByAppointmentId(Long appointmentId) {
        if (appointmentId == null) return Optional.empty();
        String sql = SELECT_BASE + "WHERE appointment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRecord(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find medical record by appointment ID: " + appointmentId, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<MedicalRecord> findByPatientId(Long patientId) {
        String sql = SELECT_BASE + "WHERE patient_id = ? ORDER BY created_at DESC";
        List<MedicalRecord> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRecord(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find medical records for patient ID: " + patientId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<MedicalRecord> findByDoctorId(Long doctorId) {
        String sql = SELECT_BASE + "WHERE doctor_id = ? ORDER BY created_at DESC";
        List<MedicalRecord> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRecord(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find medical records for doctor ID: " + doctorId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public MedicalRecord save(MedicalRecord record) {
        try (Connection conn = getConnection()) {
            return save(record, conn);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to persist medical record: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public MedicalRecord save(MedicalRecord record, Connection conn) {
        String sql = "INSERT INTO medical_records (appointment_id, patient_id, doctor_id, " +
                     "symptoms, diagnosis, treatment_plan, clinical_notes, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, record.getAppointmentId());
            stmt.setLong(2, record.getPatientId());
            stmt.setLong(3, record.getDoctorId());
            stmt.setString(4, record.getSymptoms());
            stmt.setString(5, record.getDiagnosis());
            setStringOrNull(stmt, 6, record.getTreatmentPlan());
            setStringOrNull(stmt, 7, record.getClinicalNotes());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DatabaseOperationException("Creating medical record failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    record.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseOperationException("Creating medical record failed, no ID obtained.");
                }
            }
            return record;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert medical record: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean update(MedicalRecord record) {
        String sql = "UPDATE medical_records SET symptoms = ?, diagnosis = ?, treatment_plan = ?, " +
                     "clinical_notes = ?, updated_at = CURRENT_TIMESTAMP WHERE record_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, record.getSymptoms());
            stmt.setString(2, record.getDiagnosis());
            setStringOrNull(stmt, 3, record.getTreatmentPlan());
            setStringOrNull(stmt, 4, record.getClinicalNotes());
            stmt.setLong(5, record.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update medical record ID: " + record.getId(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM medical_records WHERE record_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete medical record ID: " + id, e.getSQLState(), e);
        }
    }

    private MedicalRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        MedicalRecord record = new MedicalRecord();
        record.setId(rs.getLong("record_id"));
        record.setAppointmentId(rs.getLong("appointment_id"));
        record.setPatientId(rs.getLong("patient_id"));
        record.setDoctorId(rs.getLong("doctor_id"));
        record.setSymptoms(rs.getString("symptoms"));
        record.setDiagnosis(rs.getString("diagnosis"));
        record.setTreatmentPlan(rs.getString("treatment_plan"));
        record.setClinicalNotes(rs.getString("clinical_notes"));
        record.setCreatedAt(getInstantOrNull(rs, "created_at"));
        record.setUpdatedAt(getInstantOrNull(rs, "updated_at"));
        return record;
    }
}

