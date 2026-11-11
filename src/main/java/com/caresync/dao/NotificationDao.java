package com.caresync.dao;

import com.caresync.model.Notification;

import java.sql.Connection;
import java.util.List;

public interface NotificationDao extends GenericDao<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findUnreadByUserId(Long userId);

    long countUnreadByUserId(Long userId);

    boolean markAsRead(Long notificationId);

    boolean markAllAsRead(Long userId);

    Notification save(Notification notification, Connection conn);

    default List<Notification> findPaginatedByUserId(Long userId, int offset, int limit) {
        return List.of();
    }

    default long countByUserId(Long userId) {
        return 0L;
    }

    default List<Notification> findPaginatedAll(String query, String type, int offset, int limit) {
        return List.of();
    }

    default long countFilteredAll(String query, String type) {
        return 0L;
    }
}

