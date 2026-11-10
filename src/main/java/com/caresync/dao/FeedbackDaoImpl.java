package com.caresync.dao;

import com.caresync.exception.DatabaseOperationException;
import com.caresync.model.Feedback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeedbackDaoImpl extends BaseDao<Feedback, Long> implements FeedbackDao {

    private static final String SELECT_BASE =
            "SELECT feedback_id, appointment_id, patient_id, doctor_id, rating, comments, is_published, created_at " +
            "FROM feedback ";

    @Override
    public Optional<Feedback> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = SELECT_BASE + "WHERE feedback_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToFeedback(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find feedback by ID: " + id, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Feedback> findAll() {
        String sql = SELECT_BASE + "ORDER BY created_at DESC";
        List<Feedback> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToFeedback(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve feedback list", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public Optional<Feedback> findByAppointmentId(Long appointmentId) {
        if (appointmentId == null) return Optional.empty();
        String sql = SELECT_BASE + "WHERE appointment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToFeedback(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find feedback for appointment ID: " + appointmentId, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Feedback> findByDoctorId(Long doctorId) {
        String sql = SELECT_BASE + "WHERE doctor_id = ? ORDER BY created_at DESC";
        List<Feedback> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToFeedback(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find feedback for doctor ID: " + doctorId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<Feedback> findPublishedByDoctorId(Long doctorId) {
        String sql = SELECT_BASE + "WHERE doctor_id = ? AND is_published = TRUE ORDER BY created_at DESC";
        List<Feedback> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToFeedback(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find published feedback for doctor ID: " + doctorId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public double findAverageRatingByDoctorId(Long doctorId) {
        String sql = "SELECT COALESCE(AVG(rating), 0.0) FROM feedback WHERE doctor_id = ? AND is_published = TRUE";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Math.round(rs.getDouble(1) * 10.0) / 10.0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to calculate average rating for doctor ID: " + doctorId, e.getSQLState(), e);
        }
        return 0.0;
    }

    @Override
    public long countByDoctorId(Long doctorId) {
        String sql = "SELECT COUNT(*) FROM feedback WHERE doctor_id = ? AND is_published = TRUE";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count feedback for doctor ID: " + doctorId, e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public Feedback save(Feedback feedback) {
        String sql = "INSERT INTO feedback (appointment_id, patient_id, doctor_id, rating, comments, is_published, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, feedback.getAppointmentId());
            stmt.setLong(2, feedback.getPatientId());
            stmt.setLong(3, feedback.getDoctorId());
            stmt.setInt(4, feedback.getRating());
            setStringOrNull(stmt, 5, feedback.getComments());
            stmt.setBoolean(6, feedback.isPublished());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DatabaseOperationException("Creating feedback failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    feedback.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseOperationException("Creating feedback failed, no ID obtained.");
                }
            }
            return feedback;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert feedback: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean update(Feedback feedback) {
        String sql = "UPDATE feedback SET rating = ?, comments = ?, is_published = ? WHERE feedback_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, feedback.getRating());
            setStringOrNull(stmt, 2, feedback.getComments());
            stmt.setBoolean(3, feedback.isPublished());
            stmt.setLong(4, feedback.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update feedback ID: " + feedback.getId(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM feedback WHERE feedback_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete feedback ID: " + id, e.getSQLState(), e);
        }
    }

    private Feedback mapResultSetToFeedback(ResultSet rs) throws SQLException {
        Feedback fb = new Feedback();
        fb.setId(rs.getLong("feedback_id"));
        fb.setAppointmentId(rs.getLong("appointment_id"));
        fb.setPatientId(rs.getLong("patient_id"));
        fb.setDoctorId(rs.getLong("doctor_id"));
        fb.setRating(rs.getInt("rating"));
        fb.setComments(rs.getString("comments"));
        fb.setPublished(rs.getBoolean("is_published"));
        fb.setCreatedAt(getInstantOrNull(rs, "created_at"));
        return fb;
    }
}

