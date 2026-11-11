package com.caresync.dao;

import com.caresync.exception.DatabaseOperationException;
import com.caresync.model.NotificationPreference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotificationPreferenceDaoImpl extends BaseDao<NotificationPreference, Long> implements NotificationPreferenceDao {

    private static final String SELECT_BASE =
            "SELECT preference_id, user_id, email_confirmations, email_cancellations, " +
            "email_reminders, email_account_alerts, created_at, updated_at " +
            "FROM user_notification_preferences ";

    @Override
    public Optional<NotificationPreference> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = SELECT_BASE + "WHERE preference_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find preference by ID: " + id, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<NotificationPreference> findByUserId(Long userId) {
        if (userId == null) return Optional.empty();
        String sql = SELECT_BASE + "WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            // If table doesn't exist yet (offline/before V7 migration), return empty to allow defaults
            if ("42P01".equals(e.getSQLState())) {
                return Optional.empty();
            }
            throw new DatabaseOperationException("Failed to find notification preferences for user ID: " + userId, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<NotificationPreference> findAll() {
        String sql = SELECT_BASE + "ORDER BY user_id ASC";
        List<NotificationPreference> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            if ("42P01".equals(e.getSQLState())) {
                return list;
            }
            throw new DatabaseOperationException("Failed to retrieve notification preferences", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public NotificationPreference save(NotificationPreference pref) {
        return upsert(pref);
    }

    @Override
    public NotificationPreference upsert(NotificationPreference pref) {
        if (pref == null || pref.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required for notification preferences");
        }

        String sql = "INSERT INTO user_notification_preferences (user_id, email_confirmations, email_cancellations, " +
                     "email_reminders, email_account_alerts, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
                     "ON CONFLICT (user_id) DO UPDATE SET " +
                     "email_confirmations = EXCLUDED.email_confirmations, " +
                     "email_cancellations = EXCLUDED.email_cancellations, " +
                     "email_reminders = EXCLUDED.email_reminders, " +
                     "email_account_alerts = EXCLUDED.email_account_alerts, " +
                     "updated_at = CURRENT_TIMESTAMP " +
                     "RETURNING preference_id, created_at, updated_at";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, pref.getUserId());
            stmt.setBoolean(2, pref.isEmailConfirmations());
            stmt.setBoolean(3, pref.isEmailCancellations());
            stmt.setBoolean(4, pref.isEmailReminders());
            stmt.setBoolean(5, pref.isEmailAccountAlerts());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    pref.setId(rs.getLong("preference_id"));
                    Timestamp cat = rs.getTimestamp("created_at");
                    if (cat != null) pref.setCreatedAt(cat.toInstant());
                    Timestamp uat = rs.getTimestamp("updated_at");
                    if (uat != null) pref.setUpdatedAt(uat.toInstant());
                }
            }
            return pref;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to upsert notification preferences for user: " + pref.getUserId(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean update(NotificationPreference pref) {
        upsert(pref);
        return true;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM user_notification_preferences WHERE preference_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete notification preference ID: " + id, e.getSQLState(), e);
        }
    }

    private NotificationPreference mapRow(ResultSet rs) throws SQLException {
        NotificationPreference p = new NotificationPreference();
        p.setId(rs.getLong("preference_id"));
        p.setUserId(rs.getLong("user_id"));
        p.setEmailConfirmations(rs.getBoolean("email_confirmations"));
        p.setEmailCancellations(rs.getBoolean("email_cancellations"));
        p.setEmailReminders(rs.getBoolean("email_reminders"));
        p.setEmailAccountAlerts(rs.getBoolean("email_account_alerts"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) p.setCreatedAt(cat.toInstant());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) p.setUpdatedAt(uat.toInstant());
        return p;
    }
}
