package com.lexflow.deadline.mapper;

import com.lexflow.deadline.dto.DeadlineRequest;
import com.lexflow.deadline.dto.DeadlineResponse;
import com.lexflow.deadline.model.Deadline;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DeadlineMapper {

    public DeadlineResponse toResponse(Deadline deadline) {
        Long caseId = deadline.getLegalCase() == null ? null : deadline.getLegalCase().getId();

        boolean overdue = !deadline.isCompleted()
                && deadline.getDueDate() != null
                && deadline.getDueDate().isBefore(LocalDate.now());

        return new DeadlineResponse(
                deadline.getId(),
                caseId,
                deadline.getTitle(),
                deadline.getDueDate(),
                deadline.getPriority(),
                deadline.isCompleted(),
                overdue
        );
    }

    public Deadline toEntity(DeadlineRequest request) {
        return new Deadline(request.title().trim(), request.dueDate(), request.priority());
    }
}
