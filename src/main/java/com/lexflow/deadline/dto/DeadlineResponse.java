package com.lexflow.deadline.dto;

import com.lexflow.deadline.model.DeadlinePriority;

import java.time.LocalDate;

/**
 * Deadline as returned by the REST API.
 * "overdue" is computed, not stored: the due date has passed and the deadline is not completed.
 */
public record DeadlineResponse(
        Long id,
        Long caseId,
        String title,
        LocalDate dueDate,
        DeadlinePriority priority,
        boolean completed,
        boolean overdue
) {
}
