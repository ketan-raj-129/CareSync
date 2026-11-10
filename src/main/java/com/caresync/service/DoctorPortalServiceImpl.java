package com.caresync.service;

import com.caresync.dao.AppointmentDao;
import com.caresync.dao.AppointmentDaoImpl;
import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dao.DoctorDao;
import com.caresync.dao.DoctorDaoImpl;
import com.caresync.dao.UserDao;
import com.caresync.dao.UserDaoImpl;
import com.caresync.dto.DoctorDashboardDto;
import com.caresync.dto.DoctorProfileUpdateDto;
import com.caresync.exception.ValidationException;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.Doctor;
import com.caresync.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DoctorPortalServiceImpl implements DoctorPortalService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorPortalServiceImpl.class);
    private static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Kolkata");

    private final DoctorDao doctorDao;
    private final UserDao userDao;
    private final AppointmentDao appointmentDao;
    private final AuditLogDao auditLogDao;

    public DoctorPortalServiceImpl() {
        this(new DoctorDaoImpl(), new UserDaoImpl(), new AppointmentDaoImpl(), new AuditLogDaoImpl());
    }

    public DoctorPortalServiceImpl(DoctorDao doctorDao, UserDao userDao, AppointmentDao appointmentDao, AuditLogDao auditLogDao) {
        this.doctorDao = Objects.requireNonNull(doctorDao, "doctorDao cannot be null");
        this.userDao = Objects.requireNonNull(userDao, "userDao cannot be null");
        this.appointmentDao = Objects.requireNonNull(appointmentDao, "appointmentDao cannot be null");
        this.auditLogDao = Objects.requireNonNull(auditLogDao, "auditLogDao cannot be null");
    }

    @Override
    public Optional<Doctor> getDoctorByUserId(Long userId) {
        if (userId == null) return Optional.empty();
        return doctorDao.findByUserId(userId);
    }

    @Override
    public Optional<Doctor> getDoctorById(Long doctorId) {
        if (doctorId == null) return Optional.empty();
        return doctorDao.findById(doctorId);
    }

    @Override
    public DoctorDashboardDto getDashboardMetrics(Long doctorId) {
        if (doctorId == null) {
            throw new ValidationException("Doctor ID cannot be null");
        }

        DoctorDashboardDto dto = new DoctorDashboardDto();
        LocalDate today = LocalDate.now(CLINIC_ZONE);

        // Calculate counts
        dto.setTotalAppointments(appointmentDao.countByDoctorId(doctorId));
        dto.setTodayAppointmentsCount(appointmentDao.countTodayByDoctorId(doctorId, today));
        dto.setPendingAppointmentsCount(appointmentDao.countByDoctorAndStatus(doctorId, AppointmentStatus.PENDING));
        dto.setConfirmedAppointmentsCount(appointmentDao.countByDoctorAndStatus(doctorId, AppointmentStatus.CONFIRMED));
        dto.setCompletedAppointmentsCount(appointmentDao.countByDoctorAndStatus(doctorId, AppointmentStatus.COMPLETED));

        // Retrieve active queues with details
        List<Appointment> allAppointments = appointmentDao.findWithDetailsByDoctorId(doctorId);

        List<Appointment> todayList = new ArrayList<>();
        List<Appointment> upcomingList = new ArrayList<>();
        List<Appointment> pendingList = new ArrayList<>();

        for (Appointment apt : allAppointments) {
            if (apt.getAppointmentDate() != null && apt.getAppointmentDate().isEqual(today)) {
                todayList.add(apt);
            }
            if (apt.getAppointmentDate() != null && apt.getAppointmentDate().isAfter(today) &&
                    (apt.getStatus() == AppointmentStatus.PENDING || apt.getStatus() == AppointmentStatus.CONFIRMED)) {
                upcomingList.add(apt);
            }
            if (apt.getStatus() == AppointmentStatus.PENDING) {
                pendingList.add(apt);
            }
        }

        dto.setTodayAppointments(todayList);
        dto.setUpcomingAppointments(upcomingList);
        dto.setPendingAppointments(pendingList);

        // Recent activity: top 5 from all appointments
        List<Appointment> recent = allAppointments.size() > 5 ? allAppointments.subList(0, 5) : allAppointments;
        dto.setRecentActivity(recent);

        return dto;
    }

    @Override
    public boolean updateDoctorProfile(Long doctorId, Long userId, DoctorProfileUpdateDto dto,
                                       Long actorUserId, String ipAddress, String userAgent) {
        if (doctorId == null || userId == null || dto == null) {
            throw new ValidationException("Doctor ID, User ID, and profile update data cannot be null");
        }

        if (!Objects.equals(userId, actorUserId)) {
            logger.warn("Security Alert: User ID {} attempted to update doctor profile for user ID {}", actorUserId, userId);
            throw new ValidationException("Unauthorized attempt to update doctor profile");
        }

        Optional<Doctor> existingOpt = doctorDao.findById(doctorId);
        if (existingOpt.isEmpty()) {
            throw new ValidationException("Doctor profile not found for ID: " + doctorId);
        }

        Doctor doctor = existingOpt.get();
        if (!Objects.equals(doctor.getId(), userId)) {
            logger.warn("Security Alert: Doctor profile {} does not belong to user ID {}", doctorId, userId);
            throw new ValidationException("Doctor profile does not belong to the authenticated user");
        }

        // Validate fields
        if (dto.getQualification() != null && !dto.getQualification().trim().isEmpty()) {
            doctor.setQualification(dto.getQualification().trim());
        }

        if (dto.getExperienceYears() >= 0) {
            doctor.setExperienceYears(dto.getExperienceYears());
        }

        if (dto.getConsultationFee() != null) {
            if (dto.getConsultationFee().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("Consultation fee cannot be negative");
            }
            doctor.setConsultationFee(dto.getConsultationFee());
        }

        doctor.setBio(dto.getBio() != null ? dto.getBio().trim() : null);
        doctor.setRoomNumber(dto.getRoomNumber() != null ? dto.getRoomNumber().trim() : null);
        doctor.setAvailable(dto.isAvailable());

        if (dto.getSpecialtyId() != null && dto.getSpecialtyId() > 0) {
            doctor.setSpecialtyId(dto.getSpecialtyId());
        }

        boolean docUpdated = doctorDao.update(doctor);

        // Update phone number on User record if provided
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().trim().isEmpty()) {
            Optional<User> userOpt = userDao.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setPhoneNumber(dto.getPhoneNumber().trim());
                userDao.update(user);
            }
        }

        if (docUpdated) {
            auditLogDao.log(actorUserId, "DOCTOR_PROFILE_UPDATED", "doctor_profiles", doctorId,
                    ipAddress, userAgent, "Doctor ID " + doctorId + " profile updated by practitioner");
        }

        return docUpdated;
    }
}
