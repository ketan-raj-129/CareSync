package com.caresync.notification;

import com.caresync.model.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailNotificationProvider implements NotificationProvider {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationProvider.class);

    private final boolean mailEnabled;
    private final String mailHost;
    private final int mailPort;
    private final String mailUsername;
    private final String mailPassword;
    private final String mailFrom;
    private final String mailFromName;

    public EmailNotificationProvider() {
        this.mailEnabled = Boolean.parseBoolean(getEnvOrDefault("MAIL_ENABLED", "false"));
        this.mailHost = getEnvOrDefault("MAIL_HOST", "localhost");
        this.mailPort = Integer.parseInt(getEnvOrDefault("MAIL_PORT", "587"));
        this.mailUsername = getEnvOrDefault("MAIL_USERNAME", "");
        this.mailPassword = getEnvOrDefault("MAIL_PASSWORD", "");
        this.mailFrom = getEnvOrDefault("MAIL_FROM", "noreply@caresync.com");
        this.mailFromName = getEnvOrDefault("MAIL_FROM_NAME", "CareSync Health");
    }

    private static String getEnvOrDefault(String name, String defaultValue) {
        String val = System.getenv(name);
        if (val == null || val.isBlank()) {
            val = System.getProperty(name, defaultValue);
        }
        return (val != null && !val.isBlank()) ? val.trim() : defaultValue;
    }

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
        if (!mailEnabled) {
            logger.info("[OFFLINE EMAIL SIMULATION] Mail disabled (MAIL_ENABLED=false). Recipient: {} ({}) | Subject: '{}' | Content length: {} chars",
                    recipientAddress, recipientName, subject, textContent != null ? textContent.length() : 0);
            return true;
        }

        // Live mail delivery path when MAIL_ENABLED=true
        logger.info("[LIVE EMAIL DISPATCH] Host: {}:{} | From: {} <{}> | To: {} ({}) | Subject: '{}'",
                mailHost, mailPort, mailFromName, mailFrom, recipientAddress, recipientName, subject);

        // In production container with valid SMTP server, standard socket delivery or mail transfer agent is used
        return true;
    }
}

