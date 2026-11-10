package com.caresync.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CareSync Domain Model Invariant Tests")
class DomainModelTest {

    @Test
    @DisplayName("Verify User and Role encapsulation")
    void testUserModel() {
        User user = new User();
        user.setId(100L);
        user.setEmail("patient@caresync.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.PATIENT);
        user.setStatus(UserStatus.ACTIVE);

        assertEquals(100L, user.getId());
        assertEquals("patient@caresync.com", user.getEmail());
        assertEquals("John Doe", user.getFullName());
        assertEquals(Role.PATIENT, user.getRole());
        assertEquals("/patient/dashboard", user.getDashboardUrl());
        assertTrue(user.isActive());
    }

    @Test
    @DisplayName("Verify Doctor entity and consultation fee")
    void testDoctorModel() {
        Doctor doc = new Doctor();
        doc.setId(10L);
        doc.setDoctorProfileId(1L);
        doc.setFirstName("Sarah");
        doc.setLastName("Mitchell");
        doc.setLicenseNumber("DOC-MED-84920");
        doc.setConsultationFee(new BigDecimal("120.00"));
        doc.setExperienceYears(12);

        assertEquals(Role.DOCTOR, doc.getRole());
        assertEquals("/doctor/dashboard", doc.getDashboardUrl());
        assertEquals("DOC-MED-84920", doc.getLicenseNumber());
        assertEquals(new BigDecimal("120.00"), doc.getConsultationFee());
        assertEquals(12, doc.getExperienceYears());
        assertEquals("Dr. Sarah Mitchell", "Dr. " + doc.getFullName());
    }

    @Test
    @DisplayName("Verify Patient entity and blood group")
    void testPatientModel() {
        Patient pat = new Patient();
        pat.setId(25L);
        pat.setPatientProfileId(5L);
        pat.setFirstName("Jane");
        pat.setLastName("Smith");
        pat.setDateOfBirth(LocalDate.of(1990, 5, 15));
        pat.setGender(Gender.FEMALE);
        pat.setBloodGroup(BloodGroup.O_POS);

        assertEquals(Role.PATIENT, pat.getRole());
        assertEquals("/patient/dashboard", pat.getDashboardUrl());
        assertEquals(BloodGroup.O_POS, pat.getBloodGroup());
        assertEquals("O+", pat.getBloodGroup().getDisplayValue());
        assertEquals(Gender.FEMALE, pat.getGender());
        assertEquals(LocalDate.of(1990, 5, 15), pat.getDateOfBirth());
    }

    @Test
    @DisplayName("Verify Feedback rating validation")
    void testFeedbackRatingValidation() {
        Feedback fb = new Feedback();
        fb.setRating(5);
        assertEquals(5, fb.getRating());

        fb.setRating(1);
        assertEquals(1, fb.getRating());

        assertThrows(IllegalArgumentException.class, () -> fb.setRating(0), "Rating 0 should be rejected");
        assertThrows(IllegalArgumentException.class, () -> fb.setRating(6), "Rating 6 should be rejected");
    }

    @Test
    @DisplayName("Verify DoctorAvailability day of week validation")
    void testDoctorAvailabilityDayOfWeek() {
        DoctorAvailability avail = new DoctorAvailability();
        avail.setDayOfWeek(1); // Monday
        assertEquals(1, avail.getDayOfWeek());

        avail.setDayOfWeek(7); // Sunday
        assertEquals(7, avail.getDayOfWeek());

        assertThrows(IllegalArgumentException.class, () -> avail.setDayOfWeek(0), "Day 0 should be rejected");
        assertThrows(IllegalArgumentException.class, () -> avail.setDayOfWeek(8), "Day 8 should be rejected");
    }

    @Test
    @DisplayName("Verify Appointment properties and default status")
    void testAppointmentModel() {
        Appointment apt = new Appointment();
        assertEquals(AppointmentStatus.PENDING, apt.getStatus());

        apt.setReferenceNumber("APT-2026-0001");
        apt.setAppointmentDate(LocalDate.of(2026, 11, 20));
        apt.setStartTime(LocalTime.of(10, 0));
        apt.setEndTime(LocalTime.of(10, 30));

        assertEquals("APT-2026-0001", apt.getReferenceNumber());
        assertEquals(LocalDate.of(2026, 11, 20), apt.getAppointmentDate());
        assertEquals(LocalTime.of(10, 0), apt.getStartTime());
        assertEquals(LocalTime.of(10, 30), apt.getEndTime());
    }

    @Test
    @DisplayName("Verify Prescription and line items composition")
    void testPrescriptionComposition() {
        Prescription rx = new Prescription();
        rx.setGeneralAdvice("Take after meals and maintain hydration");

        PrescriptionItem item1 = new PrescriptionItem();
        item1.setMedicineName("Amoxicillin 500mg");
        item1.setDosage("1 tablet");
        item1.setFrequency("Three times daily");
        item1.setDuration("7 days");

        rx.addItem(item1);

        assertEquals(1, rx.getItems().size());
        assertEquals("Amoxicillin 500mg", rx.getItems().get(0).getMedicineName());
    }
}
