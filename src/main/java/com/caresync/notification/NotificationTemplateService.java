package com.caresync.notification;

import com.caresync.model.Appointment;
import com.caresync.model.Doctor;
import com.caresync.model.Patient;

public interface NotificationTemplateService {

    record RenderedTemplate(String subject, String htmlBody, String textBody) {}

    RenderedTemplate renderAppointmentBookedPatient(Appointment appointment, Patient patient, Doctor doctor);

    RenderedTemplate renderAppointmentBookedDoctor(Appointment appointment, Patient patient, Doctor doctor);

    RenderedTemplate renderAppointmentConfirmed(Appointment appointment, Patient patient, Doctor doctor);

    RenderedTemplate renderAppointmentRejected(Appointment appointment, Patient patient, Doctor doctor, String reason);

    RenderedTemplate renderAppointmentCancelled(Appointment appointment, Patient patient, Doctor doctor, String cancelledByRole, String reason);

    RenderedTemplate renderAppointmentReminder(Appointment appointment, Patient patient, Doctor doctor);
}

