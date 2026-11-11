package com.caresync.service;

import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dao.SystemSettingDao;
import com.caresync.dao.SystemSettingDaoImpl;
import com.caresync.exception.ValidationException;
import com.caresync.model.SystemSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class AdminSettingsServiceImpl implements AdminSettingsService {

    private static final Logger logger = LoggerFactory.getLogger(AdminSettingsServiceImpl.class);

    private static final Set<String> ALLOWED_KEYS = Set.of(
            "clinic.name",
            "clinic.contact_email",
            "clinic.contact_phone",
            "clinic.address",
            "clinic.working_hours",
            "clinic.default_timezone",
            "appointment.slot_duration_default",
            "appointment.cancellation_cutoff_hours",
            "appointment.reminder.lead.hours"
    );

    private final SystemSettingDao settingDao;
    private final AuditLogDao auditLogDao;

    public AdminSettingsServiceImpl() {
        this(new SystemSettingDaoImpl(), new AuditLogDaoImpl());
    }

    public AdminSettingsServiceImpl(SystemSettingDao settingDao, AuditLogDao auditLogDao) {
        this.settingDao = Objects.requireNonNull(settingDao, "settingDao cannot be null");
        this.auditLogDao = Objects.requireNonNull(auditLogDao, "auditLogDao cannot be null");
    }

    @Override
    public List<SystemSetting> getAllSettings() {
        return settingDao.findAll();
    }

    @Override
    public Optional<SystemSetting> getSetting(String key) {
        if (key == null || key.trim().isEmpty()) return Optional.empty();
        return settingDao.findById(key.trim());
    }

    @Override
    public boolean updateSetting(String key, String value, Long actorAdminId,
                                 String ipAddress, String userAgent) {
        if (key == null || key.trim().isEmpty() || value == null || actorAdminId == null) {
            throw new ValidationException("Setting key, value, and administrator ID cannot be null");
        }

        String normalizedKey = key.trim();
        if (!isAllowedSetting(normalizedKey)) {
            logger.warn("Security Alert: Admin ID {} attempted modification of unwhitelisted setting key '{}'",
                    actorAdminId, normalizedKey);
            throw new ValidationException("Setting '" + normalizedKey + "' is restricted and cannot be altered.");
        }

        // Additional validation
        String trimmedVal = value.trim();
        if (trimmedVal.isEmpty()) {
            throw new ValidationException("Setting value cannot be blank");
        }

        SystemSetting setting = new SystemSetting();
        setting.setSettingKey(normalizedKey);
        setting.setSettingValue(trimmedVal);
        setting.setUpdatedByUserId(actorAdminId);

        boolean updated = settingDao.saveOrUpdate(setting);
        if (updated) {
            auditLogDao.log(actorAdminId, "SYSTEM_SETTING_UPDATED", "system_settings", null,
                    ipAddress, userAgent, "Updated setting '" + normalizedKey + "' to '" + trimmedVal + "'");
            logger.info("Admin ID {} updated setting '{}'", actorAdminId, normalizedKey);
        }
        return updated;
    }

    @Override
    public boolean isAllowedSetting(String key) {
        return key != null && ALLOWED_KEYS.contains(key.trim());
    }
}

