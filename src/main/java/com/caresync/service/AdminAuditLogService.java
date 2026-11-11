package com.caresync.service;

import com.caresync.dto.PaginatedResult;
import com.caresync.model.AuditLog;

import java.util.Optional;

public interface AdminAuditLogService {

    PaginatedResult<AuditLog> getAuditLogs(String action, Long userId, String targetEntity, int page, int pageSize);

    Optional<AuditLog> getAuditLogById(Long logId);
}

