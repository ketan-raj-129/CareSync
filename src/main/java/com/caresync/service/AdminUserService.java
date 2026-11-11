package com.caresync.service;

import com.caresync.dto.PaginatedResult;
import com.caresync.model.Role;
import com.caresync.model.User;
import com.caresync.model.UserStatus;

import java.util.Optional;

public interface AdminUserService {

    PaginatedResult<User> getUsers(String query, Role role, UserStatus status, int page, int pageSize);

    Optional<User> getUserById(Long userId);

    boolean updateUserStatus(Long userId, UserStatus newStatus, Long actorAdminId,
                             String ipAddress, String userAgent);

    boolean updateUserRole(Long userId, Role newRole, Long actorAdminId,
                           String ipAddress, String userAgent);
}

