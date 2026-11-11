package com.caresync.dao;

import com.caresync.exception.DatabaseOperationException;
import com.caresync.model.Doctor;
import com.caresync.model.Role;
import com.caresync.model.Specialty;
import com.caresync.model.UserStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DoctorDaoImpl extends BaseDao<Doctor, Long> implements DoctorDao {

    private static final String SELECT_DOCTOR_JOIN =
        "SELECT d.doctor_id, d.user_id, d.specialty_id, d.license_number, d.qualification, " +
        "d.experience_years, d.consultation_fee, d.bio, d.room_number, d.average_rating, " +
        "d.total_reviews, d.is_available, d.created_at, d.updated_at, " +
        "u.email, u.first_name, u.last_name, u.phone_number, u.status, u.role, " +
        "s.name AS specialty_name, s.description AS specialty_desc, s.icon_name AS specialty_icon " +
        "FROM doctor_profiles d " +
        "JOIN users u ON d.user_id = u.user_id " +
        "JOIN specialties s ON d.specialty_id = s.specialty_id ";

    @Override
    public Optional<Doctor> findById(Long doctorProfileId) {
        if (doctorProfileId == null) return Optional.empty();
        String sql = SELECT_DOCTOR_JOIN + "WHERE d.doctor_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorProfileId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find doctor by ID: " + doctorProfileId, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Doctor> findByUserId(Long userId) {
        if (userId == null) return Optional.empty();
        String sql = SELECT_DOCTOR_JOIN + "WHERE d.user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find doctor by user ID: " + userId, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Doctor> findByLicenseNumber(String licenseNumber) {
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) return Optional.empty();
        String sql = SELECT_DOCTOR_JOIN + "WHERE UPPER(d.license_number) = UPPER(?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, licenseNumber.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find doctor by license: " + licenseNumber, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Doctor> findBySpecialtyId(Long specialtyId) {
        if (specialtyId == null) return new ArrayList<>();
        String sql = SELECT_DOCTOR_JOIN + "WHERE d.specialty_id = ? AND d.is_available = TRUE AND u.status = 'ACTIVE' " +
                     "ORDER BY d.average_rating DESC, u.last_name ASC";

        List<Doctor> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, specialtyId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find doctors by specialty: " + specialtyId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<Doctor> findActiveDoctors() {
        String sql = SELECT_DOCTOR_JOIN + "WHERE d.is_available = TRUE AND u.status = 'ACTIVE' " +
                     "ORDER BY d.average_rating DESC, u.last_name ASC";

        List<Doctor> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToDoctor(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve active doctors", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<Doctor> searchDoctors(Long specialtyId, String query) {
        StringBuilder sql = new StringBuilder(SELECT_DOCTOR_JOIN)
                .append("WHERE d.is_available = TRUE AND u.status = 'ACTIVE' ");
        List<Object> params = new ArrayList<>();

        if (specialtyId != null && specialtyId > 0) {
            sql.append("AND d.specialty_id = ? ");
            params.add(specialtyId);
        }

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(u.first_name) LIKE ? OR LOWER(u.last_name) LIKE ? OR LOWER(u.first_name || ' ' || u.last_name) LIKE ? OR LOWER(d.qualification) LIKE ?) ");
            String pattern = "%" + query.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append("ORDER BY d.average_rating DESC, u.last_name ASC");

        List<Doctor> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Long l) {
                    stmt.setLong(i + 1, l);
                } else if (p instanceof String s) {
                    stmt.setString(i + 1, s);
                }
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to search doctors", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<Doctor> findAll() {
        String sql = SELECT_DOCTOR_JOIN + "ORDER BY u.last_name ASC";

        List<Doctor> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToDoctor(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve all doctors", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public Doctor save(Doctor doctor) {
        try (Connection conn = getConnection()) {
            return saveProfile(doctor, conn);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save doctor profile", e.getSQLState(), e);
        }
    }

    @Override
    public Doctor saveProfile(Doctor doctor, Connection conn) {
        String sql = "INSERT INTO doctor_profiles (user_id, specialty_id, license_number, qualification, " +
                     "experience_years, consultation_fee, bio, room_number, average_rating, total_reviews, " +
                     "is_available, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, doctor.getId()); // references users.user_id
            stmt.setLong(2, doctor.getSpecialtyId());
            stmt.setString(3, doctor.getLicenseNumber());
            stmt.setString(4, doctor.getQualification());
            stmt.setInt(5, doctor.getExperienceYears());
            stmt.setBigDecimal(6, doctor.getConsultationFee() != null ? doctor.getConsultationFee() : BigDecimal.ZERO);
            setStringOrNull(stmt, 7, doctor.getBio());
            setStringOrNull(stmt, 8, doctor.getRoomNumber());
            stmt.setBigDecimal(9, doctor.getAverageRating() != null ? doctor.getAverageRating() : BigDecimal.ZERO);
            stmt.setInt(10, doctor.getTotalReviews());
            stmt.setBoolean(11, doctor.isAvailable());
            setTimestampOrNull(stmt, 12, doctor.getCreatedAt());
            setTimestampOrNull(stmt, 13, doctor.getUpdatedAt());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        doctor.setDoctorProfileId(generatedKeys.getLong(1));
                    }
                }
            }
            return doctor;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert doctor profile: " + doctor.getLicenseNumber(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean update(Doctor doctor) {
        String sql = "UPDATE doctor_profiles SET specialty_id = ?, qualification = ?, experience_years = ?, " +
                     "consultation_fee = ?, bio = ?, room_number = ?, is_available = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE doctor_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctor.getSpecialtyId());
            stmt.setString(2, doctor.getQualification());
            stmt.setInt(3, doctor.getExperienceYears());
            stmt.setBigDecimal(4, doctor.getConsultationFee());
            setStringOrNull(stmt, 5, doctor.getBio());
            setStringOrNull(stmt, 6, doctor.getRoomNumber());
            stmt.setBoolean(7, doctor.isAvailable());
            stmt.setLong(8, doctor.getDoctorProfileId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update doctor profile: " + doctor.getDoctorProfileId(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        // Soft delete via availability toggle and user status
        String sql = "UPDATE doctor_profiles SET is_available = FALSE WHERE doctor_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to deactivate doctor: " + id, e.getSQLState(), e);
        }
    }

    @Override
    public boolean updateAvailability(Long doctorId, boolean available) {
        String sql = "UPDATE doctor_profiles SET is_available = ?, updated_at = CURRENT_TIMESTAMP WHERE doctor_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, available);
            stmt.setLong(2, doctorId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update doctor availability: " + doctorId, e.getSQLState(), e);
        }
    }

    @Override
    public long countTotal() {
        String sql = "SELECT COUNT(*) FROM doctor_profiles";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count doctors", e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public long countActive() {
        String sql = "SELECT COUNT(*) FROM doctor_profiles d JOIN users u ON d.user_id = u.user_id WHERE u.status = 'ACTIVE' AND d.is_available = TRUE";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count active doctors", e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public long countBySpecialty(Long specialtyId) {
        if (specialtyId == null) return 0L;
        String sql = "SELECT COUNT(*) FROM doctor_profiles WHERE specialty_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, specialtyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count doctors by specialty: " + specialtyId, e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public List<Doctor> findPaginated(String query, Long specialtyId, UserStatus status, Boolean available, int offset, int limit) {
        StringBuilder sql = new StringBuilder(SELECT_DOCTOR_JOIN).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(u.first_name) LIKE ? OR LOWER(u.last_name) LIKE ? OR LOWER(d.license_number) LIKE ? OR LOWER(u.email) LIKE ?) ");
            String wild = "%" + query.trim().toLowerCase() + "%";
            params.add(wild);
            params.add(wild);
            params.add(wild);
            params.add(wild);
        }
        if (specialtyId != null && specialtyId > 0) {
            sql.append("AND d.specialty_id = ? ");
            params.add(specialtyId);
        }
        if (status != null) {
            sql.append("AND u.status = ? ");
            params.add(status.name());
        }
        if (available != null) {
            sql.append("AND d.is_available = ? ");
            params.add(available);
        }

        sql.append("ORDER BY u.last_name ASC LIMIT ? OFFSET ?");
        params.add(Math.max(1, limit));
        params.add(Math.max(0, offset));

        List<Doctor> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to paginate doctors", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public long countFiltered(String query, Long specialtyId, UserStatus status, Boolean available) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM doctor_profiles d " +
                "JOIN users u ON d.user_id = u.user_id " +
                "JOIN specialties s ON d.specialty_id = s.specialty_id " +
                "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(u.first_name) LIKE ? OR LOWER(u.last_name) LIKE ? OR LOWER(d.license_number) LIKE ? OR LOWER(u.email) LIKE ?) ");
            String wild = "%" + query.trim().toLowerCase() + "%";
            params.add(wild);
            params.add(wild);
            params.add(wild);
            params.add(wild);
        }
        if (specialtyId != null && specialtyId > 0) {
            sql.append("AND d.specialty_id = ? ");
            params.add(specialtyId);
        }
        if (status != null) {
            sql.append("AND u.status = ? ");
            params.add(status.name());
        }
        if (available != null) {
            sql.append("AND d.is_available = ? ");
            params.add(available);
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count filtered doctors", e.getSQLState(), e);
        }
        return 0L;
    }

    private Doctor mapResultSetToDoctor(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setDoctorProfileId(rs.getLong("doctor_id"));
        d.setId(rs.getLong("user_id"));
        d.setEmail(rs.getString("email"));
        d.setFirstName(rs.getString("first_name"));
        d.setLastName(rs.getString("last_name"));
        d.setPhoneNumber(rs.getString("phone_number"));
        d.setStatus(UserStatus.fromString(rs.getString("status")));
        d.setRole(Role.fromString(rs.getString("role")));

        d.setSpecialtyId(rs.getLong("specialty_id"));
        d.setLicenseNumber(rs.getString("license_number"));
        d.setQualification(rs.getString("qualification"));
        d.setExperienceYears(rs.getInt("experience_years"));
        d.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        d.setBio(rs.getString("bio"));
        d.setRoomNumber(rs.getString("room_number"));
        d.setAverageRating(rs.getBigDecimal("average_rating"));
        d.setTotalReviews(rs.getInt("total_reviews"));
        d.setAvailable(rs.getBoolean("is_available"));
        d.setCreatedAt(getInstantOrNull(rs, "created_at"));
        d.setUpdatedAt(getInstantOrNull(rs, "updated_at"));

        Specialty specialty = new Specialty();
        specialty.setId(rs.getLong("specialty_id"));
        specialty.setName(rs.getString("specialty_name"));
        specialty.setDescription(rs.getString("specialty_desc"));
        specialty.setIconName(rs.getString("specialty_icon"));
        d.setSpecialty(specialty);

        return d;
    }
}

