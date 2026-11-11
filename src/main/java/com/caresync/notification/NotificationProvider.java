package com.caresync.notification;

import com.caresync.model.NotificationChannel;

public interface NotificationProvider {

    NotificationChannel getSupportedChannel();

    boolean isAvailable();

    boolean send(String recipientAddress, String recipientName, String subject, String htmlContent, String textContent) throws Exception;
}

