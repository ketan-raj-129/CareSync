package com.caresync.dao;

import com.caresync.exception.DatabaseOperationException;
import com.caresync.model.Prescription;
import com.caresync.model.PrescriptionItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrescriptionDaoImpl extends BaseDao<Prescription, Long> implements PrescriptionDao {

    private static final String SELECT_BASE =
            "SELECT prescription_id, appointment_id, patient_id, doctor_id, general_advice, created_at " +
            "FROM prescriptions ";

    @Override
    public Optional<Prescription> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = SELECT_BASE + "WHERE prescription_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Prescription rx = mapResultSetToPrescription(rs);
                    rx.setItems(findItemsByPrescriptionIdInternal(rx.getId(), conn));
                    return Optional.of(rx);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find prescription by ID: " + id, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Prescription> findAll() {
        String sql = SELECT_BASE + "ORDER BY created_at DESC";
        List<Prescription> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Prescription rx = mapResultSetToPrescription(rs);
                rx.setItems(findItemsByPrescriptionIdInternal(rx.getId(), conn));
                list.add(rx);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve prescriptions", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public Optional<Prescription> findByAppointmentId(Long appointmentId) {
        if (appointmentId == null) return Optional.empty();
        String sql = SELECT_BASE + "WHERE appointment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Prescription rx = mapResultSetToPrescription(rs);
                    rx.setItems(findItemsByPrescriptionIdInternal(rx.getId(), conn));
                    return Optional.of(rx);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find prescription for appointment ID: " + appointmentId, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Prescription> findByPatientId(Long patientId) {
        String sql = SELECT_BASE + "WHERE patient_id = ? ORDER BY created_at DESC";
        List<Prescription> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Prescription rx = mapResultSetToPrescription(rs);
                    rx.setItems(findItemsByPrescriptionIdInternal(rx.getId(), conn));
                    list.add(rx);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find prescriptions for patient ID: " + patientId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<Prescription> findByDoctorId(Long doctorId) {
        String sql = SELECT_BASE + "WHERE doctor_id = ? ORDER BY created_at DESC";
        List<Prescription> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Prescription rx = mapResultSetToPrescription(rs);
                    rx.setItems(findItemsByPrescriptionIdInternal(rx.getId(), conn));
                    list.add(rx);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find prescriptions for doctor ID: " + doctorId, e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public Prescription save(Prescription prescription) {
        Connection conn = null;
        boolean initialAutoCommit = true;
        try {
            conn = getConnection();
            initialAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            Prescription saved = save(prescription, conn);
            conn.commit();
            return saved;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Rollback failed on prescription save", ex);
                }
            }
            throw new DatabaseOperationException("Failed to save prescription: " + e.getMessage(), e.getSQLState(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(initialAutoCommit);
                    conn.close();
                } catch (SQLException ex) {
                    logger.error("Closing connection failed", ex);
                }
            }
        }
    }

    @Override
    public Prescription save(Prescription prescription, Connection conn) {
        String sql = "INSERT INTO prescriptions (appointment_id, patient_id, doctor_id, general_advice, created_at) " +
                     "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, prescription.getAppointmentId());
            stmt.setLong(2, prescription.getPatientId());
            stmt.setLong(3, prescription.getDoctorId());
            setStringOrNull(stmt, 4, prescription.getGeneralAdvice());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DatabaseOperationException("Creating prescription failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    prescription.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseOperationException("Creating prescription failed, no ID obtained.");
                }
            }

            // Persist items if provided
            if (prescription.getItems() != null && !prescription.getItems().isEmpty()) {
                for (PrescriptionItem item : prescription.getItems()) {
                    item.setPrescriptionId(prescription.getId());
                    saveItem(item, conn);
                }
            }

            return prescription;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert prescription: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public PrescriptionItem saveItem(PrescriptionItem item, Connection conn) {
        String sql = "INSERT INTO prescription_items (prescription_id, medicine_name, dosage, frequency, duration, instructions) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, item.getPrescriptionId());
            stmt.setString(2, item.getMedicineName());
            stmt.setString(3, item.getDosage());
            stmt.setString(4, item.getFrequency());
            stmt.setString(5, item.getDuration());
            setStringOrNull(stmt, 6, item.getInstructions());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DatabaseOperationException("Creating prescription item failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getLong(1));
                }
            }
            return item;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert prescription item: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public List<PrescriptionItem> findItemsByPrescriptionId(Long prescriptionId) {
        try (Connection conn = getConnection()) {
            return findItemsByPrescriptionIdInternal(prescriptionId, conn);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find prescription items: " + prescriptionId, e.getSQLState(), e);
        }
    }

    private List<PrescriptionItem> findItemsByPrescriptionIdInternal(Long prescriptionId, Connection conn) throws SQLException {
        String sql = "SELECT item_id, prescription_id, medicine_name, dosage, frequency, duration, instructions " +
                     "FROM prescription_items WHERE prescription_id = ? ORDER BY item_id ASC";

        List<PrescriptionItem> items = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, prescriptionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PrescriptionItem item = new PrescriptionItem();
                    item.setId(rs.getLong("item_id"));
                    item.setPrescriptionId(rs.getLong("prescription_id"));
                    item.setMedicineName(rs.getString("medicine_name"));
                    item.setDosage(rs.getString("dosage"));
                    item.setFrequency(rs.getString("frequency"));
                    item.setDuration(rs.getString("duration"));
                    item.setInstructions(rs.getString("instructions"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    @Override
    public boolean update(Prescription prescription) {
        String sql = "UPDATE prescriptions SET general_advice = ? WHERE prescription_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setStringOrNull(stmt, 1, prescription.getGeneralAdvice());
            stmt.setLong(2, prescription.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update prescription ID: " + prescription.getId(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM prescriptions WHERE prescription_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete prescription ID: " + id, e.getSQLState(), e);
        }
    }

    private Prescription mapResultSetToPrescription(ResultSet rs) throws SQLException {
        Prescription rx = new Prescription();
        rx.setId(rs.getLong("prescription_id"));
        rx.setAppointmentId(rs.getLong("appointment_id"));
        rx.setPatientId(rs.getLong("patient_id"));
        rx.setDoctorId(rs.getLong("doctor_id"));
        rx.setGeneralAdvice(rs.getString("general_advice"));
        rx.setCreatedAt(getInstantOrNull(rs, "created_at"));
        return rx;
    }
}

