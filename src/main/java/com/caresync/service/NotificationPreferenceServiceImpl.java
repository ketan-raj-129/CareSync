package com.caresync.service;

import com.caresync.dao.NotificationPreferenceDao;
import com.caresync.dto.NotificationPreferenceDto;
import com.caresync.model.NotificationPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationPreferenceServiceImpl.class);

    private final NotificationPreferenceDao preferenceDao;

    public NotificationPreferenceServiceImpl(NotificationPreferenceDao preferenceDao) {
        this.preferenceDao = preferenceDao;
    }

    @Override
    public NotificationPreferenceDto getPreferences(Long userId) {
        if (userId == null) {
            return new NotificationPreferenceDto();
        }
        return preferenceDao.findByUserId(userId)
                .map(NotificationPreferenceDto::new)
                .orElseGet(() -> {
                    NotificationPreferenceDto defaultDto = new NotificationPreferenceDto();
                    defaultDto.setUserId(userId);
                    return defaultDto;
                });
    }

    @Override
    public NotificationPreferenceDto updatePreferences(Long userId, NotificationPreferenceDto dto) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required to update notification preferences");
        }
        if (dto == null) {
            throw new IllegalArgumentException("Preference DTO cannot be null");
        }

        NotificationPreference pref = preferenceDao.findByUserId(userId).orElse(new NotificationPreference(userId));
        pref.setUserId(userId);
        pref.setEmailConfirmations(dto.isEmailConfirmations());
        pref.setEmailCancellations(dto.isEmailCancellations());
        pref.setEmailReminders(dto.isEmailReminders());
        pref.setEmailAccountAlerts(dto.isEmailAccountAlerts());

        NotificationPreference saved = preferenceDao.upsert(pref);
        logger.info("Updated notification preferences for user ID {}", userId);
        return new NotificationPreferenceDto(saved);
    }
}

