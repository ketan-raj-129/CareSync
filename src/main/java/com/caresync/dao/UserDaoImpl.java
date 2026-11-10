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

