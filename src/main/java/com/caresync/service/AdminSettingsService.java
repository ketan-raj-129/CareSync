package com.caresync.service;

import com.caresync.model.SystemSetting;

import java.util.List;
import java.util.Optional;

public interface AdminSettingsService {

    List<SystemSetting> getAllSettings();

    Optional<SystemSetting> getSetting(String key);

    boolean updateSetting(String key, String value, Long actorAdminId, String ipAddress, String userAgent);

    boolean isAllowedSetting(String key);
}

