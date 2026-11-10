package com.caresync.dao;

import com.caresync.exception.CareSyncException;
import com.caresync.exception.DatabaseOperationException;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.exception.SlotConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CareSync DAO Architecture and Contract Tests")
class DaoContractTest {

    @Test
    @DisplayName("Verify DAO instance construction and hierarchy")
    void testDaoInstantiation() {
        UserDao userDao = new UserDaoImpl();
        assertNotNull(userDao);
        assertTrue(userDao instanceof BaseDao);

        DoctorDao doctorDao = new DoctorDaoImpl();
        assertNotNull(doctorDao);

        PatientDao patientDao = new PatientDaoImpl();
        assertNotNull(patientDao);

        SpecialtyDao specialtyDao = new SpecialtyDaoImpl();
        assertNotNull(specialtyDao);

        AppointmentDao appointmentDao = new AppointmentDaoImpl();
        assertNotNull(appointmentDao);

        DoctorAvailabilityDao availabilityDao = new DoctorAvailabilityDaoImpl();
        assertNotNull(availabilityDao);

        DoctorAvailabilityExceptionDao exceptionDao = new DoctorAvailabilityExceptionDaoImpl();
        assertNotNull(exceptionDao);

        MedicalRecordDao medicalRecordDao = new MedicalRecordDaoImpl();
        assertNotNull(medicalRecordDao);

        PrescriptionDao prescriptionDao = new PrescriptionDaoImpl();
        assertNotNull(prescriptionDao);

        FeedbackDao feedbackDao = new FeedbackDaoImpl();
        assertNotNull(feedbackDao);

        NotificationDao notificationDao = new NotificationDaoImpl();
        assertNotNull(notificationDao);

        SystemSettingDao settingDao = new SystemSettingDaoImpl();
        assertNotNull(settingDao);

        AuditLogDao auditLogDao = new AuditLogDaoImpl();
        assertNotNull(auditLogDao);
    }

    @Test
    @DisplayName("Verify SlotConflictException construction and message formatting")
    void testSlotConflictException() {
        SlotConflictException ex1 = new SlotConflictException(10L, LocalDate.of(2026, 11, 20), LocalTime.of(14, 0), "conflict message");
        assertEquals("conflict message", ex1.getMessage());
        assertEquals(10L, ex1.getDoctorId());
        assertEquals(LocalDate.of(2026, 11, 20), ex1.getDate());
        assertEquals(LocalTime.of(14, 0), ex1.getSlotTime());

        SlotConflictException ex2 = new SlotConflictException("Direct conflict message");
        assertEquals("Direct conflict message", ex2.getMessage());
        assertTrue(ex2 instanceof CareSyncException);
    }

    @Test
    @DisplayName("Verify DatabaseOperationException captures SQL state")
    void testDatabaseOperationException() {
        DatabaseOperationException dbEx = new DatabaseOperationException("Connection failed", "08001", null);
        assertEquals("Connection failed", dbEx.getMessage());
        assertEquals("08001", dbEx.getSqlState());
        assertTrue(dbEx instanceof CareSyncException);
    }

    @Test
    @DisplayName("Verify ResourceNotFoundException formatting")
    void testResourceNotFoundException() {
        ResourceNotFoundException notFound = new ResourceNotFoundException("Doctor", 42L);
        assertTrue(notFound.getMessage().contains("Doctor"));
        assertTrue(notFound.getMessage().contains("42"));
    }

    @Test
    @DisplayName("Verify AuditLogDao append-only invariants")
    void testAuditLogAppendOnlyInvariants() {
        AuditLogDao auditLogDao = new AuditLogDaoImpl();
        assertThrows(UnsupportedOperationException.class, () -> auditLogDao.update(null));
        assertThrows(UnsupportedOperationException.class, () -> auditLogDao.deleteById(1L));
    }
}
