package com.caresync.notification;

import com.caresync.model.Appointment;
import com.caresync.model.Doctor;
import com.caresync.model.Patient;

public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private static final String BRAND_HEADER =
            "<div style=\"background-color:#176E65;padding:24px;text-align:center;border-top-left-radius:8px;border-top-right-radius:8px;\">" +
            "  <h1 style=\"color:#FFFFFF;margin:0;font-size:24px;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;\">CareSync</h1>" +
            "  <p style=\"color:#E9F3F2;margin:4px 0 0 0;font-size:13px;letter-spacing:0.5px;\">Healthcare, connected.</p>" +
            "</div>";

    private static final String BRAND_FOOTER =
            "<div style=\"background-color:#F7F9FA;padding:20px;text-align:center;border-bottom-left-radius:8px;border-bottom-right-radius:8px;border-top:1px solid #E5E7EB;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;\">" +
            "  <p style=\"color:#6B7280;margin:0;font-size:12px;\">This is an automated operational notification from CareSync Health Platform.</p>" +
            "  <p style=\"color:#6B7280;margin:4px 0 0 0;font-size:12px;\">Manage your communication preferences in your portal profile.</p>" +
            "</div>";

    private String wrapHtml(String content) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body style=\"margin:0;padding:20px;background-color:#F7F9FA;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;color:#243342;\">" +
               "<div style=\"max-width:600px;margin:0 auto;background-color:#FFFFFF;border-radius:8px;box-shadow:0 4px 6px rgba(0,0,0,0.05);overflow:hidden;\">" +
               BRAND_HEADER +
               "<div style=\"padding:32px 28px;\">" +
               content +
               "</div>" +
               BRAND_FOOTER +
               "</div></body></html>";
    }

    @Override
    public RenderedTemplate renderAppointmentBookedPatient(Appointment appointment, Patient patient, Doctor doctor) {
        String patientName = patient != null && patient.getFullName() != null ? patient.getFullName() : "Valued Patient";
        String doctorName = doctor != null && doctor.getFullName() != null ? doctor.getFullName() : "Your Healthcare Provider";
        String ref = appointment != null ? appointment.getReferenceNumber() : "N/A";
        String dateStr = appointment != null && appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "TBD";
        String timeStr = appointment != null && appointment.getStartTime() != null ? appointment.getStartTime().toString() : "TBD";

        String subject = "Appointment Request Submitted - Ref #" + ref;

        String html = wrapHtml(
                "<h2 style=\"color:#176E65;margin-top:0;\">Appointment Request Received</h2>" +
                "<p>Hello <strong>" + patientName + "</strong>,</p>" +
                "<p>Your appointment request has been successfully submitted and is queued for doctor confirmation.</p>" +
                "<div style=\"background-color:#E9F3F2;border-left:4px solid:#176E65;padding:16px;border-radius:4px;margin:20px 0;\">" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Reference Number:</strong> " + ref + "</p>" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Doctor:</strong> Dr. " + doctorName + "</p>" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Date:</strong> " + dateStr + "</p>" +
                "  <p style=\"margin:0;\"><strong>Time:</strong> " + timeStr + "</p>" +
                "</div>" +
                "<p>You will receive a notification as soon as the appointment is confirmed by the clinic.</p>"
        );

        String text = "CareSync — Healthcare, connected.\n\n" +
                "Hello " + patientName + ",\n\n" +
                "Your appointment request has been submitted successfully.\n" +
                "Reference Number: " + ref + "\n" +
                "Doctor: Dr. " + doctorName + "\n" +
                "Date: " + dateStr + "\n" +
                "Time: " + timeStr + "\n\n" +
                "You will receive an update once confirmed.";

        return new RenderedTemplate(subject, html, text);
    }

    @Override
    public RenderedTemplate renderAppointmentBookedDoctor(Appointment appointment, Patient patient, Doctor doctor) {
        String patientName = patient != null && patient.getFullName() != null ? patient.getFullName() : "Patient";
        String doctorName = doctor != null && doctor.getFullName() != null ? doctor.getFullName() : "Doctor";
        String ref = appointment != null ? appointment.getReferenceNumber() : "N/A";
        String dateStr = appointment != null && appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "TBD";
        String timeStr = appointment != null && appointment.getStartTime() != null ? appointment.getStartTime().toString() : "TBD";

        String subject = "New Booking Request - Ref #" + ref;

        String html = wrapHtml(
                "<h2 style=\"color:#176E65;margin-top:0;\">New Appointment Booking Request</h2>" +
                "<p>Hello Dr. <strong>" + doctorName + "</strong>,</p>" +
                "<p>A patient has requested a consultation with you:</p>" +
                "<div style=\"background-color:#E9F3F2;border-left:4px solid:#176E65;padding:16px;border-radius:4px;margin:20px 0;\">" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Reference:</strong> " + ref + "</p>" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Patient:</strong> " + patientName + "</p>" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Date:</strong> " + dateStr + "</p>" +
                "  <p style=\"margin:0;\"><strong>Time:</strong> " + timeStr + "</p>" +
                "</div>" +
                "<p>Please log in to your CareSync Doctor Portal to confirm or reschedule this consultation.</p>"
        );

        String text = "CareSync — Healthcare, connected.\n\n" +
                "Hello Dr. " + doctorName + ",\n\n" +
                "A new appointment booking request has been submitted:\n" +
                "Reference: " + ref + "\n" +
                "Patient: " + patientName + "\n" +
                "Date: " + dateStr + "\n" +
                "Time: " + timeStr + "\n\n" +
                "Please review in your Doctor Portal.";

        return new RenderedTemplate(subject, html, text);
    }

    @Override
    public RenderedTemplate renderAppointmentConfirmed(Appointment appointment, Patient patient, Doctor doctor) {
        String patientName = patient != null && patient.getFullName() != null ? patient.getFullName() : "Valued Patient";
        String doctorName = doctor != null && doctor.getFullName() != null ? doctor.getFullName() : "Your Healthcare Provider";
        String ref = appointment != null ? appointment.getReferenceNumber() : "N/A";
        String dateStr = appointment != null && appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "TBD";
        String timeStr = appointment != null && appointment.getStartTime() != null ? appointment.getStartTime().toString() : "TBD";

        String subject = "Appointment Confirmed - Ref #" + ref;

        String html = wrapHtml(
                "<h2 style=\"color:#176E65;margin-top:0;\">Appointment Confirmed!</h2>" +
                "<p>Hello <strong>" + patientName + "</strong>,</p>" +
                "<p>Great news! Your consultation with Dr. " + doctorName + " has been confirmed.</p>" +
                "<div style=\"background-color:#E9F3F2;border-left:4px solid:#176E65;padding:16px;border-radius:4px;margin:20px 0;\">" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Reference:</strong> " + ref + "</p>" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Doctor:</strong> Dr. " + doctorName + "</p>" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Date:</strong> " + dateStr + "</p>" +
                "  <p style=\"margin:0;\"><strong>Time:</strong> " + timeStr + "</p>" +
                "</div>" +
                "<p>Please arrive 10 minutes prior to your scheduled consultation time.</p>"
        );

        String text = "CareSync — Healthcare, connected.\n\n" +
                "Hello " + patientName + ",\n\n" +
                "Your appointment has been confirmed:\n" +
                "Reference: " + ref + "\n" +
                "Doctor: Dr. " + doctorName + "\n" +
                "Date: " + dateStr + "\n" +
                "Time: " + timeStr + "\n\n" +
                "Please arrive 10 minutes prior to your scheduled consultation.";

        return new RenderedTemplate(subject, html, text);
    }

    @Override
    public RenderedTemplate renderAppointmentRejected(Appointment appointment, Patient patient, Doctor doctor, String reason) {
        String patientName = patient != null && patient.getFullName() != null ? patient.getFullName() : "Valued Patient";
        String doctorName = doctor != null && doctor.getFullName() != null ? doctor.getFullName() : "Your Healthcare Provider";
        String ref = appointment != null ? appointment.getReferenceNumber() : "N/A";
        String dateStr = appointment != null && appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "TBD";
        String timeStr = appointment != null && appointment.getStartTime() != null ? appointment.getStartTime().toString() : "TBD";
        String reasonStr = (reason != null && !reason.isBlank()) ? reason : "Doctor unavailable at requested time.";

        String subject = "Appointment Request Update - Ref #" + ref;

        String html = wrapHtml(
                "<h2 style=\"color:#DC2626;margin-top:0;\">Appointment Request Declined</h2>" +
                "<p>Hello <strong>" + patientName + "</strong>,</p>" +
                "<p>Your appointment request for <strong>" + dateStr + " at " + timeStr + "</strong> could not be accommodated.</p>" +
                "<div style=\"background-color:#FEE2E2;border-left:4px solid:#DC2626;padding:16px;border-radius:4px;margin:20px 0;\">" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Reference:</strong> " + ref + "</p>" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Doctor:</strong> Dr. " + doctorName + "</p>" +
                "  <p style=\"margin:0;\"><strong>Reason:</strong> " + reasonStr + "</p>" +
                "</div>" +
                "<p>You are welcome to select an alternative time slot through your Patient Portal.</p>"
        );

        String text = "CareSync — Healthcare, connected.\n\n" +
                "Hello " + patientName + ",\n\n" +
                "Your appointment request for " + dateStr + " at " + timeStr + " could not be accommodated.\n" +
                "Reference: " + ref + "\n" +
                "Doctor: Dr. " + doctorName + "\n" +
                "Reason: " + reasonStr + "\n\n" +
                "Please choose another convenient time slot in your portal.";

        return new RenderedTemplate(subject, html, text);
    }

    @Override
    public RenderedTemplate renderAppointmentCancelled(Appointment appointment, Patient patient, Doctor doctor, String cancelledByRole, String reason) {
        String patientName = patient != null && patient.getFullName() != null ? patient.getFullName() : "Patient";
        String doctorName = doctor != null && doctor.getFullName() != null ? doctor.getFullName() : "Doctor";
        String ref = appointment != null ? appointment.getReferenceNumber() : "N/A";
        String dateStr = appointment != null && appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "TBD";
        String timeStr = appointment != null && appointment.getStartTime() != null ? appointment.getStartTime().toString() : "TBD";
        String reasonStr = (reason != null && !reason.isBlank()) ? reason : "No reason specified.";
        String initiator = (cancelledByRole != null) ? cancelledByRole.toUpperCase() : "SYSTEM";

        String subject = "Appointment Cancelled - Ref #" + ref;

        String html = wrapHtml(
                "<h2 style=\"color:#D97706;margin-top:0;\">Appointment Cancellation Notice</h2>" +
                "<p>This notification is to inform you that appointment <strong>Ref #" + ref + "</strong> has been cancelled by <strong>" + initiator + "</strong>.</p>" +
                "<div style=\"background-color:#FEF3C7;border-left:4px solid:#D97706;padding:16px;border-radius:4px;margin:20px 0;\">" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Reference:</strong> " + ref + "</p>" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Scheduled Date & Time:</strong> " + dateStr + " at " + timeStr + "</p>" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Doctor:</strong> Dr. " + doctorName + "</p>" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Patient:</strong> " + patientName + "</p>" +
                "  <p style=\"margin:0;\"><strong>Cancellation Reason:</strong> " + reasonStr + "</p>" +
                "</div>" +
                "<p>If you require immediate medical care or need to re-book, please visit your portal.</p>"
        );

        String text = "CareSync — Healthcare, connected.\n\n" +
                "Appointment Cancellation Notice\n" +
                "Reference: " + ref + "\n" +
                "Scheduled: " + dateStr + " at " + timeStr + "\n" +
                "Doctor: Dr. " + doctorName + "\n" +
                "Patient: " + patientName + "\n" +
                "Cancelled By: " + initiator + "\n" +
                "Reason: " + reasonStr;

        return new RenderedTemplate(subject, html, text);
    }

    @Override
    public RenderedTemplate renderAppointmentReminder(Appointment appointment, Patient patient, Doctor doctor) {
        String patientName = patient != null && patient.getFullName() != null ? patient.getFullName() : "Valued Patient";
        String doctorName = doctor != null && doctor.getFullName() != null ? doctor.getFullName() : "Your Healthcare Provider";
        String ref = appointment != null ? appointment.getReferenceNumber() : "N/A";
        String dateStr = appointment != null && appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "Tomorrow";
        String timeStr = appointment != null && appointment.getStartTime() != null ? appointment.getStartTime().toString() : "TBD";

        String subject = "Upcoming Consultation Reminder - Ref #" + ref;

        String html = wrapHtml(
                "<h2 style=\"color:#176E65;margin-top:0;\">Upcoming Consultation Reminder</h2>" +
                "<p>Hello <strong>" + patientName + "</strong>,</p>" +
                "<p>This is a friendly reminder of your upcoming consultation scheduled with CareSync:</p>" +
                "<div style=\"background-color:#E9F3F2;border-left:4px solid:#176E65;padding:16px;border-radius:4px;margin:20px 0;\">" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Reference:</strong> " + ref + "</p>" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Doctor:</strong> Dr. " + doctorName + "</p>" +
                "  <p style=\"margin:0 0 8px 0;\"><strong>Date:</strong> " + dateStr + "</p>" +
                "  <p style=\"margin:0;\"><strong>Time:</strong> " + timeStr + "</p>" +
                "</div>" +
                "<p>Please ensure you bring any relevant medical documentation, identification, and medication lists.</p>"
        );

        String text = "CareSync — Healthcare, connected.\n\n" +
                "Hello " + patientName + ",\n\n" +
                "Upcoming Consultation Reminder:\n" +
                "Reference: " + ref + "\n" +
                "Doctor: Dr. " + doctorName + "\n" +
                "Date: " + dateStr + "\n" +
                "Time: " + timeStr + "\n\n" +
                "Please bring relevant medical records and arrive on time.";

        return new RenderedTemplate(subject, html, text);
    }
}
