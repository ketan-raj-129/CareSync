package com.caresync.service;

import com.caresync.dao.AppointmentDao;
import com.caresync.dao.DoctorAvailabilityDao;
import com.caresync.dao.DoctorAvailabilityExceptionDao;
import com.caresync.dao.DoctorDao;
import com.caresync.dto.TimeSlotDto;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.Doctor;
import com.caresync.model.DoctorAvailability;
import com.caresync.model.DoctorAvailabilityException;
import com.caresync.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CareSync AvailabilityService & Real-time Slot Computation Tests")
class AvailabilityServiceTest {

    private InMemoryDoctorDao doctorDao;
    private InMemoryDoctorAvailabilityDao availabilityDao;
    private InMemoryDoctorAvailabilityExceptionDao exceptionDao;
    private InMemoryAppointmentDao appointmentDao;
    private AvailabilityService availabilityService;

    private static final Long DOCTOR_PROFILE_ID = 101L;
    private static final Long PATIENT_ID = 501L;

    @BeforeEach
    void setUp() {
        doctorDao = new InMemoryDoctorDao();
        availabilityDao = new InMemoryDoctorAvailabilityDao();
        exceptionDao = new InMemoryDoctorAvailabilityExceptionDao();
        appointmentDao = new InMemoryAppointmentDao();

        availabilityService = new AvailabilityServiceImpl(doctorDao, availabilityDao, exceptionDao, appointmentDao);

        // Setup active doctor
        Doctor doc = new Doctor();
        doc.setId(DOCTOR_PROFILE_ID);
        doc.setDoctorProfileId(DOCTOR_PROFILE_ID);
        doc.setFirstName("Sarah");
        doc.setLastName("Mitchell");
        doc.setStatus(UserStatus.ACTIVE);
        doc.setAvailable(true);
        doctorDao.save(doc);
    }

    private LocalDate getNextFutureWeekday(DayOfWeek dayOfWeek) {
        return LocalDate.now(AvailabilityServiceImpl.CLINIC_ZONE)
                .plusDays(1)
                .with(TemporalAdjusters.next(dayOfWeek));
    }

    @Test
    @DisplayName("Verify doctor available slots generated cleanly during scheduled hours")
    void testAvailableSlotsDuringWorkingHours() {
        LocalDate testDate = getNextFutureWeekday(DayOfWeek.MONDAY);
        int dayOfWeek = testDate.getDayOfWeek().getValue();

        DoctorAvailability schedule = new DoctorAvailability();
        schedule.setDoctorId(DOCTOR_PROFILE_ID);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(12, 0));
        schedule.setSlotDurationMinutes(30);
        schedule.setActive(true);
        availabilityDao.save(schedule);

        List<TimeSlotDto> slots = availabilityService.getAvailableSlots(DOCTOR_PROFILE_ID, testDate, PATIENT_ID);

        assertEquals(6, slots.size(), "Should generate exactly 6 30-minute slots");
        for (TimeSlotDto slot : slots) {
            assertTrue(slot.isAvailable(), "Slot " + slot.getStartTime() + " should be available");
            assertNull(slot.getReason());
        }
    }

    @Test
    @DisplayName("Verify returns empty slots when doctor has no schedule on that day")
    void testDoctorUnavailableOutsideWorkingHours() {
        LocalDate testDate = getNextFutureWeekday(DayOfWeek.SUNDAY);

        List<TimeSlotDto> slots = availabilityService.getAvailableSlots(DOCTOR_PROFILE_ID, testDate, PATIENT_ID);
        assertTrue(slots.isEmpty(), "Should return empty list for unscheduled day");
    }

    @Test
    @DisplayName("Verify clinic-wide holiday marks all slots unavailable with reason")
    void testClinicHolidayBlocksEntireDate() {
        LocalDate testDate = getNextFutureWeekday(DayOfWeek.TUESDAY);
        int dayOfWeek = testDate.getDayOfWeek().getValue();

        DoctorAvailability schedule = new DoctorAvailability();
        schedule.setDoctorId(DOCTOR_PROFILE_ID);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(11, 0));
        schedule.setSlotDurationMinutes(30);
        schedule.setActive(true);
        availabilityDao.save(schedule);

        // Add clinic holiday
        DoctorAvailabilityException holiday = new DoctorAvailabilityException();
        holiday.setDoctorId(null); // Clinic-wide
        holiday.setExceptionDate(testDate);
        holiday.setFullDay(true);
        holiday.setReason("National Healthcare Observance Day");
        exceptionDao.save(holiday);

        List<TimeSlotDto> slots = availabilityService.getAvailableSlots(DOCTOR_PROFILE_ID, testDate, PATIENT_ID);
        assertFalse(slots.isEmpty(), "Slots are generated but marked unavailable");
        for (TimeSlotDto slot : slots) {
            assertFalse(slot.isAvailable(), "Slot should be unavailable on clinic holiday");
            assertTrue(slot.getReason().contains("National Healthcare Observance Day"));
        }
    }

    @Test
    @DisplayName("Verify full-day doctor leave blocks all slots for that doctor")
    void testFullDayDoctorLeaveBlocksEntireDate() {
        LocalDate testDate = getNextFutureWeekday(DayOfWeek.WEDNESDAY);
        int dayOfWeek = testDate.getDayOfWeek().getValue();

        DoctorAvailability schedule = new DoctorAvailability();
        schedule.setDoctorId(DOCTOR_PROFILE_ID);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(LocalTime.of(14, 0));
        schedule.setEndTime(LocalTime.of(16, 0));
        schedule.setSlotDurationMinutes(30);
        schedule.setActive(true);
        availabilityDao.save(schedule);

        // Add full-day doctor leave
        DoctorAvailabilityException leave = new DoctorAvailabilityException();
        leave.setDoctorId(DOCTOR_PROFILE_ID);
        leave.setExceptionDate(testDate);
        leave.setFullDay(true);
        leave.setReason("Medical Conference");
        exceptionDao.save(leave);

        List<TimeSlotDto> slots = availabilityService.getAvailableSlots(DOCTOR_PROFILE_ID, testDate, PATIENT_ID);
        assertFalse(slots.isEmpty());
        for (TimeSlotDto slot : slots) {
            assertFalse(slot.isAvailable());
            assertEquals("Doctor on Leave", slot.getReason());
        }
    }

    @Test
    @DisplayName("Verify partial-day leave blocks only overlapping slots")
    void testPartialDayLeaveBlocksOverlappingSlots() {
        LocalDate testDate = getNextFutureWeekday(DayOfWeek.THURSDAY);
        int dayOfWeek = testDate.getDayOfWeek().getValue();

        DoctorAvailability schedule = new DoctorAvailability();
        schedule.setDoctorId(DOCTOR_PROFILE_ID);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(12, 0));
        schedule.setSlotDurationMinutes(30);
        schedule.setActive(true);
        availabilityDao.save(schedule);

        // Partial leave: 10:00 - 11:00
        DoctorAvailabilityException partialLeave = new DoctorAvailabilityException();
        partialLeave.setDoctorId(DOCTOR_PROFILE_ID);
        partialLeave.setExceptionDate(testDate);
        partialLeave.setFullDay(false);
        partialLeave.setStartTime(LocalTime.of(10, 0));
        partialLeave.setEndTime(LocalTime.of(11, 0));
        partialLeave.setReason("Emergency Staff Meeting");
        exceptionDao.save(partialLeave);

        List<TimeSlotDto> slots = availabilityService.getAvailableSlots(DOCTOR_PROFILE_ID, testDate, PATIENT_ID);
        assertEquals(6, slots.size());

        // 09:00-09:30 -> Available
        assertTrue(slots.get(0).isAvailable());
        // 09:30-10:00 -> Available
        assertTrue(slots.get(1).isAvailable());
        // 10:00-10:30 -> Blocked by partial leave
        assertFalse(slots.get(2).isAvailable());
        assertEquals("Doctor Unavailable", slots.get(2).getReason());
        // 10:30-11:00 -> Blocked by partial leave
        assertFalse(slots.get(3).isAvailable());
        assertEquals("Doctor Unavailable", slots.get(3).getReason());
        // 11:00-11:30 -> Available
        assertTrue(slots.get(4).isAvailable());
        // 11:30-12:00 -> Available
        assertTrue(slots.get(5).isAvailable());
    }

    @Test
    @DisplayName("Verify doctor existing booked appointment blocks slot, while cancelled appointment releases it")
    void testDoctorAppointmentCollisionAndRelease() {
        LocalDate testDate = getNextFutureWeekday(DayOfWeek.FRIDAY);
        int dayOfWeek = testDate.getDayOfWeek().getValue();

        DoctorAvailability schedule = new DoctorAvailability();
        schedule.setDoctorId(DOCTOR_PROFILE_ID);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(LocalTime.of(10, 0));
        schedule.setEndTime(LocalTime.of(12, 0));
        schedule.setSlotDurationMinutes(30);
        schedule.setActive(true);
        availabilityDao.save(schedule);

        // Appointment 1: CONFIRMED 10:00 - 10:30 (should block)
        Appointment apt1 = new Appointment();
        apt1.setId(1L);
        apt1.setDoctorId(DOCTOR_PROFILE_ID);
        apt1.setPatientId(999L);
        apt1.setAppointmentDate(testDate);
        apt1.setStartTime(LocalTime.of(10, 0));
        apt1.setEndTime(LocalTime.of(10, 30));
        apt1.setStatus(AppointmentStatus.CONFIRMED);
        appointmentDao.save(apt1);

        // Appointment 2: CANCELLED 10:30 - 11:00 (should NOT block)
        Appointment apt2 = new Appointment();
        apt2.setId(2L);
        apt2.setDoctorId(DOCTOR_PROFILE_ID);
        apt2.setPatientId(999L);
        apt2.setAppointmentDate(testDate);
        apt2.setStartTime(LocalTime.of(10, 30));
        apt2.setEndTime(LocalTime.of(11, 0));
        apt2.setStatus(AppointmentStatus.CANCELLED);
        appointmentDao.save(apt2);

        List<TimeSlotDto> slots = availabilityService.getAvailableSlots(DOCTOR_PROFILE_ID, testDate, PATIENT_ID);
        assertEquals(4, slots.size());

        // Slot 1 (10:00 - 10:30) is booked
        assertFalse(slots.get(0).isAvailable());
        assertEquals("Slot Booked", slots.get(0).getReason());

        // Slot 2 (10:30 - 11:00) is released because prior booking was CANCELLED
        assertTrue(slots.get(1).isAvailable(), "Slot with cancelled appointment must be released and available");

        // Slot 3 & 4 (11:00-11:30, 11:30-12:00) are available
        assertTrue(slots.get(2).isAvailable());
        assertTrue(slots.get(3).isAvailable());
    }

    @Test
    @DisplayName("Verify patient cannot double-book across different doctors at the same time slot")
    void testPatientConcurrentAppointmentConflict() {
        LocalDate testDate = getNextFutureWeekday(DayOfWeek.MONDAY);
        int dayOfWeek = testDate.getDayOfWeek().getValue();

        DoctorAvailability schedule = new DoctorAvailability();
        schedule.setDoctorId(DOCTOR_PROFILE_ID);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(11, 0));
        schedule.setSlotDurationMinutes(30);
        schedule.setActive(true);
        availabilityDao.save(schedule);

        // Patient has existing booking with Doctor 999 at 09:30 - 10:00
        Appointment patientApt = new Appointment();
        patientApt.setId(88L);
        patientApt.setDoctorId(999L);
        patientApt.setPatientId(PATIENT_ID);
        patientApt.setAppointmentDate(testDate);
        patientApt.setStartTime(LocalTime.of(9, 30));
        patientApt.setEndTime(LocalTime.of(10, 0));
        patientApt.setStatus(AppointmentStatus.CONFIRMED);
        appointmentDao.save(patientApt);

        List<TimeSlotDto> slots = availabilityService.getAvailableSlots(DOCTOR_PROFILE_ID, testDate, PATIENT_ID);

        // 09:00 - 09:30 -> Available
        assertTrue(slots.get(0).isAvailable());
        // 09:30 - 10:00 -> Blocked by patient's own overlapping appointment
        assertFalse(slots.get(1).isAvailable());
        assertEquals("You have an existing booking at this time", slots.get(1).getReason());
        // 10:00 - 10:30 -> Available
        assertTrue(slots.get(2).isAvailable());
    }

    @Test
    @DisplayName("Verify past dates return an empty slot list")
    void testPastDateReturnsEmptyList() {
        LocalDate pastDate = LocalDate.now(AvailabilityServiceImpl.CLINIC_ZONE).minusDays(2);
        List<TimeSlotDto> slots = availabilityService.getAvailableSlots(DOCTOR_PROFILE_ID, pastDate, PATIENT_ID);
        assertTrue(slots.isEmpty(), "Past dates must return empty list");
    }

    // --- In-Memory Test Doubles ---

    private static class InMemoryDoctorDao implements DoctorDao {
        private final Map<Long, Doctor> store = new HashMap<>();

        @Override
        public Optional<Doctor> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Doctor> findByUserId(Long userId) {
            return Optional.ofNullable(store.get(userId));
        }

        @Override
        public Optional<Doctor> findByLicenseNumber(String licenseNumber) {
            return store.values().stream().filter(d -> licenseNumber.equals(d.getLicenseNumber())).findFirst();
        }

        @Override
        public List<Doctor> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public List<Doctor> findActiveDoctors() {
            return store.values().stream().filter(Doctor::isActive).collect(Collectors.toList());
        }

        @Override
        public List<Doctor> findBySpecialtyId(Long specialtyId) {
            return store.values().stream().filter(d -> specialtyId.equals(d.getSpecialtyId())).collect(Collectors.toList());
        }

        @Override
        public List<Doctor> searchDoctors(Long specialtyId, String query) {
            return findAll();
        }

        @Override
        public Doctor save(Doctor doctor) {
            store.put(doctor.getId(), doctor);
            return doctor;
        }

        @Override
        public Doctor saveProfile(Doctor doctor, Connection conn) {
            return save(doctor);
        }

        @Override
        public boolean update(Doctor doctor) {
            store.put(doctor.getId(), doctor);
            return true;
        }

        @Override
        public boolean deleteById(Long id) {
            return store.remove(id) != null;
        }
    }

    private static class InMemoryDoctorAvailabilityDao implements DoctorAvailabilityDao {
        private final List<DoctorAvailability> store = new ArrayList<>();

        @Override
        public Optional<DoctorAvailability> findById(Long id) {
            return store.stream().filter(a -> id.equals(a.getId())).findFirst();
        }

        @Override
        public List<DoctorAvailability> findAll() {
            return new ArrayList<>(store);
        }

        @Override
        public List<DoctorAvailability> findByDoctorId(Long doctorId) {
            return store.stream().filter(a -> doctorId.equals(a.getDoctorId())).collect(Collectors.toList());
        }

        @Override
        public List<DoctorAvailability> findActiveByDoctorId(Long doctorId) {
            return store.stream().filter(a -> doctorId.equals(a.getDoctorId()) && a.isActive()).collect(Collectors.toList());
        }

        @Override
        public Optional<DoctorAvailability> findByDoctorAndDay(Long doctorId, int dayOfWeek) {
            return store.stream().filter(a -> doctorId.equals(a.getDoctorId()) && a.getDayOfWeek() == dayOfWeek).findFirst();
        }

        @Override
        public DoctorAvailability save(DoctorAvailability availability) {
            store.add(availability);
            return availability;
        }

        @Override
        public boolean update(DoctorAvailability availability) {
            return true;
        }

        @Override
        public boolean deleteById(Long id) {
            return store.removeIf(a -> id.equals(a.getId()));
        }

        @Override
        public boolean deleteByDoctorId(Long doctorId) {
            return store.removeIf(a -> doctorId.equals(a.getDoctorId()));
        }
    }

    private static class InMemoryDoctorAvailabilityExceptionDao implements DoctorAvailabilityExceptionDao {
        private final List<DoctorAvailabilityException> store = new ArrayList<>();

        @Override
        public Optional<DoctorAvailabilityException> findById(Long id) {
            return store.stream().filter(e -> id.equals(e.getId())).findFirst();
        }

        @Override
        public List<DoctorAvailabilityException> findAll() {
            return new ArrayList<>(store);
        }

        @Override
        public List<DoctorAvailabilityException> findByDoctorId(Long doctorId) {
            return store.stream().filter(e -> doctorId.equals(e.getDoctorId())).collect(Collectors.toList());
        }

        @Override
        public List<DoctorAvailabilityException> findExceptionsByDoctorAndDate(Long doctorId, LocalDate date) {
            return store.stream()
                    .filter(e -> doctorId.equals(e.getDoctorId()) && date.equals(e.getExceptionDate()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<DoctorAvailabilityException> findClinicHolidays(LocalDate date) {
            return store.stream()
                    .filter(e -> e.getDoctorId() == null && date.equals(e.getExceptionDate()))
                    .collect(Collectors.toList());
        }

        @Override
        public boolean hasActiveException(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
            return false;
        }

        @Override
        public DoctorAvailabilityException save(DoctorAvailabilityException exception) {
            store.add(exception);
            return exception;
        }

        @Override
        public boolean update(DoctorAvailabilityException exception) {
            return true;
        }

        @Override
        public boolean deleteById(Long id) {
            return store.removeIf(e -> id.equals(e.getId()));
        }
    }

    private static class InMemoryAppointmentDao implements AppointmentDao {
        private final List<Appointment> store = new ArrayList<>();

        @Override
        public Optional<Appointment> findById(Long id) {
            return store.stream().filter(a -> id.equals(a.getId())).findFirst();
        }

        @Override
        public Optional<Appointment> findByReference(String reference) {
            return store.stream().filter(a -> reference.equals(a.getReferenceNumber())).findFirst();
        }

        @Override
        public List<Appointment> findAll() {
            return new ArrayList<>(store);
        }

        @Override
        public List<Appointment> findByPatientId(Long patientId) {
            return store.stream().filter(a -> patientId.equals(a.getPatientId())).collect(Collectors.toList());
        }

        @Override
        public List<Appointment> findByDoctorId(Long doctorId) {
            return store.stream().filter(a -> doctorId.equals(a.getDoctorId())).collect(Collectors.toList());
        }

        @Override
        public List<Appointment> findByDoctorAndDate(Long doctorId, LocalDate date) {
            return store.stream()
                    .filter(a -> doctorId.equals(a.getDoctorId()) && date.equals(a.getAppointmentDate()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<Appointment> findWithDetailsByPatientId(Long patientId) {
            return findByPatientId(patientId);
        }

        @Override
        public Optional<Appointment> findWithDetailsById(Long appointmentId) {
            return findById(appointmentId);
        }

        @Override
        public List<Appointment> findUpcomingByPatientId(Long patientId) {
            return findByPatientId(patientId);
        }

        @Override
        public List<Appointment> findUpcomingByDoctorId(Long doctorId) {
            return findByDoctorId(doctorId);
        }

        @Override
        public List<Appointment> findByStatus(AppointmentStatus status) {
            return store.stream().filter(a -> status.equals(a.getStatus())).collect(Collectors.toList());
        }

        @Override
        public boolean isSlotBooked(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
            return false;
        }

        @Override
        public Appointment save(Appointment appointment) {
            store.add(appointment);
            return appointment;
        }

        @Override
        public Appointment save(Appointment appointment, Connection conn) {
            return save(appointment);
        }

        @Override
        public boolean update(Appointment appointment) {
            return true;
        }

        @Override
        public boolean updateStatus(Long appointmentId, AppointmentStatus status, String cancellationReason, Long cancelledByUserId) {
            Optional<Appointment> apt = findById(appointmentId);
            if (apt.isPresent()) {
                apt.get().setStatus(status);
                apt.get().setCancellationReason(cancellationReason);
                apt.get().setCancelledByUserId(cancelledByUserId);
                return true;
            }
            return false;
        }

        @Override
        public boolean updateStatus(Long appointmentId, AppointmentStatus status, String cancellationReason, Long cancelledByUserId, Connection conn) {
            return updateStatus(appointmentId, status, cancellationReason, cancelledByUserId);
        }

        @Override
        public boolean deleteById(Long id) {
            return store.removeIf(a -> id.equals(a.getId()));
        }

        @Override
        public long countTotal() {
            return store.size();
        }

        @Override
        public long countByStatus(AppointmentStatus status) {
            return store.stream().filter(a -> status.equals(a.getStatus())).count();
        }

        @Override
        public long countTodayAppointments() {
            return 0;
        }
    }
}
