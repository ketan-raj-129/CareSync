package com.caresync.dao;

import com.caresync.model.Prescription;
import com.caresync.model.PrescriptionItem;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface PrescriptionDao extends GenericDao<Prescription, Long> {

    Optional<Prescription> findByAppointmentId(Long appointmentId);

    List<Prescription> findByPatientId(Long patientId);

    List<Prescription> findByDoctorId(Long doctorId);

    Prescription save(Prescription prescription, Connection conn);

    List<PrescriptionItem> findItemsByPrescriptionId(Long prescriptionId);

    PrescriptionItem saveItem(PrescriptionItem item, Connection conn);
}

