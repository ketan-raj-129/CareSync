package com.caresync.service;

import com.caresync.dao.AppointmentDao;
import com.caresync.dao.DoctorDao;
import com.caresync.dao.PatientDao;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.Doctor;
import com.caresync.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class NotificationSchedulerImpl implements NotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationSchedulerImpl.class);

    private final NotificationDispatcher notificationDispatcher;
    private final NotificationEventService notificationEventService;
    private final AppointmentDao appointmentDao;
    private final PatientDao patientDao;
    private final DoctorDao doctorDao;
    private final AdminSettingsService adminSettingsService;

    private ScheduledExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public NotificationSchedulerImpl(
            NotificationDispatcher notificationDispatcher,
            NotificationEventService notificationEventService,
            AppointmentDao appointmentDao,
            PatientDao patientDao,
            DoctorDao doctorDao,
            AdminSettingsService adminSettingsService) {
        this.notificationDispatcher = notificationDispatcher;
        this.notificationEventService = notificationEventService;
        this.appointmentDao = appointmentDao;
        this.patientDao = patientDao;
        this.doctorDao = doctorDao;
        this.adminSettingsService = adminSettingsService;
    }

    @Override
    public synchronized void start() {
        if (running.compareAndSet(false, true)) {
            executor = Executors.newScheduledThreadPool(2, r -> {
                Thread t = new Thread(r, "CareSync-Notification-Worker");
                t.setDaemon(true);
                return t;
            });

            // Dispatch outbox items every 30 seconds
            executor.scheduleWithFixedDelay(this::safeDispatch, 5, 30, TimeUnit.SECONDS);

            // Scan appointments for reminders every 3 minutes
            executor.scheduleWithFixedDelay(this::safeReminderScan, 10, 180, TimeUnit.SECONDS);

            logger.info("CareSync NotificationScheduler background engine started successfully");
        }
    }

    @Override
    public synchronized void stop() {
        if (running.compareAndSet(true, false) && executor != null) {
            logger.info("Stopping CareSync NotificationScheduler background engine...");
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            logger.info("CareSync NotificationScheduler background engine stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private void safeDispatch() {
        try {
            triggerDispatchNow();
        } catch (Exception e) {
            logger.error("Error during scheduled notification dispatch execution", e);
        }
    }

    private void safeReminderScan() {
        try {
            triggerReminderScanNow();
        } catch (Exception e) {
            logger.error("Error during scheduled reminder scan execution", e);
        }
    }

    @Override
    public int triggerDispatchNow() {
        // Recover timed-out leases first
        notificationDispatcher.recoverTimedOutLeases();
        // Dispatch batch
        return notificationDispatcher.dispatchPendingDeliveries(50);
    }

    @Override
    public int triggerReminderScanNow() {
        int leadHours = 24;
        try {
            if (adminSettingsService != null) {
                leadHours = adminSettingsService.getSetting("appointment.reminder.lead.hours")
                        .map(s -> {
                            try {
                                return Integer.parseInt(s.getSettingValue().trim());
                            } catch (Exception e) {
                                return 24;
                            }
                        }).orElse(24);
            }
        } catch (Exception e) {
            leadHours = 24;
        }

        long daysAhead = Math.max(0, Math.round((double) leadHours / 24.0));
        LocalDate targetDate = LocalDate.now().plusDays(daysAhead);

        List<Appointment> appointments = appointmentDao.findByDate(targetDate);
        int enqueuedCount = 0;

        for (Appointment apt : appointments) {
            if (apt.getStatus() == AppointmentStatus.CONFIRMED || apt.getStatus() == AppointmentStatus.PENDING) {
                Patient patient = apt.getPatient();
                if (patient == null && patientDao != null && apt.getPatientId() != null) {
                    patient = patientDao.findById(apt.getPatientId()).orElse(null);
                }

                Doctor doctor = apt.getDoctor();
                if (doctor == null && doctorDao != null && apt.getDoctorId() != null) {
                    doctor = doctorDao.findById(apt.getDoctorId()).orElse(null);
                }

                if (patient != null && doctor != null) {
                    notificationEventService.notifyAppointmentReminder(apt, patient, doctor);
                    enqueuedCount++;
                }
            }
        }

        if (enqueuedCount > 0) {
            logger.info("Scanned and enqueued {} upcoming appointment reminders for date {}", enqueuedCount, targetDate);
        }
        return enqueuedCount;
    }
}
