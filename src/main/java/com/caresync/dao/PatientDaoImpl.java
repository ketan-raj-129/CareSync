package com.caresync.dao;

import com.caresync.exception.DatabaseOperationException;
import com.caresync.model.BloodGroup;
import com.caresync.model.Gender;
import com.caresync.model.Patient;
import com.caresync.model.Role;
import com.caresync.model.UserStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientDaoImpl extends BaseDao<Patient, Long> implements PatientDao {

    @Override
    public Optional<Patient> findById(Long patientId) {
        if (patientId == null) return Optional.empty();
        String sql = "SELECT p.patient_id, p.user_id, p.date_of_birth, p.gender, p.blood_group, p.address, " +
                     "p.emergency_contact_name, p.emergency_contact_phone, p.created_at, p.updated_at, " +
                     "u.email, u.first_name, u.last_name, u.phone_number, u.status, u.role " +
                     "FROM patient_profiles p " +
                     "JOIN users u ON p.user_id = u.user_id " +
                     "WHERE p.patient_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find patient profile by ID: " + patientId, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Patient> findByUserId(Long userId) {
        if (userId == null) return Optional.empty();
        String sql = "SELECT p.patient_id, p.user_id, p.date_of_birth, p.gender, p.blood_group, p.address, " +
                     "p.emergency_contact_name, p.emergency_contact_phone, p.created_at, p.updated_at, " +
                     "u.email, u.first_name, u.last_name, u.phone_number, u.status, u.role " +
                     "FROM patient_profiles p " +
                     "JOIN users u ON p.user_id = u.user_id " +
                     "WHERE p.user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find patient profile by user ID: " + userId, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Patient> findAll() {
        String sql = "SELECT p.patient_id, p.user_id, p.date_of_birth, p.gender, p.blood_group, p.address, " +
                     "p.emergency_contact_name, p.emergency_contact_phone, p.created_at, p.updated_at, " +
                     "u.email, u.first_name, u.last_name, u.phone_number, u.status, u.role " +
                     "FROM patient_profiles p " +
                     "JOIN users u ON p.user_id = u.user_id " +
                     "ORDER BY u.last_name ASC";

        List<Patient> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToPatient(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve patients", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public Patient save(Patient patient) {
        try (Connection conn = getConnection()) {
            return saveProfile(patient, conn);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save patient profile", e.getSQLState(), e);
        }
    }

    @Override
    public Patient saveProfile(Patient patient, Connection conn) {
        String sql = "INSERT INTO patient_profiles (user_id, date_of_birth, gender, blood_group, address, " +
                     "emergency_contact_name, emergency_contact_phone, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, patient.getId()); // references users.user_id
            setDateOrNull(stmt, 2, patient.getDateOfBirth());
            stmt.setString(3, patient.getGender() != null ? patient.getGender().name() : Gender.PREFER_NOT_TO_SAY.name());
            setStringOrNull(stmt, 4, patient.getBloodGroup() != null ? patient.getBloodGroup().getDisplayValue() : null);
            setStringOrNull(stmt, 5, patient.getAddress());
            setStringOrNull(stmt, 6, patient.getEmergencyContactName());
            setStringOrNull(stmt, 7, patient.getEmergencyContactPhone());
            setTimestampOrNull(stmt, 8, patient.getCreatedAt());
            setTimestampOrNull(stmt, 9, patient.getUpdatedAt());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        patient.setPatientProfileId(generatedKeys.getLong(1));
                    }
                }
            }
            return patient;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert patient profile: " + patient.getId(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean update(Patient patient) {
        String sql = "UPDATE patient_profiles SET date_of_birth = ?, gender = ?, blood_group = ?, " +
                     "address = ?, emergency_contact_name = ?, emergency_contact_phone = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE patient_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setDateOrNull(stmt, 1, patient.getDateOfBirth());
            stmt.setString(2, patient.getGender() != null ? patient.getGender().name() : Gender.PREFER_NOT_TO_SAY.name());
            setStringOrNull(stmt, 3, patient.getBloodGroup() != null ? patient.getBloodGroup().getDisplayValue() : null);
            setStringOrNull(stmt, 4, patient.getAddress());
            setStringOrNull(stmt, 5, patient.getEmergencyContactName());
            setStringOrNull(stmt, 6, patient.getEmergencyContactPhone());
            stmt.setLong(7, patient.getPatientProfileId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update patient profile: " + patient.getPatientProfileId(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        // Medical history preservation: soft delete via user account
        String sql = "UPDATE users SET status = 'INACTIVE' WHERE user_id = (SELECT user_id FROM patient_profiles WHERE patient_id = ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to inactivate patient: " + id, e.getSQLState(), e);
        }
    }

    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientProfileId(rs.getLong("patient_id"));
        p.setId(rs.getLong("user_id"));
        p.setEmail(rs.getString("email"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        p.setPhoneNumber(rs.getString("phone_number"));
        p.setStatus(UserStatus.fromString(rs.getString("status")));
        p.setRole(Role.fromString(rs.getString("role")));

        p.setDateOfBirth(getLocalDateOrNull(rs, "date_of_birth"));
        p.setGender(Gender.fromString(rs.getString("gender")));
        p.setBloodGroup(BloodGroup.fromString(rs.getString("blood_group")));
        p.setAddress(rs.getString("address"));
        p.setEmergencyContactName(rs.getString("emergency_contact_name"));
        p.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
        p.setCreatedAt(getInstantOrNull(rs, "created_at"));
        p.setUpdatedAt(getInstantOrNull(rs, "updated_at"));
        return p;
    }
}

