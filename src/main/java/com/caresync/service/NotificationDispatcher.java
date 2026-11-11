package com.caresync.service;

import com.caresync.model.NotificationDelivery;
import com.caresync.model.NotificationDeliveryStatus;

import java.util.List;

public interface NotificationDispatcher {

    int dispatchPendingDeliveries(int batchSize);

    boolean retryDelivery(Long outboxId);

    int recoverTimedOutLeases();

    List<NotificationDelivery> getOutboxDeliveries(int limit, int offset, NotificationDeliveryStatus statusFilter);

    long countOutboxDeliveries(NotificationDeliveryStatus statusFilter);
}

