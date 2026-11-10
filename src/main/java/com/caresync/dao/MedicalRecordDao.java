package com.caresync.dao;

import com.caresync.model.MedicalRecord;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface MedicalRecordDao extends GenericDao<MedicalRecord, Long> {

    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);

    List<MedicalRecord> findByPatientId(Long patientId);

    List<MedicalRecord> findByDoctorId(Long doctorId);

    MedicalRecord save(MedicalRecord record, Connection conn);
}

