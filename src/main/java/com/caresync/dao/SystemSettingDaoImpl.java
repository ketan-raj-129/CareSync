package com.caresync.dao;

import com.caresync.exception.DatabaseOperationException;
import com.caresync.model.SystemSetting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SystemSettingDaoImpl extends BaseDao<SystemSetting, String> implements SystemSettingDao {

    @Override
    public Optional<SystemSetting> findById(String key) {
        if (key == null || key.trim().isEmpty()) return Optional.empty();
        String sql = "SELECT setting_key, setting_value, description, updated_at, updated_by_user_id " +
                     "FROM system_settings WHERE setting_key = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSetting(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find system setting for key: " + key, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<SystemSetting> findAll() {
        String sql = "SELECT setting_key, setting_value, description, updated_at, updated_by_user_id " +
                     "FROM system_settings ORDER BY setting_key ASC";

        List<SystemSetting> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToSetting(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve system settings", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public String getValue(String key, String defaultValue) {
        return findById(key).map(SystemSetting::getSettingValue).orElse(defaultValue);
    }

    @Override
    public SystemSetting save(SystemSetting setting) {
        String sql = "INSERT INTO system_settings (setting_key, setting_value, description, updated_at, updated_by_user_id) " +
                     "VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, setting.getSettingKey());
            stmt.setString(2, setting.getSettingValue());
            setStringOrNull(stmt, 3, setting.getDescription());
            setLongOrNull(stmt, 4, setting.getUpdatedByUserId());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DatabaseOperationException("Failed to insert system setting, no rows affected.");
            }
            return setting;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert system setting: " + setting.getSettingKey(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean update(SystemSetting setting) {
        String sql = "UPDATE system_settings SET setting_value = ?, description = ?, " +
                     "updated_at = CURRENT_TIMESTAMP, updated_by_user_id = ? " +
                     "WHERE setting_key = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, setting.getSettingValue());
            setStringOrNull(stmt, 2, setting.getDescription());
            setLongOrNull(stmt, 3, setting.getUpdatedByUserId());
            stmt.setString(4, setting.getSettingKey());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update system setting: " + setting.getSettingKey(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean saveOrUpdate(SystemSetting setting) {
        String sql = "INSERT INTO system_settings (setting_key, setting_value, description, updated_at, updated_by_user_id) " +
                     "VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?) " +
                     "ON CONFLICT (setting_key) DO UPDATE SET " +
                     "setting_value = EXCLUDED.setting_value, " +
                     "description = EXCLUDED.description, " +
                     "updated_at = CURRENT_TIMESTAMP, " +
                     "updated_by_user_id = EXCLUDED.updated_by_user_id";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, setting.getSettingKey());
            stmt.setString(2, setting.getSettingValue());
            setStringOrNull(stmt, 3, setting.getDescription());
            setLongOrNull(stmt, 4, setting.getUpdatedByUserId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to upsert system setting: " + setting.getSettingKey(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteById(String key) {
        String sql = "DELETE FROM system_settings WHERE setting_key = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete system setting: " + key, e.getSQLState(), e);
        }
    }

    private SystemSetting mapResultSetToSetting(ResultSet rs) throws SQLException {
        SystemSetting setting = new SystemSetting();
        setting.setSettingKey(rs.getString("setting_key"));
        setting.setSettingValue(rs.getString("setting_value"));
        setting.setDescription(rs.getString("description"));
        setting.setUpdatedAt(getInstantOrNull(rs, "updated_at"));
        setting.setUpdatedByUserId(getLongOrNull(rs, "updated_by_user_id"));
        return setting;
    }
}

