package com.caresync.dao;

import com.caresync.model.NotificationDelivery;
import com.caresync.model.NotificationDeliveryStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationDeliveryDao extends GenericDao<NotificationDelivery, Long> {

    Optional<NotificationDelivery> findByIdempotencyKey(String idempotencyKey);

    List<NotificationDelivery> claimPendingDeliveries(int limit, int leaseSeconds);

    void updateStatus(Long outboxId, NotificationDeliveryStatus status, String lastError, Instant sentAt);

    void scheduleRetry(Long outboxId, int attemptCount, Instant nextRetryAt, String lastError);

    int recoverTimedOutLeases();

    List<NotificationDelivery> findAll(int limit, int offset, NotificationDeliveryStatus statusFilter);

    long countAll(NotificationDeliveryStatus statusFilter);
}

