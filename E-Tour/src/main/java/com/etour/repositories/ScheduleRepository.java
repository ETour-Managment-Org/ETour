package com.etour.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entities.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByTour_TourIdOrderByStartDateAsc(Integer tourId);

    List<Schedule> findByStartDateBetween(LocalDate from, LocalDate to);
}
