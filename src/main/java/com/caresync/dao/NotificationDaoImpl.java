package com.caresync.dao;

import com.caresync.exception.DatabaseOperationException;
import com.caresync.model.Notification;
import com.caresync.model.NotificationType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotificationDaoImpl extends BaseDao<Notification, Long> implements NotificationDao {

    private static final String SELECT_BASE =
            "SELECT notification_id, user_id, title, message, notification_type, " +
            "related_entity_type, related_entity_id, is_read, created_at " +
            "FROM notifications ";

    @Override
    public Optional<Notification> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = SELECT_BASE + "WHERE notification_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find notification by ID: " + id, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Notification> findAll() {
        String sql = SELECT_BASE + "ORDER BY created_at DESC LIMIT 200";
        List<Notification> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve notifications", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<Notification> findByUserId(Long userId) {
        String sql = SELECT_BASE + "WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
        List<Notification> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find notifications for user ID: " + userId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<Notification> findUnreadByUserId(Long userId) {
        String sql = SELECT_BASE + "WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC";
        List<Notification> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find unread notifications for user ID: " + userId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count unread notifications for user ID: " + userId, e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public boolean markAsRead(Long notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to mark notification as read: " + notificationId, e.getSQLState(), e);
        }
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to mark all notifications as read for user ID: " + userId, e.getSQLState(), e);
        }
    }

    @Override
    public Notification save(Notification notification) {
        try (Connection conn = getConnection()) {
            return save(notification, conn);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to persist notification: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public Notification save(Notification notification, Connection conn) {
        String sql = "INSERT INTO notifications (user_id, title, message, notification_type, " +
                     "related_entity_type, related_entity_id, is_read, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, notification.getUserId());
            stmt.setString(2, notification.getTitle());
            stmt.setString(3, notification.getMessage());
            stmt.setString(4, notification.getNotificationType().name());
            setStringOrNull(stmt, 5, notification.getRelatedEntityType());
            setLongOrNull(stmt, 6, notification.getRelatedEntityId());
            stmt.setBoolean(7, notification.isRead());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DatabaseOperationException("Creating notification failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    notification.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseOperationException("Creating notification failed, no ID obtained.");
                }
            }
            return notification;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert notification: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean update(Notification notification) {
        String sql = "UPDATE notifications SET title = ?, message = ?, notification_type = ?, " +
                     "related_entity_type = ?, related_entity_id = ?, is_read = ? WHERE notification_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, notification.getTitle());
            stmt.setString(2, notification.getMessage());
            stmt.setString(3, notification.getNotificationType().name());
            setStringOrNull(stmt, 4, notification.getRelatedEntityType());
            setLongOrNull(stmt, 5, notification.getRelatedEntityId());
            stmt.setBoolean(6, notification.isRead());
            stmt.setLong(7, notification.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update notification ID: " + notification.getId(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM notifications WHERE notification_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete notification ID: " + id, e.getSQLState(), e);
        }
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification note = new Notification();
        note.setId(rs.getLong("notification_id"));
        note.setUserId(rs.getLong("user_id"));
        note.setTitle(rs.getString("title"));
        note.setMessage(rs.getString("message"));
        note.setNotificationType(NotificationType.valueOf(rs.getString("notification_type")));
        note.setRelatedEntityType(rs.getString("related_entity_type"));
        note.setRelatedEntityId(getLongOrNull(rs, "related_entity_id"));
        note.setRead(rs.getBoolean("is_read"));
        note.setCreatedAt(getInstantOrNull(rs, "created_at"));
        return note;
    }
}

