package com.caresync.service;

import com.caresync.dto.NotificationPreferenceDto;

public interface NotificationPreferenceService {

    NotificationPreferenceDto getPreferences(Long userId);

    NotificationPreferenceDto updatePreferences(Long userId, NotificationPreferenceDto dto);
}

