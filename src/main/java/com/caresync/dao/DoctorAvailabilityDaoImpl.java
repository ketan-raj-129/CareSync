package com.caresync.dao;

import com.caresync.exception.DatabaseOperationException;
import com.caresync.model.DoctorAvailability;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DoctorAvailabilityDaoImpl extends BaseDao<DoctorAvailability, Long> implements DoctorAvailabilityDao {

    @Override
    public Optional<DoctorAvailability> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT availability_id, doctor_id, day_of_week, start_time, end_time, " +
                     "slot_duration_minutes, is_active, created_at, updated_at " +
                     "FROM doctor_availability WHERE availability_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAvailability(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find availability by ID: " + id, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<DoctorAvailability> findAll() {
        String sql = "SELECT availability_id, doctor_id, day_of_week, start_time, end_time, " +
                     "slot_duration_minutes, is_active, created_at, updated_at " +
                     "FROM doctor_availability ORDER BY doctor_id, day_of_week, start_time";

        List<DoctorAvailability> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAvailability(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve all doctor availabilities", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<DoctorAvailability> findByDoctorId(Long doctorId) {
        String sql = "SELECT availability_id, doctor_id, day_of_week, start_time, end_time, " +
                     "slot_duration_minutes, is_active, created_at, updated_at " +
                     "FROM doctor_availability WHERE doctor_id = ? ORDER BY day_of_week, start_time";

        List<DoctorAvailability> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAvailability(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find availability for doctor ID: " + doctorId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<DoctorAvailability> findActiveByDoctorId(Long doctorId) {
        String sql = "SELECT availability_id, doctor_id, day_of_week, start_time, end_time, " +
                     "slot_duration_minutes, is_active, created_at, updated_at " +
                     "FROM doctor_availability WHERE doctor_id = ? AND is_active = TRUE ORDER BY day_of_week, start_time";

        List<DoctorAvailability> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAvailability(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find active availability for doctor ID: " + doctorId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public Optional<DoctorAvailability> findByDoctorAndDay(Long doctorId, int dayOfWeek) {
        String sql = "SELECT availability_id, doctor_id, day_of_week, start_time, end_time, " +
                     "slot_duration_minutes, is_active, created_at, updated_at " +
                     "FROM doctor_availability WHERE doctor_id = ? AND day_of_week = ? AND is_active = TRUE";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            stmt.setInt(2, dayOfWeek);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAvailability(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find availability for doctor ID: " + doctorId + " day: " + dayOfWeek, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public DoctorAvailability save(DoctorAvailability availability) {
        String sql = "INSERT INTO doctor_availability (doctor_id, day_of_week, start_time, end_time, " +
                     "slot_duration_minutes, is_active, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, availability.getDoctorId());
            stmt.setInt(2, availability.getDayOfWeek());
            setTimeOrNull(stmt, 3, availability.getStartTime());
            setTimeOrNull(stmt, 4, availability.getEndTime());
            stmt.setInt(5, availability.getSlotDurationMinutes());
            stmt.setBoolean(6, availability.isActive());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DatabaseOperationException("Creating doctor availability failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    availability.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseOperationException("Creating doctor availability failed, no ID obtained.");
                }
            }
            return availability;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert doctor availability: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean update(DoctorAvailability availability) {
        String sql = "UPDATE doctor_availability SET day_of_week = ?, start_time = ?, end_time = ?, " +
                     "slot_duration_minutes = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE availability_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, availability.getDayOfWeek());
            setTimeOrNull(stmt, 2, availability.getStartTime());
            setTimeOrNull(stmt, 3, availability.getEndTime());
            stmt.setInt(4, availability.getSlotDurationMinutes());
            stmt.setBoolean(5, availability.isActive());
            stmt.setLong(6, availability.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update doctor availability: " + availability.getId(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM doctor_availability WHERE availability_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete doctor availability ID: " + id, e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteByDoctorId(Long doctorId) {
        String sql = "DELETE FROM doctor_availability WHERE doctor_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete availability for doctor ID: " + doctorId, e.getSQLState(), e);
        }
    }

    private DoctorAvailability mapResultSetToAvailability(ResultSet rs) throws SQLException {
        DoctorAvailability avail = new DoctorAvailability();
        avail.setId(rs.getLong("availability_id"));
        avail.setDoctorId(rs.getLong("doctor_id"));
        avail.setDayOfWeek(rs.getInt("day_of_week"));
        avail.setStartTime(getLocalTimeOrNull(rs, "start_time"));
        avail.setEndTime(getLocalTimeOrNull(rs, "end_time"));
        avail.setSlotDurationMinutes(rs.getInt("slot_duration_minutes"));
        avail.setActive(rs.getBoolean("is_active"));
        avail.setCreatedAt(getInstantOrNull(rs, "created_at"));
        avail.setUpdatedAt(getInstantOrNull(rs, "updated_at"));
        return avail;
    }
}

