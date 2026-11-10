package com.caresync.dto;

import com.caresync.model.Appointment;

import java.util.ArrayList;
import java.util.List;

/**
 * Metric and queue snapshot for the authenticated practitioner dashboard.
 */
public class DoctorDashboardDto {

    private long totalAppointments;
    private long todayAppointmentsCount;
    private long pendingAppointmentsCount;
    private long confirmedAppointmentsCount;
    private long completedAppointmentsCount;

    private List<Appointment> todayAppointments = new ArrayList<>();
    private List<Appointment> upcomingAppointments = new ArrayList<>();
    private List<Appointment> pendingAppointments = new ArrayList<>();
    private List<Appointment> recentActivity = new ArrayList<>();

    public DoctorDashboardDto() {}

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public long getTodayAppointmentsCount() {
        return todayAppointmentsCount;
    }

    public void setTodayAppointmentsCount(long todayAppointmentsCount) {
        this.todayAppointmentsCount = todayAppointmentsCount;
    }

    public long getPendingAppointmentsCount() {
        return pendingAppointmentsCount;
    }

    public void setPendingAppointmentsCount(long pendingAppointmentsCount) {
        this.pendingAppointmentsCount = pendingAppointmentsCount;
    }

    public long getConfirmedAppointmentsCount() {
        return confirmedAppointmentsCount;
    }

    public void setConfirmedAppointmentsCount(long confirmedAppointmentsCount) {
        this.confirmedAppointmentsCount = confirmedAppointmentsCount;
    }

    public long getCompletedAppointmentsCount() {
        return completedAppointmentsCount;
    }

    public void setCompletedAppointmentsCount(long completedAppointmentsCount) {
        this.completedAppointmentsCount = completedAppointmentsCount;
    }

    public List<Appointment> getTodayAppointments() {
        return todayAppointments;
    }

    public void setTodayAppointments(List<Appointment> todayAppointments) {
        this.todayAppointments = todayAppointments != null ? todayAppointments : new ArrayList<>();
    }

    public List<Appointment> getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public void setUpcomingAppointments(List<Appointment> upcomingAppointments) {
        this.upcomingAppointments = upcomingAppointments != null ? upcomingAppointments : new ArrayList<>();
    }

    public List<Appointment> getPendingAppointments() {
        return pendingAppointments;
    }

    public void setPendingAppointments(List<Appointment> pendingAppointments) {
        this.pendingAppointments = pendingAppointments != null ? pendingAppointments : new ArrayList<>();
    }

    public List<Appointment> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<Appointment> recentActivity) {
        this.recentActivity = recentActivity != null ? recentActivity : new ArrayList<>();
    }
}
