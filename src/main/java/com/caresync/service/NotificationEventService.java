package com.caresync.service;

import com.caresync.model.Appointment;
import com.caresync.model.Doctor;
import com.caresync.model.Patient;

public interface NotificationEventService {

    void notifyAppointmentBooked(Appointment appointment, Patient patient, Doctor doctor);

    void notifyAppointmentConfirmed(Appointment appointment, Patient patient, Doctor doctor);

    void notifyAppointmentRejected(Appointment appointment, Patient patient, Doctor doctor, String reason);

    void notifyAppointmentCancelled(Appointment appointment, Patient patient, Doctor doctor, String cancelledByRole, String reason);

    void notifyAppointmentReminder(Appointment appointment, Patient patient, Doctor doctor);
}

