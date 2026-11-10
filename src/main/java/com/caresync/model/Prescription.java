package com.caresync.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Prescription header tied 1:1 to an appointment and containing 1:N medication line items.
 */
public class Prescription extends BaseModel {

    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private String generalAdvice;
    private List<PrescriptionItem> items = new ArrayList<>();

    public Prescription() {
        super();
    }

    public Prescription(Long id) {
        super(id);
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getGeneralAdvice() {
        return generalAdvice;
    }

    public void setGeneralAdvice(String generalAdvice) {
        this.generalAdvice = generalAdvice;
    }

    public List<PrescriptionItem> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(PrescriptionItem item) {
        if (item != null) {
            this.items.add(item);
        }
    }
}

