package com.caresync.service;

import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dto.PaginatedResult;
import com.caresync.model.AuditLog;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AdminAuditLogServiceImpl implements AdminAuditLogService {

    private final AuditLogDao auditLogDao;

    public AdminAuditLogServiceImpl() {
        this(new AuditLogDaoImpl());
    }

    public AdminAuditLogServiceImpl(AuditLogDao auditLogDao) {
        this.auditLogDao = Objects.requireNonNull(auditLogDao, "auditLogDao cannot be null");
    }

    @Override
    public PaginatedResult<AuditLog> getAuditLogs(String action, Long userId, String targetEntity,
                                                 int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = pageSize > 0 ? pageSize : 15;
        int offset = (safePage - 1) * safeSize;

        long total = auditLogDao.countFiltered(action, userId, targetEntity);
        List<AuditLog> list = auditLogDao.findPaginated(action, userId, targetEntity, offset, safeSize);

        // Redact any accidental sensitive data
        for (AuditLog log : list) {
            redactSensitiveData(log);
        }

        return new PaginatedResult<>(list, total, safePage, safeSize);
    }

    @Override
    public Optional<AuditLog> getAuditLogById(Long logId) {
        if (logId == null) return Optional.empty();
        Optional<AuditLog> logOpt = auditLogDao.findById(logId);
        logOpt.ifPresent(this::redactSensitiveData);
        return logOpt;
    }

    private void redactSensitiveData(AuditLog log) {
        if (log == null || log.getDetails() == null) return;
        String details = log.getDetails();
        details = details.replaceAll("(?i)(password|token|salt|secret)=\\S+", "$1=[REDACTED]");
        details = details.replaceAll("(?i)\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"[REDACTED]\"");
        log.setDetails(details);
    }
}

