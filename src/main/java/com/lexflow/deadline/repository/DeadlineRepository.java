package com.lexflow.deadline.repository;

import com.lexflow.deadline.model.Deadline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DeadlineRepository extends JpaRepository<Deadline, Long> {

    List<Deadline> findByDueDateBeforeAndCompletedFalse(LocalDate date);

    List<Deadline> findByDueDateGreaterThanEqualAndCompletedFalse(LocalDate date);

    long countByDueDateBeforeAndCompletedFalse(LocalDate date);

    long countByDueDateGreaterThanEqualAndCompletedFalse(LocalDate date);
}