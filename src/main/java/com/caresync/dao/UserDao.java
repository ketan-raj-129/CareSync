package com.caresync.dao;

import com.caresync.model.User;
import com.caresync.model.UserStatus;

import java.sql.Connection;
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
}

