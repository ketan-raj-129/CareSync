package com.caresync.dto;

import com.caresync.model.NotificationChannel;
import com.caresync.model.NotificationDeliveryStatus;
import com.caresync.model.NotificationType;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Filter container for administrative notification delivery audit monitoring.
 */
public class NotificationFilterDto implements Serializable {

    private NotificationChannel channel;
    private NotificationDeliveryStatus status;
    private NotificationType eventType;
    private String search;
    private LocalDate startDate;
    private LocalDate endDate;
    private int page = 1;
    private int pageSize = 15;

    public NotificationFilterDto() {}

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public NotificationDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationDeliveryStatus status) {
        this.status = status;
    }

    public NotificationType getEventType() {
        return eventType;
    }

    public void setEventType(NotificationType eventType) {
        this.eventType = eventType;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(1, page);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = Math.min(100, Math.max(5, pageSize));
    }
}

