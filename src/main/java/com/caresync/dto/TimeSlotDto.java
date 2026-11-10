package com.caresync.dto;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Representation of a discrete appointment consultation time slot and its real-time availability.
 */
public class TimeSlotDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    private final LocalTime startTime;
    private final LocalTime endTime;
    private final boolean available;
    private final String reason;

    public TimeSlotDto(LocalTime startTime, LocalTime endTime, boolean available, String reason) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.available = available;
        this.reason = reason;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getReason() {
        return reason;
    }

    public String getFormattedRange() {
        if (startTime == null || endTime == null) return "";
        return startTime.format(TIME_FORMATTER) + " - " + endTime.format(TIME_FORMATTER);
    }

    public String getFormattedStart() {
        return startTime != null ? startTime.format(TIME_FORMATTER) : "";
    }

    public String getFormattedEnd() {
        return endTime != null ? endTime.format(TIME_FORMATTER) : "";
    }

    @Override
    public String toString() {
        return "TimeSlotDto{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", available=" + available +
                ", reason='" + reason + '\'' +
                '}';
    }
}

