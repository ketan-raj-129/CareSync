package com.caresync.service;

import com.caresync.config.DatabaseConfig;
import com.caresync.dao.AppointmentDao;
import com.caresync.dao.AppointmentDaoImpl;
import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dao.MedicalRecordDao;
import com.caresync.dao.MedicalRecordDaoImpl;
import com.caresync.dao.PrescriptionDao;
import com.caresync.dao.PrescriptionDaoImpl;
import com.caresync.dto.ConsultationRequestDto;
import com.caresync.dto.PrescriptionItemDto;
import com.caresync.exception.DatabaseOperationException;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.MedicalRecord;
import com.caresync.model.Prescription;
import com.caresync.model.PrescriptionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ClinicalServiceImpl implements ClinicalService {

    private static final Logger logger = LoggerFactory.getLogger(ClinicalServiceImpl.class);

    private final AppointmentDao appointmentDao;
    private final MedicalRecordDao medicalRecordDao;
    private final PrescriptionDao prescriptionDao;
    private final AuditLogDao auditLogDao;
    private final ConnectionSupplier connectionSupplier;

    @FunctionalInterface
    public interface ConnectionSupplier {
        Connection get() throws SQLException;
    }

    public ClinicalServiceImpl() {
        this(new AppointmentDaoImpl(), new MedicalRecordDaoImpl(), new PrescriptionDaoImpl(),
             new AuditLogDaoImpl(), DatabaseConfig::getConnection);
    }

    public ClinicalServiceImpl(AppointmentDao appointmentDao,
                               MedicalRecordDao medicalRecordDao,
                               PrescriptionDao prescriptionDao,
                               AuditLogDao auditLogDao,
                               ConnectionSupplier connectionSupplier) {
        this.appointmentDao = Objects.requireNonNull(appointmentDao, "appointmentDao cannot be null");
        this.medicalRecordDao = Objects.requireNonNull(medicalRecordDao, "medicalRecordDao cannot be null");
        this.prescriptionDao = Objects.requireNonNull(prescriptionDao, "prescriptionDao cannot be null");
        this.auditLogDao = Objects.requireNonNull(auditLogDao, "auditLogDao cannot be null");
        this.connectionSupplier = Objects.requireNonNull(connectionSupplier, "connectionSupplier cannot be null");
    }

    @Override
    public Optional<MedicalRecord> getMedicalRecordByAppointment(Long appointmentId, Long doctorId) {
        if (appointmentId == null || doctorId == null) {
            return Optional.empty();
        }

        Optional<Appointment> aptOpt = appointmentDao.findById(appointmentId);
        if (aptOpt.isEmpty()) {
            return Optional.empty();
        }

        if (!Objects.equals(aptOpt.get().getDoctorId(), doctorId)) {
            logger.warn("Security Alert: Doctor ID {} attempted unauthorized access to medical record of appointment {}",
                    doctorId, appointmentId);
            throw new ValidationException("Unauthorized access to clinical record");
        }

        return medicalRecordDao.findByAppointmentId(appointmentId);
    }

    @Override
    public Optional<Prescription> getPrescriptionByAppointment(Long appointmentId, Long doctorId) {
        if (appointmentId == null || doctorId == null) {
            return Optional.empty();
        }

        Optional<Appointment> aptOpt = appointmentDao.findById(appointmentId);
        if (aptOpt.isEmpty()) {
            return Optional.empty();
        }

        if (!Objects.equals(aptOpt.get().getDoctorId(), doctorId)) {
            logger.warn("Security Alert: Doctor ID {} attempted unauthorized access to prescription of appointment {}",
                    doctorId, appointmentId);
            throw new ValidationException("Unauthorized access to prescription record");
        }

        Optional<Prescription> rxOpt = prescriptionDao.findByAppointmentId(appointmentId);
        rxOpt.ifPresent(rx -> rx.setItems(prescriptionDao.findItemsByPrescriptionId(rx.getId())));
        return rxOpt;
    }

    @Override
    public List<MedicalRecord> getPatientMedicalHistory(Long patientId, Long doctorId) {
        if (patientId == null || doctorId == null) {
            return Collections.emptyList();
        }

        // Verify active or historical clinical encounter between this doctor and patient
        List<Appointment> encounters = appointmentDao.findByPatientId(patientId);
        boolean hasEncounter = encounters.stream().anyMatch(a -> Objects.equals(a.getDoctorId(), doctorId));
        if (!hasEncounter) {
            logger.warn("Security Alert: Doctor ID {} attempted access to medical history of unrelated patient ID {}",
                    doctorId, patientId);
            throw new ValidationException("Unauthorized attempt to access clinical history of unassigned patient");
        }

        return medicalRecordDao.findByPatientId(patientId);
    }

    @Override
    public List<Prescription> getPrescriptionsByDoctor(Long doctorId) {
        if (doctorId == null) {
            throw new ValidationException("Doctor ID cannot be null");
        }
        List<Prescription> list = prescriptionDao.findByDoctorId(doctorId);
        for (Prescription rx : list) {
            rx.setItems(prescriptionDao.findItemsByPrescriptionId(rx.getId()));
        }
        return list;
    }

    @Override
    public Optional<Prescription> getPrescriptionById(Long prescriptionId, Long doctorId) {
        if (prescriptionId == null || doctorId == null) {
            return Optional.empty();
        }

        Optional<Prescription> rxOpt = prescriptionDao.findById(prescriptionId);
        if (rxOpt.isEmpty()) {
            return Optional.empty();
        }

        Prescription rx = rxOpt.get();
        if (!Objects.equals(rx.getDoctorId(), doctorId)) {
            logger.warn("Security Alert: Doctor ID {} attempted access to prescription ID {} belonging to doctor ID {}",
                    doctorId, prescriptionId, rx.getDoctorId());
            throw new ValidationException("Unauthorized access to prescription record");
        }

        rx.setItems(prescriptionDao.findItemsByPrescriptionId(rx.getId()));
        return Optional.of(rx);
    }

    @Override
    public boolean recordConsultation(ConsultationRequestDto request, Long doctorId,
                                      Long actorUserId, String ipAddress, String userAgent) {
        if (request == null || doctorId == null) {
            throw new ValidationException("Consultation request and Doctor ID cannot be null");
        }

        if (request.getSymptoms() == null || request.getSymptoms().trim().isEmpty()) {
            throw new ValidationException("Symptoms are required to document consultation findings");
        }

        if (request.getDiagnosis() == null || request.getDiagnosis().trim().isEmpty()) {
            throw new ValidationException("Diagnosis is required for clinical consultation");
        }

        Optional<Appointment> aptOpt = appointmentDao.findById(request.getAppointmentId());
        if (aptOpt.isEmpty()) {
            throw new ResourceNotFoundException("Appointment", request.getAppointmentId());
        }

        Appointment apt = aptOpt.get();
        if (!Objects.equals(apt.getDoctorId(), doctorId)) {
            logger.warn("Security Alert: Doctor ID {} attempted consultation documentation for appointment ID {} belonging to doctor ID {}",
                    doctorId, apt.getId(), apt.getDoctorId());
            throw new ValidationException("Unauthorized attempt to document consultation for another practitioner's appointment");
        }

        if (apt.getStatus() == AppointmentStatus.CANCELLED || apt.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new ValidationException("Cannot document consultation for an appointment with status: " + apt.getStatus());
        }

        // Validate prescription items if present
        List<PrescriptionItem> itemsToPersist = new ArrayList<>();
        if (request.getPrescriptionItems() != null && !request.getPrescriptionItems().isEmpty()) {
            for (PrescriptionItemDto itemDto : request.getPrescriptionItems()) {
                if (itemDto.getMedicineName() == null || itemDto.getMedicineName().trim().isEmpty()) {
                    continue; // Skip empty rows
                }
                if (itemDto.getDosage() == null || itemDto.getDosage().trim().isEmpty()) {
                    throw new ValidationException("Dosage is required for medication: " + itemDto.getMedicineName());
                }
                if (itemDto.getFrequency() == null || itemDto.getFrequency().trim().isEmpty()) {
                    throw new ValidationException("Frequency is required for medication: " + itemDto.getMedicineName());
                }
                if (itemDto.getDuration() == null || itemDto.getDuration().trim().isEmpty()) {
                    throw new ValidationException("Duration is required for medication: " + itemDto.getMedicineName());
                }

                PrescriptionItem item = new PrescriptionItem();
                item.setMedicineName(itemDto.getMedicineName().trim());
                item.setDosage(itemDto.getDosage().trim());
                item.setFrequency(itemDto.getFrequency().trim());
                item.setDuration(itemDto.getDuration().trim());
                item.setInstructions(itemDto.getInstructions() != null ? itemDto.getInstructions().trim() : null);
                itemsToPersist.add(item);
            }
        }

        Connection conn = null;
        boolean initialAutoCommit = true;
        try {
            conn = connectionSupplier.get();
            initialAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Save or Update Medical Record
            Optional<MedicalRecord> existingRecordOpt = medicalRecordDao.findByAppointmentId(apt.getId());
            MedicalRecord record;
            if (existingRecordOpt.isPresent()) {
                record = existingRecordOpt.get();
                record.setSymptoms(request.getSymptoms().trim());
                record.setDiagnosis(request.getDiagnosis().trim());
                record.setTreatmentPlan(request.getTreatmentPlan() != null ? request.getTreatmentPlan().trim() : null);
                record.setClinicalNotes(request.getClinicalNotes() != null ? request.getClinicalNotes().trim() : null);
                medicalRecordDao.update(record);
            } else {
                record = new MedicalRecord();
                record.setAppointmentId(apt.getId());
                record.setPatientId(apt.getPatientId());
                record.setDoctorId(doctorId);
                record.setSymptoms(request.getSymptoms().trim());
                record.setDiagnosis(request.getDiagnosis().trim());
                record.setTreatmentPlan(request.getTreatmentPlan() != null ? request.getTreatmentPlan().trim() : null);
                record.setClinicalNotes(request.getClinicalNotes() != null ? request.getClinicalNotes().trim() : null);
                medicalRecordDao.save(record, conn);
            }

            // 2. Save Prescription and line items if provided
            if (!itemsToPersist.isEmpty()) {
                Optional<Prescription> existingRxOpt = prescriptionDao.findByAppointmentId(apt.getId());
                if (existingRxOpt.isEmpty()) {
                    Prescription prescription = new Prescription();
                    prescription.setAppointmentId(apt.getId());
                    prescription.setPatientId(apt.getPatientId());
                    prescription.setDoctorId(doctorId);
                    prescription.setGeneralAdvice(request.getGeneralAdvice() != null ? request.getGeneralAdvice().trim() : null);
                    prescription.setItems(itemsToPersist);
                    prescriptionDao.save(prescription, conn);
                }
            }

            // 3. Transition Appointment Status to COMPLETED
            appointmentDao.updateStatus(apt.getId(), AppointmentStatus.COMPLETED, null, null, conn);

            // 4. Record Audit Log in same transaction
            auditLogDao.log(actorUserId, "CONSULTATION_DOCUMENTED", "appointments", apt.getId(),
                    ipAddress, userAgent, "Doctor ID " + doctorId + " documented clinical consultation and issued diagnosis for " + apt.getReferenceNumber(), conn);

            conn.commit();
            logger.info("Successfully committed consultation encounter for appointment ID {}", apt.getId());
            return true;

        } catch (SQLException | RuntimeException e) {
            rollbackQuietly(conn);
            logger.error("Failed to document consultation encounter atomically: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Failed to document consultation: " + e.getMessage(), e);
        } finally {
            closeQuietly(conn, initialAutoCommit);
        }
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                logger.error("Rollback failed during consultation operation", e);
            }
        }
    }

    private void closeQuietly(Connection conn, boolean initialAutoCommit) {
        if (conn != null) {
            try {
                conn.setAutoCommit(initialAutoCommit);
                conn.close();
            } catch (SQLException e) {
                logger.error("Failed to restore autocommit and close connection", e);
            }
        }
    }
}
