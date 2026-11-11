package com.caresync.service;

import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dao.UserDao;
import com.caresync.dao.UserDaoImpl;
import com.caresync.dto.PaginatedResult;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Role;
import com.caresync.model.User;
import com.caresync.model.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AdminUserServiceImpl implements AdminUserService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserServiceImpl.class);

    private final UserDao userDao;
    private final AuditLogDao auditLogDao;

    public AdminUserServiceImpl() {
        this(new UserDaoImpl(), new AuditLogDaoImpl());
    }

    public AdminUserServiceImpl(UserDao userDao, AuditLogDao auditLogDao) {
        this.userDao = Objects.requireNonNull(userDao, "userDao cannot be null");
        this.auditLogDao = Objects.requireNonNull(auditLogDao, "auditLogDao cannot be null");
    }

    @Override
    public PaginatedResult<User> getUsers(String query, Role role, UserStatus status, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = pageSize > 0 ? pageSize : 10;
        int offset = (safePage - 1) * safeSize;

        long total = userDao.countFiltered(query, role, status);
        List<User> list = userDao.findPaginated(query, role, status, offset, safeSize);

        return new PaginatedResult<>(list, total, safePage, safeSize);
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        if (userId == null) return Optional.empty();
        return userDao.findById(userId);
    }

    @Override
    public boolean updateUserStatus(Long userId, UserStatus newStatus, Long actorAdminId,
                                    String ipAddress, String userAgent) {
        if (userId == null || newStatus == null || actorAdminId == null) {
            throw new ValidationException("User ID, status, and administrator ID cannot be null");
        }

        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User", userId);
        }

        User user = userOpt.get();

        // Safety check: Cannot deactivate or suspend the last active administrator
        if (user.getRole() == Role.ADMIN && newStatus != UserStatus.ACTIVE) {
            long activeAdmins = userDao.countActiveAdmins();
            if (activeAdmins <= 1) {
                logger.warn("Security Alert: Blocked attempt to deactivate last active admin ID: {}", userId);
                throw new ValidationException("Cannot deactivate or suspend the last active administrator in the system.");
            }
        }

        boolean updated = userDao.updateStatus(userId, newStatus);
        if (updated) {
            auditLogDao.log(actorAdminId, "USER_STATUS_UPDATED", "users", userId,
                    ipAddress, userAgent, "User ID " + userId + " (" + user.getEmail() + ") status set to " + newStatus);
            logger.info("Admin ID {} changed user ID {} status to {}", actorAdminId, userId, newStatus);
        }
        return updated;
    }

    @Override
    public boolean updateUserRole(Long userId, Role newRole, Long actorAdminId,
                                  String ipAddress, String userAgent) {
        if (userId == null || newRole == null || actorAdminId == null) {
            throw new ValidationException("User ID, role, and administrator ID cannot be null");
        }

        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User", userId);
        }

        User user = userOpt.get();

        // Safety check 1: Admin cannot change their own role (privilege escalation or accidental lockout)
        if (Objects.equals(userId, actorAdminId)) {
            throw new ValidationException("Administrators cannot alter their own system role");
        }

        // Safety check 2: Cannot demote the last active administrator
        if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            long activeAdmins = userDao.countActiveAdmins();
            if (activeAdmins <= 1) {
                logger.warn("Security Alert: Blocked attempt to demote last active admin ID: {}", userId);
                throw new ValidationException("Cannot demote the last active administrator in the system.");
            }
        }

        boolean updated = userDao.updateRole(userId, newRole);
        if (updated) {
            auditLogDao.log(actorAdminId, "USER_ROLE_UPDATED", "users", userId,
                    ipAddress, userAgent, "User ID " + userId + " (" + user.getEmail() + ") role changed from " +
                            user.getRole() + " to " + newRole);
            logger.info("Admin ID {} changed user ID {} role from {} to {}", actorAdminId, userId, user.getRole(), newRole);
        }
        return updated;
    }
}

