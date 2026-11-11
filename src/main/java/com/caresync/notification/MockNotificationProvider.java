package com.caresync.notification;

import com.caresync.model.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MockNotificationProvider implements NotificationProvider {

    private static final Logger logger = LoggerFactory.getLogger(MockNotificationProvider.class);

    public record SentMessage(String recipientAddress, String recipientName, String subject, String htmlContent, String textContent) {}

    private final List<SentMessage> sentMessages = Collections.synchronizedList(new ArrayList<>());
    private boolean shouldFail = false;
    private String failureMessage = "Simulated delivery failure";

    @Override
    public NotificationChannel getSupportedChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean send(String recipientAddress, String recipientName, String subject, String htmlContent, String textContent) throws Exception {
        if (shouldFail) {
            logger.warn("MockNotificationProvider simulating failure for {}", recipientAddress);
            throw new RuntimeException(failureMessage);
        }
        SentMessage msg = new SentMessage(recipientAddress, recipientName, subject, htmlContent, textContent);
        sentMessages.add(msg);
        logger.info("[MOCK EMAIL DISPATCHED] To: {} ({}) | Subject: {}", recipientAddress, recipientName, subject);
        return true;
    }

    public List<SentMessage> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }

    public void clear() {
        sentMessages.clear();
        shouldFail = false;
    }

    public void setShouldFail(boolean shouldFail) {
        this.shouldFail = shouldFail;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }
}

