package com.caresync.dao;

import com.caresync.exception.DatabaseOperationException;
import com.caresync.model.Specialty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpecialtyDaoImpl extends BaseDao<Specialty, Long> implements SpecialtyDao {

    @Override
    public Optional<Specialty> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT specialty_id, name, description, icon_name, is_active, created_at " +
                     "FROM specialties WHERE specialty_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSpecialty(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find specialty by ID: " + id, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Specialty> findByName(String name) {
        if (name == null || name.trim().isEmpty()) return Optional.empty();
        String sql = "SELECT specialty_id, name, description, icon_name, is_active, created_at " +
                     "FROM specialties WHERE LOWER(name) = LOWER(?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSpecialty(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find specialty by name: " + name, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Specialty> findActiveSpecialties() {
        String sql = "SELECT specialty_id, name, description, icon_name, is_active, created_at " +
                     "FROM specialties WHERE is_active = TRUE ORDER BY name ASC";

        List<Specialty> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToSpecialty(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve active specialties", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public List<Specialty> findAll() {
        String sql = "SELECT specialty_id, name, description, icon_name, is_active, created_at " +
                     "FROM specialties ORDER BY name ASC";

        List<Specialty> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToSpecialty(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve all specialties", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public Specialty save(Specialty specialty) {
        String sql = "INSERT INTO specialties (name, description, icon_name, is_active, created_at) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, specialty.getName());
            setStringOrNull(stmt, 2, specialty.getDescription());
            setStringOrNull(stmt, 3, specialty.getIconName());
            stmt.setBoolean(4, specialty.isActive());
            setTimestampOrNull(stmt, 5, specialty.getCreatedAt());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        specialty.setId(generatedKeys.getLong(1));
                    }
                }
            }
            return specialty;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save specialty: " + specialty.getName(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean update(Specialty specialty) {
        String sql = "UPDATE specialties SET name = ?, description = ?, icon_name = ?, is_active = ? " +
                     "WHERE specialty_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, specialty.getName());
            setStringOrNull(stmt, 2, specialty.getDescription());
            setStringOrNull(stmt, 3, specialty.getIconName());
            stmt.setBoolean(4, specialty.isActive());
            stmt.setLong(5, specialty.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update specialty: " + specialty.getId(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "UPDATE specialties SET is_active = FALSE WHERE specialty_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to deactivate specialty: " + id, e.getSQLState(), e);
        }
    }

    @Override
    public long countTotal() {
        String sql = "SELECT COUNT(*) FROM specialties";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count specialties", e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public long countActive() {
        String sql = "SELECT COUNT(*) FROM specialties WHERE is_active = TRUE";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count active specialties", e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public long countDoctorsBySpecialtyId(Long specialtyId) {
        if (specialtyId == null) return 0L;
        String sql = "SELECT COUNT(*) FROM doctor_profiles WHERE specialty_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, specialtyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count doctors for specialty: " + specialtyId, e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public List<Specialty> findPaginated(String query, Boolean activeOnly, int offset, int limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT specialty_id, name, description, icon_name, is_active, created_at " +
                "FROM specialties WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ?) ");
            String wild = "%" + query.trim().toLowerCase() + "%";
            params.add(wild);
            params.add(wild);
        }
        if (Boolean.TRUE.equals(activeOnly)) {
            sql.append("AND is_active = TRUE ");
        }

        sql.append("ORDER BY name ASC LIMIT ? OFFSET ?");
        params.add(Math.max(1, limit));
        params.add(Math.max(0, offset));

        List<Specialty> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSpecialty(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to paginate specialties", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public long countFiltered(String query, Boolean activeOnly) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM specialties WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ?) ");
            String wild = "%" + query.trim().toLowerCase() + "%";
            params.add(wild);
            params.add(wild);
        }
        if (Boolean.TRUE.equals(activeOnly)) {
            sql.append("AND is_active = TRUE ");
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count filtered specialties", e.getSQLState(), e);
        }
        return 0L;
    }

    private Specialty mapResultSetToSpecialty(ResultSet rs) throws SQLException {
        Specialty s = new Specialty();
        s.setId(rs.getLong("specialty_id"));
        s.setName(rs.getString("name"));
        s.setDescription(rs.getString("description"));
        s.setIconName(rs.getString("icon_name"));
        s.setActive(rs.getBoolean("is_active"));
        s.setCreatedAt(getInstantOrNull(rs, "created_at"));
        return s;
    }
}

