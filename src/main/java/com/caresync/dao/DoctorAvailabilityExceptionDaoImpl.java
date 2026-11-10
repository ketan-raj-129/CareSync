package com.caresync.dao;

import com.caresync.exception.DatabaseOperationException;
import com.caresync.model.DoctorAvailabilityException;
import com.caresync.model.ExceptionType;

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

public class DoctorAvailabilityExceptionDaoImpl extends BaseDao<DoctorAvailabilityException, Long>
        implements DoctorAvailabilityExceptionDao {

    @Override
    public Optional<DoctorAvailabilityException> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT exception_id, doctor_id, exception_date, is_full_day, start_time, end_time, " +
                     "exception_type, reason, created_at " +
                     "FROM doctor_availability_exceptions WHERE exception_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToException(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find exception by ID: " + id, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<DoctorAvailabilityException> findByDoctorId(Long doctorId) {
        String sql = "SELECT exception_id, doctor_id, exception_date, is_full_day, start_time, end_time, " +
                     "exception_type, reason, created_at " +
                     "FROM doctor_availability_exceptions WHERE doctor_id = ? ORDER BY exception_date DESC";

        List<DoctorAvailabilityException> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToException(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find exceptions for doctor: " + doctorId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<DoctorAvailabilityException> findExceptionsByDoctorAndDate(Long doctorId, LocalDate date) {
        String sql = "SELECT exception_id, doctor_id, exception_date, is_full_day, start_time, end_time, " +
                     "exception_type, reason, created_at " +
                     "FROM doctor_availability_exceptions " +
                     "WHERE (doctor_id = ? OR doctor_id IS NULL) AND exception_date = ?";

        List<DoctorAvailabilityException> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            setDateOrNull(stmt, 2, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToException(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find exceptions for doctor and date", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<DoctorAvailabilityException> findClinicHolidays(LocalDate date) {
        String sql = "SELECT exception_id, doctor_id, exception_date, is_full_day, start_time, end_time, " +
                     "exception_type, reason, created_at " +
                     "FROM doctor_availability_exceptions " +
                     "WHERE doctor_id IS NULL AND exception_date = ?";

        List<DoctorAvailabilityException> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setDateOrNull(stmt, 1, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToException(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find clinic holidays for date: " + date, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public boolean hasActiveException(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        String sql = "SELECT 1 FROM doctor_availability_exceptions " +
                     "WHERE (doctor_id = ? OR doctor_id IS NULL) AND exception_date = ? AND (" +
                     "  is_full_day = TRUE OR (" +
                     "    start_time < ? AND end_time > ?" +
                     "  )" +
                     ") LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            setDateOrNull(stmt, 2, date);
            setTimeOrNull(stmt, 3, endTime);
            setTimeOrNull(stmt, 4, startTime);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to check availability exception", e.getSQLState(), e);
        }
    }

    @Override
    public List<DoctorAvailabilityException> findAll() {
        String sql = "SELECT exception_id, doctor_id, exception_date, is_full_day, start_time, end_time, " +
                     "exception_type, reason, created_at " +
                     "FROM doctor_availability_exceptions ORDER BY exception_date DESC";

        List<DoctorAvailabilityException> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToException(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve exceptions", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public DoctorAvailabilityException save(DoctorAvailabilityException exception) {
        String sql = "INSERT INTO doctor_availability_exceptions (doctor_id, exception_date, is_full_day, " +
                     "start_time, end_time, exception_type, reason, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setLongOrNull(stmt, 1, exception.getDoctorId());
            setDateOrNull(stmt, 2, exception.getExceptionDate());
            stmt.setBoolean(3, exception.isFullDay());
            setTimeOrNull(stmt, 4, exception.getStartTime());
            setTimeOrNull(stmt, 5, exception.getEndTime());
            stmt.setString(6, exception.getExceptionType().name());
            setStringOrNull(stmt, 7, exception.getReason());
            setTimestampOrNull(stmt, 8, exception.getCreatedAt());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        exception.setId(generatedKeys.getLong(1));
                    }
                }
            }
            return exception;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save availability exception", e.getSQLState(), e);
        }
    }

    @Override
    public boolean update(DoctorAvailabilityException exception) {
        String sql = "UPDATE doctor_availability_exceptions SET is_full_day = ?, start_time = ?, " +
                     "end_time = ?, exception_type = ?, reason = ? WHERE exception_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, exception.isFullDay());
            setTimeOrNull(stmt, 2, exception.getStartTime());
            setTimeOrNull(stmt, 3, exception.getEndTime());
            stmt.setString(4, exception.getExceptionType().name());
            setStringOrNull(stmt, 5, exception.getReason());
            stmt.setLong(6, exception.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update availability exception: " + exception.getId(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM doctor_availability_exceptions WHERE exception_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete availability exception: " + id, e.getSQLState(), e);
        }
    }

    private DoctorAvailabilityException mapResultSetToException(ResultSet rs) throws SQLException {
        DoctorAvailabilityException ex = new DoctorAvailabilityException();
        ex.setId(rs.getLong("exception_id"));
        ex.setDoctorId(getLongOrNull(rs, "doctor_id"));
        ex.setExceptionDate(getLocalDateOrNull(rs, "exception_date"));
        ex.setFullDay(rs.getBoolean("is_full_day"));
        ex.setStartTime(getLocalTimeOrNull(rs, "start_time"));
        ex.setEndTime(getLocalTimeOrNull(rs, "end_time"));
        ex.setExceptionType(ExceptionType.fromString(rs.getString("exception_type")));
        ex.setReason(rs.getString("reason"));
        ex.setCreatedAt(getInstantOrNull(rs, "created_at"));
        return ex;
    }
}

