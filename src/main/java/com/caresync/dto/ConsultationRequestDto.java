package com.caresync.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object encapsulating consultation documentation:
 * clinical findings, diagnosis, notes, treatment plan, and medication prescription.
 */
public class ConsultationRequestDto {

    private Long appointmentId;
    private String symptoms;
    private String diagnosis;
    private String treatmentPlan;
    private String clinicalNotes;
    private String generalAdvice;
    private List<PrescriptionItemDto> prescriptionItems = new ArrayList<>();

    public ConsultationRequestDto() {}

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }

    public String getGeneralAdvice() {
        return generalAdvice;
    }

    public void setGeneralAdvice(String generalAdvice) {
        this.generalAdvice = generalAdvice;
    }

    public List<PrescriptionItemDto> getPrescriptionItems() {
        return prescriptionItems;
    }

    public void setPrescriptionItems(List<PrescriptionItemDto> prescriptionItems) {
        this.prescriptionItems = prescriptionItems != null ? prescriptionItems : new ArrayList<>();
    }
}
