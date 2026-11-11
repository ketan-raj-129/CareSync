package com.caresync.dao;

import com.caresync.exception.DatabaseOperationException;
import com.caresync.model.NotificationChannel;
import com.caresync.model.NotificationDelivery;
import com.caresync.model.NotificationDeliveryStatus;
import com.caresync.model.NotificationType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NotificationDeliveryDaoImpl extends BaseDao<NotificationDelivery, Long> implements NotificationDeliveryDao {

    private static final String SELECT_BASE =
            "SELECT outbox_id, user_id, recipient_email, recipient_name, channel, " +
            "event_type, title, content_html, content_text, related_entity_type, " +
            "related_entity_id, idempotency_key, status, attempt_count, max_attempts, " +
            "next_retry_at, locked_until, last_error, created_at, sent_at " +
            "FROM notification_outbox ";

    @Override
    public Optional<NotificationDelivery> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = SELECT_BASE + "WHERE outbox_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            if ("42P01".equals(e.getSQLState())) return Optional.empty();
            throw new DatabaseOperationException("Failed to find notification delivery by ID: " + id, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<NotificationDelivery> findByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) return Optional.empty();
        String sql = SELECT_BASE + "WHERE idempotency_key = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idempotencyKey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            if ("42P01".equals(e.getSQLState())) return Optional.empty();
            throw new DatabaseOperationException("Failed to find notification delivery by idempotency key: " + idempotencyKey, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<NotificationDelivery> findAll() {
        return findAll(100, 0, null);
    }

    @Override
    public List<NotificationDelivery> findAll(int limit, int offset, NotificationDeliveryStatus statusFilter) {
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        if (statusFilter != null) {
            sql.append("WHERE status = ? ");
        }
        sql.append("ORDER BY created_at DESC LIMIT ? OFFSET ?");

        List<NotificationDelivery> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int p = 1;
            if (statusFilter != null) {
                stmt.setString(p++, statusFilter.name());
            }
            stmt.setInt(p++, Math.max(1, limit));
            stmt.setInt(p, Math.max(0, offset));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            if ("42P01".equals(e.getSQLState())) return Collections.emptyList();
            throw new DatabaseOperationException("Failed to retrieve paginated notification deliveries", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public long countAll(NotificationDeliveryStatus statusFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM notification_outbox ");
        if (statusFilter != null) {
            sql.append("WHERE status = ?");
        }
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            if (statusFilter != null) {
                stmt.setString(1, statusFilter.name());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            if ("42P01".equals(e.getSQLState())) return 0L;
            throw new DatabaseOperationException("Failed to count notification deliveries", e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public NotificationDelivery save(NotificationDelivery delivery) {
        if (delivery == null) throw new IllegalArgumentException("NotificationDelivery cannot be null");

        String sql = "INSERT INTO notification_outbox (" +
                     "user_id, recipient_email, recipient_name, channel, event_type, " +
                     "title, content_html, content_text, related_entity_type, related_entity_id, " +
                     "idempotency_key, status, attempt_count, max_attempts, next_retry_at, " +
                     "locked_until, last_error, created_at, sent_at" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "RETURNING outbox_id, created_at";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, delivery.getUserId());
            stmt.setString(2, delivery.getRecipientEmail());
            stmt.setString(3, delivery.getRecipientName());
            stmt.setString(4, delivery.getChannel() != null ? delivery.getChannel().name() : NotificationChannel.IN_APP.name());
            stmt.setString(5, delivery.getEventType() != null ? delivery.getEventType().name() : NotificationType.SYSTEM_ALERT.name());
            stmt.setString(6, delivery.getTitle());
            stmt.setString(7, delivery.getContentHtml());
            stmt.setString(8, delivery.getContentText());
            stmt.setString(9, delivery.getRelatedEntityType());
            if (delivery.getRelatedEntityId() != null) {
                stmt.setLong(10, delivery.getRelatedEntityId());
            } else {
                stmt.setNull(10, java.sql.Types.BIGINT);
            }
            stmt.setString(11, delivery.getIdempotencyKey());
            stmt.setString(12, delivery.getStatus() != null ? delivery.getStatus().name() : NotificationDeliveryStatus.PENDING.name());
            stmt.setInt(13, delivery.getAttemptCount());
            stmt.setInt(14, delivery.getMaxAttempts());
            stmt.setTimestamp(15, delivery.getNextRetryAt() != null ? Timestamp.from(delivery.getNextRetryAt()) : Timestamp.from(Instant.now()));
            stmt.setTimestamp(16, delivery.getLockedUntil() != null ? Timestamp.from(delivery.getLockedUntil()) : null);
            stmt.setString(17, delivery.getLastError());
            stmt.setTimestamp(18, delivery.getCreatedAt() != null ? Timestamp.from(delivery.getCreatedAt()) : Timestamp.from(Instant.now()));
            stmt.setTimestamp(19, delivery.getSentAt() != null ? Timestamp.from(delivery.getSentAt()) : null);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    delivery.setId(rs.getLong("outbox_id"));
                    Timestamp cat = rs.getTimestamp("created_at");
                    if (cat != null) delivery.setCreatedAt(cat.toInstant());
                }
            }
            return delivery;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                // Duplicate idempotency key - fetch existing
                return findByIdempotencyKey(delivery.getIdempotencyKey()).orElse(delivery);
            }
            throw new DatabaseOperationException("Failed to save notification delivery to outbox", e.getSQLState(), e);
        }
    }

    @Override
    public List<NotificationDelivery> claimPendingDeliveries(int limit, int leaseSeconds) {
        String claimSql =
                "WITH claimed AS (" +
                "  SELECT outbox_id FROM notification_outbox " +
                "  WHERE status IN ('PENDING', 'FAILED') " +
                "    AND attempt_count < max_attempts " +
                "    AND next_retry_at <= CURRENT_TIMESTAMP " +
                "    AND (locked_until IS NULL OR locked_until < CURRENT_TIMESTAMP) " +
                "  ORDER BY next_retry_at ASC " +
                "  LIMIT ? " +
                "  FOR UPDATE SKIP LOCKED " +
                ") " +
                "UPDATE notification_outbox o " +
                "SET status = 'PROCESSING', " +
                "    locked_until = CURRENT_TIMESTAMP + (? * INTERVAL '1 second'), " +
                "    attempt_count = attempt_count + 1 " +
                "FROM claimed " +
                "WHERE o.outbox_id = claimed.outbox_id " +
                "RETURNING o.outbox_id, o.user_id, o.recipient_email, o.recipient_name, " +
                "  o.channel, o.event_type, o.title, o.content_html, o.content_text, " +
                "  o.related_entity_type, o.related_entity_id, o.idempotency_key, " +
                "  o.status, o.attempt_count, o.max_attempts, o.next_retry_at, " +
                "  o.locked_until, o.last_error, o.created_at, o.sent_at";

        List<NotificationDelivery> claimed = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(claimSql)) {
            stmt.setInt(1, Math.max(1, limit));
            stmt.setInt(2, Math.max(5, leaseSeconds));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    claimed.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            if ("42P01".equals(e.getSQLState())) return Collections.emptyList();
            throw new DatabaseOperationException("Failed to claim pending notification deliveries", e.getSQLState(), e);
        }
        return claimed;
    }

    @Override
    public void updateStatus(Long outboxId, NotificationDeliveryStatus status, String lastError, Instant sentAt) {
        if (outboxId == null) return;
        String sql = "UPDATE notification_outbox SET status = ?, last_error = ?, sent_at = ?, locked_until = NULL " +
                     "WHERE outbox_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setString(2, lastError);
            stmt.setTimestamp(3, sentAt != null ? Timestamp.from(sentAt) : null);
            stmt.setLong(4, outboxId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if ("42P01".equals(e.getSQLState())) return;
            throw new DatabaseOperationException("Failed to update notification delivery status ID: " + outboxId, e.getSQLState(), e);
        }
    }

    @Override
    public void scheduleRetry(Long outboxId, int attemptCount, Instant nextRetryAt, String lastError) {
        if (outboxId == null) return;
        String sql = "UPDATE notification_outbox SET status = 'FAILED', attempt_count = ?, " +
                     "next_retry_at = ?, last_error = ?, locked_until = NULL WHERE outbox_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attemptCount);
            stmt.setTimestamp(2, nextRetryAt != null ? Timestamp.from(nextRetryAt) : Timestamp.from(Instant.now()));
            stmt.setString(3, lastError);
            stmt.setLong(4, outboxId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if ("42P01".equals(e.getSQLState())) return;
            throw new DatabaseOperationException("Failed to schedule retry for outbox ID: " + outboxId, e.getSQLState(), e);
        }
    }

    @Override
    public int recoverTimedOutLeases() {
        String sql = "UPDATE notification_outbox " +
                     "SET status = 'PENDING', locked_until = NULL " +
                     "WHERE status = 'PROCESSING' AND locked_until < CURRENT_TIMESTAMP";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            if ("42P01".equals(e.getSQLState())) return 0;
            throw new DatabaseOperationException("Failed to recover timed-out notification leases", e.getSQLState(), e);
        }
    }

    @Override
    public boolean update(NotificationDelivery entity) {
        throw new UnsupportedOperationException("Use updateStatus or scheduleRetry for outbox delivery updates");
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM notification_outbox WHERE outbox_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("42P01".equals(e.getSQLState())) return false;
            throw new DatabaseOperationException("Failed to delete notification delivery ID: " + id, e.getSQLState(), e);
        }
    }

    private NotificationDelivery mapRow(ResultSet rs) throws SQLException {
        NotificationDelivery d = new NotificationDelivery(rs.getLong("outbox_id"));
        d.setUserId(rs.getLong("user_id"));
        d.setRecipientEmail(rs.getString("recipient_email"));
        d.setRecipientName(rs.getString("recipient_name"));
        try {
            d.setChannel(NotificationChannel.valueOf(rs.getString("channel")));
        } catch (Exception ignored) {
            d.setChannel(NotificationChannel.IN_APP);
        }
        try {
            d.setEventType(NotificationType.valueOf(rs.getString("event_type")));
        } catch (Exception ignored) {
            d.setEventType(NotificationType.SYSTEM_ALERT);
        }
        d.setTitle(rs.getString("title"));
        d.setContentHtml(rs.getString("content_html"));
        d.setContentText(rs.getString("content_text"));
        d.setRelatedEntityType(rs.getString("related_entity_type"));
        long relatedId = rs.getLong("related_entity_id");
        if (!rs.wasNull()) d.setRelatedEntityId(relatedId);
        d.setIdempotencyKey(rs.getString("idempotency_key"));
        try {
            d.setStatus(NotificationDeliveryStatus.valueOf(rs.getString("status")));
        } catch (Exception ignored) {
            d.setStatus(NotificationDeliveryStatus.PENDING);
        }
        d.setAttemptCount(rs.getInt("attempt_count"));
        d.setMaxAttempts(rs.getInt("max_attempts"));
        Timestamp nra = rs.getTimestamp("next_retry_at");
        if (nra != null) d.setNextRetryAt(nra.toInstant());
        Timestamp lu = rs.getTimestamp("locked_until");
        if (lu != null) d.setLockedUntil(lu.toInstant());
        d.setLastError(rs.getString("last_error"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) d.setCreatedAt(cat.toInstant());
        Timestamp sat = rs.getTimestamp("sent_at");
        if (sat != null) d.setSentAt(sat.toInstant());
        return d;
    }
}
