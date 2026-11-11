package com.caresync.service;

import com.caresync.dao.NotificationDeliveryDao;
import com.caresync.model.NotificationChannel;
import com.caresync.model.NotificationDelivery;
import com.caresync.model.NotificationDeliveryStatus;
import com.caresync.notification.NotificationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationDispatcherImpl implements NotificationDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDispatcherImpl.class);

    private static final int LEASE_SECONDS = 60;
    private static final int BASE_BACKOFF_SECONDS = 30;

    private final NotificationDeliveryDao deliveryDao;
    private final Map<NotificationChannel, NotificationProvider> providers = new ConcurrentHashMap<>();

    public NotificationDispatcherImpl(NotificationDeliveryDao deliveryDao, NotificationProvider defaultEmailProvider) {
        this.deliveryDao = deliveryDao;
        if (defaultEmailProvider != null) {
            this.providers.put(NotificationChannel.EMAIL, defaultEmailProvider);
        }
    }

    public void registerProvider(NotificationChannel channel, NotificationProvider provider) {
        if (channel != null && provider != null) {
            providers.put(channel, provider);
        }
    }

    @Override
    public int dispatchPendingDeliveries(int batchSize) {
        List<NotificationDelivery> claimed = deliveryDao.claimPendingDeliveries(batchSize, LEASE_SECONDS);
        if (claimed.isEmpty()) {
            return 0;
        }

        int processed = 0;
        for (NotificationDelivery delivery : claimed) {
            try {
                processSingleDelivery(delivery);
                processed++;
            } catch (Exception e) {
                logger.error("Unhandled error processing delivery ID {}", delivery.getId(), e);
            }
        }
        return processed;
    }

    private void processSingleDelivery(NotificationDelivery delivery) {
        NotificationProvider provider = providers.get(delivery.getChannel());
        if (provider == null) {
            String err = "No active provider registered for channel: " + delivery.getChannel();
            logger.warn("Delivery {} failed: {}", delivery.getId(), err);
            handleFailure(delivery, err);
            return;
        }

        try {
            boolean sent = provider.send(
                    delivery.getRecipientEmail(),
                    delivery.getRecipientName(),
                    delivery.getTitle(),
                    delivery.getContentHtml(),
                    delivery.getContentText()
            );

            if (sent) {
                deliveryDao.updateStatus(delivery.getId(), NotificationDeliveryStatus.SENT, null, Instant.now());
                logger.info("Successfully delivered notification ID {} to {}", delivery.getId(), delivery.getRecipientEmail());
            } else {
                handleFailure(delivery, "Provider returned false without exception");
            }
        } catch (Exception ex) {
            String errorMsg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            logger.warn("Delivery exception for ID {}: {}", delivery.getId(), errorMsg);
            handleFailure(delivery, errorMsg);
        }
    }

    private void handleFailure(NotificationDelivery delivery, String errorMsg) {
        int currentAttempts = delivery.getAttemptCount();
        if (currentAttempts >= delivery.getMaxAttempts()) {
            // Permanently mark as FAILED
            deliveryDao.updateStatus(delivery.getId(), NotificationDeliveryStatus.FAILED, errorMsg, null);
            logger.error("Notification ID {} permanently failed after {} attempts. Error: {}",
                    delivery.getId(), currentAttempts, errorMsg);
        } else {
            // Exponential backoff: 30s, 60s, 120s...
            long delaySec = (long) (BASE_BACKOFF_SECONDS * Math.pow(2, currentAttempts - 1));
            Instant nextRetry = Instant.now().plus(delaySec, ChronoUnit.SECONDS);
            deliveryDao.scheduleRetry(delivery.getId(), currentAttempts, nextRetry, errorMsg);
            logger.info("Scheduled retry #{} for notification ID {} at {} (delay: {}s)",
                    currentAttempts + 1, delivery.getId(), nextRetry, delaySec);
        }
    }

    @Override
    public boolean retryDelivery(Long outboxId) {
        if (outboxId == null) return false;
        Optional<NotificationDelivery> opt = deliveryDao.findById(outboxId);
        if (opt.isEmpty()) return false;

        NotificationDelivery d = opt.get();
        deliveryDao.scheduleRetry(d.getId(), 0, Instant.now(), "Admin manual retry triggered");
        logger.info("Admin reset notification ID {} for immediate retry", outboxId);
        return true;
    }

    @Override
    public int recoverTimedOutLeases() {
        int recovered = deliveryDao.recoverTimedOutLeases();
        if (recovered > 0) {
            logger.info("Recovered {} timed-out notification leases back to PENDING", recovered);
        }
        return recovered;
    }

    @Override
    public List<NotificationDelivery> getOutboxDeliveries(int limit, int offset, NotificationDeliveryStatus statusFilter) {
        return deliveryDao.findAll(limit, offset, statusFilter);
    }

    @Override
    public long countOutboxDeliveries(NotificationDeliveryStatus statusFilter) {
        return deliveryDao.countAll(statusFilter);
    }
}

