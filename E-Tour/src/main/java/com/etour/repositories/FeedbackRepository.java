package com.etour.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entities.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    List<Feedback> findAllByOrderByFeedbackIdDesc();

    List<Feedback> findByStatusOrderByFeedbackIdDesc(String status);

    long countByStatus(String status);

    List<Feedback> findByPublishedTrueOrderByFeedbackIdDesc();

    long countByPublishedTrue();
}
