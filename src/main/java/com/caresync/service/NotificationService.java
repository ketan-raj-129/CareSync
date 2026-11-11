package com.caresync.service;

import com.caresync.dto.NotificationDto;
import com.caresync.model.Notification;

import java.util.List;

public interface NotificationService {

    List<NotificationDto> getUserNotifications(Long userId, int limit, int offset, Boolean unreadOnly);

    long countUserNotifications(Long userId, Boolean unreadOnly);

    long getUnreadCount(Long userId);

    boolean markAsRead(Long notificationId, Long userId);

    int markAllAsRead(Long userId);

    List<NotificationDto> getAdminNotifications(int limit, int offset, String typeFilter, Boolean unreadOnly);

    long countAdminNotifications(String typeFilter, Boolean unreadOnly);

    Notification sendInAppNotification(Long userId, String title, String message, String type, String relatedEntityType, Long relatedEntityId);
}

