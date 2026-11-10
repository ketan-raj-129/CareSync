package com.caresync.service;

import com.caresync.dao.DoctorDao;
import com.caresync.dao.DoctorDaoImpl;
import com.caresync.dao.SpecialtyDao;
import com.caresync.dao.SpecialtyDaoImpl;
import com.caresync.model.Doctor;
import com.caresync.model.Specialty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DoctorDiscoveryServiceImpl implements DoctorDiscoveryService {

    private final DoctorDao doctorDao;
    private final SpecialtyDao specialtyDao;

    public DoctorDiscoveryServiceImpl() {
        this(new DoctorDaoImpl(), new SpecialtyDaoImpl());
    }

    public DoctorDiscoveryServiceImpl(DoctorDao doctorDao, SpecialtyDao specialtyDao) {
        this.doctorDao = doctorDao;
        this.specialtyDao = specialtyDao;
    }

    @Override
    public List<Specialty> getActiveSpecialties() {
        try {
            return specialtyDao.findActiveSpecialties();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Doctor> searchDoctors(Long specialtyId, String query) {
        try {
            return doctorDao.searchDoctors(specialtyId, query);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<Doctor> getDoctorById(Long doctorId) {
        if (doctorId == null) return Optional.empty();
        try {
            return doctorDao.findById(doctorId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

