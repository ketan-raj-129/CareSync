package com.caresync.dao;

import com.caresync.model.AuditLog;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface AuditLogDao extends GenericDao<AuditLog, Long> {

    default List<AuditLog> findRecent(int limit) {
        return List.of();
    }

    default List<AuditLog> findByUserId(Long userId, int limit) {
        return List.of();
    }

    default List<AuditLog> findByAction(String action, int limit) {
        return List.of();
    }

    default Optional<AuditLog> findLatestLog() {
        return Optional.empty();
    }

    default AuditLog log(Long userId, String action, String targetEntity, Long targetId, 
                         String ipAddress, String userAgent, String details, Connection conn) {
        return log(userId, action, targetEntity, targetId, ipAddress, userAgent, details);
    }

    AuditLog log(Long userId, String action, String targetEntity, Long targetId, 
                 String ipAddress, String userAgent, String details);
}

