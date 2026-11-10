package com.caresync.dao;

import com.caresync.model.Feedback;

import java.util.List;
import java.util.Optional;

public interface FeedbackDao extends GenericDao<Feedback, Long> {

    Optional<Feedback> findByAppointmentId(Long appointmentId);

    List<Feedback> findByDoctorId(Long doctorId);

    List<Feedback> findPublishedByDoctorId(Long doctorId);

    double findAverageRatingByDoctorId(Long doctorId);

    long countByDoctorId(Long doctorId);
}

