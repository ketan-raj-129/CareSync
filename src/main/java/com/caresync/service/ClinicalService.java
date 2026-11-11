package com.caresync.service;

import com.caresync.dto.ConsultationRequestDto;
import com.caresync.model.MedicalRecord;
import com.caresync.model.Prescription;

import java.util.List;
import java.util.Optional;

/**
 * Service orchestrating clinical consultation encounters, medical diagnoses,
 * and atomic prescription issuance.
 */
public interface ClinicalService {

    Optional<MedicalRecord> getMedicalRecordByAppointment(Long appointmentId, Long doctorId);

    Optional<Prescription> getPrescriptionByAppointment(Long appointmentId, Long doctorId);

    List<MedicalRecord> getPatientMedicalHistory(Long patientId, Long doctorId);

    List<Prescription> getPrescriptionsByDoctor(Long doctorId);

    Optional<Prescription> getPrescriptionById(Long prescriptionId, Long doctorId);

    boolean recordConsultation(ConsultationRequestDto request, Long doctorId,
                              Long actorUserId, String ipAddress, String userAgent);
}

