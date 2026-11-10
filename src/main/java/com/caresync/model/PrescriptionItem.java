package com.caresync.model;

/**
 * Normalized individual medication line item.
 */
public class PrescriptionItem extends BaseModel {

    private Long prescriptionId;
    private String medicineName;
    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;

    public PrescriptionItem() {
        super();
    }

    public PrescriptionItem(Long id) {
        super(id);
    }

    public PrescriptionItem(String medicineName, String dosage, String frequency, String duration) {
        super();
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}

