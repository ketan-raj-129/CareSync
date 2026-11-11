package com.caresync.service;

import com.caresync.dao.NotificationDao;
import com.caresync.dao.NotificationDeliveryDao;
import com.caresync.dto.NotificationPreferenceDto;
import com.caresync.model.Appointment;
import com.caresync.model.Doctor;
import com.caresync.model.Notification;
import com.caresync.model.NotificationChannel;
import com.caresync.model.NotificationDelivery;
import com.caresync.model.NotificationDeliveryStatus;
import com.caresync.model.NotificationType;
import com.caresync.model.Patient;
import com.caresync.notification.NotificationTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class NotificationEventServiceImpl implements NotificationEventService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventServiceImpl.class);

    private final NotificationDao notificationDao;
    private final NotificationDeliveryDao deliveryDao;
    private final NotificationPreferenceService preferenceService;
    private final NotificationTemplateService templateService;

    public NotificationEventServiceImpl(
            NotificationDao notificationDao,
            NotificationDeliveryDao deliveryDao,
            NotificationPreferenceService preferenceService,
            NotificationTemplateService templateService) {
        this.notificationDao = notificationDao;
        this.deliveryDao = deliveryDao;
        this.preferenceService = preferenceService;
        this.templateService = templateService;
    }

    @Override
    public void notifyAppointmentBooked(Appointment appointment, Patient patient, Doctor doctor) {
        if (appointment == null) return;
        try {
            Long patientUserId = patient != null ? patient.getId() : null;
            Long doctorUserId = doctor != null ? doctor.getId() : null;
            String ref = appointment.getReferenceNumber();

            // 1. Patient In-App & Email
            if (patientUserId != null) {
                createInAppNotification(
                        patientUserId,
                        "Appointment Requested: Ref #" + ref,
                        "Your appointment request for " + appointment.getAppointmentDate() + " at " + appointment.getStartTime() + " has been submitted.",
                        NotificationType.APPOINTMENT_BOOKED,
                        "APPOINTMENT",
                        appointment.getId()
                );

                NotificationPreferenceDto prefs = preferenceService.getPreferences(patientUserId);
                if (prefs.isEmailConfirmations() && patient.getEmail() != null) {
                    var rendered = templateService.renderAppointmentBookedPatient(appointment, patient, doctor);
                    enqueueOutbox(
                            patientUserId,
                            patient.getEmail(),
                            patient.getFullName(),
                            NotificationChannel.EMAIL,
                            NotificationType.APPOINTMENT_BOOKED,
                            rendered.subject(),
                            rendered.htmlBody(),
                            rendered.textBody(),
                            "APPOINTMENT",
                            appointment.getId(),
                            "APT-BOOK-PT-" + appointment.getId() + "-" + patientUserId
                    );
                }
            }

            // 2. Doctor In-App & Email
            if (doctorUserId != null) {
                createInAppNotification(
                        doctorUserId,
                        "New Booking Request: Ref #" + ref,
                        "A new appointment has been scheduled by " + (patient != null && patient.getFullName() != null ? patient.getFullName() : "a patient") + " for " + appointment.getAppointmentDate() + " at " + appointment.getStartTime() + ".",
                        NotificationType.APPOINTMENT_BOOKED,
                        "APPOINTMENT",
                        appointment.getId()
                );

                NotificationPreferenceDto prefs = preferenceService.getPreferences(doctorUserId);
                if (prefs.isEmailConfirmations() && doctor.getEmail() != null) {
                    var rendered = templateService.renderAppointmentBookedDoctor(appointment, patient, doctor);
                    enqueueOutbox(
                            doctorUserId,
                            doctor.getEmail(),
                            doctor.getFullName(),
                            NotificationChannel.EMAIL,
                            NotificationType.APPOINTMENT_BOOKED,
                            rendered.subject(),
                            rendered.htmlBody(),
                            rendered.textBody(),
                            "APPOINTMENT",
                            appointment.getId(),
                            "APT-BOOK-DOC-" + appointment.getId() + "-" + doctorUserId
                    );
                }
            }
        } catch (Exception e) {
            logger.error("Failed to process notifyAppointmentBooked for appointment ID {}", appointment.getId(), e);
        }
    }

    @Override
    public void notifyAppointmentConfirmed(Appointment appointment, Patient patient, Doctor doctor) {
        if (appointment == null) return;
        try {
            Long patientUserId = patient != null ? patient.getId() : null;
            String ref = appointment.getReferenceNumber();

            if (patientUserId != null) {
                createInAppNotification(
                        patientUserId,
                        "Appointment Confirmed: Ref #" + ref,
                        "Your appointment scheduled for " + appointment.getAppointmentDate() + " at " + appointment.getStartTime() + " has been confirmed by Dr. " + (doctor != null && doctor.getFullName() != null ? doctor.getFullName() : "") + ".",
                        NotificationType.APPOINTMENT_CONFIRMED,
                        "APPOINTMENT",
                        appointment.getId()
                );

                NotificationPreferenceDto prefs = preferenceService.getPreferences(patientUserId);
                if (prefs.isEmailConfirmations() && patient.getEmail() != null) {
                    var rendered = templateService.renderAppointmentConfirmed(appointment, patient, doctor);
                    enqueueOutbox(
                            patientUserId,
                            patient.getEmail(),
                            patient.getFullName(),
                            NotificationChannel.EMAIL,
                            NotificationType.APPOINTMENT_CONFIRMED,
                            rendered.subject(),
                            rendered.htmlBody(),
                            rendered.textBody(),
                            "APPOINTMENT",
                            appointment.getId(),
                            "APT-CONFIRM-PT-" + appointment.getId() + "-" + patientUserId
                    );
                }
            }
        } catch (Exception e) {
            logger.error("Failed to process notifyAppointmentConfirmed for appointment ID {}", appointment.getId(), e);
        }
    }

    @Override
    public void notifyAppointmentRejected(Appointment appointment, Patient patient, Doctor doctor, String reason) {
        if (appointment == null) return;
        try {
            Long patientUserId = patient != null ? patient.getId() : null;
            String ref = appointment.getReferenceNumber();

            if (patientUserId != null) {
                createInAppNotification(
                        patientUserId,
                        "Appointment Request Declined: Ref #" + ref,
                        "Your appointment request for " + appointment.getAppointmentDate() + " could not be accepted. " + (reason != null ? "Reason: " + reason : ""),
                        NotificationType.APPOINTMENT_REJECTED,
                        "APPOINTMENT",
                        appointment.getId()
                );

                NotificationPreferenceDto prefs = preferenceService.getPreferences(patientUserId);
                if (prefs.isEmailCancellations() && patient.getEmail() != null) {
                    var rendered = templateService.renderAppointmentRejected(appointment, patient, doctor, reason);
                    enqueueOutbox(
                            patientUserId,
                            patient.getEmail(),
                            patient.getFullName(),
                            NotificationChannel.EMAIL,
                            NotificationType.APPOINTMENT_REJECTED,
                            rendered.subject(),
                            rendered.htmlBody(),
                            rendered.textBody(),
                            "APPOINTMENT",
                            appointment.getId(),
                            "APT-REJECT-PT-" + appointment.getId() + "-" + patientUserId
                    );
                }
            }
        } catch (Exception e) {
            logger.error("Failed to process notifyAppointmentRejected for appointment ID {}", appointment.getId(), e);
        }
    }

    @Override
    public void notifyAppointmentCancelled(Appointment appointment, Patient patient, Doctor doctor, String cancelledByRole, String reason) {
        if (appointment == null) return;
        try {
            Long patientUserId = patient != null ? patient.getId() : null;
            Long doctorUserId = doctor != null ? doctor.getId() : null;
            String ref = appointment.getReferenceNumber();

            var rendered = templateService.renderAppointmentCancelled(appointment, patient, doctor, cancelledByRole, reason);

            // Notify patient if cancellation was not initiated by patient
            if (patientUserId != null && !"PATIENT".equalsIgnoreCase(cancelledByRole)) {
                createInAppNotification(
                        patientUserId,
                        "Appointment Cancelled: Ref #" + ref,
                        "Your appointment for " + appointment.getAppointmentDate() + " has been cancelled by " + cancelledByRole + ". " + (reason != null ? "Reason: " + reason : ""),
                        NotificationType.APPOINTMENT_CANCELLED,
                        "APPOINTMENT",
                        appointment.getId()
                );

                NotificationPreferenceDto prefs = preferenceService.getPreferences(patientUserId);
                if (prefs.isEmailCancellations() && patient.getEmail() != null) {
                    enqueueOutbox(
                            patientUserId,
                            patient.getEmail(),
                            patient.getFullName(),
                            NotificationChannel.EMAIL,
                            NotificationType.APPOINTMENT_CANCELLED,
                            rendered.subject(),
                            rendered.htmlBody(),
                            rendered.textBody(),
                            "APPOINTMENT",
                            appointment.getId(),
                            "APT-CANCEL-PT-" + appointment.getId() + "-" + patientUserId
                    );
                }
            }

            // Notify doctor if cancellation was not initiated by doctor
            if (doctorUserId != null && !"DOCTOR".equalsIgnoreCase(cancelledByRole)) {
                createInAppNotification(
                        doctorUserId,
                        "Appointment Cancelled: Ref #" + ref,
                        "The appointment for " + appointment.getAppointmentDate() + " at " + appointment.getStartTime() + " has been cancelled by " + cancelledByRole + ". " + (reason != null ? "Reason: " + reason : ""),
                        NotificationType.APPOINTMENT_CANCELLED,
                        "APPOINTMENT",
                        appointment.getId()
                );

                NotificationPreferenceDto prefs = preferenceService.getPreferences(doctorUserId);
                if (prefs.isEmailCancellations() && doctor.getEmail() != null) {
                    enqueueOutbox(
                            doctorUserId,
                            doctor.getEmail(),
                            doctor.getFullName(),
                            NotificationChannel.EMAIL,
                            NotificationType.APPOINTMENT_CANCELLED,
                            rendered.subject(),
                            rendered.htmlBody(),
                            rendered.textBody(),
                            "APPOINTMENT",
                            appointment.getId(),
                            "APT-CANCEL-DOC-" + appointment.getId() + "-" + doctorUserId
                    );
                }
            }
        } catch (Exception e) {
            logger.error("Failed to process notifyAppointmentCancelled for appointment ID {}", appointment.getId(), e);
        }
    }

    @Override
    public void notifyAppointmentReminder(Appointment appointment, Patient patient, Doctor doctor) {
        if (appointment == null) return;
        try {
            Long patientUserId = patient != null ? patient.getId() : null;
            String ref = appointment.getReferenceNumber();

            if (patientUserId != null) {
                createInAppNotification(
                        patientUserId,
                        "Upcoming Consultation Reminder: Ref #" + ref,
                        "Reminder: Your consultation with Dr. " + (doctor != null && doctor.getFullName() != null ? doctor.getFullName() : "") + " is scheduled for " + appointment.getAppointmentDate() + " at " + appointment.getStartTime() + ".",
                        NotificationType.APPOINTMENT_REMINDER,
                        "APPOINTMENT",
                        appointment.getId()
                );

                NotificationPreferenceDto prefs = preferenceService.getPreferences(patientUserId);
                if (prefs.isEmailReminders() && patient.getEmail() != null) {
                    var rendered = templateService.renderAppointmentReminder(appointment, patient, doctor);
                    enqueueOutbox(
                            patientUserId,
                            patient.getEmail(),
                            patient.getFullName(),
                            NotificationChannel.EMAIL,
                            NotificationType.APPOINTMENT_REMINDER,
                            rendered.subject(),
                            rendered.htmlBody(),
                            rendered.textBody(),
                            "APPOINTMENT",
                            appointment.getId(),
                            "APT-REMIND-PT-" + appointment.getId() + "-" + patientUserId
                    );
                }
            }
        } catch (Exception e) {
            logger.error("Failed to process notifyAppointmentReminder for appointment ID {}", appointment.getId(), e);
        }
    }

    private void createInAppNotification(Long userId, String title, String message, NotificationType type, String relatedEntityType, Long relatedEntityId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        n.setNotificationType(type);
        n.setRelatedEntityType(relatedEntityType);
        n.setRelatedEntityId(relatedEntityId);
        n.setRead(false);
        notificationDao.save(n);
    }

    private void enqueueOutbox(
            Long userId,
            String recipientEmail,
            String recipientName,
            NotificationChannel channel,
            NotificationType eventType,
            String title,
            String htmlBody,
            String textBody,
            String entityType,
            Long entityId,
            String idempotencyKey) {

        NotificationDelivery delivery = new NotificationDelivery();
        delivery.setUserId(userId);
        delivery.setRecipientEmail(recipientEmail);
        delivery.setRecipientName(recipientName);
        delivery.setChannel(channel);
        delivery.setEventType(eventType);
        delivery.setTitle(title);
        delivery.setContentHtml(htmlBody);
        delivery.setContentText(textBody);
        delivery.setRelatedEntityType(entityType);
        delivery.setRelatedEntityId(entityId);
        delivery.setIdempotencyKey(idempotencyKey);
        delivery.setStatus(NotificationDeliveryStatus.PENDING);
        delivery.setAttemptCount(0);
        delivery.setMaxAttempts(3);
        delivery.setNextRetryAt(Instant.now());

        deliveryDao.save(delivery);
        logger.info("Enqueued notification outbox delivery: key={}, recipient={}", idempotencyKey, recipientEmail);
    }
}
