package com.caresync.service;

public interface NotificationScheduler {

    void start();

    void stop();

    boolean isRunning();

    int triggerDispatchNow();

    int triggerReminderScanNow();
}

