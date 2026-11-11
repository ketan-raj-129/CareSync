package com.caresync.dto;

import com.caresync.model.Appointment;
import com.caresync.model.AuditLog;
import com.caresync.model.Doctor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data aggregation container for the CareSync central administrative executive dashboard.
 */
public class AdminDashboardDto implements Serializable {

    private long totalPatients;
    private long totalDoctors;
    private long activeDoctors;
    private long pendingDoctorReviews;
    private long totalAppointments;
    private long todayAppointments;
    private long pendingAppointments;
    private long confirmedAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private long totalSpecialties;
    private long activeSpecialties;

    private List<AuditLog> recentAuditLogs = new ArrayList<>();
    private List<Appointment> todaySchedule = new ArrayList<>();
    private List<Doctor> pendingDoctors = new ArrayList<>();

    public AdminDashboardDto() {}

    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getTotalDoctors() {
        return totalDoctors;
    }

    public void setTotalDoctors(long totalDoctors) {
        this.totalDoctors = totalDoctors;
    }

    public long getActiveDoctors() {
        return activeDoctors;
    }

    public void setActiveDoctors(long activeDoctors) {
        this.activeDoctors = activeDoctors;
    }

    public long getPendingDoctorReviews() {
        return pendingDoctorReviews;
    }

    public long getPendingDoctorVerifications() {
        return pendingDoctorReviews;
    }

    public void setPendingDoctorReviews(long pendingDoctorReviews) {
        this.pendingDoctorReviews = pendingDoctorReviews;
    }

    public void setPendingDoctorVerifications(long count) {
        this.pendingDoctorReviews = count;
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public long getTodayAppointments() {
        return todayAppointments;
    }

    public void setTodayAppointments(long todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public long getPendingAppointments() {
        return pendingAppointments;
    }

    public void setPendingAppointments(long pendingAppointments) {
        this.pendingAppointments = pendingAppointments;
    }

    public long getConfirmedAppointments() {
        return confirmedAppointments;
    }

    public void setConfirmedAppointments(long confirmedAppointments) {
        this.confirmedAppointments = confirmedAppointments;
    }

    public long getCompletedAppointments() {
        return completedAppointments;
    }

    public void setCompletedAppointments(long completedAppointments) {
        this.completedAppointments = completedAppointments;
    }

    public long getCancelledAppointments() {
        return cancelledAppointments;
    }

    public void setCancelledAppointments(long cancelledAppointments) {
        this.cancelledAppointments = cancelledAppointments;
    }

    public long getTotalSpecialties() {
        return totalSpecialties;
    }

    public void setTotalSpecialties(long totalSpecialties) {
        this.totalSpecialties = totalSpecialties;
    }

    public long getActiveSpecialties() {
        return activeSpecialties;
    }

    public void setActiveSpecialties(long activeSpecialties) {
        this.activeSpecialties = activeSpecialties;
    }

    public List<AuditLog> getRecentAuditLogs() {
        return recentAuditLogs;
    }

    public void setRecentAuditLogs(List<AuditLog> recentAuditLogs) {
        this.recentAuditLogs = recentAuditLogs != null ? recentAuditLogs : new ArrayList<>();
    }

    public List<Appointment> getTodaySchedule() {
        return todaySchedule;
    }

    public void setTodaySchedule(List<Appointment> todaySchedule) {
        this.todaySchedule = todaySchedule != null ? todaySchedule : new ArrayList<>();
    }

    public List<Doctor> getPendingDoctors() {
        return pendingDoctors;
    }

    public List<Doctor> getPendingVerificationDoctors() {
        return pendingDoctors;
    }

    public void setPendingDoctors(List<Doctor> pendingDoctors) {
        this.pendingDoctors = pendingDoctors != null ? pendingDoctors : new ArrayList<>();
    }
}
