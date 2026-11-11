package com.caresync.dao;

import com.caresync.model.NotificationPreference;

import java.util.Optional;

public interface NotificationPreferenceDao extends GenericDao<NotificationPreference, Long> {
    Optional<NotificationPreference> findByUserId(Long userId);
    NotificationPreference upsert(NotificationPreference preference);
}

