package com.caresync.dao;

import com.caresync.model.AuditLog;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface AuditLogDao extends GenericDao<AuditLog, Long> {

    List<AuditLog> findRecent(int limit);

    List<AuditLog> findByUserId(Long userId, int limit);

    List<AuditLog> findByAction(String action, int limit);

    Optional<AuditLog> findLatestLog();

    AuditLog log(Long userId, String action, String targetEntity, Long targetId, 
                 String ipAddress, String userAgent, String details, Connection conn);

    AuditLog log(Long userId, String action, String targetEntity, Long targetId, 
                 String ipAddress, String userAgent, String details);
}

