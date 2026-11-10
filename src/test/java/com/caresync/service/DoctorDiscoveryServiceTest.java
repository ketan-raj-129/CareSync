package com.caresync.service;

import com.caresync.dao.DoctorDao;
import com.caresync.dao.SpecialtyDao;
import com.caresync.model.Doctor;
import com.caresync.model.Specialty;
import com.caresync.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CareSync DoctorDiscoveryService Tests")
class DoctorDiscoveryServiceTest {

    private InMemoryDoctorDao doctorDao;
    private InMemorySpecialtyDao specialtyDao;
    private DoctorDiscoveryService discoveryService;

    @BeforeEach
    void setUp() {
        doctorDao = new InMemoryDoctorDao();
        specialtyDao = new InMemorySpecialtyDao();
        discoveryService = new DoctorDiscoveryServiceImpl(doctorDao, specialtyDao);

        Specialty cardio = new Specialty(1L);
        cardio.setName("Cardiology");
        cardio.setDescription("Heart and cardiovascular health");
        cardio.setIconName("heart-pulse");
        cardio.setActive(true);

        Specialty neuro = new Specialty(2L);
        neuro.setName("Neurology");
        neuro.setDescription("Brain and nervous system");
        neuro.setIconName("brain");
        neuro.setActive(true);

        specialtyDao.save(cardio);
        specialtyDao.save(neuro);

        Doctor doc1 = new Doctor();
        doc1.setId(10L);
        doc1.setDoctorProfileId(100L);
        doc1.setFirstName("Sarah");
        doc1.setLastName("Mitchell");
        doc1.setSpecialtyId(1L);
        doc1.setSpecialty(cardio);
        doc1.setQualification("MD Cardiology, FACC");
        doc1.setStatus(UserStatus.ACTIVE);
        doctorDao.save(doc1);

        Doctor doc2 = new Doctor();
        doc2.setId(20L);
        doc2.setDoctorProfileId(200L);
        doc2.setFirstName("James");
        doc2.setLastName("Wilson");
        doc2.setSpecialtyId(2L);
        doc2.setSpecialty(neuro);
        doc2.setQualification("MD Neurology");
        doc2.setStatus(UserStatus.ACTIVE);
        doctorDao.save(doc2);
    }

    @Test
    @DisplayName("Verify getActiveSpecialties returns all active medical specialties")
    void testGetActiveSpecialties() {
        List<Specialty> specialties = discoveryService.getActiveSpecialties();
        assertEquals(2, specialties.size());
    }

    @Test
    @DisplayName("Verify searchDoctors finds matching doctor by query or specialty")
    void testSearchDoctors() {
        List<Doctor> results = discoveryService.searchDoctors(1L, "Sarah");
        assertEquals(1, results.size());
        assertEquals("Sarah", results.get(0).getFirstName());
    }

    @Test
    @DisplayName("Verify getDoctorById returns doctor by profile ID")
    void testGetDoctorById() {
        Optional<Doctor> doc = discoveryService.getDoctorById(100L);
        assertTrue(doc.isPresent());
        assertEquals("Mitchell", doc.get().getLastName());
    }

    // --- In-Memory Test Doubles ---

    private static class InMemorySpecialtyDao implements SpecialtyDao {
        private final Map<Long, Specialty> store = new HashMap<>();

        @Override
        public Optional<Specialty> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override
        public Optional<Specialty> findByName(String name) {
            return store.values().stream().filter(s -> name.equalsIgnoreCase(s.getName())).findFirst();
        }
        @Override
        public List<Specialty> findAll() { return new ArrayList<>(store.values()); }
        @Override
        public List<Specialty> findActiveSpecialties() { return findAll(); }
        @Override
        public Specialty save(Specialty specialty) { store.put(specialty.getId(), specialty); return specialty; }
        @Override
        public boolean update(Specialty specialty) { store.put(specialty.getId(), specialty); return true; }
        @Override
        public boolean deleteById(Long id) { return store.remove(id) != null; }
    }

    private static class InMemoryDoctorDao implements DoctorDao {
        private final Map<Long, Doctor> store = new HashMap<>();

        @Override
        public Optional<Doctor> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override
        public Optional<Doctor> findByUserId(Long userId) {
            return store.values().stream().filter(d -> userId.equals(d.getId())).findFirst();
        }
        @Override
        public Optional<Doctor> findByLicenseNumber(String licenseNumber) { return Optional.empty(); }
        @Override
        public List<Doctor> findAll() { return new ArrayList<>(store.values()); }
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
            return store.values().stream()
                    .filter(d -> specialtyId == null || specialtyId.equals(d.getSpecialtyId()))
                    .filter(d -> query == null || d.getFullName().toLowerCase().contains(query.toLowerCase()) ||
                            (d.getQualification() != null && d.getQualification().toLowerCase().contains(query.toLowerCase())))
                    .collect(Collectors.toList());
        }
        @Override
        public Doctor save(Doctor doctor) {
            Long key = doctor.getDoctorProfileId() != null ? doctor.getDoctorProfileId() : doctor.getId();
            store.put(key, doctor);
            return doctor;
        }
        @Override
        public Doctor saveProfile(Doctor doctor, Connection conn) { return save(doctor); }
        @Override
        public boolean update(Doctor doctor) {
            Long key = doctor.getDoctorProfileId() != null ? doctor.getDoctorProfileId() : doctor.getId();
            store.put(key, doctor);
            return true;
        }
        @Override
        public boolean deleteById(Long id) { return store.remove(id) != null; }
    }
}
