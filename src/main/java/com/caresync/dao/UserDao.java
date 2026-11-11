package com.caresync.dao;

import com.caresync.model.Role;
import com.caresync.model.User;
import com.caresync.model.UserStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface UserDao extends GenericDao<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    default User save(User user, Connection conn) {
        return save(user);
    }

    default boolean updatePassword(Long userId, String passwordHash, String passwordSalt, int iterations) {
        return false;
    }

    default boolean updatePassword(Long userId, String passwordHash, String passwordSalt, int iterations, Connection conn) {
        return updatePassword(userId, passwordHash, passwordSalt, iterations);
    }

    default boolean updateStatus(Long userId, UserStatus status) {
        return false;
    }

    default boolean updateRole(Long userId, Role role) {
        return false;
    }

    default long countTotal() {
        return 0L;
    }

    default long countByRole(Role role) {
        return 0L;
    }

    default long countByStatus(UserStatus status) {
        return 0L;
    }

    default long countActiveAdmins() {
        return 0L;
    }

    default List<User> findPaginated(String query, Role role, UserStatus status, int offset, int limit) {
        return List.of();
    }

    default long countFiltered(String query, Role role, UserStatus status) {
        return 0L;
    }
}

