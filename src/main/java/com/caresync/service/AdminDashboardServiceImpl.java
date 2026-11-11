package com.caresync.service;

import com.caresync.dao.AppointmentDao;
import com.caresync.dao.AppointmentDaoImpl;
import com.caresync.dao.AuditLogDao;
import com.caresync.dao.AuditLogDaoImpl;
import com.caresync.dao.DoctorDao;
import com.caresync.dao.DoctorDaoImpl;
import com.caresync.dao.PatientDao;
import com.caresync.dao.PatientDaoImpl;
import com.caresync.dao.SpecialtyDao;
import com.caresync.dao.SpecialtyDaoImpl;
import com.caresync.dto.AdminDashboardDto;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.Doctor;
import com.caresync.model.UserStatus;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Kolkata");

    private final PatientDao patientDao;
    private final DoctorDao doctorDao;
    private final AppointmentDao appointmentDao;
    private final SpecialtyDao specialtyDao;
    private final AuditLogDao auditLogDao;

    public AdminDashboardServiceImpl() {
        this(new PatientDaoImpl(), new DoctorDaoImpl(), new AppointmentDaoImpl(),
             new SpecialtyDaoImpl(), new AuditLogDaoImpl());
    }

    public AdminDashboardServiceImpl(PatientDao patientDao,
                                     DoctorDao doctorDao,
                                     AppointmentDao appointmentDao,
                                     SpecialtyDao specialtyDao,
                                     AuditLogDao auditLogDao) {
        this.patientDao = Objects.requireNonNull(patientDao, "patientDao cannot be null");
        this.doctorDao = Objects.requireNonNull(doctorDao, "doctorDao cannot be null");
        this.appointmentDao = Objects.requireNonNull(appointmentDao, "appointmentDao cannot be null");
        this.specialtyDao = Objects.requireNonNull(specialtyDao, "specialtyDao cannot be null");
        this.auditLogDao = Objects.requireNonNull(auditLogDao, "auditLogDao cannot be null");
    }

    @Override
    public AdminDashboardDto getDashboardMetrics() {
        AdminDashboardDto dto = new AdminDashboardDto();

        dto.setTotalPatients(patientDao.countTotal());
        dto.setTotalDoctors(doctorDao.countTotal());
        dto.setActiveDoctors(doctorDao.countActive());

        dto.setTotalAppointments(appointmentDao.countTotal());
        dto.setTodayAppointments(appointmentDao.countTodayAppointments());
        dto.setPendingAppointments(appointmentDao.countByStatus(AppointmentStatus.PENDING));
        dto.setConfirmedAppointments(appointmentDao.countByStatus(AppointmentStatus.CONFIRMED));
        dto.setCompletedAppointments(appointmentDao.countByStatus(AppointmentStatus.COMPLETED));
        dto.setCancelledAppointments(appointmentDao.countByStatus(AppointmentStatus.CANCELLED));

        dto.setTotalSpecialties(specialtyDao.countTotal());
        dto.setActiveSpecialties(specialtyDao.countActive());

        // Recent Audit logs
        dto.setRecentAuditLogs(auditLogDao.findRecent(8));

        // Today's schedule
        LocalDate today = LocalDate.now(CLINIC_ZONE);
        dto.setTodaySchedule(appointmentDao.findByDate(today));

        // Doctors pending verification/review (e.g. inactive or newly registered)
        List<Doctor> doctors = doctorDao.findAll();
        List<Doctor> pendingReview = doctors.stream()
                .filter(d -> d.getStatus() != UserStatus.ACTIVE || !d.isAvailable())
                .limit(5)
                .collect(Collectors.toList());
        dto.setPendingDoctors(pendingReview);
        dto.setPendingDoctorReviews(doctors.stream().filter(d -> d.getStatus() != UserStatus.ACTIVE).count());

        return dto;
    }
}
