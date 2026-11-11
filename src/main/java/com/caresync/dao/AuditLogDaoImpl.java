package com.caresync.dao;

import com.caresync.exception.DatabaseOperationException;
import com.caresync.model.AuditLog;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

public class AuditLogDaoImpl extends BaseDao<AuditLog, Long> implements AuditLogDao {

    private static final String SELECT_BASE =
            "SELECT log_id, user_id, action, target_entity, target_id, ip_address, " +
            "user_agent, details, log_hash, prev_log_hash, created_at " +
            "FROM audit_logs ";

    @Override
    public Optional<AuditLog> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = SELECT_BASE + "WHERE log_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find audit log by ID: " + id, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<AuditLog> findAll() {
        return findRecent(100);
    }

    @Override
    public List<AuditLog> findRecent(int limit) {
        String sql = SELECT_BASE + "ORDER BY created_at DESC, log_id DESC LIMIT ?";
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Math.max(1, limit));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve recent audit logs", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<AuditLog> findByUserId(Long userId, int limit) {
        String sql = SELECT_BASE + "WHERE user_id = ? ORDER BY created_at DESC, log_id DESC LIMIT ?";
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setInt(2, Math.max(1, limit));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find audit logs for user ID: " + userId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<AuditLog> findByAction(String action, int limit) {
        String sql = SELECT_BASE + "WHERE action = ? ORDER BY created_at DESC, log_id DESC LIMIT ?";
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, action);
            stmt.setInt(2, Math.max(1, limit));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find audit logs by action: " + action, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public Optional<AuditLog> findLatestLog() {
        String sql = SELECT_BASE + "ORDER BY log_id DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapResultSetToAuditLog(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find latest audit log", e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<AuditLog> findPaginated(String action, Long userId, String targetEntity, int offset, int limit) {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (action != null && !action.trim().isEmpty()) {
            sql.append("AND action = ? ");
            params.add(action.trim());
        }
        if (userId != null && userId > 0) {
            sql.append("AND user_id = ? ");
            params.add(userId);
        }
        if (targetEntity != null && !targetEntity.trim().isEmpty()) {
            sql.append("AND target_entity = ? ");
            params.add(targetEntity.trim());
        }

        sql.append("ORDER BY created_at DESC, log_id DESC LIMIT ? OFFSET ?");
        params.add(Math.max(1, limit));
        params.add(Math.max(0, offset));

        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to paginate audit logs", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public long countFiltered(String action, Long userId, String targetEntity) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM audit_logs WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (action != null && !action.trim().isEmpty()) {
            sql.append("AND action = ? ");
            params.add(action.trim());
        }
        if (userId != null && userId > 0) {
            sql.append("AND user_id = ? ");
            params.add(userId);
        }
        if (targetEntity != null && !targetEntity.trim().isEmpty()) {
            sql.append("AND target_entity = ? ");
            params.add(targetEntity.trim());
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
            throw new DatabaseOperationException("Failed to count filtered audit logs", e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public AuditLog log(Long userId, String action, String targetEntity, Long targetId,
                        String ipAddress, String userAgent, String details) {
        try (Connection conn = getConnection()) {
            return log(userId, action, targetEntity, targetId, ipAddress, userAgent, details, conn);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to obtain connection for audit logging", e.getSQLState(), e);
        }
    }

    @Override
    public AuditLog log(Long userId, String action, String targetEntity, Long targetId,
                        String ipAddress, String userAgent, String details, Connection conn) {
        String prevHash = null;
        String latestSql = "SELECT log_hash FROM audit_logs ORDER BY log_id DESC LIMIT 1";
        try (PreparedStatement checkStmt = conn.prepareStatement(latestSql);
             ResultSet rs = checkStmt.executeQuery()) {
            if (rs.next()) {
                prevHash = rs.getString("log_hash");
            }
        } catch (SQLException e) {
            logger.warn("Could not query previous audit log hash, using default GENESIS: {}", e.getMessage());
        }

        String calculatedHash = computeHash(prevHash, userId, action, targetEntity, targetId, Instant.now());

        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setTargetEntity(targetEntity);
        log.setTargetId(targetId);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setDetails(details);
        log.setLogHash(calculatedHash);
        log.setPrevLogHash(prevHash);

        return saveInternal(log, conn);
    }

    @Override
    public AuditLog save(AuditLog log) {
        try (Connection conn = getConnection()) {
            return saveInternal(log, conn);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to persist audit log: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    private AuditLog saveInternal(AuditLog log, Connection conn) {
        if (log.getLogHash() == null) {
            String hash = computeHash(log.getPrevLogHash(), log.getUserId(), log.getAction(),
                    log.getTargetEntity(), log.getTargetId(), Instant.now());
            log.setLogHash(hash);
        }

        String sql = "INSERT INTO audit_logs (user_id, action, target_entity, target_id, " +
                     "ip_address, user_agent, details, log_hash, prev_log_hash, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setLongOrNull(stmt, 1, log.getUserId());
            stmt.setString(2, log.getAction());
            setStringOrNull(stmt, 3, log.getTargetEntity());
            setLongOrNull(stmt, 4, log.getTargetId());
            setStringOrNull(stmt, 5, log.getIpAddress());
            setStringOrNull(stmt, 6, log.getUserAgent());
            setStringOrNull(stmt, 7, log.getDetails());
            stmt.setString(8, log.getLogHash());
            setStringOrNull(stmt, 9, log.getPrevLogHash());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DatabaseOperationException("Creating audit log failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    log.setId(generatedKeys.getLong(1));
                }
            }
            return log;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert audit log: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean update(AuditLog entity) {
        throw new UnsupportedOperationException("Audit logs are append-only and cannot be updated.");
    }

    @Override
    public boolean deleteById(Long id) {
        throw new UnsupportedOperationException("Audit logs are append-only and cannot be deleted.");
    }

    private String computeHash(String prevHash, Long userId, String action, String targetEntity, Long targetId, Instant timestamp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String payload = (prevHash != null ? prevHash : "GENESIS_ROOT") + "|" +
                             (userId != null ? userId : "ANONYMOUS") + "|" +
                             (action != null ? action : "") + "|" +
                             (targetEntity != null ? targetEntity : "") + "|" +
                             (targetId != null ? targetId : "") + "|" +
                             timestamp.toEpochMilli();
            byte[] hashBytes = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getLong("log_id"));
        log.setUserId(getLongOrNull(rs, "user_id"));
        log.setAction(rs.getString("action"));
        log.setTargetEntity(rs.getString("target_entity"));
        log.setTargetId(getLongOrNull(rs, "target_id"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setUserAgent(rs.getString("user_agent"));
        log.setDetails(rs.getString("details"));
        log.setLogHash(rs.getString("log_hash"));
        log.setPrevLogHash(rs.getString("prev_log_hash"));
        log.setCreatedAt(getInstantOrNull(rs, "created_at"));
        return log;
    }
}

