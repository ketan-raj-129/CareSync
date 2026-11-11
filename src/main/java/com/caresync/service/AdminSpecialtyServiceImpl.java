package com.caresync.service;

import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dao.SpecialtyDao;
import com.caresync.dao.SpecialtyDaoImpl;
import com.caresync.dto.PaginatedResult;
import com.caresync.dto.SpecialtyFormDto;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.ValidationException;
import com.caresync.model.Specialty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AdminSpecialtyServiceImpl implements AdminSpecialtyService {

    private static final Logger logger = LoggerFactory.getLogger(AdminSpecialtyServiceImpl.class);

    private final SpecialtyDao specialtyDao;
    private final AuditLogDao auditLogDao;

    public AdminSpecialtyServiceImpl() {
        this(new SpecialtyDaoImpl(), new AuditLogDaoImpl());
    }

    public AdminSpecialtyServiceImpl(SpecialtyDao specialtyDao, AuditLogDao auditLogDao) {
        this.specialtyDao = Objects.requireNonNull(specialtyDao, "specialtyDao cannot be null");
        this.auditLogDao = Objects.requireNonNull(auditLogDao, "auditLogDao cannot be null");
    }

    @Override
    public List<Specialty> getAllSpecialties() {
        return specialtyDao.findAll();
    }

    @Override
    public PaginatedResult<Specialty> getSpecialties(String query, Boolean activeOnly, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = pageSize > 0 ? pageSize : 10;
        int offset = (safePage - 1) * safeSize;

        long total = specialtyDao.countFiltered(query, activeOnly);
        List<Specialty> list = specialtyDao.findPaginated(query, activeOnly, offset, safeSize);

        return new PaginatedResult<>(list, total, safePage, safeSize);
    }

    @Override
    public Optional<Specialty> getSpecialtyById(Long id) {
        if (id == null) return Optional.empty();
        return specialtyDao.findById(id);
    }

    @Override
    public Specialty createSpecialty(SpecialtyFormDto dto, Long actorAdminId, String ipAddress, String userAgent) {
        if (dto == null || actorAdminId == null) {
            throw new ValidationException("Specialty payload and administrator ID cannot be null");
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ValidationException("Specialty name is required");
        }

        String trimmedName = dto.getName().trim();
        Optional<Specialty> existing = specialtyDao.findByName(trimmedName);
        if (existing.isPresent()) {
            throw new ValidationException("A medical specialty named '" + trimmedName + "' already exists");
        }

        Specialty specialty = new Specialty();
        specialty.setName(trimmedName);
        specialty.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        specialty.setIconName(dto.getIconName() != null && !dto.getIconName().trim().isEmpty() ? dto.getIconName().trim() : "stethoscope");
        specialty.setActive(dto.isActive());

        Specialty saved = specialtyDao.save(specialty);

        auditLogDao.log(actorAdminId, "SPECIALTY_CREATED", "specialties", saved.getId(),
                ipAddress, userAgent, "Created specialty: " + saved.getName());
        logger.info("Admin ID {} created specialty '{}' (ID: {})", actorAdminId, saved.getName(), saved.getId());

        return saved;
    }

    @Override
    public boolean updateSpecialty(Long id, SpecialtyFormDto dto, Long actorAdminId, String ipAddress, String userAgent) {
        if (id == null || dto == null || actorAdminId == null) {
            throw new ValidationException("Specialty ID, payload, and administrator ID cannot be null");
        }

        Optional<Specialty> existingOpt = specialtyDao.findById(id);
        if (existingOpt.isEmpty()) {
            throw new ResourceNotFoundException("Specialty", id);
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ValidationException("Specialty name is required");
        }

        String trimmedName = dto.getName().trim();
        Optional<Specialty> nameConflict = specialtyDao.findByName(trimmedName);
        if (nameConflict.isPresent() && !nameConflict.get().getId().equals(id)) {
            throw new ValidationException("Another specialty named '" + trimmedName + "' already exists");
        }

        Specialty s = existingOpt.get();
        s.setName(trimmedName);
        s.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        s.setIconName(dto.getIconName() != null && !dto.getIconName().trim().isEmpty() ? dto.getIconName().trim() : "stethoscope");
        s.setActive(dto.isActive());

        boolean updated = specialtyDao.update(s);
        if (updated) {
            auditLogDao.log(actorAdminId, "SPECIALTY_UPDATED", "specialties", id,
                    ipAddress, userAgent, "Updated specialty: " + s.getName());
            logger.info("Admin ID {} updated specialty '{}' (ID: {})", actorAdminId, s.getName(), id);
        }
        return updated;
    }

    @Override
    public boolean toggleSpecialtyActive(Long id, boolean active, Long actorAdminId, String ipAddress, String userAgent) {
        if (id == null || actorAdminId == null) {
            throw new ValidationException("Specialty ID and administrator ID cannot be null");
        }

        Optional<Specialty> specialtyOpt = specialtyDao.findById(id);
        if (specialtyOpt.isEmpty()) {
            throw new ResourceNotFoundException("Specialty", id);
        }

        // If deactivating, check if referenced by existing doctors
        if (!active) {
            long docCount = specialtyDao.countDoctorsBySpecialtyId(id);
            if (docCount > 0) {
                logger.warn("Admin ID {} attempted to deactivate specialty ID {} referenced by {} doctors",
                        actorAdminId, id, docCount);
                // Soft deactivation is allowed, but log clearly
            }
        }

        Specialty s = specialtyOpt.get();
        s.setActive(active);
        boolean updated = specialtyDao.update(s);
        if (updated) {
            String action = active ? "SPECIALTY_ACTIVATED" : "SPECIALTY_DEACTIVATED";
            auditLogDao.log(actorAdminId, action, "specialties", id,
                    ipAddress, userAgent, "Specialty '" + s.getName() + "' active status changed to " + active);
            logger.info("Admin ID {} set specialty '{}' (ID: {}) active to {}", actorAdminId, s.getName(), id, active);
        }
        return updated;
    }

    @Override
    public long getDoctorCountForSpecialty(Long specialtyId) {
        if (specialtyId == null) return 0L;
        return specialtyDao.countDoctorsBySpecialtyId(specialtyId);
    }
}
