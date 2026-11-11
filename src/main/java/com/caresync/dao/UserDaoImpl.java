package com.caresync.dao;

import com.caresync.exception.DatabaseOperationException;
import com.caresync.model.Role;
import com.caresync.model.User;
import com.caresync.model.UserStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl extends BaseDao<User, Long> implements UserDao {

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT user_id, email, password_hash, password_salt, hash_iterations, role, status, " +
                     "first_name, last_name, phone_number, created_at, updated_at " +
                     "FROM users WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find user by ID: " + id, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return Optional.empty();
        String sql = "SELECT user_id, email, password_hash, password_salt, hash_iterations, role, status, " +
                     "first_name, last_name, phone_number, created_at, updated_at " +
                     "FROM users WHERE email = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find user by email: " + email, e.getSQLState(), e);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to check email existence: " + email, e.getSQLState(), e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT user_id, email, password_hash, password_salt, hash_iterations, role, status, " +
                     "first_name, last_name, phone_number, created_at, updated_at " +
                     "FROM users ORDER BY created_at DESC";

        List<User> users = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve users", e.getSQLState(), e);
        }
        return users;
    }

    @Override
    public User save(User user) {
        try (Connection conn = getConnection()) {
            return save(user, conn);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert user: " + user.getEmail(), e.getSQLState(), e);
        }
    }

    @Override
    public User save(User user, Connection conn) {
        String sql = "INSERT INTO users (email, password_hash, password_salt, hash_iterations, role, status, " +
                     "first_name, last_name, phone_number, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getPasswordSalt());
            stmt.setInt(4, user.getHashIterations());
            stmt.setString(5, user.getRole().name());
            stmt.setString(6, user.getStatus().name());
            stmt.setString(7, user.getFirstName());
            stmt.setString(8, user.getLastName());
            setStringOrNull(stmt, 9, user.getPhoneNumber());
            setTimestampOrNull(stmt, 10, user.getCreatedAt());
            setTimestampOrNull(stmt, 11, user.getUpdatedAt());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getLong(1));
                    }
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert user: " + user.getEmail(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, phone_number = ?, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            setStringOrNull(stmt, 3, user.getPhoneNumber());
            stmt.setLong(4, user.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update user profile: " + user.getId(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean updatePassword(Long userId, String passwordHash, String passwordSalt, int iterations) {
        try (Connection conn = getConnection()) {
            return updatePassword(userId, passwordHash, passwordSalt, iterations, conn);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update password for user: " + userId, e.getSQLState(), e);
        }
    }

    @Override
    public boolean updatePassword(Long userId, String passwordHash, String passwordSalt, int iterations, Connection conn) {
        String sql = "UPDATE users SET password_hash = ?, password_salt = ?, hash_iterations = ?, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, passwordHash);
            stmt.setString(2, passwordSalt);
            stmt.setInt(3, iterations);
            stmt.setLong(4, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update password for user: " + userId, e.getSQLState(), e);
        }
    }

    @Override
    public boolean updateStatus(Long userId, UserStatus status) {
        String sql = "UPDATE users SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update status for user: " + userId, e.getSQLState(), e);
        }
    }

    @Override
    public boolean updateRole(Long userId, Role role) {
        String sql = "UPDATE users SET role = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role.name());
            stmt.setLong(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update role for user: " + userId, e.getSQLState(), e);
        }
    }

    @Override
    public long countTotal() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count users", e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public long countByRole(Role role) {
        if (role == null) return 0L;
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count users by role: " + role, e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public long countByStatus(UserStatus status) {
        if (status == null) return 0L;
        String sql = "SELECT COUNT(*) FROM users WHERE status = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count users by status: " + status, e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public long countActiveAdmins() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to count active administrators", e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public List<User> findPaginated(String query, Role role, UserStatus status, int offset, int limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT user_id, email, password_hash, password_salt, hash_iterations, role, status, " +
                "first_name, last_name, phone_number, created_at, updated_at " +
                "FROM users WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR LOWER(email) LIKE ? OR phone_number LIKE ?) ");
            String wild = "%" + query.trim().toLowerCase() + "%";
            params.add(wild);
            params.add(wild);
            params.add(wild);
            params.add(wild);
        }
        if (role != null) {
            sql.append("AND role = ? ");
            params.add(role.name());
        }
        if (status != null) {
            sql.append("AND status = ? ");
            params.add(status.name());
        }

        sql.append("ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(Math.max(1, limit));
        params.add(Math.max(0, offset));

        List<User> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to paginate users", e.getSQLState(), e);
        }
        return list;
    }

    @Override
    public long countFiltered(String query, Role role, UserStatus status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR LOWER(email) LIKE ? OR phone_number LIKE ?) ");
            String wild = "%" + query.trim().toLowerCase() + "%";
            params.add(wild);
            params.add(wild);
            params.add(wild);
            params.add(wild);
        }
        if (role != null) {
            sql.append("AND role = ? ");
            params.add(role.name());
        }
        if (status != null) {
            sql.append("AND status = ? ");
            params.add(status.name());
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
            throw new DatabaseOperationException("Failed to count filtered users", e.getSQLState(), e);
        }
        return 0L;
    }

    @Override
    public boolean deleteById(Long id) {
        // Enforce soft deletion per business rules
        return updateStatus(id, UserStatus.INACTIVE);
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setPasswordSalt(rs.getString("password_salt"));
        user.setHashIterations(rs.getInt("hash_iterations"));
        user.setRole(Role.fromString(rs.getString("role")));
        user.setStatus(UserStatus.fromString(rs.getString("status")));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setCreatedAt(getInstantOrNull(rs, "created_at"));
        user.setUpdatedAt(getInstantOrNull(rs, "updated_at"));
        return user;
    }
}

