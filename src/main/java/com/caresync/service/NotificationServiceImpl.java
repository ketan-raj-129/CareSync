package com.caresync.service;

import com.caresync.dao.NotificationDao;
import com.caresync.dto.NotificationDto;
import com.caresync.model.Notification;
import com.caresync.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationDao notificationDao;

    public NotificationServiceImpl(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    @Override
    public List<NotificationDto> getUserNotifications(Long userId, int limit, int offset, Boolean unreadOnly) {
        if (userId == null) return Collections.emptyList();
        if (Boolean.TRUE.equals(unreadOnly)) {
            return notificationDao.findUnreadByUserId(userId).stream()
                    .skip(Math.max(0, offset))
                    .limit(Math.max(1, limit))
                    .map(NotificationDto::new)
                    .collect(Collectors.toList());
        }
        List<Notification> list = notificationDao.findPaginatedByUserId(userId, offset, limit);
        return list.stream().map(NotificationDto::new).collect(Collectors.toList());
    }

    @Override
    public long countUserNotifications(Long userId, Boolean unreadOnly) {
        if (userId == null) return 0;
        if (Boolean.TRUE.equals(unreadOnly)) {
            return notificationDao.countUnreadByUserId(userId);
        }
        return notificationDao.countByUserId(userId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        if (userId == null) return 0;
        return notificationDao.countUnreadByUserId(userId);
    }

    @Override
    public boolean markAsRead(Long notificationId, Long userId) {
        if (notificationId == null || userId == null) return false;
        Optional<Notification> opt = notificationDao.findById(notificationId);
        if (opt.isEmpty()) {
            logger.warn("Attempt to mark non-existent notification ID {} as read", notificationId);
            return false;
        }
        Notification n = opt.get();
        if (!userId.equals(n.getUserId())) {
            logger.warn("Security Alert: User ID {} attempted to mark notification ID {} belonging to user ID {}",
                    userId, notificationId, n.getUserId());
            return false;
        }
        return notificationDao.markAsRead(notificationId);
    }

    @Override
    public int markAllAsRead(Long userId) {
        if (userId == null) return 0;
        boolean updated = notificationDao.markAllAsRead(userId);
        return updated ? 1 : 0;
    }

    @Override
    public List<NotificationDto> getAdminNotifications(int limit, int offset, String typeFilter, Boolean unreadOnly) {
        List<Notification> list = notificationDao.findPaginatedAll(null, typeFilter, offset, limit);
        if (Boolean.TRUE.equals(unreadOnly)) {
            return list.stream().filter(n -> !n.isRead()).map(NotificationDto::new).collect(Collectors.toList());
        }
        return list.stream().map(NotificationDto::new).collect(Collectors.toList());
    }

    @Override
    public long countAdminNotifications(String typeFilter, Boolean unreadOnly) {
        return notificationDao.countFilteredAll(null, typeFilter);
    }

    @Override
    public Notification sendInAppNotification(Long userId, String title, String message, String type, String relatedEntityType, Long relatedEntityId) {
        if (userId == null) throw new IllegalArgumentException("User ID is required for notification");
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        try {
            n.setNotificationType(NotificationType.valueOf(type));
        } catch (Exception e) {
            n.setNotificationType(NotificationType.SYSTEM_ALERT);
        }
        n.setRelatedEntityType(relatedEntityType);
        n.setRelatedEntityId(relatedEntityId);
        n.setRead(false);
        return notificationDao.save(n);
    }
}
