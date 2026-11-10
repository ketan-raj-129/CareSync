package com.caresync.dao;

import com.caresync.model.User;
import com.caresync.model.UserStatus;

import java.sql.Connection;
import java.util.Optional;

public interface UserDao extends GenericDao<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user, Connection conn);

    boolean updatePassword(Long userId, String passwordHash, String passwordSalt, int iterations);

    boolean updatePassword(Long userId, String passwordHash, String passwordSalt, int iterations, Connection conn);

    boolean updateStatus(Long userId, UserStatus status);
}

