package com.lexflow.deadline.dto;

import com.lexflow.deadline.model.DeadlinePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request body for creating or updating a deadline.
 * "completed" is deliberately not here: completing a deadline is a state change
 * with its own endpoint (PATCH /api/v1/deadlines/{id}/complete).
 */
public record DeadlineRequest(

        @NotBlank(message = "Deadline title is required")
        @Size(max = 255, message = "Deadline title must be at most 255 characters")
        String title,

        @NotNull(message = "Due date is required")
        LocalDate dueDate,

        @NotNull(message = "Priority is required")
        DeadlinePriority priority
) {
}
